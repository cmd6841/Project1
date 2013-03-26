import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.rit.ds.registry.AlreadyBoundException;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;


public class GPSOffice implements GPSOfficeInterface {
	
	private String host;
	private int port;
	private String name;
	private double xpos;
	private double ypos;
	private RegistryProxy registry;
	private List<String> allOffices;
	private Map<String, GPSOfficeInterface> neighbors;
	
	public GPSOffice(String[] args) throws IOException {
		if ( args.length != 5 ) {
			throw new IllegalArgumentException("Usage: java Start GPSOffice <host> <port> <name> <X> <Y>");
		}
		host = args[0];
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException
			("GPSOffice: Invalid <port>: \""+args[1]+"\"");
		}
		name = args[2];
		try {
			xpos = Double.parseDouble(args[3]);
		} catch (NullPointerException npe) {
			throw new IllegalArgumentException
			("GPSOffice: Invalid <xpos>: \"null\"");
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException
			("GPSOffice: Invalid <xpos>: \""+args[3]+"\"");
		}
		try {
			ypos = Double.parseDouble(args[4]);
		} catch (NullPointerException npe) {
			throw new IllegalArgumentException
			("GPSOffice: Invalid <ypos>: \"null\"");
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException
			("GPSOffice: Invalid <ypos>: \""+args[4]+"\"");
		}
		
		registry = new RegistryProxy (host, port);
		
		UnicastRemoteObject.exportObject (this, 0);
		
		try {
			registry.bind(name, this);
		} catch (AlreadyBoundException abe) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
		 	} catch (NoSuchObjectException nse) {
		 		
		 	}
			throw new IllegalArgumentException("GPSOffice: <name> = \""+name+"\" already exists");
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
		neighbors = new TreeMap<String, GPSOfficeInterface>();
		nearestDistances = new TreeMap<Double, String>();
		
		addNeighbors();
	}

	public double getX() {
		return this.xpos;
	}

	public double getY() {
		return this.ypos;
	}

	private Map<Double, String> nearestDistances;
	
	public void addNeighbors() {
		if(allOffices.size() < 5) {
			for (String office : allOffices) {
				if(!office.equals(name)) {
					try {
						neighbors.put(office, (GPSOfficeInterface) registry.lookup(office));
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (NotBoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
			String nearestNeighbor;
			double nearestDistance = Double.MAX_VALUE;
			for (String office : allOffices) {
				if(!office.equals(name)) {
					if(neighbors.size() < 3) {
						try {
							GPSOfficeInterface city = (GPSOfficeInterface) registry.lookup(office); 
							neighbors.put(office, city);
							double temp = distance(city.getX(), city.getY());
							if(nearestDistance > temp) {
								nearestDistance = temp;
								nearestNeighbor = office;
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (NotBoundException e) {
							e.printStackTrace();
						}
					} else {
						
					}
				}
			}
		}
		System.out.println(neighbors);
	}
	
	
	private double distance(double x, double y) {
		return Math.sqrt((x - xpos) * (x - xpos) + (y - ypos) * (y - ypos));
	}

	public void show() throws RemoteException {
		allOffices = registry.list();
		System.out.println(neighbors);
	}

}
