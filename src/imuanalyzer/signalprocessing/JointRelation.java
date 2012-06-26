package imuanalyzer.signalprocessing;

import imuanalyzer.filter.Quaternion;

/**
 * Relationship to another joint
 * 
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
			quat = quat.pow(factor);
			other.carryOrientationFromOther(quat, false);
		}
	}

	public Joint getOther() {
		return other;
	}

	public void setOther(Joint other) {
		this.other = other;
	}

	public float getFactor() {
		return factor;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}
}
