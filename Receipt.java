import java.io.Serializable;

/**
 * Class Receipt is a representation of a receipt of a package that is being
 * beamed in the Geographic Package System.
 * 
 * @author Chinmay Dani
 * 
 */
public class Receipt implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * The unique track number that identifies a package.
	 */
	private final long trackNumber;
	/**
	 * The X coordinate of the destination of the package.
	 */
	private double x;
	/**
	 * The X coordinate of the destination of the package.
	 */
	private double y;

	/**
	 * Constructs a new Receipt object.
	 * 
	 * @param trackNumber
	 *            The unique track number that identifies a package.
	 * @param x
	 *            The X coordinate of the destination of the package.
	 * @param y
	 *            The Y coordinate of the destination of the package.
	 */
	public Receipt(long trackNumber, double x, double y) {
		this.x = x;
		this.y = y;
		this.trackNumber = trackNumber;
	}

	/**
	 * Returns the track number of the pacakge.
	 * 
	 * @return the track number of the pacakge.
	 */
	public long getTrackNumber() {
		return this.trackNumber;
	}

	/**
	 * Returns the X coordinate of the location of the package.
	 * 
	 * @return the X coordinate of the location of the package.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the Y coordinate of the location of the package.
	 * 
	 * @return the Y coordinate of the location of the package.
	 */
	public double getY() {
		return y;
	}
}
