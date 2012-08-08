package imuanalyzer.signalprocessing;

import imuanalyzer.filter.IFilterListener;
import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.IInfoContent;
import imuanalyzer.utils.math.AngleHelper;

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

	private static final Quaternion SENSOR_OFFSET = new Quaternion(0, 0, 3, 0);

	protected final Lock id_lock = new ReentrantLock();

	protected int sensorID = -1;

	protected JointType type;
	protected Joint parent;
	protected ArrayList<Joint> children = new ArrayList<Joint>();

	protected Quaternion localOrientation = new Quaternion();

	protected Quaternion worldOrientation = new Quaternion();

	protected Quaternion localRestOrientation = new Quaternion();

	protected Quaternion wordRestOrientation;

	protected Quaternion localPosition = new Quaternion();

	Quaternion lastSensorPos;

	protected Restriction restriction;

	protected IOrientationSensors sensors;

	protected Hand hand;

	protected Quaternion lastMeasuredWROrientation = new Quaternion();

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
	public Quaternion updateOrientation(Quaternion measuredWROrientation) {
		// System.out.println("-------------------");
		// System.out.println("updated with:");
		// measuredWROrientation.print(3);
		// measuredWROrientation.printDegree(3);
		return update(measuredWROrientation, false);
	}

	/**
	 * Process new orientation data
	 * 
	 * @param measuredWROrientation
	 * @param updateChildrensWorldOrientation
	 * @return
	 */
	public Quaternion update(Quaternion measuredWROrientation,
			boolean updateChildrensWorldOrientation) {

		if (this.worldOrientation.equals(measuredWROrientation)) {
			return this.worldOrientation;
		}

		// save old/current valid world orientation
		Quaternion oldLocalOrientation = this.localOrientation;

		if (parent != null) {
			// adjust measured orientation with known
			// restrictions
			this.worldOrientation = updateWithRestrictions(
					measuredWROrientation, true);

		} else {
			this.worldOrientation = measuredWROrientation;
		}
		updateLocalOrientation();

		if (updateChildrensWorldOrientation) {
			updateChildrenWorldOrientation();
		}

		// update dependent objects
		if (!oldLocalOrientation.equals(this.worldOrientation)) {
			// update joints in relation to this one
			Quaternion lastChange = this.localOrientation
					.quaternionProduct(oldLocalOrientation.getConjugate());

			double dotProduct = lastChange.dotProdcut(Quaternion.EMPTY);
			if (dotProduct < 0.999999999) // Dotproduct near 1 means equal
				for (JointRelation relation : relationsToOtherJoints) {

					relation.update(lastChange);
				}

			hand.informJointsUpdated(this);
		}

		return this.worldOrientation;
	}

	/**
	 * Check if new measured orientation is influenced by constraints and update
	 * orientation with constraints if necessary
	 * 
	 * @param measuredWROrientation
	 * @param carryOffsetToChild
	 * @return
	 */
	private Quaternion updateWithRestrictions(Quaternion measuredWROrientation,
			boolean carryOffsetToChild) {

		Quaternion parentWR = parent.getWorldOrientation();

		// calculate difference with parent = local orientation
		Quaternion tmpLocalOrientation = parentWR.getConjugate()
				.quaternionProduct(measuredWROrientation);

		Quaternion diff = getConstraintOffset(tmpLocalOrientation);

		if (diff != null) {

			// correct orientation with constraint offset
			tmpLocalOrientation = diff.quaternionProduct(tmpLocalOrientation);

			if (carryOffsetToChild) {
				// give difference offset as carry to parent and get updated
				// parentWR
				parentWR = parent.carryOrientationFromOther(
						diff.getConjugate(), carryOffsetToChild);
			}

			// update world orientation after carry processing in parent
			measuredWROrientation = parentWR
					.quaternionProduct(tmpLocalOrientation);

			return measuredWROrientation;

		} else {
			return measuredWROrientation;
		}
	}

	/**
	 * Calculate violation offset from joint constraints
	 * 
	 * @param newlocalOrientation
	 * @return
	 */
	private Quaternion getConstraintOffset(Quaternion newlocalOrientation) {
		double[] angles = newlocalOrientation.getAnglesRad();

		double roll = angles[0];
		double pitch = angles[1];
		double yaw = angles[2];

		double rollOff = 0, pitchOff = 0, yawOff = 0;

		boolean update = false;

		// restriction constraints are relative to rest position
		double[] anglesRest = localRestOrientation.getAnglesRad();

		double maxRoll = restriction.maxRoll + anglesRest[0];
		double minRoll = restriction.minRoll + anglesRest[0];
		double maxPitch = restriction.maxPitch + anglesRest[1];
		double minPitch = restriction.minPitch + anglesRest[1];
		double maxYaw = restriction.maxYaw + anglesRest[2];
		double minYaw = restriction.minYaw + anglesRest[2];

		if (roll > maxRoll) {
			rollOff = maxRoll - roll;
			update = true;
		} else if (roll < minRoll) {
			rollOff = minRoll - roll;
			update = true;
		}
		if (pitch > maxPitch) {
			pitchOff = maxPitch - pitch;
			update = true;
		} else if (pitch < minPitch) {
			pitchOff = minPitch - pitch;
			update = true;
		}
		if (yaw > maxYaw) {
			yawOff = maxYaw - yaw;
			update = true;
		} else if (yaw < minYaw) {
			yawOff = minYaw - yaw;
			update = true;
		}

		if (update) {
			return new Quaternion(rollOff, pitchOff, yawOff);
		} else {
			return null;
		}
	}

	/**
	 * Takes carry from other joint for updating orientation
	 * 
	 * @param carry
	 * @param carryOffsetToChild
	 */
	public Quaternion carryOrientationFromOther(Quaternion carry,
			boolean carryOffsetToChild) {

		// only handle carry if we do not know it better from out own sensor
		if (isActive()) {
			return worldOrientation;
		}

		worldOrientation = getWorldOrientation();

		// rotate by restiction offset from child
		Quaternion measuredOrientation = worldOrientation
				.quaternionProduct(carry);

		// adjust measured orientation with known restrictions
		if (parent != null) {
			// adjust rotation if it conflicts with restrictions
			this.worldOrientation = updateWithRestrictions(measuredOrientation,
					carryOffsetToChild);

		} else {
			this.worldOrientation = measuredOrientation;
		}

		// update local orientation from new world orientation
		updateLocalOrientation();

		return worldOrientation;
	}

	/**
	 * Check if current joint has an attached sensor
	 * 
	 * @return
	 */
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

	public void updateChildrenWorldOrientation() {
		for (Joint j : children) {
			j.setLocalOrientation(j.localOrientation);
		}
	}

	public Quaternion getWorldOrientation() {
		if (isActive() || parent == null) {
			return worldOrientation;
		} else {
			return parent.getWorldOrientation().quaternionProduct(
					localOrientation);
		}
	}

	public Quaternion getLocalOrientation() {
		if (parent != null) {

			return parent.getWorldOrientation().getConjugate()
					.quaternionProduct(getWorldOrientation());
		} else {
			return worldOrientation;
		}
	}

	/**
	 * Update local orientation from world orientation
	 */
	public void updateLocalOrientation() {
		localOrientation = getLocalOrientation();
	}

	public void setLocalOrientation(Quaternion orientation) {

		if (parent != null) {
			worldOrientation = parent.getWorldOrientation().quaternionProduct(
					orientation);
		} else {
			worldOrientation = orientation;
		}
		localOrientation = orientation;

		updateChildrenWorldOrientation();

		if (isActive()) {
			sensors.removeListner(this);
			// TODO think about
			// first could be removed with some improvements in sensor
			sensors.addListener(this);
		}
	}

	public void setLocalRestOrientation(Quaternion orientation) {
		localRestOrientation = orientation;
		setLocalOrientation(localRestOrientation);
		calcWorldRestOrientation();
	}

	public Quaternion getWorldRestOrientation() {
		return wordRestOrientation;
	}

	protected void calcWorldRestOrientation() {
		Quaternion worldOrientation;
		if (parent != null) {
			worldOrientation = parent.getWorldRestOrientation()
					.quaternionProduct(localRestOrientation);
		} else {
			worldOrientation = localRestOrientation;
		}
		wordRestOrientation = worldOrientation;
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
		return getWorldRestOrientation();
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

	@Override
	public String getInfoValue() {

		Quaternion quat = getLocalOrientation();

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

	@Override
	public Quaternion getCurrentWRFilterOrientation() {
		return worldOrientation;
	}

}
