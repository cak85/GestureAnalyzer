package imuanalyzer.device;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

/**
 * Observer for sharing and buffering new MARG data events
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class MARGEventManager {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(MARGEventManager.class.getName());

	private ArrayList<MARGUpdateListener> currentListeners = new ArrayList<MARGUpdateListener>();

	private BlockingQueue<MARGEvent> events = new ArrayBlockingQueue<MARGEvent>(
			1000, false);

	Worker worker;

	public MARGEventManager() {
	}

	/**
	 * Start event consumer thread
	 */
	private void startWorker() {

		worker = (new Worker());

		new Thread(worker).start();
	}

	/**
	 * End consumer thread
	 */
	private void stopWorker() {
		worker.stopWorker = true;
		worker = null;
	}

	/**
	 * Add new listener for event data
	 * 
	 * @param listener
	 */
	public void addEventListener(MARGUpdateListener listener) {
		currentListeners.add(listener);
		if (worker == null) {
			startWorker();
		}
	}

	/**
	 * Remove listener
	 * 
	 * @param listener
	 */
	public void removeEventListener(MARGUpdateListener listener) {
		currentListeners.remove(listener);
		listener = null;
		if (currentListeners.isEmpty()) {
			stopWorker();
		}
	}

	/**
	 * Add/Fire new event, will be buffered in queue
	 * 
	 * @param event
	 */
	public void fireEvent(MARGEvent event) {

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
				MARGEvent event = events.poll();
				if (event != null) {
					for (MARGUpdateListener listener: currentListeners) {
						listener.notifyImuDataUpdate(event);
					}
				}
			}
		}
	}

}
