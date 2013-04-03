import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;

public interface GPSOfficeInterface extends Remote {
	public void show(double x, double y,
			RemoteEventListener<PackageEvent> listener) throws RemoteException;

	public double getX() throws RemoteException;

	public double getY() throws RemoteException;

	public void forwardPackage(Package pack, final double destx,
			final double desty, RemoteEventListener<PackageEvent> listener)
			throws RemoteException;

	public Lease addListener(RemoteEventListener<PackageEvent> listener)
			throws RemoteException;
}
