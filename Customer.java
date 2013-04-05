import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;

public class Customer {

	private static String host;
	private static int port;
	private static String name;
	private static double x;
	private static double y;
	private static Package pack;
	private static String currentOffice;
	private static RegistryProxy proxy;

	public static void main(String[] args) {
		if (args.length != 5) {
			System.out
					.println("Usage: java Customer <host> <port> <name> <X> <Y>");
			System.exit(0);
		}
		host = args[0];
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Customer: Invalid <port>: \""
					+ args[1] + "\"");
		}
		name = args[2];
		try {
			x = Double.parseDouble(args[3]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Customer: Invalid <X>: \""
					+ args[3] + "\"");
		}
		try {
			y = Double.parseDouble(args[4]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Customer: Invalid <Y>: \""
					+ args[4] + "\"");
		}

		
		PackageEventListener listener = new PackageEventListener();
		try {
			UnicastRemoteObject.exportObject(listener, 0);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		GPSOfficeInterface gpsOffice;

		try {
			proxy = new RegistryProxy(host, port);
			gpsOffice = (GPSOfficeInterface) proxy.lookup(name);
			gpsOffice.show(x, y, listener);
		} catch (RemoteException e) {
			System.out.println("Package number " + pack.getTrackNumber()
					+ " lost by " + currentOffice + " office");
			e.printStackTrace();
			System.exit(0);
		} catch (NotBoundException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			System.out.println("Package number " + pack.getTrackNumber()
					+ " lost by " + currentOffice + " office");
			e.printStackTrace();
			System.exit(0);
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
				System.exit(0);
			} else if (event.isDelivered) {
				System.out.println("Package number "
						+ event.pack.getTrackNumber() + " delivered from "
						+ event.currentOffice + " office to (" + x + "," + y
						+ ")");
				System.exit(0);
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
