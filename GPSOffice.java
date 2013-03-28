import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

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
		neighbors = new ArrayList<Pair>();
		closest = new ArrayList<Pair>();
		addNeighbors();
	}

	public double getX() {
		return this.xpos;
	}

	public double getY() {
		return this.ypos;
	}

	private List<Pair> neighbors;
	private List<Pair> closest;
	public void addNeighbors() {
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
		System.out.println(closest);
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
