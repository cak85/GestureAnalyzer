package imuanalyzer.device;

/**
 * Interface for beeing updated by new MARG-Data
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface MARGUpdateListener {
	public void notifyImuDataUpdate(MARGEvent event);
}
