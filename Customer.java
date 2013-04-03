import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.RegistryProxy;

public class Customer {

	private static double x;
	private static double y;
	private static Package pack;
	private static String currentOffice;

	public static void main(String[] args) throws Exception {
		if (args.length != 5) {
			System.out
					.println("Usage: java Customer <host> <port> <name> <X> <Y>");
			System.exit(0);
		}
		x = Double.parseDouble(args[3]);
		y = Double.parseDouble(args[4]);

		RegistryProxy proxy = new RegistryProxy("localhost", 2000);
		
		PackageEventListener listener = new PackageEventListener();
		UnicastRemoteObject.exportObject(listener, 0);
		GPSOfficeInterface gps = (GPSOfficeInterface) proxy.lookup(args[2]);
		try {
		gps.show(x, y, listener);
		} catch (RemoteException e) {
			System.out.println("Package number "
					+ pack.getTrackNumber() + " lost by "
					+ currentOffice + " office");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Package number "
					+ pack.getTrackNumber() + " lost by "
					+ currentOffice + " office");
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
			}
			if (event.isDelivered) {
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
