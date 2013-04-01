import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.rit.ds.registry.AlreadyBoundException;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;

public class GPSOffice implements GPSOfficeInterface {

	private String host;
	private int port;
	private String name;
	private double xpos;
	private double ypos;
	private RegistryProxy registry;
	private List<String> allOffices;
	private List<Pair> neighbors;
	private List<Pair> closest;
	private RegistryEventListener registryListener;
	private RegistryEventFilter registryFilter;
	private ExecutorService executor;

	public GPSOffice(String[] args) throws IOException {
		if (args.length != 5) {
			throw new IllegalArgumentException(
					"Usage: java Start GPSOffice <host> <port> <name> <X> <Y>");
		}
		host = args[0];
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("GPSOffice: Invalid <port>: \""
					+ args[1] + "\"");
		}
		name = args[2];
		try {
			xpos = Double.parseDouble(args[3]);
		} catch (NullPointerException npe) {
			throw new IllegalArgumentException(
					"GPSOffice: Invalid <xpos>: \"null\"");
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("GPSOffice: Invalid <xpos>: \""
					+ args[3] + "\"");
		}
		try {
			ypos = Double.parseDouble(args[4]);
		} catch (NullPointerException npe) {
			throw new IllegalArgumentException(
					"GPSOffice: Invalid <ypos>: \"null\"");
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("GPSOffice: Invalid <ypos>: \""
					+ args[4] + "\"");
		}

		registry = new RegistryProxy(host, port);

		UnicastRemoteObject.exportObject(this, 0);

		try {
			registry.bind(name, this);
		} catch (AlreadyBoundException abe) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException nse) {

			}
			throw new IllegalArgumentException("GPSOffice: <name> = \"" + name
					+ "\" already exists");
		} catch (RemoteException re) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException nse) {

			}
			throw re;
		} catch (Exception e) {
			throw e;
		}

		allOffices = new ArrayList<String>();
		allOffices = registry.list();
		addNeighbors();

		registryListener = new RegistryEventListener() {
			public void report(long seq, final RegistryEvent event)
					throws RemoteException {
				System.out.println(event.objectName());
				allOffices = registry.list();
				if (event.objectWasBound()) {
					new Thread(new Runnable() {
						public void run() {
							GPSOfficeInterface newOffice;
							synchronized (closest) {
								try {
									newOffice = (GPSOfficeInterface) registry
											.lookup(event.objectName());
									double x = newOffice.getX(), y = newOffice
											.getY();
									double newDistance = getDistance(x, y);
									closest.add(new Pair(newDistance,
											newOffice, event.objectName(), x, y));
									Collections.sort(closest);
									if (closest.size() > 3) {
										closest.remove(3);
									}
									System.out.println("Updated neigbors: "
											+ closest);
								} catch (RemoteException e) {
									e.printStackTrace();
								} catch (NotBoundException e) {
									e.printStackTrace();
								}
							}
						}
					}).start();
				} else {
					new Thread(new Runnable() {
						public void run() {
							synchronized (closest) {
								for (Pair p : closest) {
									if (p.name.equals(event.objectName())) {
										closest.remove(p);
										break;
									}
								}
								addNeighbors();
							}
						}
					}).start();
				}
			}
		};
		UnicastRemoteObject.exportObject(registryListener, 0);
		registryFilter = new RegistryEventFilter().reportType("GPSOffice")
				.reportBound().reportUnbound();
		registry.addEventListener(registryListener, registryFilter);

		executor = Executors.newCachedThreadPool();

	}

	public double getX() {
		return this.xpos;
	}

	public double getY() {
		return this.ypos;
	}

	public void addNeighbors() {
		neighbors = new ArrayList<Pair>();
		closest = new ArrayList<Pair>();
		for (String office : allOffices) {
			if (!office.equals(name)) {
				GPSOfficeInterface node;
				try {
					node = (GPSOfficeInterface) registry.lookup(office);
					double x = node.getX(), y = node.getY();
					double dist = getDistance(x, y);
					neighbors.add(new Pair(dist, node, office, x, y));
				} catch (ConnectException e) {
					continue; // When the registry contains a value for an
								// unbound object.
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			}
		}
		Collections.sort(neighbors);
		if (neighbors.size() < 3) {
			closest.addAll(neighbors);
		} else {
			closest.addAll(neighbors.subList(0, 3));
		}
		System.out.println("Updated neigbors: " + closest);
	}

	private double getDistance(double x, double y) {
		return Math.sqrt((x - xpos) * (x - xpos) + (y - ypos) * (y - ypos));
	}

	public String toString() {
		return name;
	}

	public static class Pair implements Comparable<Pair> {
		public double distance;
		public GPSOfficeInterface office;
		public String name;
		public double xpos;
		public double ypos;

		public Pair(double d, GPSOfficeInterface gpso, String n, double x,
				double y) {
			distance = d;
			office = gpso;
			name = n;
			xpos = x;
			ypos = y;
		}

		@Override
		public int compareTo(Pair o) {
			return ((distance - o.distance) == 0) ? 0
					: ((distance - o.distance) > 0.0) ? 1 : -1;
		}

		public String toString() {
			return name + ": " + Double.toString(distance);
		}

		private double getDistance(double x, double y) {
			return Math.sqrt((x - xpos) * (x - xpos) + (y - ypos) * (y - ypos));
		}
	}

	public void show(final double destx, final double desty)
			throws RemoteException {
		examinePackage();
		forwardPackage(createPackage(destx, desty), destx, desty);
	}

	private Package createPackage(double destx, double desty) {
		return new Package(System.currentTimeMillis(), destx, desty);
	}

	private List<Pair> passer;

	public void forwardPackage(final Package pack, final double destx,
			final double desty) {
		System.out.println("In city: " + name);
		passer = new ArrayList<Pair>();
		passer.addAll(closest);
		passer.add(new Pair(0, this, name, xpos, ypos));
		Collections.sort(passer, new Comparator<Pair>() {

			@Override
			public int compare(Pair o1, Pair o2) {
				double d1 = o1.getDistance(destx, desty);
				double d2 = o2.getDistance(destx, desty);
				return ((d1 - d2) == 0) ? 0 : ((d1 - d2) > 0.0) ? 1 : -1;
			}

		});
		if (passer.get(0).name.equals(name))
			System.out.println("Package number <tracknum> delivered from "
					+ name + " office to ( " + Double.toString(destx) + ", "
					+ Double.toString(desty) + " )");

		else {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						passer.get(0).office.forwardPackage(pack, destx, desty);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private void examinePackage() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
