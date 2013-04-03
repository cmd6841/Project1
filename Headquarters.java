import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;

public class Headquarters {
	private static String host;
	private static int port;
	private static Package pack;
	private static String currentOffice;
	private static double x;
	private static double y;
	private static RegistryProxy registry;
	private static RegistryEventListener registryListener;
	private static RegistryEventFilter registryFilter;
	private static final PackageEventListener listener = new PackageEventListener();;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Customer <host> <port>");
			System.exit(0);
		}
		host = args[0];
		port = Integer.parseInt(args[1]);

		try {
			UnicastRemoteObject.exportObject(listener, 0);
			registry = new RegistryProxy("localhost", 2000);

			registryListener = new RegistryEventListener() {

				@Override
				public void report(long seq, final RegistryEvent event)
						throws RemoteException {
					if (event.objectWasBound()) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									GPSOfficeInterface office = (GPSOfficeInterface) registry
											.lookup(event.objectName());
									if (listener != null)
										office.addListener(listener);
								} catch (RemoteException e) {
									e.printStackTrace();
								} catch (NotBoundException e) {
									e.printStackTrace();
								}
							}
						}).start();
					}
				}

			};
			UnicastRemoteObject.exportObject(registryListener, 0);
			registryFilter = new RegistryEventFilter().reportType("GPSOffice")
					.reportBound();
			registry.addEventListener(registryListener, registryFilter);

			addListener();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private static void addListener() {
		try {
			for (String name : registry.list("GPSOffice")) {
				GPSOfficeInterface office = (GPSOfficeInterface) registry
						.lookup(name);
				office.addListener(listener);
			}
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
			currentOffice = event.currentOffice;
			if (event.lost) {
				System.out.println("Package number "
						+ event.pack.getTrackNumber() + " lost by "
						+ event.currentOffice + " office");
			}
			if (event.isDelivered) {
				System.out.println("Package number "
						+ event.pack.getTrackNumber() + " delivered from "
						+ event.currentOffice + " office to (" + x + "," + y
						+ ")");
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
