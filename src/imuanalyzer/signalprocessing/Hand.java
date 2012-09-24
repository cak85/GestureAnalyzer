package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.data.DatasetMetadata;
import imuanalyzer.utils.math.AngleHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Represents human hand. This is the base class for the handmodell
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class Hand {

	private static final Logger LOGGER = Logger.getLogger(Hand.class.getName());

	/**
	 * Representation of the finger bones: First letter D = Daumen Z =
	 * Zeigefinger M = Mittelfinger R = Ringfinger K = Kleiner Finger H = Hand
	 * Second letter T = Top M = Middle D = Down R = Root
	 */
	public enum JointType { // order is important!!
		HAND_ROOT, THUMB_BOTTOM, INDEX_BOTTOM, MIDDLE_BOTTOM, RING_BOTTOM, LITTLE_BOTTOM, THUMB_MID, INDEX_MID, MIDDLE_MID, RING_MID, LITTLE_MID, THUMB_TOP, INDEX_TOP, MIDDLE_TOP, RING_TOP, LITTLE_TOP
	};

	/**
	 * Current orientation sensors
	 */
	protected IOrientationSensors sensors;

	/**
	 * Set of joints
	 */
	private EnumMap<JointType, Joint> joints = new EnumMap<JointType, Joint>(
			JointType.class);

	/**
	 * Current dataset description
	 */
	protected DatasetMetadata currentDataset;

	/**
	 * Database
	 */
	protected Database db;

	/**
	 * Current enabled analysis for touch
	 */
	protected ArrayList<TouchAnalysis> runningTouchAnalysis = new ArrayList<TouchAnalysis>();

	/**
	 * Current enabled analysis for motion
	 */
	protected ArrayList<MotionAnalysis> runningMotionAnalysis = new ArrayList<MotionAnalysis>();

	/**
	 * Lock for changeing analysis conficuration
	 */
	private Object analysisLock = new Object();

	/**
	 * save subjective feelings about gesture
	 */
	protected volatile FeelingScale feelingScale;

	/**
	 * Constructor
	 * 
	 * @param sensors
	 *            current used sensors
	 * @param marker
	 *            dataset description
	 */
	public Hand(IOrientationSensors sensors, DatasetMetadata marker) {
		this.sensors = sensors;
		this.currentDataset = marker;

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}

		ArrayList<Integer> values = new ArrayList<Integer>();
		values.add(0);
		feelingScale = db.getFeelingScale();

		if (sensors != null) {
			// register for record notification
			sensors.getRecorder().setRecordDataNotifyListener(feelingScale);
		}

		Joint elemKT = new Joint(this, JointType.LITTLE_TOP, sensors,
				db.getJointConstraint(JointType.LITTLE_TOP));
		Joint elemRT = new Joint(this, JointType.RING_TOP, sensors,
				db.getJointConstraint(JointType.RING_TOP));
		Joint elemMT = new Joint(this, JointType.MIDDLE_TOP, sensors,
				db.getJointConstraint(JointType.MIDDLE_TOP));
		Joint elemZT = new Joint(this, JointType.INDEX_TOP, sensors,
				db.getJointConstraint(JointType.INDEX_TOP));
		Joint elemDT = new Joint(this, JointType.THUMB_TOP, sensors,
				db.getJointConstraint(JointType.THUMB_TOP));

		Joint elemKM = new Joint(this, JointType.LITTLE_MID, sensors,
				db.getJointConstraint(JointType.LITTLE_MID));
		Joint elemRM = new Joint(this, JointType.RING_MID, sensors,
				db.getJointConstraint(JointType.RING_MID));
		Joint elemMM = new Joint(this, JointType.MIDDLE_MID, sensors,
				db.getJointConstraint(JointType.MIDDLE_MID));
		Joint elemZM = new Joint(this, JointType.INDEX_MID, sensors,
				db.getJointConstraint(JointType.INDEX_MID));
		Joint elemDM = new Joint(this, JointType.THUMB_MID, sensors,
				db.getJointConstraint(JointType.THUMB_MID));

		Joint elemKD = new Joint(this, JointType.LITTLE_BOTTOM, sensors,
				db.getJointConstraint(JointType.LITTLE_BOTTOM));
		Joint elemRD = new Joint(this, JointType.RING_BOTTOM, sensors,
				db.getJointConstraint(JointType.RING_BOTTOM));
		Joint elemMD = new Joint(this, JointType.MIDDLE_BOTTOM, sensors,
				db.getJointConstraint(JointType.MIDDLE_BOTTOM));
		Joint elemZD = new Joint(this, JointType.INDEX_BOTTOM, sensors,
				db.getJointConstraint(JointType.INDEX_BOTTOM));
		Joint elemDD = new Joint(this, JointType.THUMB_BOTTOM, sensors,
				db.getJointConstraint(JointType.THUMB_BOTTOM));

		Joint elemHR = new Joint(this, JointType.HAND_ROOT, sensors,
				db.getJointConstraint(JointType.HAND_ROOT));

		elemHR.addChild(elemKD);
		elemHR.addChild(elemRD);
		elemHR.addChild(elemMD);
		elemHR.addChild(elemZD);
		elemHR.addChild(elemDD);

		elemKD.addChild(elemKM);
		elemKM.addChild(elemKT);

		elemRD.addChild(elemRM);
		elemRM.addChild(elemRT);

		elemMD.addChild(elemMM);
		elemMM.addChild(elemMT);

		elemZD.addChild(elemZM);
		elemZM.addChild(elemZT);

		elemDD.addChild(elemDM);
		elemDM.addChild(elemDT);

		addAllElementsToMap(elemHR);

		loadJointMappingFromMarker();

		loadJointRelations();
	}

	/**
	 * Update motion radius constraints from DB
	 */
	public void refreshJointConstraintsFromDB() {
		for (JointType j : JointType.values()) {
			getJoint(j).setRestriction(db.getJointConstraint(j));
		}
	}

	/**
	 * refresh current sensor mapping from marker
	 */
	public void loadJointMappingFromMarker() {
		if (sensors != null) {
			for (JointType j : JointType.values()) {
				int id = db.getJointSensorMapping(currentDataset, j);
				setSensorID(j, id);
			}
		}
	}

	/**
	 * refresh current sensor mapping from marker
	 */
	public void loadJointRelations() {
		if (sensors != null) {
			for (JointType j : JointType.values()) {
				Joint independent = getJoint(j);
				ArrayList<JointRelation> relations = db.getJointRelation(this,
						independent);
				for (JointRelation relation : relations) {
					independent.addRelation(relation);
				}
			}
		}
	}

	/**
	 * Get current set of joints
	 * 
	 * @return
	 */
	public Set<Entry<JointType, Joint>> getJointSet() {
		return joints.entrySet();
	}

	/**
	 * Create a map from a joint and all of its children
	 * 
	 * @param elem
	 */
	private void addAllElementsToMap(Joint elem) {
		joints.put(elem.type, elem);
		for (Joint e : elem.children) {
			addAllElementsToMap(e);
		}
	}


	/**
	 * Change sensor id of joint
	 * 
	 * @param type
	 * @param id
	 */
	public void setSensorID(JointType type, int id) {
		Joint joint = ((Joint) joints.get(type));
		joint.setSensorID(id);
	}

	/**
	 * Save mapping of one joint
	 * 
	 * @param type
	 */
	public void saveJointSensorMapping(JointType type) {
		Joint joint = ((Joint) joints.get(type));
		db.setJointSensorMapping(currentDataset, type, joint.getSensorID());
	}

	/**
	 * Get joint
	 * 
	 * @param type
	 * @return
	 */
	public Joint getJoint(JointType type) {
		return ((Joint) joints.get(type));
	}

	/**
	 * Get current dataset description
	 * 
	 * @return
	 */
	public DatasetMetadata getCurrentDatasetDescription() {
		return currentDataset;
	}

	/**
	 * Set current dataset description
	 * 
	 * @param dataset
	 */
	public void setCurrentDatasetDescription(DatasetMetadata dataset) {
		this.currentDataset = dataset;
	}

	/**
	 * Get notification about updated joint
	 * 
	 * @param updatedBy
	 */
	public synchronized void informJointsUpdated(Joint updatedBy) {

		synchronized (analysisLock) {

			for (TouchAnalysis touchAnalyis : runningTouchAnalysis) {
				touchAnalyis.update(updatedBy);
			}
			for (MotionAnalysis motionAnalyis : runningMotionAnalysis) {
				motionAnalyis.update(updatedBy);
			}
		}
	}

	/**
	 * Get current running motion analyses
	 */
	public ArrayList<MotionAnalysis> getRunningMotionAnalysis() {
		return runningMotionAnalysis;
	}

	/**
	 * Get specific motion analysis
	 * 
	 * @param type
	 * @return
	 */
	public MotionAnalysis getMotionAnalysis(JointType type) {
		for (MotionAnalysis touch : runningMotionAnalysis) {
			if (touch.getObservedJoint().getType() == type) {
				return touch;
			}
		}
		return null;
	}

	/**
	 * add new joint for motion analysis
	 * 
	 * @param jointType
	 * @return true if already existing analysises were removed means updating
	 *         visual is necessary
	 * @throws Exception
	 */
	public boolean addSaveMotionJoint(JointType jointType) throws Exception {
		boolean haveRemovedOldOnes = false;
		MotionAnalysis current = getMotionAnalysis(jointType);
		if (current != null) {
			current.clear();
			haveRemovedOldOnes = true;
		} else {
			Joint newObservedJoint = getJoint(jointType);

			// check if new joint is already covered by existing one
			for (MotionAnalysis motion : runningMotionAnalysis) {
				if (newObservedJoint.hasParent(motion.getObservedJoint())) {
					throw new Exception(
							"Analysis is already covered by parent joint analysis");
				}
			}

			// remove all child analysis of new one --> obsolete
			ArrayList<MotionAnalysis> toRemove = new ArrayList<MotionAnalysis>();
			for (MotionAnalysis motion : runningMotionAnalysis) {
				if (motion.getObservedJoint().hasParent(newObservedJoint)) {
					toRemove.add(motion);
					haveRemovedOldOnes = true;
				}
			}
			// remove obsolete ones if necessary
			for (MotionAnalysis motion : toRemove) {
				runningMotionAnalysis.remove(motion);
			}
			// create new analysis
			MotionAnalysis newAnalysis = new MotionAnalysis(this,
					newObservedJoint);
			synchronized (analysisLock) {
				runningMotionAnalysis.add(newAnalysis);
			}
		}
		return haveRemovedOldOnes;
	}

	/**
	 * Remove motion analyis of one joint
	 * 
	 * @param joint
	 */
	public void removeSaveMotionJoint(JointType joint) {
		MotionAnalysis current = getMotionAnalysis(joint);
		synchronized (analysisLock) {
			runningMotionAnalysis.remove(current);
		}
	}

	/**
	 * Disalble all motion analysis
	 */
	public void disableMotionAnalysis() {
		synchronized (analysisLock) {
			runningMotionAnalysis.clear();
		}
	}

	/**
	 * Get current number of saved motion steps
	 * 
	 * @return
	 */
	public int getNumberOfSavedMotionSteps() {
		int size = 0;
		for (MotionAnalysis m : getRunningMotionAnalysis()) {
			size += m.getSavedMovementFlow().size();
		}

		return size;
	}

	/**
	 * Get current specific touch analysis
	 */
	public TouchAnalysis getTouchAnalysis(JointType type) {
		for (TouchAnalysis touch : runningTouchAnalysis) {
			if (touch.getObservedJoint().getType() == type) {
				return touch;
			}
		}
		return null;
	}

	/**
	 * Add new touch analysis on joint
	 * 
	 * @param saveMovementLineJoint
	 */
	public void addSaveTouchLineJoint(JointType saveMovementLineJoint) {
		TouchAnalysis current = getTouchAnalysis(saveMovementLineJoint);
		if (current != null) {
			current.clear();
		} else {
			TouchAnalysis newAnalysis = new TouchAnalysis(this,
					getJoint(saveMovementLineJoint));
			synchronized (analysisLock) {
				runningTouchAnalysis.add(newAnalysis);
			}
		}
	}

	/**
	 * Remove touch analysis
	 * 
	 * @param saveMovementLineJoint
	 */
	public void removeSaveTouchLineJoint(JointType saveMovementLineJoint) {

		TouchAnalysis current = getTouchAnalysis(saveMovementLineJoint);
		synchronized (analysisLock) {
			runningTouchAnalysis.remove(current);
		}
	}

	/**
	 * Disable all touch analysis
	 */
	public void disableTouchAnalysis() {
		synchronized (analysisLock) {
			runningTouchAnalysis.clear();
		}
	}

	/**
	 * Get all runnuing touch analysis
	 * 
	 * @return
	 */
	public ArrayList<TouchAnalysis> getRunningTouchAnalysis() {
		return runningTouchAnalysis;
	}

	/**
	 * Get max touch lines of all analyses
	 * 
	 * @return
	 */
	public ArrayList<VectorLine> getMaxTouchLines() {
		ArrayList<VectorLine> maxLines = new ArrayList<VectorLine>();
		for (TouchAnalysis touch : runningTouchAnalysis) {
			maxLines.add(touch.getMaxLine());
		}
		return maxLines;
	}

	/**
	 * Get current feeling scale
	 * 
	 * @return
	 */
	public FeelingScale getComfortScale() {
		return feelingScale;
	}

	/**
	 * Get a joint relation
	 * 
	 * @param independent
	 * @param dependent
	 * @return relation
	 */
	public JointRelation getJointRelation(JointType independent,
			JointType dependent) {
		Joint joint = getJoint(independent);
		for (JointRelation r : joint.getRelationsToOtherJoints()) {
			if (r.getDependent().getType().equals(dependent)) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Build string name from joint type
	 * 
	 * @param type
	 * @return
	 */
	public static String jointTypeToName(JointType type) {
		String name = type.toString().replaceAll("_", " ").toLowerCase();
		name = name.substring(0, 1).toUpperCase()
				+ name.substring(1, name.length());
		return name;
	}

	/**
	 * Get joint type from joint name
	 * 
	 * @param name
	 * @return
	 */
	public static JointType nameToJointType(String name) {
		name = name.replace(" ", "_");

		return JointType.valueOf(name.toUpperCase());
	}

	/**
	 * Write default constraints to database This is only used on first start
	 * 
	 * @param db
	 */
	public static void writeDefaultJointConstraints(Database db) {
		Restriction fingerTopRestriction = new Restriction(
				-AngleHelper.radFromDeg(70), AngleHelper.radFromDeg(10), 0, 0,
				0, 0);

		db.setJointConstraint(JointType.LITTLE_TOP, fingerTopRestriction);
		db.setJointConstraint(JointType.RING_TOP, fingerTopRestriction);
		db.setJointConstraint(JointType.MIDDLE_TOP, fingerTopRestriction);
		db.setJointConstraint(JointType.INDEX_TOP, fingerTopRestriction);

		Restriction thumbTopRestriction = new Restriction(
				-AngleHelper.radFromDeg(90), 0, 0, 0, 0, 0);

		db.setJointConstraint(JointType.THUMB_TOP, thumbTopRestriction);

		Restriction fingerMidRestriction = new Restriction(
				-AngleHelper.radFromDeg(110), AngleHelper.radFromDeg(2), 0, 0,
				0, 0);

		db.setJointConstraint(JointType.LITTLE_MID, fingerMidRestriction);
		db.setJointConstraint(JointType.RING_MID, fingerMidRestriction);
		db.setJointConstraint(JointType.MIDDLE_MID, fingerMidRestriction);
		db.setJointConstraint(JointType.INDEX_MID, fingerMidRestriction);

		Restriction thumbMidRestriction = new Restriction(
				-AngleHelper.radFromDeg(85), AngleHelper.radFromDeg(2), 0, 0,
				0, 0);
		db.setJointConstraint(JointType.THUMB_MID, thumbMidRestriction);

		Restriction fingerBottomRestriction = new Restriction(
				-AngleHelper.radFromDeg(90), AngleHelper.radFromDeg(10), -0.2,
				+0.2, 0, 0);

		db.setJointConstraint(JointType.LITTLE_BOTTOM, fingerBottomRestriction);
		db.setJointConstraint(JointType.RING_BOTTOM, fingerBottomRestriction);
		db.setJointConstraint(JointType.MIDDLE_BOTTOM, fingerBottomRestriction);
		db.setJointConstraint(JointType.INDEX_BOTTOM, fingerBottomRestriction);

		Restriction thumbBottomRestriction = new Restriction(
				-AngleHelper.radFromDeg(70), AngleHelper.radFromDeg(35),
				-AngleHelper.radFromDeg(89), AngleHelper.radFromDeg(10),
				-AngleHelper.radFromDeg(70), AngleHelper.radFromDeg(30));
		db.setJointConstraint(JointType.THUMB_BOTTOM, thumbBottomRestriction);

	}

	/**
	 * Write default relation to db, this is only used on first start
	 * 
	 * @param db
	 */
	public static void writeDefaultJointRelations(Database db) {

		JointRelation relation = new JointRelation(new Joint(null,
				JointType.INDEX_TOP, null), new Joint(null,
				JointType.INDEX_MID, null), 2f / 3f);
		db.setJointRelation(relation);
		relation = new JointRelation(
				new Joint(null, JointType.MIDDLE_TOP, null), new Joint(null,
						JointType.MIDDLE_MID, null), 2f / 3f);
		db.setJointRelation(relation);
		relation = new JointRelation(new Joint(null, JointType.RING_TOP, null),
				new Joint(null, JointType.RING_MID, null), 2f / 3f);
		db.setJointRelation(relation);
		relation = new JointRelation(
				new Joint(null, JointType.LITTLE_TOP, null), new Joint(null,
						JointType.LITTLE_MID, null), 2f / 3f);
		db.setJointRelation(relation);
		relation = new JointRelation(
				new Joint(null, JointType.THUMB_TOP, null), new Joint(null,
						JointType.THUMB_MID, null), 2f / 3f);
		db.setJointRelation(relation);
	}

}
