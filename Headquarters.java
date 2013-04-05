import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;

public class Headquarters implements Serializable {

	private static final long serialVersionUID = 1L;
	private static String host;
	private static int port;
	private static Package pack;
	// private static String currentOffice;
	private static RegistryProxy registry;
	private static RegistryEventListener registryListener;
	private static RegistryEventFilter registryFilter;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Headquarters <host> <port>");
			System.exit(0);
		}
		host = args[0];
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(
					"Headquarters: Invalid <port>: \"" + args[1] + "\"");
		}
		final PackageEventListener packageListener = new PackageEventListener();
		try {
			registry = new RegistryProxy(host, port);

			registryListener = new RegistryEventListener() {

				@Override
				public void report(long arg0, RegistryEvent event)
						throws RemoteException {
					System.out.println(event.objectName());
					listen(event.objectName(), packageListener);
				}
			};
			UnicastRemoteObject.exportObject(registryListener, 0);

			registryFilter = new RegistryEventFilter().reportType(
					"GPSOfficeInterface").reportBound();
			registry.addEventListener(registryListener, registryFilter);

			UnicastRemoteObject.exportObject(packageListener, 0);

			for (String office : registry.list("GPSOfficeInterface")) {
				listen(office, packageListener);
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private static void listen(String office,
			PackageEventListener packageListener) {
		try {
			GPSOfficeInterface gps = (GPSOfficeInterface) registry
					.lookup(office);
			gps.addListener(packageListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

	}

	static class PackageEventListener implements
			RemoteEventListener<PackageEvent> {
		public PackageEventListener() {
		}

		@Override
		public void report(long seq, PackageEvent event) throws RemoteException {
			if (pack == null) {
				pack = event.pack;
			}
			// currentOffice = event.currentOffice;
			if (event.lost) {
				System.out.println("Package number "
						+ event.pack.getTrackNumber() + " lost by "
						+ event.currentOffice + " office");
			} else if (event.isDelivered) {
				System.out.println("Package number "
						+ event.pack.getTrackNumber() + " delivered from "
						+ event.currentOffice + " office to (" + pack.getX()
						+ "," + pack.getY() + ")");
			} else {
				if (event.arrived) {
					System.out.println("Package number "
							+ event.pack.getTrackNumber() + " arrived at "
							+ event.currentOffice + " office");
				} else {
					System.out.println("Package number "
							+ event.pack.getTrackNumber() + " departed from "
							+ event.currentOffice + " office");
				}
			}
		}

	}
}
