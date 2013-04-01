import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;


public interface GPSOfficeInterface extends Remote {
	public void show (double x, double y) throws RemoteException;
	
	public double getX() throws RemoteException;
	
	public double getY() throws RemoteException;
	
	public void forwardPackage(Package pack, final double destx, final double desty) throws RemoteException;
	
}
