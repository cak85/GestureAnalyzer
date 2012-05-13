package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.tools.parallel.LoopBody;
import imuanalyzer.tools.parallel.Parallel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class Analyses {

	private static final Logger LOGGER = Logger.getLogger(Analyses.class
			.getName());

	public enum AnalysesMode {
		AVG, SUM, NONE
	};

	private AnalysesMode mode = AnalysesMode.SUM;

	Database db;

	ArrayList<Hand> hands = new ArrayList<Hand>();

	LinkedList<MovementStep> result;

	Collection<Marker> markers;
	FilterTypes filterType;
	JointType movementStartJoint;

	public Analyses() {
		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}

	}

	/**
	 * return all made movement steps
	 * 
	 * @return
	 */
	public LinkedList<MovementStep> getResult() {
		return result;
	}

	public void recalculate() {
		if (markers != null && filterType != null && movementStartJoint != null) {
			calculate(markers, filterType, movementStartJoint);
			switch (mode) {
			case AVG:
				calculateMotionAvg();
				break;
			case SUM:
				calculateMotionSum();
				break;

			default:
				break;
			}
		}

		LOGGER.info("Calculation complete");
	}

	public void calculateAvg(Collection<Marker> _markers,
			FilterTypes _filterType, JointType _movementStartJoint) {
		calculate(AnalysesMode.AVG, _markers, _filterType, _movementStartJoint);
	}

	public void calculateSUM(Collection<Marker> _markers,
			FilterTypes _filterType, JointType _movementStartJoint) {
		calculate(AnalysesMode.SUM, _markers, _filterType, _movementStartJoint);
	}

	public void calculate(AnalysesMode mode, Collection<Marker> _markers,
			FilterTypes _filterType, JointType _movementStartJoint) {

		this.mode = mode;
		calculate(_markers, _filterType, _movementStartJoint);

		switch (mode) {
		case AVG:
			calculateMotionAvg();
			break;
		case SUM:
			calculateMotionSum();
			break;

		default:
			break;
		}
		LOGGER.info("Calculation complete");
	}

	private void calculate(Collection<Marker> _markers,
			FilterTypes _filterType, JointType _movementStartJoint) {

		this.markers = _markers;
		this.filterType = _filterType;
		this.movementStartJoint = _movementStartJoint;

		LOGGER.debug("Start calulation with FilterType " + filterType
				+ " and startJoint: " + movementStartJoint);

		hands.clear();

		Parallel.ForEach(markers, new LoopBody<Marker>() {

			@Override
			public void run(Marker marker) {

				ArrayList<ImuRawData> rawData = db.getImuData(marker);

				IOrientationSensors orientationManager;

				try {
					orientationManager = OrientationSensorManagerFactory
							.getOrientationManager();
					orientationManager.setFilterType(filterType);

					Hand hand = new Hand(orientationManager, marker);

					// set initial orientation
					for (Entry<JointType, Joint> entry : hand.getJointSet()) {
						JointType type = entry.getKey();
						Joint joint = entry.getValue();
						// initial orientation
						joint.setInitialOrientation(db.getInitialOrientation(
								marker, type));
					}

					// save movement
					hand.setSavedMovementStartJoint(movementStartJoint);
					hand.setSaveMovement(true);

					// calculate movement based on recorded raw data
					if (rawData.size() > 0) {

						Date currentPeriod = rawData.get(0).getTimeStamp();
						ImuRawData[] currentSet = new ImuRawData[orientationManager
								.getNumberOfSensors()];

						int dataIdx = 0;
						for (int i = 0; i < rawData.size(); i++) {

							ImuRawData newData = rawData.get(i);
							Date newPeriod = newData.getTimeStamp();

							if (newPeriod.compareTo(currentPeriod) == 0
									&& dataIdx < currentSet.length) {
								currentSet[dataIdx] = newData;
								dataIdx++;
							} else {

								orientationManager.processImuData(currentSet,
										newData.getSamplePeriod());
								currentPeriod = newData.getTimeStamp();
								dataIdx = 0;
							}
						}
					}

					hands.add(hand);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void calculateMotionAvg() {
		if (hands.size() > 0) {

			int motionLength = 0;
			// get maximum motion length
			for (Hand h : hands) {
				int currentLength = h.getSavedMovementFlow().size();
				if (currentLength > motionLength) {
					motionLength = currentLength;
				}
			}

			// calculate avg
			result = new LinkedList<MovementStep>();

			Hand emptyhand = new Hand(null, Marker.getDefaultMarker());

			for (int i = 0; i < motionLength; i++) {

				MovementStep avgElement = new MovementStep(
						new StoredJointState(
								emptyhand.getJoint(movementStartJoint)));
				avgElement.setCount(0);
				result.add(avgElement);

				EnumMap<JointType, StoredJointState> avgJoints = avgElement
						.getMove().get(movementStartJoint).getAll();

				for (int j = 0; j < hands.size(); j++) {

					if (i < hands.get(j).getSavedMovementFlow().size()) {
						MovementStep current = hands.get(j)
								.getSavedMovementFlow().get(i);

						EnumMap<JointType, StoredJointState> currentJoints = current
								.getMove().getAll();

						for (Entry<JointType, StoredJointState> entry : avgJoints
								.entrySet()) {

							// sum up
							Quaternion addedQuat = currentJoints.get(
									entry.getKey()).getLocalOrientation();

							StoredJointState avgJoint = entry.getValue();
							avgJoint.setLocalOrientation(avgJoint
									.getLocalOrientation().plus(addedQuat));
							
						}
						avgElement.setCount(avgElement.getCount()+current.getCount());
					}

				}

				// calculate average
				for (Entry<JointType, StoredJointState> entry : avgJoints
						.entrySet()) {
					StoredJointState avgJoint = entry.getValue();
					avgJoint.setLocalOrientation(avgJoint.getLocalOrientation()
							.times(1 / (double)(hands.size())));
					avgJoint.getLocalOrientation().normalizeLocal();
				}
				avgElement.setCount(Math.round(avgElement.getCount()/(float)hands.size()));

			}

		}
	}

	private void calculateMotionSum() {
		// calculate sum of movements
		result = new LinkedList<MovementStep>();
		for (Hand h : hands) {
			// improve performance by aggregation
			// over several hands
			for (MovementStep newState : h.getSavedMovementFlow()) {

				// check if an almost same position is already saved
				// if yes increase counter
				boolean exists = false;
				for (int i = 0; i < result.size(); i++) {
					MovementStep m = result.get(i);
					if (m.getMove().equals(newState)) {
						m.setCount(m.getCount() + newState.getCount());
						exists = true;
						// LOGGER.debug("Find existing position - Increase count");
						break;
					}
				}
				if (!exists) {
					result.add(newState);
				}
			}
		}
	}

	public FilterTypes getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterTypes filterType) {
		this.filterType = filterType;
	}

	public JointType getMovementStartJoint() {
		return movementStartJoint;
	}

	public void setMovementStartJoint(JointType movementStartJoint) {
		this.movementStartJoint = movementStartJoint;
	}
}
