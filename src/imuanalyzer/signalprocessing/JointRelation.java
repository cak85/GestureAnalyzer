package imuanalyzer.signalprocessing;

import imuanalyzer.utils.math.Quaternion;


/**
 * Relationship from one joint to another joint
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class JointRelation {

	protected Joint dependent;
	protected Joint independent;
	
	/**
	 * Linear factor
	 */
	protected float factor;

	public JointRelation(Joint dependent, Joint independent, float factor) {
		this.dependent = dependent;
		this.independent = independent;
		this.factor = factor;
	}

	/**
	 * Have to be called on every update of independent joint
	 * @param quat orientation of the independent joint
	 */
	public void update(Quaternion quat) {
		if (!dependent.isActive()) {
			quat = quat.pow(factor);
			dependent.carryOrientationFromOther(quat, false);
		}
	}

	/**
	 * Get idenpendent joint
	 * @return
	 */
	public Joint getIndependent() {
		return independent;
	}

	/**
	 * Set independent joint
	 * @param inde
	 */
	public void setIndependent(Joint inde) {
		this.independent = inde;
	}

	/**
	 * get linear factor
	 * @return
	 */
	public float getFactor() {
		return factor;
	}

	/**
	 * set linear factor
	 * @param factor
	 */
	public void setFactor(float factor) {
		this.factor = factor;
	}

	/**
	 * get dependent joint
	 * @return
	 */
	public Joint getDependent() {
		return dependent;
	}

	/**
	 * set dependend joint
	 * @param dependend
	 */
	public void setDependend(Joint dependend) {
		this.dependent = dependend;
	}
}
