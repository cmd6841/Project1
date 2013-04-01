import edu.rit.ds.RemoteEvent;

public class PackageEvent extends RemoteEvent {
	private static final long serialVersionUID = 1L;
	public final Package pack;
	public String currentOffice;
	public boolean isDelivered = false;

	public PackageEvent(Package pack, String name, boolean isDelivered) {
		this.pack = pack;
		this.currentOffice = name;
		this.isDelivered = isDelivered;
	}

}
