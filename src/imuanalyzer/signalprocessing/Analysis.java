package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.data.DatasetMetadata;
import imuanalyzer.device.MARGRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.math.Quaternion;
import imuanalyzer.utils.parallel.LoopBody;
import imuanalyzer.utils.parallel.Parallel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Main class for doing offline analysis on motion data.
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class Analysis {

	private static final Logger LOGGER = Logger.getLogger(Analysis.class
			.getName());

	/**
	 * Available analysis modes
	 * 
	 */
	public enum AnalysesMode {
		AVG, SUM, WITHOUTPOSTPROCCESIG, NONE
	};

	/**
	 * selected analysis
	 */
	private AnalysesMode mode = AnalysesMode.SUM;

	/**
	 * Database for getting data of current motion
	 */
	Database db;

	/**
	 * One hand per dataset
	 */
	ArrayList<Hand> hands = new ArrayList<Hand>();

	/**
	 * calculated move results
	 */
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

	/**
	 * For the analysis used datasets
	 */
	ArrayList<DatasetMetadata> datasets;

	/**
	 * Used filter type
	 */
	FilterTypes filterType;

	/**
	 * Joints with enabled motion analysis
	 */
	ArrayList<JointType> saveMotionJoints;

	/**
	 * Joints with enabled touch analysis
	 */
	ArrayList<JointType> saveTouchJoints;

	/**
	 * special percent values for extra highlithing
	 */
	ArrayList<Float> specialPercentPoints;

	/**
	 * Paint touch boxplot
	 */
	boolean withTouchBox;

	/**
	 * Paint min motion boxplot
	 */
	boolean withMinMotionBox;

	/**
	 * Paint max motion boxplot
	 */
	boolean withMaxMotionBox;

	/**
	 * maximum count of one hand position
	 */
	int maxMotionCount = 0;

	/**
	 * additional analyis listener
	 */
	IAnalysisExtension analysisExtension;

	/**
	 * touch analysis statistics Data
	 */
	ArrayList<IBoxplotData> statistics;

	/**
	 * Inform about analysis progress
	 */
	IProgress progress;

	/**
	 * Constructor
	 * 
	 * @param progress
	 */
	public Analysis(IProgress progress) {
		this.progress = progress;

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}

	/**
	 * recalculate current data
	 */
	public void recalculate() {
		if (datasets != null && filterType != null && saveMotionJoints != null) {
			prepare(datasets, filterType, saveMotionJoints, saveTouchJoints);
			switch (mode) {
			case AVG:
				moveResult = calculateMotionAvg();
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

	/**
	 * Start calculation
	 * 
	 * @param mode
	 *            calculation mode
	 * @param _markers
	 *            used datasets
	 * @param _filterType
	 *            used filtertype
	 * @param _movementStartJoint
	 *            joints from which the movement should be analyzed
	 * @param _touchJoint
	 *            joints from which the touch should be analyzed
	 * @param specialPercentPoints
	 *            special percent highliters
	 * @param extension
	 *            an externsion for this analysis, will be updated on every new
	 *            calculated motion step
	 * @param withTouchBox
	 *            paint touch boxplot
	 * @param withMinMotionBox
	 *            paint min motion boxplot
	 * @param withMaxMotionBox
	 *            paint max motion boxplot
	 */
	public void calculate(AnalysesMode mode,
			ArrayList<DatasetMetadata> _markers, FilterTypes _filterType,
			ArrayList<JointType> _movementStartJoint,
			ArrayList<JointType> _touchJoint,
			ArrayList<Float> specialPercentPoints,
			IAnalysisExtension extension, boolean withTouchBox,
			boolean withMinMotionBox, boolean withMaxMotionBox) {

		this.datasets = _markers;
		this.mode = mode;
		this.specialPercentPoints = specialPercentPoints;
		this.analysisExtension = extension;

		if (progress != null) {
			progress.setMaxSteps(datasets.size() + 2);
			progress.setStepSize(1);
		}

		prepare(_markers, _filterType, _movementStartJoint, _touchJoint);

		switch (mode) {
		case AVG:
			moveResult = calculateMotionAvg();
			if (progress != null) {
				progress.stepUp();
				progress.stepUp();
			}
			break;
		case SUM:
			calculateMotionSum();
			if (progress != null) {
				progress.stepUp();
			}
			calculateBoxPlots(specialPercentPoints, withTouchBox,
					withMinMotionBox, withMaxMotionBox);
			if (progress != null) {
				progress.stepUp();
			}
			break;
		case WITHOUTPOSTPROCCESIG:
		default:
			break;
		}
		// LOGGER.info("Calculation complete");
	}

	/**
	 * Calculate everything like its done live in high speed from the database
	 * data
	 * 
	 * @param _markers
	 *            datasets for which the calculation is done
	 * @param _filterType
	 *            used filtertypes
	 * @param _saveMotionJoints
	 *            analyzed motion joints
	 * @param _touchJoints
	 *            analyzed touch joints
	 */
	private void prepare(ArrayList<DatasetMetadata> _markers,
			FilterTypes _filterType, ArrayList<JointType> _saveMotionJoints,
			ArrayList<JointType> _touchJoints) {

		this.filterType = _filterType;
		this.saveMotionJoints = _saveMotionJoints;
		this.saveTouchJoints = _touchJoints;

		hands.clear();

		Parallel.For(0, datasets.size(), new LoopBody<Integer>() {

			@Override
			public void run(Integer markerIdx) {

				DatasetMetadata marker = datasets.get(markerIdx);

				ArrayList<MARGRawData> rawData = db.getImuData(marker);
				ArrayList<FeelingScale> feelingData = db.selectFeelings(marker);

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
						joint.setLocalRestOrientation(db.getInitialOrientation(
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
						MARGRawData[] currentSet = new MARGRawData[OrientationSensorManagerFactory.NUMBER_OF_SENSORS];

						Double sumSamplePeriod = new Double(0);
						for (int i = 0; i < rawData.size(); i++) {

							MARGRawData newData = rawData.get(i);
							Date newPeriod = newData.getTimeStamp();

							if (newPeriod.compareTo(currentPeriod) == 0
									&& newData.getId() < currentSet.length) {
								// order array by id
								currentSet[newData.getId()] = newData;
							} else {

								double samplePeriod = newData.getSamplePeriod();
								sumSamplePeriod += samplePeriod;

								// we have only one scale entry per set
								if (feelingData.size() > currentSet.length) {
									hand.getComfortScale().setAllValues(
											feelingData.get(currentSet.length));
								}
								orientationManager.processImuData(
										currentSet.clone(), samplePeriod);
								currentPeriod = newData.getTimeStamp();
								// do not forget to process current item
								currentSet[newData.getId()] = newData;

								if (analysisExtension != null) {
									analysisExtension.update(
											currentPeriod.getTime(), hand,
											markerIdx);
								}
							}
						}
					}

					hands.add(hand);

				} catch (Exception e) {
					e.printStackTrace();
				}
				if (progress != null) {
					progress.stepUp();
				}
			}
		});
		analysisExtension.finished();
	}

	/**
	 * Calculate average motion flow over all available hands/datasets
	 * 
	 * @return
	 */
	private LinkedList<MovementStep> calculateMotionAvg() {
		// calculate avg
		LinkedList<MovementStep> moveResult = new LinkedList<MovementStep>();

		if (hands.size() > 0) {

			// calculate per motion analysis
			for (int n = 0; n < saveMotionJoints.size(); n++) {

				// get maximum motion length of all captured motions
				for (Hand h : hands) {
					int currentLength = h.getRunningMotionAnalysis().get(n)
							.getSavedMovementFlow().size();
					if (currentLength > maxMotionCount) {
						maxMotionCount = currentLength;
					}
				}

				Hand emptyhand = new Hand(null,
						DatasetMetadata.getDefaultMarker());

				JointType saveMotionJoint = saveMotionJoints.get(n);

				// calculate motion average for every step in motion
				for (int i = 0; i < maxMotionCount; i++) {

					StoredJointState newBaseState = new StoredJointState(
							emptyhand.getJoint(saveMotionJoint));

					MovementStep avgElement = new MovementStep(newBaseState);
					avgElement.setCount(0);
					moveResult.add(avgElement);

					EnumMap<JointType, StoredJointState> avgSet = newBaseState
							.getAll();

					for (int j = 0; j < hands.size(); j++) {

						LinkedList<MovementStep> currentFlow = hands.get(j)
								.getRunningMotionAnalysis().get(n)
								.getSavedMovementFlow();

						if (i < currentFlow.size()) {
							MovementStep current = currentFlow.get(i);

							EnumMap<JointType, StoredJointState> currentJoints = current
									.getMove().getAll();

							for (Entry<JointType, StoredJointState> entry : avgSet
									.entrySet()) {

								StoredJointState avgJoint = entry.getValue();

								StoredJointState currentJoint = currentJoints
										.get(entry.getKey());

								Quaternion addedQuat = currentJoint
										.getLocalOrientation();
								// sum up

								if (avgElement.getCount() == 0) {
									avgJoint.setLocalOrientation(addedQuat);
								} else {

									avgJoint.setLocalOrientation(avgJoint
											.getLocalOrientation().plus(
													addedQuat));

								}

							}
							avgElement.setCount(avgElement.getCount()
									+ current.getCount());

						}

					}

					// calculate average --> divide
					for (Entry<JointType, StoredJointState> entry : avgSet
							.entrySet()) {

						StoredJointState avgJoint = entry.getValue();

						avgJoint.setLocalOrientation(avgJoint
								.getLocalOrientation()
								.times(1 / (double) (hands.size()))
								.normalized());

						avgJoint.updateWorldOrientation();


					}
					avgElement.setCount(Math.round(avgElement.getCount()
							/ (float) hands.size()) + 1);

				}

			}
		}

		return moveResult;
	}

	/**
	 * Calculate boxplots based on before calculated data
	 * 
	 * @param specialPercentPoints
	 * @param withTouchBox
	 * @param withMinMotionBox
	 * @param withMaxMotionBox
	 */
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

	/**
	 * sum up all motion steps to one representation
	 */
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

	/**
	 * get all calculated movement steps
	 * 
	 * @return
	 */
	public LinkedList<MovementStep> getMoveResult() {
		return moveResult;
	}

	/**
	 * get all touch lines
	 * 
	 * @return
	 */
	public ArrayList<VectorLine> getTouchResult() {
		return touchResult;
	}

	/**
	 * Get all calculated statistics
	 * 
	 * @return
	 */
	public ArrayList<IBoxplotData> getStatistics() {
		return statistics;
	}

	/**
	 * Get maximum number of motion steps
	 * 
	 * @return
	 */
	public int getMaxMotionCount() {
		return maxMotionCount;
	}

}
