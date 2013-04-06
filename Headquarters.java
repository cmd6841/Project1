import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;

/**
 * Class Headquarters is a client program that listens to all the ongoing
 * GPSOffice event in a Geographic Package System and prints the notification
 * messages on the console.
 * 
 * @author Chinmay Dani
 * 
 */
public class Headquarters {

	/**
	 * Name of the computer where the Registry Server is running.
	 */
	private static String host;
	/**
	 * Port number to which the Registry Server is listening.
	 */
	private static int port;
	/**
	 * A registry proxy reference.
	 */
	private static RegistryProxy registry;
	/**
	 * A registry event listener reference to listen to bind events from a
	 * registry server.
	 */
	private static RegistryEventListener registryListener;
	/**
	 * A registry event filter to listen only to the events of
	 * GPSOfficeInterface objects.
	 */
	private static RegistryEventFilter registryFilter;

	/**
	 * The main method.
	 * 
	 * @param args
	 *            command line arguments.
	 * @throws IllegalArgumentException
	 *             Thrown if there is a discrepancy in the command line
	 *             arguments.
	 */
	public static void main(String[] args) {
		// Parse the command line arguments.
		if (args.length != 2) {
			System.out.println("Usage: java Headquarters <host> <port>");
			System.exit(0);
		}
		host = args[0];
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Headquarters: Invalid <port>: "
					+ args[1]);
		}

		// Create a PackageListener object to listen to GPSOfficeEvent remote
		// events.
		final PackageEventListener packageListener = new PackageEventListener();
		try {
			registry = new RegistryProxy(host, port);
		} catch (RemoteException e) {
			System.out.println("Cannot connect to the registry server at "
					+ host + ":" + port);
			e.printStackTrace();
			System.exit(0);
		}

		// Creates a registry event listener.
		registryListener = new RegistryEventListener() {

			@Override
			public void report(long arg0, RegistryEvent event)
					throws RemoteException {
				listen(event.objectName(), packageListener);
			}
		};
		try {
			UnicastRemoteObject.exportObject(registryListener, 0);
		} catch (RemoteException e) {
			System.out.println("Exception caught while creating a listener.");
			e.printStackTrace();
			System.exit(0);
		}

		// Creates a filter on the registry event listener.
		registryFilter = new RegistryEventFilter().reportType(
				"GPSOfficeInterface").reportBound();
		try {
			registry.addEventListener(registryListener, registryFilter);
		} catch (RemoteException e) {
			System.out.println("Exception caught while adding a listener"
					+ " on the registry server.");
			e.printStackTrace();
			System.exit(0);
		}

		try {
			UnicastRemoteObject.exportObject(packageListener, 0);
		} catch (RemoteException e) {
			System.out.println("Exception caught while creating a listener.");
			e.printStackTrace();
			System.exit(0);
		}

		// Lookup each remote object of type GPSOfficeInterface and add the
		// remote event listener to listen to their events.
		try {
			for (String office : registry.list("GPSOfficeInterface")) {
				listen(office, packageListener);
			}
		} catch (RemoteException e) {
			System.out.println("Error while retrieving bound names "
					+ "list from registry server.");
			e.printStackTrace();
			System.exit(0);
		}

	}

	/**
	 * Adds a listener to the remote event generator of a GPSOfficeInterface
	 * object.
	 * 
	 * @param office
	 *            the name of the remote object.
	 * @param packageListener
	 *            the listener that is added.
	 */
	private static void listen(String office,
			PackageEventListener packageListener) {
		try {
			GPSOfficeInterface gps = (GPSOfficeInterface) registry
					.lookup(office);
			gps.addListener(packageListener);
		} catch (RemoteException e) {
			System.out.println("Cannot connect to the remote object.");
			e.printStackTrace();
			System.exit(0);
		} catch (NotBoundException e) {
			System.out.println("Object is not bound to the registry server.");
			e.printStackTrace();
			System.exit(0);
		}

	}

	/**
	 * Class PackageEventListener is a RemoteEventListener encapsulation that
	 * specifically listens to remote events of the type GPSOfficeEvent.
	 * 
	 * @author Chinmay Dani
	 * 
	 */
	private static class PackageEventListener implements
			RemoteEventListener<GPSOfficeEvent> {

		/**
		 * Performs actions after receiving events.
		 */
		@Override
		public void report(long seq, GPSOfficeEvent event)
				throws RemoteException {
			if (event != null) {
				// If the package is lost by an office.
				if (event.isLost()) {
					System.out.println("Package number "
							+ event.getReceipt().getTrackNumber() + " lost by "
							+ event.getCurrentOffice() + " office");
				}
				// If the package is delivered by an office.
				else if (event.isDelivered()) {
					System.out.println("Package number "
							+ event.getReceipt().getTrackNumber()
							+ " delivered from " + event.getCurrentOffice()
							+ " office to (" + event.getReceipt().getX() + ","
							+ event.getReceipt().getY() + ")");
				}
				// If the package arrives at an office or departs from an
				// office.
				else {
					if (event.isArrived()) {
						System.out.println("Package number "
								+ event.getReceipt().getTrackNumber()
								+ " arrived at " + event.getCurrentOffice()
								+ " office");
					} else {
						System.out.println("Package number "
								+ event.getReceipt().getTrackNumber()
								+ " departed from " + event.getCurrentOffice()
								+ " office");
					}
				}
			}
		}

	}
}
