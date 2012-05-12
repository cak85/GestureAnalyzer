package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.tools.parallel.LoopBody;
import imuanalyzer.tools.parallel.Parallel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class Analyses {

	private static final Logger LOGGER = Logger.getLogger(Analyses.class
			.getName());

	Database db;

	ArrayList<Hand> hands = new ArrayList<Hand>();

	LinkedList<MovementStep> sum;

	LinkedList<MovementStep> avg;

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
	public LinkedList<MovementStep> getSumOfAll() {
		return sum;
	}

	/**
	 * return avg of all made movement steps
	 * 
	 * @return
	 */
	public LinkedList<MovementStep> getAvgOfAll() {
		return avg;
	}

	public void recalculate() {
		if (markers != null && filterType != null && movementStartJoint != null) {
			calculate(markers, filterType, movementStartJoint);
		}
	}

	public void calculate(Collection<Marker> _markers, FilterTypes _filterType,
			JointType _movementStartJoint) {

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

		// calculate sum of movements
		sum = new LinkedList<MovementStep>();
		for (Hand h : hands) {
			// improve performance by aggregation
			// over several hands
			for (MovementStep newState : h.getSavedMovementFlow()) {
				// check if an almost same position is already saved
				// if yes increase counter

				boolean exists = false;

				for (int i = 0; i < sum.size(); i++) {
					MovementStep m = sum.get(i);
					if (m.getMove().equals(newState)) {
						m.setCount(m.getCount() + newState.getCount());
						exists = true;
						// LOGGER.debug("Find existing position - Increase count");
						break;
					}
				}
				if (!exists) {
					sum.add(newState);
				}
			}
		}

		// calculate avg
		// average in every angle at start --> normalized
		// average of angle change added to former position
		// inclusion of counter...?
		avg = new LinkedList<MovementStep>();

		// TODO implement movement average calculation
		if (hands.size() > 0) {
			avg.addAll(hands.get(0).getSavedMovementFlow());
		}

		LOGGER.info("Calculation complete");
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
