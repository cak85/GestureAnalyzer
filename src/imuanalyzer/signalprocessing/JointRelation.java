package imuanalyzer.signalprocessing;

import imuanalyzer.filter.Quaternion;

public class JointRelation {

	protected Joint other;
	protected float factor;

	public JointRelation(Joint other, float factor) {
		this.other = other;
		this.factor = factor;
	}

	public void update(Quaternion quat) {
		quat = quat.times(factor);
		quat.normalizeLocal();
		other.carryOrientationFromOther(quat, false);
	}
}
