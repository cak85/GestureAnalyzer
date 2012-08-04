package imuanalyzer.device;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

public class ImuEventManager {

	private static final Logger LOGGER = Logger.getLogger(ImuEventManager.class
			.getName());

	private ImuUpdateListener currentListener = null;

	private BlockingQueue<ImuEvent> events = new ArrayBlockingQueue<ImuEvent>(
			500, false);

	private long lastFilterUpdate = 0;

	Worker worker;

	public ImuEventManager() {
	}

	private void startWorker() {

		worker = (new Worker());

		new Thread(worker).start();
	}

	private void stopWorker() {
		worker.stopWorker = false;
		worker = null;
	}

	public void addEventListener(ImuUpdateListener listener) {
		if (currentListener == null) {
			currentListener = listener;
			startWorker();
		}
	}

	public void removeEventListener(ImuUpdateListener listener) {
		if (currentListener == listener) {
			stopWorker();
			listener = null;
		}
	}

	public void fireEvent(ImuEvent event) {

		// ignore first event
		if (lastFilterUpdate == 0) {
			lastFilterUpdate = System.currentTimeMillis();
			return;
		}

		long newFilterUpdate = System.currentTimeMillis();

		double samplePeriod = ((double) newFilterUpdate - (double) lastFilterUpdate)
				/ (double) 1000;
		//LOGGER.debug("SamplePeriod: " + samplePeriod);

		lastFilterUpdate = newFilterUpdate;

		event.setSamplePeriod(samplePeriod);

		events.add(event);
	}

	/**
	 * Worker/Consumer task for handling all events
	 * 
	 * @author "Christopher-Eyk Hrabia"
	 * 
	 */
	class Worker implements Runnable {

		boolean stopWorker = false;

		@Override
		public void run() {
			while (!stopWorker) {
				ImuEvent event = events.poll();
				if (event != null) {
					currentListener.notifyImuDataUpdate(event);
				}
			}
		}
	}

}
