package imuanalyzer.signalprocessing;

import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.math.Quaternion;

/**
 * Interface for all important methods of a joint implementation
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IJoint {
	public Quaternion getWorldOrientation();

	public Quaternion getLocalOrientation();

	public JointType getType();

	public IJoint getParent();

	public void setParent(IJoint parent);

	public Quaternion getWorldPosition();

}
