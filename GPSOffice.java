import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private static RegistryEventListener registryListener;
	private static RegistryEventFilter registryFilter;
	
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
		addNeighbors();
		
		registryListener = new RegistryEventListener() {
			public void report(long seq, RegistryEvent event) throws RemoteException {
				System.out.println(event.objectName());
				allOffices = registry.list();
				if(event.objectWasBound())
					updateNeighbors(event.objectName());
				else
					deleteNeighbor(event.objectName());
			}
		};
		UnicastRemoteObject.exportObject (registryListener, 0);
		registryFilter = new RegistryEventFilter()
		.reportType ("GPSOffice")
		.reportBound()
		.reportUnbound();
		registry.addEventListener (registryListener, registryFilter);
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
			if(!office.equals(name)) {
				GPSOfficeInterface node;
				try {
					node = (GPSOfficeInterface)registry.lookup(office);
					double dist = getDistance(node.getX(), node.getY());
					neighbors.add(new Pair(dist, node, office));
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			}
		}
		Collections.sort(neighbors);
		if(neighbors.size() < 3) {
			closest.addAll(neighbors);
		} else {
			closest.addAll(neighbors.subList(0, 3));
		}
		System.out.println("Updated neigbors: " + closest);
	}
	

	public void deleteNeighbor(String office) {
		for(Pair p : closest) {
			if (p.name.equals(office)) {
				closest.remove(p);
				break;
			}
		}
		addNeighbors();
	}
	
	public void updateNeighbors(String office) {
		GPSOfficeInterface newOffice;
		try {
			newOffice = (GPSOfficeInterface) registry.lookup(office);
			double newDistance = getDistance(newOffice.getX(), newOffice.getY());
			closest.add(new Pair(newDistance, newOffice, office));
			Collections.sort(closest);
			if(closest.size() > 3) {
				closest.remove(3);
			}
			System.out.println("Updated neigbors: " + closest);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	private double getDistance(double x, double y) {
		return Math.sqrt((x - xpos) * (x - xpos) + (y - ypos) * (y - ypos));
	}

	public void show() throws RemoteException {
		System.out.println(allOffices);
		System.out.println(neighbors);
		System.out.println(closest);
	}
	
	public String toString() {
		return name;
	}
	
	private class Pair implements Comparable<Pair> {
		public double distance;
		public GPSOfficeInterface office;
		public String name;
		public Pair(double d, GPSOfficeInterface gpso, String n){
			distance = d;
			office = gpso;
			name = n;
		}
		
		@Override
		public int compareTo(Pair o) {
			return (int)(distance - o.distance);
		}
		
		public String toString() {
			return name + ": " + Double.toString(distance);
		}
	}

}
