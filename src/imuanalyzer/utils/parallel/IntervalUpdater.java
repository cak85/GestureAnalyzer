package imuanalyzer.utils.parallel;

import java.util.ArrayList;

/**
 * thread for updating something in intervals
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class IntervalUpdater extends Thread {

	private ArrayList<IIntervalUpdate> items;

	private boolean stop = false;

	private long update_cycle;

	public IntervalUpdater(ArrayList<IIntervalUpdate> items, long update_cycle) {
		this.items = items;
		this.update_cycle = update_cycle;
	}

	public void run() {
		while (true) {
			if (stop) {
				break;
			}
			try {

				for (IIntervalUpdate jR : items) {
					jR.update();
				}

				Thread.sleep(update_cycle);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
}
