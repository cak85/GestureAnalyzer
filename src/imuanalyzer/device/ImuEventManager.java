package imuanalyzer.device;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

public class ImuEventManager {

	private static final Logger LOGGER = Logger.getLogger(ImuEventManager.class
			.getName());

	private ImuUpdateListener currentListener = null;

	private BlockingQueue<ImuEvent> events = new ArrayBlockingQueue<ImuEvent>(
			1000, false);

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
