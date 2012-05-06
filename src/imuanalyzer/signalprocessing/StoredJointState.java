package imuanalyzer.signalprocessing;

import java.util.ArrayList;
import java.util.EnumMap;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;

/**
 * lightweight datastructure for saving joint state without overhead of db,
 * locks, sensors, update-routines....
 * 
 */
public class StoredJointState implements IJoint{
	protected JointType type;

	protected Quaternion localOrientation;

	protected ArrayList<StoredJointState> children = new ArrayList<StoredJointState>();

	protected IJoint parent;

	protected Quaternion worldOrientation;

	public StoredJointState(Joint joint) {
		this(joint, null, true);
	}

	public StoredJointState(Joint joint, IJoint parent,
			boolean addChildren) {
		this.parent = parent;
		localOrientation = new Quaternion(joint.getLocalOrientation());
		this.type = joint.getType();
		worldOrientation = joint.getWorldOrientation();
		if (addChildren) {
			for (Joint j : joint.children) {
				addChild(new StoredJointState(j, this, true));
			}
		}
	}

	public Quaternion getWorldOrientation() {
		return worldOrientation;
	}

	public Quaternion getLocalOrientation() {
		return localOrientation;
	}

	public JointType getType() {
		return type;
	}

	public void addChild(StoredJointState elem) {
		this.children.add(elem);
	}

	protected void updateWorldOrientation() {
		if (parent != null) {
			worldOrientation = parent.getWorldOrientation().quaternionProduct(
					localOrientation);
		} else {
			worldOrientation = localOrientation;
		}
		for (StoredJointState j : children) {
			j.updateWorldOrientation();
		}
	}

	/**
	 * this is an almost equal implementation
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StoredJointState)) {
			return false;
		} else {
			StoredJointState obj_j = (StoredJointState) obj;
			Quaternion difference = getDifferenceAbs(obj_j);

			double dotProduct = Quaternion.EMPTY.dotProdcut(difference);
			
			if (dotProduct > 0.998) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * calculates aggregated difference over this jointstate and all of its
	 * children
	 * 
	 * @param other
	 * @return null if no comparison is possible e.g. different jointtypes ...
	 */
	public Quaternion getDifferenceAbs(StoredJointState other) {
		if (type != other.getType()) {
			return null;
		}

		if (children.size() != other.children.size()) {
			return null;
		} else if (children.size() == 0) {

			return worldOrientation.quaternionProduct(other
					.getWorldOrientation().getConjugate());
		} else {

			Quaternion maxChildDiff = new Quaternion(0, 0, 0, 0);

			// get the biggest difference of all childs
			for (int i = 0; i < children.size(); i++) {

				Quaternion childSum = children.get(i).getDifferenceAbs(
						other.children.get(i));


				if (childSum == null) {
					return null;
				}

				if (childSum.compareTo(maxChildDiff) > 0) {
					maxChildDiff = childSum;
				}

			}
			return maxChildDiff;
		}
	}

	public EnumMap<JointType, StoredJointState> getAll() {
		EnumMap<JointType, StoredJointState> all = new EnumMap<JointType, StoredJointState>(
				JointType.class);
		all.put(this.getType(), this);
		for (StoredJointState j : children) {
			all.putAll(j.getAll());
		}
		return all;
	}

	public IJoint getParent() {
		return parent;
	}

	public void setParent(IJoint parent) {
		this.parent = parent;
		updateWorldOrientation();
	}
}
