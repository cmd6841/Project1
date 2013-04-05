import java.io.Serializable;

import edu.rit.ds.RemoteEvent;

public class PackageEvent extends RemoteEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	public final Package pack;
	public boolean lost = false;
	public String currentOffice;
	public boolean isDelivered = false;
	public boolean arrived = false;

	public PackageEvent(Package pack, String name, boolean lost,
			boolean isDelivered, boolean arrived) {
		this.pack = pack;
		this.currentOffice = name;
		this.lost = lost;
		this.isDelivered = isDelivered;
		this.arrived = arrived;
	}

}
