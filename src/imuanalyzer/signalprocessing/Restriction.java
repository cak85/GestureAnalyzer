package imuanalyzer.signalprocessing;

/**
 * Class for holding joint restriction configuration.
 * 
 * @author toffer
 * 
 */
public class Restriction {

	public Restriction(double minRoll, double maxRoll, double minPitch,
			double maxPitch, double minYaw, double maxYaw) {
		this.minRoll = minRoll;
		this.maxRoll = maxRoll;
		this.minPitch = minPitch;
		this.maxPitch = maxPitch;
		this.minYaw = minYaw;
		this.maxYaw = maxYaw;
	}

	public Restriction() {
	}

	public boolean isRollAllowed() {
		return (minRoll != maxRoll);
	}

	public boolean isPitchAllowed() {
		return (minPitch != maxPitch);
	}

	public boolean isYawAllowed() {
		return (minYaw != maxYaw);
	}

	public double maxYaw = Double.MAX_VALUE;
	public double minYaw = -Double.MAX_VALUE;
	public double maxRoll = Double.MAX_VALUE;
	public double minRoll = -Double.MAX_VALUE;
	public double maxPitch = Double.MAX_VALUE;
	public double minPitch = -Double.MAX_VALUE;

}