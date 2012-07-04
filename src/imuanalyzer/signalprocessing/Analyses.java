package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.parallel.LoopBody;
import imuanalyzer.utils.parallel.Parallel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class Analyses {

	private static final Logger LOGGER = Logger.getLogger(Analyses.class
			.getName());

	public enum AnalysesMode {
		AVG, SUM, GRAPH, NONE
	};

	private AnalysesMode mode = AnalysesMode.SUM;

	Database db;

	ArrayList<Hand> hands = new ArrayList<Hand>();

	LinkedList<MovementStep> moveResult;

	/**
	 * all max line(s)
	 */
	ArrayList<VectorLine> touchResult;

	/**
	 * Max of maximums
	 */
	ArrayList<VectorLine> touchResultMax;

	/**
	 * average of maximums
	 */
	ArrayList<VectorLine> touchResultAvg;

	ArrayList<Marker> markers;
	FilterTypes filterType;
	ArrayList<JointType> saveMotionJoints;
	ArrayList<JointType> saveTouchJoints;

	ArrayList<Float> specialPercentPoints;

	boolean withTouchBox;
	boolean withMinMotionBox;
	boolean withMaxMotionBox;

	/**
	 * maximum count of one hand position
	 */
	int maxMotionCount = 0;

	IAnalysisExtension chartFiller;

	/**
	 * touch analysis statistics Data
	 */
	ArrayList<IBoxplotData> statistics;

	IProgress progress;

	public Analyses(IProgress progress) {
		this.progress = progress;

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}

	public void recalculate() {
		if (markers != null && filterType != null && saveMotionJoints != null) {
			prepare(markers, filterType, saveMotionJoints, saveTouchJoints);
			switch (mode) {
			case AVG:
				calculateMotionAvg();
				break;
			case SUM:
				calculateMotionSum();
				calculateBoxPlots(specialPercentPoints, withTouchBox,
						withMinMotionBox, withMaxMotionBox);
				break;

			default:
				break;
			}
		}

		LOGGER.info("Calculation complete");
	}

	public void calculate(AnalysesMode mode, ArrayList<Marker> _markers,
			FilterTypes _filterType, ArrayList<JointType> _movementStartJoint,
			ArrayList<JointType> _touchJoint,
			ArrayList<Float> specialPercentPoints,
			IAnalysisExtension chartFiller, boolean withTouchBox,
			boolean withMinMotionBox, boolean withMaxMotionBox) {

		this.markers = _markers;
		this.mode = mode;
		this.specialPercentPoints = specialPercentPoints;
		this.chartFiller = chartFiller;

		progress.setMaxSteps(markers.size() + 2);
		progress.setStepSize(1);

		prepare(_markers, _filterType, _movementStartJoint, _touchJoint);

		switch (mode) {
		case AVG:
			calculateMotionAvg();
			progress.stepUp();
			progress.stepUp();
			break;
		case SUM:
			calculateMotionSum();
			progress.stepUp();
			calculateBoxPlots(specialPercentPoints, withTouchBox,
					withMinMotionBox, withMaxMotionBox);
			progress.stepUp();
			break;
		case GRAPH:
		default:
			break;
		}
		LOGGER.info("Calculation complete");
	}

	private void prepare(ArrayList<Marker> _markers, FilterTypes _filterType,
			ArrayList<JointType> _saveMotionJoints,
			ArrayList<JointType> _touchJoints) {

		this.filterType = _filterType;
		this.saveMotionJoints = _saveMotionJoints;
		this.saveTouchJoints = _touchJoints;

		LOGGER.debug("Start calulation with FilterType " + filterType
				+ " and startJoint: " + saveMotionJoints);

		hands.clear();

		Parallel.For(0, markers.size(), new LoopBody<Integer>() {

			@Override
			public void run(Integer markerIdx) {

				Marker marker = markers.get(markerIdx);

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
						joint.setLocalOrientation(db.getInitialOrientation(
								marker, type));
						joint.setLocalPosition(db.getInitialPosition(marker,
								type));
					}

					// save movement
					for (JointType type : saveMotionJoints) {
						hand.addSaveMotionJoint(type);
					}

					// save touch
					for (JointType type : saveTouchJoints) {
						hand.addSaveTouchLineJoint(type);
					}

					// calculate movement based on recorded raw data
					if (rawData.size() > 0) {

						Date currentPeriod = rawData.get(0).getTimeStamp();
						ImuRawData[] currentSet = new ImuRawData[OrientationSensorManagerFactory.NUMBER_OF_SENSORS];

						Double sumSamplePeriod = new Double(0);
						for (int i = 0; i < rawData.size(); i++) {

							ImuRawData newData = rawData.get(i);
							Date newPeriod = newData.getTimeStamp();

							if (newPeriod.compareTo(currentPeriod) == 0
									&& newData.getId() < currentSet.length) {
								// order array by id
								currentSet[newData.getId()] = newData;
							} else {

								double samplePeriod = newData.getSamplePeriod();
								sumSamplePeriod += samplePeriod;
								db.selectFeelingData(currentPeriod,
										hand.getComfortScale());
								orientationManager.processImuData(
										currentSet.clone(), samplePeriod);
								currentPeriod = newData.getTimeStamp();
								// do not forget to process current item
								currentSet[newData.getId()] = newData;

								if (chartFiller != null) {
									chartFiller.update(hand, markerIdx,
											sumSamplePeriod);
								}
							}
						}
					}

					hands.add(hand);

				} catch (Exception e) {
					e.printStackTrace();
				}
				progress.stepUp();
			}
		});
	}

	private void calculateMotionAvg() {
		if (hands.size() > 0) {

			// calculate avg
			moveResult = new LinkedList<MovementStep>();

			// calculate per motion analysis
			for (int n = 0; n < saveMotionJoints.size(); n++) {

				int maxMotionLength = 0;
				// get maximum motion length of all captured motions
				for (Hand h : hands) {
					int currentLength = h.getRunningMotionAnalysis().get(n)
							.getSavedMovementFlow().size();
					if (currentLength > maxMotionLength) {
						maxMotionLength = currentLength;
					}
				}

				Hand emptyhand = new Hand(null, Marker.getDefaultMarker());
				Quaternion nullQuat = new Quaternion(0, 0, 0, 0);

				for (Entry<JointType, Joint> j : emptyhand.getJointSet()) {
					j.getValue().setLocalOrientation(nullQuat);
				}

				JointType saveMotionJoint = saveMotionJoints.get(n);

				for (int i = 0; i < maxMotionLength; i++) {

					MovementStep avgElement = new MovementStep(
							new StoredJointState(
									emptyhand.getJoint(saveMotionJoint)));
					avgElement.setCount(0);
					moveResult.add(avgElement);

					EnumMap<JointType, StoredJointState> avgJoints = avgElement
							.getMove().get(saveMotionJoint).getAll();

					for (int j = 0; j < hands.size(); j++) {

						LinkedList<MovementStep> currentFlow = hands.get(j)
								.getRunningMotionAnalysis().get(n)
								.getSavedMovementFlow();

						if (i < currentFlow.size()) {
							MovementStep current = currentFlow.get(i);

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
							avgElement.setCount(avgElement.getCount()
									+ current.getCount());
						}

					}

					// calculate average
					for (Entry<JointType, StoredJointState> entry : avgJoints
							.entrySet()) {
						StoredJointState avgJoint = entry.getValue();
						avgJoint.setLocalOrientation(avgJoint
								.getLocalOrientation().times(
										1 / (double) (hands.size())));
						avgJoint.getLocalOrientation().normalized();
					}
					avgElement.setCount(Math.round(avgElement.getCount()
							/ (float) hands.size()));

				}

			}
		}

	}

	private void calculateBoxPlots(ArrayList<Float> specialPercentPoints,
			boolean withTouchBox, boolean withMinMotionBox,
			boolean withMaxMotionBox) {
		statistics = new ArrayList<IBoxplotData>();

		// touchline
		if (withTouchBox) {
			touchResult = new ArrayList<VectorLine>();
			for (Hand h : hands) {
				touchResult.addAll(h.getMaxTouchLines());
			}

			for (int i = 0; i < saveTouchJoints.size(); i++) {
				ArrayList<VectorLine> linesOfOneJointAnalysis = new ArrayList<VectorLine>();
				for (Hand h : hands) {
					TouchAnalysis touchAnalysis = h.getRunningTouchAnalysis()
							.get(i);
					linesOfOneJointAnalysis.add(touchAnalysis.getMaxLine());
				}
				String name = Hand.jointTypeToName(saveTouchJoints.get(i));
				statistics.add(new VectorLineStatistics(name,
						linesOfOneJointAnalysis, specialPercentPoints));
			}
		}

		// motionline max
		if (withMaxMotionBox) {
			for (int i = 0; i < saveMotionJoints.size(); i++) {
				ArrayList<VectorLine> linesOfOneJointAnalysis = new ArrayList<VectorLine>();
				for (Hand h : hands) {
					MotionAnalysis motionAnalysis = h
							.getRunningMotionAnalysis().get(i);
					LOGGER.debug("MaxID:" + motionAnalysis.getMaxIdMotion());

					linesOfOneJointAnalysis.addAll(motionAnalysis.getMaxLine());
				}
				String name = Hand.jointTypeToName(saveMotionJoints.get(i))
						+ " max";
				statistics.add(new VectorLineStatistics(name,
						linesOfOneJointAnalysis, specialPercentPoints));
			}
		}

		// motionline min
		if (withMinMotionBox) {
			for (int i = 0; i < saveMotionJoints.size(); i++) {
				ArrayList<VectorLine> linesOfOneJointAnalysis = new ArrayList<VectorLine>();
				for (Hand h : hands) {
					MotionAnalysis motionAnalysis = h
							.getRunningMotionAnalysis().get(i);
					LOGGER.debug("MinID:" + motionAnalysis.getMinIdMotion());

					linesOfOneJointAnalysis.addAll(motionAnalysis.getMinLine());
				}
				String name = Hand.jointTypeToName(saveMotionJoints.get(i))
						+ " min";
				statistics.add(new VectorLineStatistics(name,
						linesOfOneJointAnalysis, specialPercentPoints));
			}
		}

	}

	private void calculateMotionSum() {
		// calculate sum of movements
		moveResult = new LinkedList<MovementStep>();

		maxMotionCount = 0;

		if (hands.size() < 1) {
			return;
		}

		ArrayList<LinkedList<MovementStep>> resultPerMotionAnalysis = new ArrayList<LinkedList<MovementStep>>();
		for (int i = 0; i < saveMotionJoints.size(); i++) {
			resultPerMotionAnalysis.add(new LinkedList<MovementStep>());
		}

		for (Hand h : hands) {
			// improve visual performance by aggregation
			// over several hands
			for (int i = 0; i < h.getRunningMotionAnalysis().size(); i++) {

				MotionAnalysis motionAnalysis = h.getRunningMotionAnalysis()
						.get(i);

				maxMotionCount = Math.max(motionAnalysis.getMaxCount(),
						maxMotionCount);
				for (MovementStep newState : motionAnalysis
						.getSavedMovementFlow()) {

					LinkedList<MovementStep> tmpResult = resultPerMotionAnalysis
							.get(i);

					// check if an almost same position is already saved
					// if yes increase counter
					boolean exists = false;
					for (int j = 0; j < tmpResult.size(); j++) {
						MovementStep m = tmpResult.get(j);
						if (m.getMove().equals(newState)) {
							int newCount = m.getCount() + newState.getCount();
							m.setCount(newCount);
							maxMotionCount = Math.max(newCount, maxMotionCount);
							exists = true;
							// LOGGER.debug("Find existing position - Increase count");
							break;
						}
					}
					if (!exists) {
						moveResult.add(newState);
					}
				}
			}
		}
		for (int i = 0; i < resultPerMotionAnalysis.size(); i++) {
			moveResult.addAll(resultPerMotionAnalysis.get(i));
		}
	}

	public FilterTypes getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterTypes filterType) {
		this.filterType = filterType;
	}

	public ArrayList<JointType> getSaveMotionJoints() {
		return saveMotionJoints;
	}

	public void setSaveMotionJoints(ArrayList<JointType> saveMotionJoints) {
		this.saveMotionJoints = saveMotionJoints;
	}

	public ArrayList<JointType> getSaveTouchJoints() {
		return saveTouchJoints;
	}

	public void setSaveTouchJoints(ArrayList<JointType> saveTouchJoints) {
		this.saveTouchJoints = saveTouchJoints;
	}

	/**
	 * return all made movement steps
	 * 
	 * @return
	 */
	public LinkedList<MovementStep> getMoveResult() {
		return moveResult;
	}

	public ArrayList<VectorLine> getTouchResult() {
		return touchResult;
	}

	public ArrayList<IBoxplotData> getStatistics() {
		return statistics;
	}

	public int getMaxMotionCount() {
		return maxMotionCount;
	}

}
