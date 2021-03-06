package imuanalyzer.signalprocessing;

import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.math.AngleHelper;
import imuanalyzer.utils.math.Quaternion;

import java.util.ArrayList;
import java.util.EnumMap;

import org.apache.log4j.Logger;

/**
 * lightweight datastructure for saving joint state without overhead of db,
 * locks, sensors, update-routines....
 * 
 */
public class StoredJointState implements IJoint, Comparable<StoredJointState> {

	private static final Logger LOGGER = Logger
			.getLogger(StoredJointState.class.getName());
	
	private static final double MAX_ANGLE_DIFFERENCE = 8 * (Math.PI / 180);
	
	/**
	 * Offset from Joint to finger top position
	 */
	private static final Quaternion FINGER_TOP_OFFSET = new Quaternion(0, 0,
			1.35, 0);

	protected JointType type;

	protected Quaternion localOrientation;
	
	protected Quaternion localPosition = new Quaternion();
	
	protected Quaternion worldPosition = new Quaternion();

	protected ArrayList<StoredJointState> children = new ArrayList<StoredJointState>();

	protected IJoint parent;

	protected Quaternion worldOrientation;

	public StoredJointState(Joint joint) {
		this(joint, null, true);
	}

	public StoredJointState(Joint joint, IJoint parent, boolean addChildren) {
		this.parent = parent;
		localOrientation = new Quaternion(joint.getLocalOrientation());
		localPosition = new Quaternion(joint.getLocalPosition());
		this.type = joint.getType();
		worldOrientation = joint.getWorldOrientation();
		worldPosition = getWorldPosition();
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

	public void setLocalOrientation(Quaternion quad) {
		localOrientation = quad;
	}

	public JointType getType() {
		return type;
	}

	public void addChild(StoredJointState elem) {
		this.children.add(elem);
	}

	public void updateWorldOrientation() {
		if (parent != null) {
			worldOrientation = parent.getWorldOrientation().quaternionProduct(
					localOrientation);
		} else {
			worldOrientation = localOrientation;
		}
		
		worldPosition = getWorldPosition();
		
		for (StoredJointState j : children) {
			j.updateWorldOrientation();
		}
	}
	
	public Quaternion getLocalPosition(){
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

	public Quaternion getFingerTopPosition() {
		Quaternion bonePos = getWorldPosition();
		Quaternion rotation = getWorldOrientation();

		return bonePos.plus(rotation.quaternionProduct(FINGER_TOP_OFFSET)
				.quaternionProduct(rotation.getConjugate()));

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

			if (this.hasAngelDifferenceGreaterThan(obj_j, MAX_ANGLE_DIFFERENCE)) {
				return false;
			} else {
				return true;
			}
		}
	}

	public double[] getMaxAngle() {
		double[] angles = getLocalOrientation().getAnglesRad();
		angles[0] = Math.abs(angles[0]);
		angles[1] = Math.abs(angles[1]);
		angles[2] = Math.abs(angles[2]);

		if (children.size() == 0) {
			return angles;
		} else {
			double angleSum = 0;
			double[] childMaxAngles = { 0, 0, 0 };

			double childAngleSum = 0;
			for (int i = 0; i < children.size(); i++) {
				double[] childAngle = children.get(i).getMaxAngle();
				childAngleSum = Math.abs(childAngle[0])
						+ Math.abs(childAngle[1]) + Math.abs(childAngle[2]);
				if (childAngleSum > angleSum) {
					angleSum = childAngleSum;
					childMaxAngles = childAngle;
				}
			}

			LOGGER.debug("ChildAngleSum - " + type + ": " + childAngleSum
					+ " - " + AngleHelper.degFromRad(childAngleSum));

			angles[0] += Math.abs(childMaxAngles[0]);
			angles[1] += Math.abs(childMaxAngles[1]);
			angles[2] += Math.abs(childMaxAngles[2]);

			return angles;
		}
	}

	public boolean hasAngelDifferenceGreaterThan(StoredJointState other,
			double angleRad) {
		// check if same structure = compareable
		if (type != other.getType()) {
			return true;
		} else if (children.size() != other.children.size()) {
			return true;
		} else { // check own angle difference
			Quaternion diff = worldOrientation.quaternionProduct(other
					.getWorldOrientation().getConjugate());

			double[] angles = diff.getAnglesRad();

			// LOGGER.debug("Diff: " + angles[0] + " " + angles[1] + " "
			// + angles[2]);

			if (Math.abs(angles[0]) > angleRad
					|| Math.abs(angles[1]) > angleRad
					|| Math.abs(angles[2]) > angleRad) {
				return true;
			} else { // check children
				if (children.size() != 0) {
					for (int i = 0; i < children.size(); i++) {
						if (children.get(i).hasAngelDifferenceGreaterThan(
								other.children.get(i), angleRad)) {
							return true;
						}
					}
					return false;
				} else {
					return false;
				}
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
	
	/**
	 * This function return latest child in child subtree
	 * with just one child per parent!!
	 * @return
	 */
	public StoredJointState getLatestChild(){
		if(children.size()>0){
			return children.get(0);
		}else{
			return this;
		}
	}

	public StoredJointState get(JointType type) {
		if (this.type.equals(type)) {
			return this;
		} else {
			StoredJointState res = null;
			for (StoredJointState s : children) {
				res = s.get(type);
				if (res != null) {
					break;
				}
			}
			return res;
		}
	}

	@Override
	public int compareTo(StoredJointState other) {

		double[] anglesMe = this.getLocalOrientation()
				.getAnglesRad();

		double[] anglesOther = other.getLocalOrientation()
				.getAnglesRad();

		//we compare just x axis angles!!!
		//otherwise it would be possibel to compare the sums of all angles
		if (anglesMe[0] > anglesOther[0]) {
			return 1;
		} else if (anglesMe[0] < anglesOther[0]) {
			return -1;
		} else {
			if (children.size() != 0) {				
				for (int i = 0; i < children.size(); i++) {
					int comp = children.get(i).compareTo(other.children.get(i));
					if (comp != 0) {
						return comp;
					}
				}
			}
		}

		return 0;
	}
}
