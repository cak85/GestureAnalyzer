package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.device.IIMUDataProvider;
import imuanalyzer.device.ImuEvent;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.device.ImuUpdateListener;
import imuanalyzer.filter.Filter;
import imuanalyzer.filter.FilterFactory;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.IFilterListener;
import imuanalyzer.filter.ITuneFilter;
import imuanalyzer.utils.SensorVector;
import imuanalyzer.utils.math.AngleHelper;

import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

public class OrientationSensorManager implements IOrientationSensors {

	/**
	 * compares FilterMapping by priority of its listener
	 */
	class PriorityComparator implements Comparator<FilterMapping> {

		@Override
		public int compare(FilterMapping o1, FilterMapping o2) {
			int prio1 = o1.getListener().getPriority();
			int prio2 = o2.getListener().getPriority();

			if (prio1 > prio2) {
				return 1;
			} else if (prio1 < prio2) {
				return -1;
			} else {
				return 0;
			}
		}

	}

	class FilterMapping {

		private Filter filter;

		private IFilterListener listener;

		public FilterMapping(IFilterListener listener, FilterTypes filterType) {
			filter = FilterFactory.getFilter(filterType);
			this.listener = listener;
			filter.setListener(listener);
		}

		public void setFilter(Filter filter) {
			this.filter = filter;
			filter.setListener(listener);
		}

		public Filter getFilter() {
			return filter;
		}

		public IFilterListener getListener() {
			return listener;
		}
	}

	private static final Logger LOGGER = Logger
			.getLogger(OrientationSensorManager.class.getName());

	private long lastFilterUpdate = 0;

	private TreeSet<FilterMapping> filters = new TreeSet<FilterMapping>(
			new PriorityComparator());

	private IIMUDataProvider imureader;

	private Object filterEditLock = new Object();

	private FilterTypes currentType;

	private Database database = Database.getInstance();

	private volatile boolean isRecording = false;

	private IRecordDataNotify recordListener = null;

	public OrientationSensorManager(FilterTypes filterType, int numberOfImus)
			throws Exception {
		currentType = filterType;
	}

	public void setImuReader(IIMUDataProvider imureader) throws Exception {
		try {

			this.imureader = imureader;

			lastFilterUpdate = System.currentTimeMillis();

			imureader.getEventManager().addEventListener(
					new ImuUpdateListener() {
						@Override
						public void notifyImuDataUpdate(final ImuEvent event) {

							long newFilterUpdate = System.currentTimeMillis();

							final double samplePeriod = ((double) newFilterUpdate - (double) lastFilterUpdate)
									/ (double) 1000;
							// LOGGER.debug("SamplePeriod: " + samplePeriod);

							lastFilterUpdate = newFilterUpdate;

							synchronized (filterEditLock) {
								if (isRecording) {
									recordData(event.getData(), samplePeriod);
								}

								processImuData(event.getData(), samplePeriod);
							}
						}

					});
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * data array must be ordered by id!!
	 */
	public void processImuData(final ImuRawData data[],
			final double samplePeriod) {

		// LOGGER.debug("Process IMU Sampleperiod " + samplePeriod);

		// update filters in logical order
		for (FilterMapping fm : filters) {
			int id = fm.getListener().getSensorID();

			if (id > -1) {

				SensorVector accel = data[id].getAccelerometer();
				SensorVector magneto = data[id].getMagnetometer();
				SensorVector gyro = data[id].getGyroskope();

				// degree to rad
				fm.getFilter().filterStep(samplePeriod,
						AngleHelper.radFromDeg(gyro.x),
						AngleHelper.radFromDeg(gyro.y),
						AngleHelper.radFromDeg(gyro.z), accel.x, accel.y,
						accel.z, magneto.x, magneto.y, magneto.z);
			}
		}

	}

	private void recordData(final ImuRawData[] data, final double samplePeriod) {
		final Date timestamp = new Date();

		for (int i = 0; i < data.length; i++) {
			SensorVector accel = data[i].getAccelerometer();
			SensorVector magneto = data[i].getMagnetometer();
			SensorVector gyro = data[i].getGyroskope();
			if (i > data[i].getId()) {
				LOGGER.error("ERRRRRRRRRRROOOOOOOR");
			}
			database.writeImuData(data[i].getId(), accel, gyro, magneto,
					samplePeriod, timestamp);
		}

		if (recordListener != null) {
			recordListener.notifyRecordNewData(timestamp);
		}
	}

	public void setFilterType(FilterTypes filterType) {
		currentType = filterType;
		synchronized (filterEditLock) {
			for (FilterMapping fm : filters) {
				fm.setFilter(FilterFactory.getFilter(filterType));
			}
		}
	}

	@Override
	public void calibrate() {
		if (imureader != null) {
			imureader.calibrate();
			synchronized (filterEditLock) {
				for (FilterMapping fm : filters) {
					fm.getFilter().init();
					fm.getFilter().setCalibrationMode(true);
				}
			}
		}
	}

	@Override
	public boolean connect(String port) {
		try {
			imureader.connectToPort(port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void disconnect() {
		imureader.close();
	}

	@Override
	public Vector<FilterTypes> getAvailableFilters() {
		Vector<FilterTypes> types = new Vector<FilterFactory.FilterTypes>();
		types.add(FilterTypes.AHRS);
		types.add(FilterTypes.KALMAN);
		types.add(FilterTypes.QUATERNION_COMPLEMENTARY);
		types.add(FilterTypes.AHRSMAHONY);
		types.add(FilterTypes.AHRSMADGWICK);
		return types;
	}

	@Override
	public FilterTypes getCurrentFilter() {
		return currentType;
	}

	@Override
	public void addListener(IFilterListener listener) {
		FilterMapping fm = new FilterMapping(listener, currentType);
		synchronized (filterEditLock) {
			filters.add(fm);
		}
	}

	@Override
	public void removeListner(IFilterListener listner) {
		synchronized (filterEditLock) {
			for (FilterMapping fm : filters) {
				if (fm.getListener().equals(listner)) {
					filters.remove(fm);
					break;
				}
			}
		}
	}

	@Override
	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	@Override
	public void setRecordDataNotifyListener(IRecordDataNotify listener) {
		recordListener = listener;
	}

	@Override
	public boolean isConnected() {
		return imureader.isConnected();
	}

	@Override
	public IIMUDataProvider getImuDataProvider() {
		return imureader;
	}

	@Override
	public ITuneFilter getCurrentTuning() {
		if (filters.size() > 0) {
			return filters.first().getFilter();
		} else {
			return null;
		}
	}

}
