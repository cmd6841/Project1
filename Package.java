import java.io.Serializable;


public class Package implements Serializable {
	private final long trackNumber;
	private double x;
	private double y;
	
	public Package(long trackNumber, double x, double y) {
		this.x = x;
		this.y = y;
		this.trackNumber = trackNumber;
	}
	
	public long getTrackNumber() {
		return this.trackNumber;
	}
	
}
