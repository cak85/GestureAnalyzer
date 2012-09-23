package imuanalyzer.signalprocessing;

import imuanalyzer.device.IMARGDataProvider;
import imuanalyzer.device.MARGRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.IFilterListener;
import imuanalyzer.filter.ITuneFilter;

import java.util.Vector;

/**
 * Interface for a general orientation sensor implementation
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IOrientationSensors {

	/**
	 * add a listener which have to be notifyed on every update
	 * 
	 * @param listner
	 */
	void addListener(IFilterListener listner);

	/**
	 * Remove a listener
	 * 
	 * @param listner
	 */
	void removeListner(IFilterListener listner);

	/**
	 * Start calibration
	 */
	void calibrate();

	/**
	 * Connect to device
	 * 
	 * @param port
	 * @return
	 */
	boolean connect(String port);

	/**
	 * disconnect device
	 */
	void disconnect();

	/**
	 * Check conntection
	 * 
	 * @return
	 */
	boolean isConnected();

	/**
	 * Set filter type, only accept available ones
	 * 
	 * @param filterType
	 */
	void setFilterType(FilterTypes filterType);

	/**
	 * Get all available filters
	 * 
	 * @return
	 */
	Vector<FilterTypes> getAvailableFilters();

	/**
	 * Get current used filter
	 * 
	 * @return
	 */
	FilterTypes getCurrentFilter();

	/**
	 * Start recording of measured data
	 * 
	 * @param isRecording
	 */
	void setRecording(boolean isRecording);

	/**
	 * Update from external
	 * 
	 * @param data
	 * @param samplePeriod
	 */
	void processImuData(final MARGRawData data[], final double samplePeriod);

	/**
	 * Get current recorder
	 * 
	 * @return
	 */
	IRecorder getRecorder();

	/**
	 * Get data provider
	 * 
	 * @return
	 */
	IMARGDataProvider getImuDataProvider();

	/**
	 * Get current filter tuning configuration
	 * 
	 * @return
	 */
	ITuneFilter getCurrentTuning();

}
