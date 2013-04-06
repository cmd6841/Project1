import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;

/**
 * Interface GPSOfficeInterface serves the Java RMI interface to a GPSOffice
 * object in the Geographical Package System.
 * 
 * @author Chinmay Dani
 * 
 */
public interface GPSOfficeInterface extends Remote {

	/**
	 * Return the X coordinate of the location of the current GPSOffice object.
	 * 
	 * @return the X coordinate of the location of the current GPSOffice object.
	 * @throws RemoteException
	 *             Thrown if a remote error occurs.
	 */
	public double getX() throws RemoteException;

	/**
	 * Return the Y coordinate of the location of the current GPSOffice object.
	 * 
	 * @return the Y coordinate of the location of the current GPSOffice object.
	 * @throws RemoteException
	 *             Thrown if a remote error occurs.
	 */
	public double getY() throws RemoteException;

	/**
	 * Return the name of the city of the current GPSOffice object.
	 * 
	 * @return the name of the city of the current GPSOffice object.
	 * @throws RemoteException
	 *             Thrown if a remote error occurs.
	 */
	public String getName() throws RemoteException;

	/**
	 * Assign a track number to the package, examine it and send it to the
	 * current GPSOffice for further delivery process.
	 * 
	 * @param x
	 *            X coordinate of the destination
	 * @param y
	 *            Y coordinate of the destination
	 * @param listener
	 *            A remote event listener given by the customer to receive
	 *            notifications while the package is being beamed.
	 * @throws RemoteException
	 *             Thrown if a remote error occurs.
	 */
	public void sendPackage(double x, double y,
			RemoteEventListener<GPSOfficeEvent> listener)
			throws RemoteException;

	/**
	 * Calculates the GPSOffice that has the minimum distance from the
	 * destination and forward the package to that office for further delivery
	 * process.
	 * 
	 * @param receipt
	 *            A receipt object that contains the track number.
	 * @param destx
	 *            X coordinate of the destination
	 * @param desty
	 *            Y coordinate of the destination
	 * @param listener
	 *            A remote event listener given by the customer to receive
	 *            notifications while the package is being beamed.
	 * @throws RemoteException
	 *             Thrown if a remote error occurs.
	 */
	public void forwardPackage(Receipt receipt, final double destx,
			final double desty, RemoteEventListener<GPSOfficeEvent> listener)
			throws RemoteException;

	public Lease addListener(RemoteEventListener<GPSOfficeEvent> listener)
			throws RemoteException;

}
