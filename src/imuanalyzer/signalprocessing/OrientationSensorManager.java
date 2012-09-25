package imuanalyzer.signalprocessing;

import imuanalyzer.configuration.Configuration;
import imuanalyzer.device.IMARGDataProvider;
import imuanalyzer.device.MARGEvent;
import imuanalyzer.device.MARGRawData;
import imuanalyzer.device.MARGUpdateListener;
import imuanalyzer.filter.Filter;
import imuanalyzer.filter.FilterFactory;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.IFilterListener;
import imuanalyzer.filter.ITuneFilter;
import imuanalyzer.utils.SensorVector;
import imuanalyzer.utils.math.AngleHelper;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
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

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(OrientationSensorManager.class.getName());

	private TreeSet<FilterMapping> filters = new TreeSet<FilterMapping>(
			new PriorityComparator());

	private IMARGDataProvider imureader;

	private Object filterEditLock = new Object();

	private FilterTypes currentType;

	private volatile boolean isRecording = false;

	private Recorder recorder = new Recorder();

	public OrientationSensorManager(FilterTypes filterType, int numberOfImus)
			throws Exception {
		currentType = filterType;
	}

	public void setImuReader(IMARGDataProvider imureader) throws Exception {
		try {

			this.imureader = imureader;

			imureader.getEventManager().addEventListener(
					new MARGUpdateListener() {
						@Override
						public void notifyImuDataUpdate(final MARGEvent event) {

							synchronized (filterEditLock) {
								if (isRecording) {
									recorder.recordData(event);
								}

								processImuData(event.getData(),
										event.getSamplePeriod());
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
	public void processImuData(final MARGRawData data[],
			final double samplePeriod) {

		// LOGGER.debug("Process IMU Sampleperiod " + samplePeriod);

		// update filters in logical order
		for (FilterMapping fm : filters) {

			int id = fm.getListener().getSensorID();

			if (id > -1) {

				SensorVector accel = data[id].getAccelerometer();
				SensorVector magneto = data[id].getMagnetometer();
				SensorVector gyro = data[id].getGyroskope();
				double temp = 35 + ((data[id].getRawTemp()) + 13200) / 280.0;

				double gyroX = AngleHelper.radFromDeg(gyro.x / 14.375);
				double gyroY = AngleHelper.radFromDeg(gyro.y / 14.375);
				double gyroZ = AngleHelper.radFromDeg(gyro.z / 14.375);

				// degree to rad
				fm.getFilter().filterStep(samplePeriod, gyroX, gyroY, gyroZ,
						accel.x, accel.y, accel.z, magneto.x, magneto.y,
						magneto.z, temp);
			}
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

	/**
	 * Only the best working are shown
	 */
	@Override
	public Vector<FilterTypes> getAvailableFilters() {
		Vector<FilterTypes> types = new Vector<FilterFactory.FilterTypes>();
		types.add(FilterTypes.CF_MAHONY_MAGNETIC_DISTORSION);
		types.add(FilterTypes.KALMAN);
		// types.add(FilterTypes.CF_QUATERNION);
		types.add(FilterTypes.CF_MAHONY);
		types.add(FilterTypes.CF_MADGWICK_GRADIENT_DECENT);
		// not well usable
		// types.add(FilterTypes.VARANESO_DOF);
		// types.add(FilterTypes.CF_FREEIMU);
		types.add(FilterTypes.MY_FILTER);
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
		if (isRecording) {
			recorder.startRecording();
		} else {
			recorder.stopRecording();
		}
	}

	@Override
	public boolean isConnected() {
		return imureader.isConnected();
	}

	@Override
	public IMARGDataProvider getImuDataProvider() {
		return imureader;
	}

	@Override
	public ITuneFilter getCurrentTuning() {
		// THIS works because conficutation in Filter is always static!
		return FilterFactory.getFilter(Configuration.getInstance()
				.getFilterType());
	}

	@Override
	public IRecorder getRecorder() {
		return recorder;
	}

}
