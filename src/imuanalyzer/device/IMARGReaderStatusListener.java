package imuanalyzer.device;

/**
 * Interface for consuming error events from MARG-Reader
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IMARGReaderStatusListener {

	/**
	 * Get information about error
	 * 
	 * @param errormassage
	 */
	void notifyImuReaderError(String string);
}
