package imuanalyzer.device;

import imuanalyzer.device.ImuUpdateListener;
import imuanalyzer.device.ImuEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImuEventManager {
	private List<ImuUpdateListener> listeners = new LinkedList<ImuUpdateListener>();

	ExecutorService eventExecutor = Executors.newCachedThreadPool();

	public void addEventListener(ImuUpdateListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeEventListener(ImuUpdateListener listener) {
		listeners.remove(listener);
	}

	public void fireEvent(ImuEvent event) {
		for (int i = 0; i < listeners.size(); i++) {
			eventExecutor
					.execute(new ImuEventRunnable(listeners.get(i), event));
		}
	}

	/**
	 * 
	 * Runnable for IMU events
	 * 
	 */
	class ImuEventRunnable implements Runnable {
		ImuUpdateListener listener;
		ImuEvent event;

		ImuEventRunnable(ImuUpdateListener listener, ImuEvent event) {
			this.listener = listener;
			this.event = event;
		}

		@Override
		public void run() {
			listener.notifyImuDataUpdate(event);
		}

	}
}
