package imuanalyzer.signalprocessing;

import imuanalyzer.filter.IFilterListener;
import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.IInfoContent;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Joint implements IFilterListener, IJoint, IInfoContent {

	private static final Logger LOGGER = Logger
			.getLogger(Joint.class.getName());

	private static final Quaternion FINGER_TIP_OFFSET = new Quaternion(0, 0,
			0.7, 0);

	protected final Lock id_lock = new ReentrantLock();

	protected int sensorID = -1;

	protected JointType type;
	protected Joint parent;
	protected ArrayList<Joint> children = new ArrayList<Joint>();

	protected Quaternion localOrientation = new Quaternion();

	protected Quaternion localPosition = new Quaternion();

	protected Restriction restriction;

	protected IOrientationSensors sensors;

	protected Hand hand;

	protected Quaternion lastActiveChange = new Quaternion();

	protected Quaternion lastMeasuredOrientation = new Quaternion();

	protected String name;

	protected ArrayList<JointRelation> relationsToOtherJoints = new ArrayList<JointRelation>();

	public Joint(Hand hand, JointType f, IOrientationSensors sensors,
			Restriction restriction) {
		this.hand = hand;
		this.type = f;
		this.sensors = sensors;
		if (restriction == null) {
			this.restriction = new Restriction();
		} else {
			this.restriction = restriction;
		}
		name = Hand.jointTypeToName(type);
	}

	public Joint(Hand hand, JointType f, IOrientationSensors sensors) {
		this(hand, f, sensors, new Restriction());
	}

	@Override
	public Quaternion updateOrientation(Quaternion measuredOrientation) {

		return update(measuredOrientation, true);
	}

	public Quaternion update(Quaternion measuredOrientation,
			boolean storeLastMovement) {
		if (lastMeasuredOrientation.equals(measuredOrientation)) {
			lastActiveChange.set(1, 0, 0, 0);
			return localOrientation;
		} else {
			lastMeasuredOrientation = measuredOrientation;
		}

		Quaternion oldOrientation = this.localOrientation;

		if (parent != null) { // adjust measured orientation with known
								// restrictions

			// substract change of parent/reference for getting the local frame
			measuredOrientation = measuredOrientation.quaternionProduct(parent
					.getLastActiveChange().getConjugate());

			this.localOrientation = updateWithRestrictions(measuredOrientation,
					true);

		} else {
			this.localOrientation = measuredOrientation;
		}

		if (!oldOrientation.equals(this.localOrientation)) {
			// store last orientation change,
			if (storeLastMovement) {
				lastActiveChange = localOrientation
						.quaternionProduct(oldOrientation.getConjugate());

				// update joints in relation to this one
				for (JointRelation relation : relationsToOtherJoints) {
					relation.update(lastActiveChange);
				}
			}
			hand.informJointsUpdated(this);
		} else {
			lastActiveChange.set(1, 0, 0, 0);
		}
		return this.localOrientation;
	}

	public void carryOrientationFromOther(Quaternion carry,
			boolean carryOffsetToChild) {

		// only handle carry if we do not know it better from out own sensor
		if (isActive()) {
			return;
		}

		// rotate by restiction offset from child
		Quaternion measuredOrientation = carry
				.quaternionProduct(localOrientation);

		if (parent != null) { // adjust measured orientation with known
			// restrictions
			// adjust rotation if it conflicts with restrictions
			this.localOrientation = updateWithRestrictions(measuredOrientation,
					carryOffsetToChild);

		} else {
			this.localOrientation = measuredOrientation;
		}
	}

	private Quaternion getRestrictionOffset(Quaternion rotDiff) {
		double[] angles = rotDiff.getAnglesRad();

		double roll = angles[0];
		double pitch = angles[1];
		double yaw = angles[2];

		double rollOff = 0, pitchOff = 0, yawOff = 0;

		boolean update = false;

		// System.out.printf("R[%.3f;%.3f] P[%.3f;%.3f] Y[%.3f;%.3f]\n",
		// restriction.minRoll, restriction.maxRoll, restriction.minPitch,
		// restriction.maxPitch, restriction.minYaw, restriction.maxYaw);
		//
		// System.out.printf("Diff R%.3f: P%.3f: Y%.3f\n", roll, pitch, yaw);

		if (roll > restriction.maxRoll) {
			rollOff = restriction.maxRoll - roll;
			update = true;
		} else if (roll < restriction.minRoll) {
			rollOff = restriction.minRoll - roll;
			update = true;
		}
		if (pitch > restriction.maxPitch) {
			pitchOff = restriction.maxPitch - pitch;
			update = true;
		} else if (pitch < restriction.minPitch) {
			pitchOff = restriction.minPitch - pitch;
			update = true;
		}
		if (yaw > restriction.maxYaw) {
			yawOff = restriction.maxYaw - yaw;
			update = true;
		} else if (yaw < restriction.minYaw) {
			yawOff = restriction.minYaw - yaw;
			update = true;
		}

		if (update) {
			// System.out.printf("Diff Offset R%.8f: P%.8f: Y%.8f\n", rollOff,
			// pitchOff, yawOff);

			return new Quaternion(rollOff, pitchOff, yawOff);
		} else {
			return null;
		}
	}

	private Quaternion updateWithRestrictions(Quaternion measuredOrientation,
			boolean carryOffsetToChild) {

		// System.out.println(this.type);

		Quaternion parentWR = parent.getWorldOrientation();

		// transfer new measured orientation in world coordinates calculate
		// difference with parent
		Quaternion rotDiff = measuredOrientation.quaternionProduct(parentWR)
				.quaternionProduct(parentWR.getConjugate());

		Quaternion diff = getRestrictionOffset(rotDiff);

		if (diff != null && carryOffsetToChild) {

			measuredOrientation = diff.quaternionProduct(measuredOrientation);

			parent.carryOrientationFromOther(diff.getConjugate(),
					carryOffsetToChild);

			return measuredOrientation;

		} else {
			return measuredOrientation;
		}
	}

	public boolean isActive() {
		if (id_lock.tryLock()) {
			boolean ret = sensorID > -1;
			id_lock.unlock();
			return ret;
		} else {
			return false;
		}
	}

	public void addChild(Joint elem) {
		elem.parent = this;
		this.children.add(elem);
	}

	public int getSensorID() {
		return sensorID;
	}

	public void setSensorID(int sensorID) {
		id_lock.lock(); // necessary because currentOrientation could be reseted
						// by update
		if (isActive()) {
			sensors.removeListner(this);
		}
		this.sensorID = sensorID;
		if (isActive()) {
			sensors.addListener(this);
		}
		id_lock.unlock();
	}

	public Quaternion getWorldOrientation() {
		Quaternion worldOrientation;
		if (parent != null) {
			worldOrientation = parent.getWorldOrientation().quaternionProduct(
					localOrientation);
		} else {
			worldOrientation = localOrientation;
		}
		return worldOrientation;
	}

	public Quaternion getLocalOrientation() {

		return localOrientation;
	}

	public void setLocalOrientation(Quaternion orientation) {
		localOrientation = orientation;
		if (isActive()) {
			sensors.removeListner(this);
			// first could be removed with some improvements in sensor
			sensors.addListener(this);
		}
	}

	public void setLocalPosition(Quaternion pos) {
		localPosition = pos;
		lastSensorPos = getSensorPosition();
	}

	public Quaternion getLocalPosition() {
		return localPosition;
	}

	public Quaternion getWorldPosition() {
		if (parent != null) {
			Quaternion rotation = parent.getWorldOrientation();
			return parent.getWorldPosition().plus(
					rotation.quaternionProduct(localPosition)
							.quaternionProduct(rotation.getConjugate()));
		} else {
			return localPosition;
		}
	}

	public Quaternion getFingertipPosition() {
		Quaternion bonePos = getWorldPosition();
		Quaternion rotation = getWorldOrientation();

		return bonePos.plus(rotation.quaternionProduct(FINGER_TIP_OFFSET)
				.quaternionProduct(rotation.getConjugate()));

	}

	public void setRestrictions(Restriction restriction) {
		this.restriction = restriction;
	}

	@Override
	public Quaternion getInitialOrientation() {
		return localOrientation;
	}

	public String getInfoName() {
		return this.toString();
	}

	public Restriction getRestriction() {
		return restriction;
	}

	public void setRestriction(Restriction restriction) {
		this.restriction = restriction;
	}

	public JointType getType() {
		return type;
	}

	@Override
	public IJoint getParent() {
		return parent;
	}

	@Override
	public void setParent(IJoint parent) {
		throw new NotImplementedException();
	}

	/**
	 * Checks if given joint corresponds to one of its parents
	 * 
	 * @param joint
	 * @return
	 */
	public boolean hasParent(Joint joint) {
		if (parent == null) {
			return joint == null;
		} else if (parent.equals(joint)) {
			return true;
		} else {
			return parent.hasParent(joint);
		}
	}

	@Override
	public int getPriority() {
		return this.type.ordinal();
	}

	public Quaternion getLastActiveChange() {
		if (!isActive()) {
			lastActiveChange.set(1, 0, 0, 0);
		}
		if (parent == null) {
			return lastActiveChange;

		} else {
			return lastActiveChange.quaternionProduct(parent
					.getLastActiveChange());
		}
	}

	Quaternion acceleration = new Quaternion();

	public float[] getAcceleration() {
		float[] ret = { 0, 0, 0 };
		if (isActive()) {
			ret[0] = (float) acceleration.getX();
			ret[1] = (float) acceleration.getY();
			ret[2] = (float) acceleration.getZ();
		}
		return ret;
	}

	@Override
	public void updateAcceleration(Quaternion acceleration) {
		this.acceleration = acceleration;
	}

	private static final Quaternion SENSOR_OFFSET = new Quaternion(0, 0, 3, 0);

	Quaternion lastSensorPos;

	private Quaternion getSensorPosition() {
		Quaternion bonePos = getWorldPosition();
		Quaternion rotation = getWorldOrientation();

		return bonePos.plus(rotation.quaternionProduct(SENSOR_OFFSET)
				.quaternionProduct(rotation.getConjugate()));
	}

	@Override
	public void updateMove(Quaternion move) {

		Quaternion pos = getSensorPosition();

		if (lastSensorPos == null) {
			lastSensorPos = pos;
			return;
		}

		Quaternion diff = pos.minus(lastSensorPos);

		lastSensorPos = pos;

		// LOGGER.debug("Pos diff");
		//
		// diff.print(3);
		//
		// LOGGER.debug("Move update");
		// move.print(3);
		//
		// LOGGER.debug("Difference move/pos ");

		Quaternion movePosDiff = diff.minus(move);

		lastMovePosDiff = lastMovePosDiff.plus(movePosDiff);

		// movePosDiff.print(3);

		localPosition.plus(movePosDiff);
	}

	Quaternion lastMovePosDiff = new Quaternion();

	public Quaternion getLastMove() {
		Quaternion wR = getWorldOrientation();

		Quaternion ret = wR.quaternionProduct(lastMovePosDiff)
				.quaternionProduct(wR.getConjugate());
		// Quaternion ret = new Quaternion(lastMovePosDiff);

		lastMovePosDiff.clear();

		return ret;
	}

	public Quaternion getRotationBetweenParent() {
		Joint joint = this;
		IJoint parent = joint.getParent();

		Quaternion quat = null;
		if (parent != null) {
			quat = joint.getWorldOrientation().quaternionProduct(
					parent.getWorldOrientation().getConjugate());
		} else {
			quat = joint.getLocalOrientation();
		}
		return quat;
	}

	@Override
	public String getInfoValue() {

		Quaternion quat = getRotationBetweenParent();

		double[] angles = quat.getAnglesDeg();
		Restriction restriction = getRestriction();

		StringBuffer values = new StringBuffer("");
		if (restriction.isRollAllowed()) {
			values.append("x:");
			values.append(String.format("%.1f", angles[0]));
		}
		if (restriction.isPitchAllowed()) {
			values.append("y:");
			values.append(String.format("%.1f", angles[1]));
		}
		if (restriction.isYawAllowed()) {
			values.append("z:");
			values.append(String.format("%.1f", angles[2]));
		}

		return values.toString();
	}

	@Override
	public String toString() {
		return name;
	}

	public void addRelation(JointRelation relation) {
		if (!relationsToOtherJoints.contains(relation)) {
			relationsToOtherJoints.add(relation);
		}
	}

	public void removeRelation(JointRelation relation) {
		relationsToOtherJoints.remove(relation);
	}

	public ArrayList<JointRelation> getRelationsToOtherJoints() {
		return relationsToOtherJoints;
	}

}
