package imuanalyzer.signalprocessing;

import imuanalyzer.filter.Quaternion;

/**
 * Relationship to another joint
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class JointRelation {

	protected Joint other;
	protected float factor;

	public JointRelation(Joint other, float factor) {
		this.other = other;
		this.factor = factor;
	}

	public void update(Quaternion quat) {
		if (!other.isActive()) {
			quat = quat.times(factor);
			quat.normalizeLocal();
			other.carryOrientationFromOther(quat, false);
		}
	}
}
