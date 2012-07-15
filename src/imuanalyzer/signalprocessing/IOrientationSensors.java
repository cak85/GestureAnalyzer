package imuanalyzer.signalprocessing;

import imuanalyzer.device.IIMUDataProvider;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.IFilterListener;
import imuanalyzer.filter.ITuneFilter;

import java.util.Vector;

public interface IOrientationSensors {
	
	void addListener(IFilterListener listner);
	void removeListner(IFilterListener listner);
	
	void calibrate();

	boolean connect(String port);
	
	void disconnect();
	
	boolean isConnected();

	void setFilterType(FilterTypes filterType);
	
	Vector<FilterTypes> getAvailableFilters();
	
	FilterTypes getCurrentFilter();
	
	void setRecording(boolean isRecording);
	
	void processImuData(final ImuRawData data[],
			final double samplePeriod);
	
	void setRecordDataNotifyListener(IRecordDataNotify listener);
	
	IIMUDataProvider getImuDataProvider();
	
	ITuneFilter getCurrentTuning();
	
}
