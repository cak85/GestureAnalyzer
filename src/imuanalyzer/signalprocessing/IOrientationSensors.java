package imuanalyzer.signalprocessing;

import imuanalyzer.device.ImuRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.IFilterListener;

import java.util.Vector;

public interface IOrientationSensors {
	
	void addListener(int i, IFilterListener listner);
	void removeListner(int i, IFilterListener listner);
	
	int getNumberOfSensors();

	void calibrate();

	boolean connect(String port);

	void disconnect();

	void setFilterType(FilterTypes filterType);
	
	Vector<FilterTypes> getAvailableFilters();
	
	FilterTypes getCurrentFilter();
	
	void setRecording(boolean isRecording);
	
	public void processImuData(final ImuRawData data[],
			final double samplePeriod);
	
}
