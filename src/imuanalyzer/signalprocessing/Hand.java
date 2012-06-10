package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.filter.Quaternion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

public class Hand {

	/**
	 * Representation of the finger bones: First letter D = Daumen Z =
	 * Zeigefinger M = Mittelfinger R = Ringfinger K = Kleiner Finger H = Hand
	 * Second letter T = Top M = Middle D = Down R = Root
	 */
	public enum JointType { // order is important!!
		HR, DD, ZD, MD, RD, KD, DM, ZM, MM, RM, KM, DT, ZT, MT, RT, KT
	};

	protected IOrientationSensors sensors;

	private EnumMap<JointType, Joint> joints = new EnumMap<JointType, Joint>(
			JointType.class);

	protected Marker currentMarker;

	private static final Logger LOGGER = Logger.getLogger(Hand.class.getName());

	protected Database db;

	protected ArrayList<TouchAnalysis> runningTouchAnalysis = new ArrayList<TouchAnalysis>();

	protected ArrayList<MotionAnalysis> runningMotionAnalysis = new ArrayList<MotionAnalysis>();

	/**
	 * save subjective feelings about gesture
	 */
	protected volatile FeelingScale feelingScale;

	public Hand(IOrientationSensors sensors, Marker marker) {
		this.sensors = sensors;
		this.currentMarker = marker;
		
		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		
		ArrayList<Integer> values = new ArrayList<Integer>();
		values.add(0);
		feelingScale = new FeelingScale(-5, +5, values);

		if (sensors != null) {
			// register for record notification
			sensors.setRecordDataNotifyListener(feelingScale);
		}
		
		Restriction fingerTopRestriction = new Restriction(-1, 0.05, 0, 0, 0, 0);

		Joint elemKT = new Joint(this, JointType.KT, sensors,
				fingerTopRestriction);
		Joint elemRT = new Joint(this, JointType.RT, sensors,
				fingerTopRestriction);
		Joint elemMT = new Joint(this, JointType.MT, sensors,
				fingerTopRestriction);
		Joint elemZT = new Joint(this, JointType.ZT, sensors,
				fingerTopRestriction);
		Joint elemDT = new Joint(this, JointType.DT, sensors,
				fingerTopRestriction);

		Restriction fingerMidRestriction = new Restriction(-1.8, 0.05, 0, 0, 0,
				0);

		Joint elemKM = new Joint(this, JointType.KM, sensors,
				fingerMidRestriction);
		Joint elemRM = new Joint(this, JointType.RM, sensors,
				fingerMidRestriction);
		Joint elemMM = new Joint(this, JointType.MM, sensors,
				fingerMidRestriction);
		Joint elemZM = new Joint(this, JointType.ZM, sensors,
				fingerMidRestriction);
		Joint elemDM = new Joint(this, JointType.DM, sensors,
				fingerMidRestriction);

		Restriction fingerBottomRestriction = new Restriction(-1.5, 0.05, -0.2,
				+0.2, 0, 0);

		Joint elemKD = new Joint(this, JointType.KD, sensors,
				fingerBottomRestriction);
		Joint elemRD = new Joint(this, JointType.RD, sensors,
				fingerBottomRestriction);
		Joint elemMD = new Joint(this, JointType.MD, sensors,
				fingerBottomRestriction);
		Joint elemZD = new Joint(this, JointType.ZD, sensors,
				fingerBottomRestriction);
		Joint elemDD = new Joint(this, JointType.DD, sensors);

		Joint elemHR = new Joint(this, JointType.HR, sensors);
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
	}

	/**
	 * refresh current sensor mapping from marker
	 */
	public void loadJointMappingFromMarker() {
		if (sensors != null) {
			for (JointType j : JointType.values()) {
				int id = db.getJointSensorMapping(currentMarker, j);
				setSensorID(j, id);
			}
		}
	}

	public Set<Entry<JointType, Joint>> getJointSet() {
		return joints.entrySet();
	}

	private void addAllElementsToMap(Joint elem) {
		joints.put(elem.type, elem);
		for (Joint e : elem.children) {
			addAllElementsToMap(e);
		}
	}

	public void setInitialOrientation(JointType finger, Quaternion quad) {
		Joint joint = ((Joint) joints.get(finger));
		joint.setLocalOrientation(quad);

	}

