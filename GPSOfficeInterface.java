import java.rmi.Remote;
import java.rmi.RemoteException;


public interface GPSOfficeInterface extends Remote {
	public void show () throws RemoteException;
}
