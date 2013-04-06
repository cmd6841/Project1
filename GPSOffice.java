import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventGenerator;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.AlreadyBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;

/**
 * 
 * @author Chinmay Dani
 * 
 *         Class GPSOffice represents Java RMI object for a GPS office in the
 *         Geographic Package System.
 * 
 *         The GPSOffice object instance is run by the following command line
 *         parameters using the Start class from edu.rit pacakage.
 * 
 *         Usage: java Start GPSOffice <"host"> <"port"> <"name"> <"X"> <"Y">
 *         where: "host" : name of the computer where the Registry Server is
 *         running. "port" : port number to which the Registry Server is
 *         listening. "name" : name of the city where the GPS office is located.
 *         "X" : GPS office's X coordinate. "Y" : GPS office's Y coordinate.
 * 
 */
public class GPSOffice implements GPSOfficeInterface {

	/**
	 * Name of the computer where the Registry Server is running.
	 */
	private String host;
	/**
	 * Port number to which the Registry Server is listening.
	 */
	private int port;
	/**
	 * Name of the city where the GPS office is located.
	 */
	private String name;
	/**
	 * GPS office's X coordinate.
	 */
	private double xpos;
	/**
	 * GPS office's Y coordinate.
	 */
	private double ypos;
	/**
	 * A registry proxy reference.
	 */
	private RegistryProxy registry;
	/**
	 * A list that has the names of all the GPS office objects currently bound
	 * to the registry server.
	 */
	private List<String> allOfficeNames;
	/**
	 * A list that has the three closest neighbor GPS offices.
	 */
	private List<GPSOfficeInfo> neighbors;
	/**
	 * A registry event listener object.
	 */
	private RegistryEventListener registryListener;
	/**
	 * A registry event filter object.
	 */
	private RegistryEventFilter registryFilter;
	/**
	 * An object to create a thread pool.
	 */
	private ExecutorService executor;
	/**
	 * A remote event generator object that reports GPSOfficeEvent to the
	 * Headquarters client class.
	 */
	private RemoteEventGenerator<GPSOfficeEvent> hqGenerator;

	/**
	 * Constructs a new GPSOffice object.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws IllegalArgumentException
	 *             Thrown if there is a discrepancy in the command line
	 *             arguments.
	 * @throws IOException
	 *             Thrown if an IO error or remote error occurs.
	 */
	public GPSOffice(String[] args) throws IOException {
		// Parse command line arguments.
		if (args.length != 5) {
			throw new IllegalArgumentException(
					"Usage: java Start GPSOffice <host> <port> <name> <X> <Y>");
		}
		host = args[0];
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("GPSOffice: Invalid <port>: "
					+ args[1]);
		}
		name = args[2];
		try {
			xpos = Double.parseDouble(args[3]);
		} catch (NullPointerException npe) {
			throw new IllegalArgumentException(
					"GPSOffice: Invalid <xpos>: null");
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("GPSOffice: Invalid <xpos>: "
					+ args[3]);
		}
		try {
			ypos = Double.parseDouble(args[4]);
		} catch (NullPointerException npe) {
			throw new IllegalArgumentException(
					"GPSOffice: Invalid <ypos>: null");
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("GPSOffice: Invalid <port>: "
					+ args[4]);
		}

		// Create a new RemoteEvenetGenerator object for the Headquarters class.
		hqGenerator = new RemoteEventGenerator<GPSOfficeEvent>();

		// Create a new thread pool executor object.
		executor = Executors.newCachedThreadPool();