	public void setSensorID(JointType type, int id) {
		Joint joint = ((Joint) joints.get(type));
		joint.setSensorID(id);
	}

	public void saveJointSensorMapping(JointType type) {
		Joint joint = ((Joint) joints.get(type));
		db.setJointSensorMapping(currentMarker, type, joint.getSensorID());
	}

	public Quaternion getLocalJointOrientation(JointType type) {
		Joint joint = ((Joint) joints.get(type));
		return joint.getLocalOrientation();
	}

	public Quaternion setLocalJointOrientation(JointType type, Quaternion quad) {
		Joint joint = ((Joint) joints.get(type));
		return joint.update(quad, false);
	}

	public Joint getJoint(JointType type) {
		return ((Joint) joints.get(type));
	}

	public Marker getCurrentMarker() {
		return currentMarker;
	}

	public void setCurrentMarker(Marker currentMarker) {
		this.currentMarker = currentMarker;
	}

	public synchronized void informJointsUpdated(Joint updatedBy) {

		for (TouchAnalysis touchAnalyis : runningTouchAnalysis) {
			touchAnalyis.update(updatedBy);
		}
		for (MotionAnalysis motionAnalyis : runningMotionAnalysis) {
			motionAnalyis.update(updatedBy);
		}

	}

	// Motion analyses

	public ArrayList<MotionAnalysis> getRunningMotionAnalysis() {
		return runningMotionAnalysis;
	}

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
			for (MotionAnalysis touch : runningMotionAnalysis) {
				if (newObservedJoint.hasParent(touch.getObservedJoint())) {
					throw new Exception(
							"Analysis is already covered by parent joint analysis");
				}
			}

			// remove all child analysis of new one --> obsolete
			ArrayList<MotionAnalysis> toRemove = new ArrayList<MotionAnalysis>();
			for (MotionAnalysis touch : runningMotionAnalysis) {
				if (touch.getObservedJoint().hasParent(newObservedJoint)) {
					toRemove.add(touch);
					haveRemovedOldOnes = true;
				}
			}
			// remove obsolete ones if necessary
			for (MotionAnalysis touch : toRemove) {
				runningMotionAnalysis.remove(touch);
			}
			// create new analysis
			MotionAnalysis newAnalysis = new MotionAnalysis(this,
					newObservedJoint);
			runningMotionAnalysis.add(newAnalysis);
		}
		return haveRemovedOldOnes;
	}

	public void removeSaveMotionJoint(JointType joint) {
		MotionAnalysis current = getMotionAnalysis(joint);
		runningMotionAnalysis.remove(current);
	}

	public void disableMotionAnalysis() {
		runningMotionAnalysis.clear();
	}

	public int getNumberOfSavedMotionSteps() {
		int size = 0;
		for (MotionAnalysis m : getRunningMotionAnalysis()) {
			size += m.getSavedMovementFlow().size();
		}

		return size;
	}

	// touch analysis

	public TouchAnalysis getTouchAnalysis(JointType type) {
		for (TouchAnalysis touch : runningTouchAnalysis) {
			if (touch.getObservedJoint().getType() == type) {
				return touch;
			}
		}
		return null;
	}

	public void addSaveTouchLineJoint(JointType saveMovementLineJoint) {
		TouchAnalysis current = getTouchAnalysis(saveMovementLineJoint);
		if (current != null) {
			current.clear();
		} else {
			TouchAnalysis newAnalysis = new TouchAnalysis(this,
					getJoint(saveMovementLineJoint));
			runningTouchAnalysis.add(newAnalysis);
		}
	}

	public void removeSaveTouchLineJoint(JointType saveMovementLineJoint) {
		TouchAnalysis current = getTouchAnalysis(saveMovementLineJoint);
		runningTouchAnalysis.remove(current);
	}

	public void disableTouchAnalysis() {
		runningTouchAnalysis.clear();
	}

	public ArrayList<TouchAnalysis> getRunningTouchAnalysis() {
		return runningTouchAnalysis;
	}

	public ArrayList<VectorLine> getMaxTouchLines() {
		ArrayList<VectorLine> maxLines = new ArrayList<VectorLine>();
		for (TouchAnalysis touch : runningTouchAnalysis) {
			maxLines.add(touch.getMaxLine());
		}
		return maxLines;
	}

	public FeelingScale getComfortScale() {
		return feelingScale;
	}

}
