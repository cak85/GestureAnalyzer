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

	public double maxYaw = Math.PI;
	public double minYaw = -Math.PI;
	public double maxRoll = Math.PI;
	public double minRoll = -Math.PI;
	public double maxPitch = Math.PI;
	public double minPitch = -Math.PI;

}