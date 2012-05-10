package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.filter.Quaternion;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.LinkedList;
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

	IOrientationSensors sensors;

	private EnumMap<JointType, Joint> joints = new EnumMap<JointType, Joint>(
			JointType.class);

	Marker currentMarker;

	private static final Logger LOGGER = Logger.getLogger(Hand.class.getName());

	Database db;

	protected LinkedList<MovementStep> savedMovementFlow = new LinkedList<MovementStep>();

	protected JointType saveMovementStartJoint = JointType.RD;

	protected Boolean saveMovement = false;
	protected double movementMinDifference = 0.12;

	public Hand(IOrientationSensors sensors, Marker marker) {
		this.sensors = sensors;
		this.currentMarker = marker;

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
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

		for (JointType j : JointType.values()) {
			int id = db.getJointSensorMapping(currentMarker, j);
			setSensorID(j, id);
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
		joint.setInitialOrientation(quad);

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
		return joint.update(quad);
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

	public Boolean isSaveMovement() {
		return saveMovement;
	}

	public void setSaveMovement(Boolean saveMovement) {
		this.saveMovement = saveMovement;
		if (saveMovement == false) {
			savedMovementFlow.clear();
		} else if (savedMovementFlow.size() == 0) {
			addInitialSavedMove();
		}
	}

	public JointType getSavedMovementStartJoint() {
		return saveMovementStartJoint;
	}

	public void setSavedMovementStartJoint(JointType savedMovementStartJoint) {
		if (this.saveMovementStartJoint != savedMovementStartJoint) {
			this.saveMovementStartJoint = savedMovementStartJoint;
			savedMovementFlow.clear();
			addInitialSavedMove();
		}
	}

	/**
	 * Add first saved movement
	 */
	private void addInitialSavedMove() {
		Joint startMovementJoint = joints.get(saveMovementStartJoint);
		StoredJointState newState = new StoredJointState(startMovementJoint,
				startMovementJoint.parent, true);
		savedMovementFlow.addLast(new MovementStep(newState));
	}

	public synchronized void informJointsUpdated(Joint updatedBy) {

		if (saveMovement) {

			Joint startMovementJoint = joints.get(saveMovementStartJoint);

			for (MovementStep s : savedMovementFlow) {
				s.getMove().updateWorldOrientation();
			}

			// check if updateBy is parent of currently observed joint
			// if yes its not neccessary to update
			if (startMovementJoint.hasParent(updatedBy)) {
				return;
			}

			StoredJointState newState = new StoredJointState(
					startMovementJoint, startMovementJoint.parent, true);

			if (savedMovementFlow.size() > 0) {
				StoredJointState lastState = savedMovementFlow.getLast()
						.getMove();
				Quaternion diff = lastState.getDifferenceAbs(newState);

				if (diff == null) {
					return;
				}

				double[] angles = diff.getAnglesRadFromQuaternion();

				// if difference to low return
				if ((Math.abs(angles[0]) + Math.abs(angles[1]) + Math
						.abs(angles[2])) < movementMinDifference) {
					return;
				}
			}

			// check if an almost same position is already saved and its not
			// the last one (not increasing counter if we move slightly on
			// position)
			// if yes increase counter
			for (int i = 0; i < savedMovementFlow.size() - 1; i++) {
				MovementStep m = savedMovementFlow.get(i);
				if (m.getMove().equals(newState)) {
					m.incCount();
					// LOGGER.debug("Find existing position - Increase count");
					return;
				}
			}

			savedMovementFlow.addLast(new MovementStep(newState));
		}

	}

	public LinkedList<MovementStep> getSavedMovementFlow() {
		return savedMovementFlow;
	}

}
