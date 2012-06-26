package imuanalyzer.signalprocessing;

import imuanalyzer.filter.Quaternion;

/**
 * Relationship to from one joint to another joint
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class JointRelation {

	protected Joint dependent;
	protected Joint independent;
	protected float factor;

	public JointRelation(Joint dependent, Joint independent, float factor) {
		this.dependent = dependent;
		this.independent = independent;
		this.factor = factor;
	}

	public void update(Quaternion quat) {
		if (!dependent.isActive()) {
			quat = quat.pow(factor);
			dependent.carryOrientationFromOther(quat, false);
		}
	}

	public Joint getIndependent() {
		return independent;
	}

	public void setOther(Joint other) {
		this.independent = other;
	}

	public float getFactor() {
		return factor;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}

	public Joint getDependent() {
		return dependent;
	}

	public void setDependend(Joint dependend) {
		this.dependent = dependend;
	}
}