		// Create a registry proxy and bind it to the registry server with the
		// given name.
		try {
			registry = new RegistryProxy(host, port);
			UnicastRemoteObject.exportObject(this, 0);
			registry.bind(name, this);
		} catch (AlreadyBoundException abe) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException nse) {
			}
			throw new IllegalArgumentException("GPSOffice: " + name
					+ " already exists");
		} catch (RemoteException re) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException nse) {
			}
			throw new IllegalArgumentException(
					"Cannot connect to the registry server at " + host + ":"
							+ port);
		} catch (Exception e) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException nse) {
			}
			throw e;
		}

		// Add the closest three neighbor GPSOffice objects.
		addNeighbors();

		// Create a new registry event listener.
		registryListener = new RegistryEventListener() {
			public void report(long seq, final RegistryEvent event)
					throws RemoteException {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// Add updated neighbor GPSOffice objects.
						addNeighbors();
					}
				}).start();
			}
		};
		UnicastRemoteObject.exportObject(registryListener, 0);

		// Filter the type of objects to listen to for bind events.
		registryFilter = new RegistryEventFilter()
				.reportType("GPSOfficeInterface").reportBound().reportUnbound();
		registry.addEventListener(registryListener, registryFilter);

	}

	/**
	 * Gets the X coordinate of this GPSOffice.
	 * 
	 * @return The X coordinate of GPSOffice.
	 * @throws RemoteException
	 *             Thrown if there is a remote error.
	 */
	public double getX() throws RemoteException {
		return this.xpos;
	}

	/**
	 * Gets the Y coordinate of this GPSOffice.
	 * 
	 * @return The Y coordinate of GPSOffice.
	 * @throws RemoteException
	 *             Thrown if there is a remote error.
	 */
	public double getY() throws RemoteException {
		return this.ypos;
	}

	/**
	 * Gets the name of the city of this GPSOffice location.
	 * 
	 * @return The name of the city of GPSOffice.
	 * @throws RemoteException
	 *             Thrown if there is a remote error.
	 */
	public String getName() throws RemoteException {
		return this.name;
	}

	/**
	 * Adds the given listener to the remote event generator
	 * 
	 * @param listener
	 *            A RemoteEventListener object that listens to GPSOfficeEvents.
	 * @return Lease object for the given listener.
	 * @throws RemoteException
	 *             Thrown if there is a remote error.
	 */
	@Override
	public Lease addListener(RemoteEventListener<GPSOfficeEvent> listener)
			throws RemoteException {
		return hqGenerator.addListener(listener);
	}

	/**
	 * Finds the closest three neighbor GPSOffice objects in the system.
	 */
	private void addNeighbors() {
		// Get the names of the objects bound to the registry server.
		try {
			allOfficeNames = new ArrayList<String>();
			allOfficeNames = registry.list("GPSOfficeInterface");
		} catch (Exception e) {
			System.out.println("Error while retrieving bound names "
					+ "list from registry server.");
			e.printStackTrace();
		}
		neighbors = new ArrayList<GPSOfficeInfo>();
		synchronized (neighbors) {
			for (String office : allOfficeNames) {
				if (!office.equals(name)) {
					GPSOfficeInterface nextOffice;
					try {
						nextOffice = (GPSOfficeInterface) registry
								.lookup(office);
						double x = nextOffice.getX(), y = nextOffice.getY();

						// Find the distance of the current GPSOffice from this
						// GPSOffice.
						double distanceFromThisOffice = getDistance(x, y);

						// Adds a new GPSOfficeInfo object to the neighbors list
						neighbors.add(new GPSOfficeInfo(distanceFromThisOffice,
								nextOffice, office, x, y));

						// If the current neighbors list contains more than 3
						// objects then sorts the list according to the
						// distances from the current GPSOffice object and
						// removes the last element.
						if (neighbors.size() > 3) {
							Collections.sort(neighbors);
							neighbors.remove(3);
						}
					} catch (Exception e) {
						// If an exception is caught, it is ignored and the next
						// GPSOfficeInterface object is looked up.
						continue;
					}
				}
			}
		}
	}

	/**
	 * Calculates the distance from this GPSOffice object to any point in the
	 * coordinate system.
	 * 
	 * @param x
	 *            X coordinate of a location
	 * @param y
	 *            Y coordinate of a location
	 * @return The straight line distance between this GPSOffice object and the
	 *         given location.
	 */
	private double getDistance(double x, double y) {
		return Math.sqrt((x - xpos) * (x - xpos) + (y - ypos) * (y - ypos));
	}

	/**
	 * Creates a new Receipt object and forwards the package through this GPS
	 * Office.
	 * 
	 * @param destx
	 *            X coordinate of the destination.
	 * @param desty
	 *            Y coordinate of the destination/
	 * @param listener
	 *            A remote event listener that listens to events related to this
	 *            package transportation.
	 * @throws RemoteException
	 *             Thrown if a remote error occurs.
	 */
	public void sendPackage(final double destx, final double desty,
			final RemoteEventListener<GPSOfficeEvent> listener)
			throws RemoteException {
		forwardPackage(createReceipt(destx, desty), destx, desty, listener);
	}

	/**
	 * 
	 * @param destx
	 *            X coordinate of the destination.
	 * @param desty
	 *            Ycoordinate of the destination.
	 * @return A new Receipt object with a new tracknumber and the destination
	 *         coordinates.
	 */
	private Receipt createReceipt(double destx, double desty) {
		return new Receipt(System.currentTimeMillis(), destx, desty);
	}

	/**
	 * Forwards the package to the destination by taking the shortest possible
	 * route through the neighbor GPSOffice objects or itself.
	 * 
	 * @param receipt
	 *            A receipt object that is used for forwarding the package.
	 * @param destx
	 *            X coordinate of the destination.
	 * @param desty
	 *            Y coordinate of the destination/
	 * @param listener
	 *            A remote event listener that listens to events related to this
	 *            package transportation.
	 */
	public void forwardPackage(final Receipt receipt, final double destx,
			final double desty,
			final RemoteEventListener<GPSOfficeEvent> listener) {

		// A list that has the GPSOffice objects in ascending order of the
		// distance from the destination.
		final List<GPSOfficeInfo> deliverers;

		// Add the customers remote event listener to the generator object and
		// report event of the arrival of the package.
		RemoteEventGenerator<GPSOfficeEvent> generator = null;
		try {
			generator = new RemoteEventGenerator<GPSOfficeEvent>();
			generator.addListener(listener);
			generator.reportEvent(new GPSOfficeEvent(receipt, name, false,
					false, true));
			hqGenerator.reportEvent(new GPSOfficeEvent(receipt, name, false,
					false, true));
		} catch (RemoteException e) {
			System.out.println("Could not add the listener successfully.");
			e.printStackTrace();
		}

		// Wait for 3 seconds to examine the package.
		examinePackage();

		// Add this and the neighbor GPSOffice objects to the list and sort them
		// according to their distance from the destination.
		deliverers = new ArrayList<GPSOfficeInfo>();
		deliverers.addAll(neighbors);
		deliverers.add(new GPSOfficeInfo(0, this, name, xpos, ypos));
		Collections.sort(deliverers, new Comparator<GPSOfficeInfo>() {

			@Override
			public int compare(GPSOfficeInfo o1, GPSOfficeInfo o2) {
				double d1 = o1.getDistance(destx, desty);
				double d2 = o2.getDistance(destx, desty);
				return ((d1 - d2) == 0) ? -1 : ((d1 - d2) > 0.0) ? 1 : -1;
			}

		});

		// If the current GPS Office is the closest to the destination then
		// delivers the package and reports the corresponding events to the
		// listeners.
		if (deliverers.get(0).name.equals(name)) {
			generator.reportEvent(new GPSOfficeEvent(receipt, name, false,
					true, false));
			hqGenerator.reportEvent(new GPSOfficeEvent(receipt, name, false,
					true, false));
		}

		// Else forwards the package to the neighbor GPS office that is closest
		// to the destination.
		else {
			generator.reportEvent(new GPSOfficeEvent(receipt, name, false,
					false, false));
			hqGenerator.reportEvent(new GPSOfficeEvent(receipt, name, false,
					false, false));
			final RemoteEventGenerator<GPSOfficeEvent> finalGenerator = generator;
			executor.execute(new Runnable() {
				@Override
				public void run() {
					String cityName = name;
					try {
						cityName = deliverers.get(0).office.getName();
						deliverers.get(0).office.forwardPackage(receipt, destx,
								desty, listener);
					} catch (Exception e) {
						finalGenerator.reportEvent(new GPSOfficeEvent(receipt,
								cityName, true, false, false));
						hqGenerator.reportEvent(new GPSOfficeEvent(receipt,
								cityName, true, false, false));
					}
				}
			});
		}
	}

	/**
	 * Waits for 3 seconds to examine a package.
	 */
	private void examinePackage() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Class GPSOfficeInfo holds a tuple of a remote reference to a
	 * GPSOfficeInterface object, the distance from the current GPSOffice object
	 * and the location of that object. It is used mainly in the sorting of the
	 * neighbors according to their distances from the current GPS office. This
	 * class facilitates re-usability in the program.
	 * 
	 * @author Chinmay Dani
	 * 
	 */
	private static class GPSOfficeInfo implements Comparable<GPSOfficeInfo> {
		/**
		 * Distance between this object and another GPSOffice object.
		 */
		public double distance;
		/**
		 * A remote reference.
		 */
		public GPSOfficeInterface office;
		/**
		 * The name of the remote reference.
		 */
		public String name;
		/**
		 * X coordinate of the remote reference.
		 */
		public double xpos;
		/**
		 * Y coordinate of the remote reference.
		 */
		public double ypos;

		/**
		 * Construct a new GPSOfficeInfo object.
		 * 
		 * @param d
		 *            distance from the current GPSOffice object
		 * @param gpso
		 *            the remote reference to GPSOfficeInterface object
		 * @param n
		 *            name of the remote reference to GPSOfficeInterface object
		 * @param x
		 *            x coordinate of the remote reference to GPSOfficeInterface
		 *            object
		 * @param y
		 *            y coordinate of the remote reference to GPSOfficeInterface
		 *            object
		 */
		public GPSOfficeInfo(double d, GPSOfficeInterface gpso, String n,
				double x, double y) {
			distance = d;
			office = gpso;
			name = n;
			xpos = x;
			ypos = y;
		}

		/**
		 * The compareTo method that compares the distances of two GPSOffice
		 * objects from the current GPSOffice object.
		 * 
		 * @return -1 if this object is closer to the GPSOffice object, 1 if
		 *         this object is not closer to the GPSOffice object, 0
		 *         otherwise
		 */
		@Override
		public int compareTo(GPSOfficeInfo o) {
			return ((distance - o.distance) == 0) ? 0
					: ((distance - o.distance) > 0.0) ? 1 : -1;
		}

		/**
		 * Finds the distance between any location and this GPSOfficeInfo
		 * object.
		 * 
		 * @param x
		 *            X coordinate of location.
		 * @param y
		 *            Y coordinate of location.
		 * @return distance between any location and this GPSOfficeInfo object.
		 */
		private double getDistance(double x, double y) {
			return Math.sqrt((x - xpos) * (x - xpos) + (y - ypos) * (y - ypos));
		}
	}
}
