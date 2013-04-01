import edu.rit.ds.registry.RegistryProxy;

public class Customer {
	public static void main(String[] args) throws Exception {
		if (args.length != 5) {
			System.out
					.println("Usage: java Customer <host> <port> <name> <X> <Y>");
			System.exit(0);
		}
		RegistryProxy proxy = new RegistryProxy("localhost", 2000);
		GPSOfficeInterface gps = (GPSOfficeInterface) proxy.lookup(args[2]);
		gps.show(Double.parseDouble(args[3]), Double.parseDouble(args[3]));
	}
}
