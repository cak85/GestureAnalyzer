package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.device.ImuEvent;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.utils.SensorVector;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

public class Recorder implements IRecorder {

	private static final Logger LOGGER = Logger.getLogger(Recorder.class
			.getName());

	private Database db;

	private IRecordDataNotify recordListener = null;

	private BlockingQueue<ImuEvent> events = new ArrayBlockingQueue<ImuEvent>(
			500, false);

	Worker currentWorker = null;

	public Recorder() {
		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}

	public void recordData(ImuEvent event) {
		events.add(event);
	}

	public void startRecording() {
		if (currentWorker == null) {
			currentWorker = new Worker();

			new Thread(currentWorker).start();
		}
	}

	public void stopRecording() {
		currentWorker.stopWorker = true;
		currentWorker = null;
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
			db.writeImuData(data[i].getId(), accel, gyro, magneto,
					samplePeriod, timestamp);
		}

		if (recordListener != null) {
			recordListener.notifyRecordNewData(timestamp);
		}
	}

	public void setRecordDataNotifyListener(IRecordDataNotify listener) {
		recordListener = listener;
	}

	class Worker implements Runnable {

		boolean stopWorker = false;

		@Override
		public void run() {
			while (true) {
				ImuEvent event = events.poll();
				// stop when stopped and queue empty
				if (stopWorker && event == null) {
					break;
				}
				if (event != null) {
					recordData(event.getData(), event.getSamplePeriod());
				}
			}
		}
	}
}
