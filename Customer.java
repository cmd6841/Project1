import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.RegistryProxy;

public class Customer {
	public static void main(String[] args) throws Exception {
		if (args.length != 5) {
			System.out
					.println("Usage: java Customer <host> <port> <name> <X> <Y>");
			System.exit(0);
		}
		RegistryProxy proxy = new RegistryProxy("localhost", 2000);
		RemoteEventListener<PackageEvent> listener = new RemoteEventListener<PackageEvent>() {

			@Override
			public void report(long seq, PackageEvent event)
					throws RemoteException {
				String s = null;
				if(event.isDelivered) {
					s = "Delivered.";
					System.out.println(event.pack.getTrackNumber() + ": " + event.currentOffice + ": " + s);
					System.exit(0);
				}
				else 
					s = "Not yet delivered.";
				System.out.println(event.pack.getTrackNumber() + ": " + event.currentOffice + ": " + s);
			}
		};
		UnicastRemoteObject.exportObject (listener, 0);
		GPSOfficeInterface gps = (GPSOfficeInterface) proxy.lookup(args[2]);
		gps.show(Double.parseDouble(args[3]), Double.parseDouble(args[3]), listener);
	}
}
