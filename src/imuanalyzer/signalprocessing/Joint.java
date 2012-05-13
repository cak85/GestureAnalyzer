package imuanalyzer.signalprocessing;

import imuanalyzer.filter.IFilterListener;
import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Joint implements IFilterListener, IJoint {

	private static final Logger LOGGER = Logger
			.getLogger(Joint.class.getName());

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

	protected boolean visible = true;

	public Joint(Hand hand, JointType f, IOrientationSensors sensors,
			Restriction restriction) {
		this.hand = hand;
		this.type = f;
		this.sensors = sensors;
		this.restriction = restriction;
	}

	public Joint(Hand hand, JointType f, IOrientationSensors sensors) {
		this(hand, f, sensors, new Restriction());
	}

	@Override
	public Quaternion update(Quaternion measuredOrientation) {

		Quaternion oldOrientation = this.localOrientation;

		if (parent != null) { // adjust measured orientation with know
								// restrictions

			this.localOrientation = updateWithRestrictions(measuredOrientation);

		} else {
			this.localOrientation = measuredOrientation;
		}

		if (!oldOrientation.equals(this.localOrientation)) {
			hand.informJointsUpdated(this);
		}

		return this.localOrientation;

	}

	public void carryOrientationFromChild(Quaternion carry) {

		// only handle carry if we do not know it better from out own sensor
		if (isActive()) {
			return;
		}

		// rotate by restiction offset from child
		Quaternion measuredOrientation = carry
				.quaternionProduct(localOrientation);

		if (parent != null) { // adjust measured orientation with know
			// restrictions
			// adjust rotation if it conflicts with restrictions
			this.localOrientation = updateWithRestrictions(measuredOrientation);

		} else {
			this.localOrientation = measuredOrientation;
		}
	}

	private Quaternion getRestrictionOffset(Quaternion rotDiff) {
		double[] angles = rotDiff.getAnglesRadFromQuaternion();

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

	private Quaternion updateWithRestrictions(Quaternion measuredOrientation) {

		// System.out.println(this.type);

		Quaternion parentWR = parent.getWorldOrientation();

		// transfer new measured orientation in world coordinates calculate
		// difference with parent
		Quaternion rotDiff = measuredOrientation.quaternionProduct(parentWR)
				.quaternionProduct(parentWR.getConjugate());

		Quaternion diff = getRestrictionOffset(rotDiff);

		if (diff != null) {

			measuredOrientation = diff.quaternionProduct(measuredOrientation);

			parent.carryOrientationFromChild(diff.getConjugate());

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
			sensors.removeListner(this.sensorID, this);
		}
		this.sensorID = sensorID;
		if (isActive()) {
			sensors.addListener(this.sensorID, this);
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

	public void setInitialOrientation(Quaternion orientation) {
		localOrientation = orientation;
		if (isActive()) {
			sensors.removeListner(sensorID, this);
			// first could be removed with some improvements in sensor
			sensors.addListener(sensorID, this);
		}
	}

	public void setInitialPosition(Quaternion pos) {
		localPosition = pos;

		// TODO update for position
		// if (isActive()) {
		// // first could be removed with some improvements in sensor
		// sensors.setInitialOrientation(sensorID, currentOrientation);
		// sensors.init(sensorID, this);
		// }
	}

	public Quaternion getWorldPosition() {
		// TODO wrong calculation
		if (parent != null) {
			Quaternion parentRotation = parent.getWorldOrientation();

			Quaternion localRotatedPosition = parentRotation
					.quaternionProduct(localPosition);
			// parentRotation.getConjugate().quaternionProduct(localPosition)
			// .quaternionProduct(parentRotation);

			return localRotatedPosition.plus(parent.getWorldPosition());
		} else {
			return localPosition;
		}
	}

	public Quaternion getWorldTranslation() {
		if (parent != null) {
			return parent.getWorldTranslation().plus(localPosition);
		} else {
			return localPosition;
		}

	}

	public void setRestrictionsRoll(Restriction restriction) {
		this.restriction = restriction;
	}

	@Override
	public Quaternion getInitialOrientation() {
		return localOrientation;
	}

	public String getName() {
		return type.toString();
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

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
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

}
