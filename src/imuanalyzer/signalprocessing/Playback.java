package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.lang.Thread.State;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class Playback {

	private static final Logger LOGGER = Logger.getLogger(Playback.class
			.getName());

	Database db;

	Hand hand;

	IOrientationSensors orientationManager;

	Thread currentThread;

	volatile boolean stop = false;

	IPlaybackNotify notifier = null;

	float speed = 1;

	public Playback(Hand hand, IOrientationSensors orientationManager) {
		this.hand = hand;
		this.orientationManager = orientationManager;
		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}

	public void play(Marker marker, boolean loop) {
		if (currentThread != null
				&& (currentThread.getState() == State.RUNNABLE || currentThread
						.getState() == State.TIMED_WAITING)) {
			stop();
			// wait for termination
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
		if (currentThread == null
				|| currentThread.getState() == State.TERMINATED) {
			stop = false;
			currentThread = new Thread(new PlaybackRunnable(this, marker, loop));
			currentThread.start();
		}
		LOGGER.debug(currentThread.getState());
	}

	public void stop() {
		stop = true;
	}

	protected void internalPlay(Marker marker, boolean loop) {
		ArrayList<ImuRawData> rawData = db.getImuData(marker);
		ArrayList<FeelingScale> feelingData = db.selectFeelings(marker);

		Marker oldMarker = hand.getCurrentMarker();
		hand.setCurrentMarker(marker);
		hand.loadJointMappingFromMarker();

		do {

			// set initial orientation and position
			for (Entry<JointType, Joint> entry : hand.getJointSet()) {
				JointType type = entry.getKey();
				Joint joint = entry.getValue();
				joint.setLocalOrientation(db
						.getInitialOrientation(marker, type));
				joint.setLocalPosition(db.getInitialPosition(marker, type));
			}

			// calculate movement based on recorded raw data
			if (rawData.size() > 0) {

				Date currentPeriod = rawData.get(0).getTimeStamp();
				ImuRawData[] currentSet = new ImuRawData[OrientationSensorManagerFactory.NUMBER_OF_SENSORS];

				for (int i = 0; i < rawData.size(); i++) {

					if (stop) {
						loop = false;
						break;
					}

					ImuRawData newData = rawData.get(i);
					Date newPeriod = newData.getTimeStamp();

					if (newPeriod.compareTo(currentPeriod) == 0
							&& newData.getId() < currentSet.length) {
						// order array by id
						currentSet[newData.getId()] = newData;
					} else {
						try {
							Thread.sleep((long) (currentSet[0]
									.getSamplePeriod() * 1000 * speed));
						} catch (InterruptedException e) {
							LOGGER.error(e);
						}
						if (feelingData.size() > i / currentSet.length) {
							hand.getComfortScale().setAllValues(
									feelingData.get(i / currentSet.length));
						}
						orientationManager.processImuData(currentSet.clone(),
								currentSet[0].getSamplePeriod());
						currentPeriod = newData.getTimeStamp();
						// do not forget to process current item
						currentSet[newData.getId()] = newData;
					}
				}
			}
		} while (loop);

		hand.setCurrentMarker(oldMarker);
		hand.loadJointMappingFromMarker();

		if (notifier != null) {
			notifier.playbackStopped();
		}
	}

	public void setNotifyer(IPlaybackNotify notifier) {
		this.notifier = notifier;
	}

	class PlaybackRunnable implements Runnable {

		Marker marker;
		Playback player;
		boolean loop;

		public PlaybackRunnable(Playback player, Marker marker, boolean loop) {
			this.player = player;
			this.marker = marker;
			this.loop = loop;
		}

		@Override
		public void run() {
			player.internalPlay(marker, loop);
		}

	}

	/**
	 * @return the speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @param speed
	 *            the speed to set
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}

}
