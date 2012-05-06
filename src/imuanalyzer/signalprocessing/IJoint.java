package imuanalyzer.signalprocessing;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;

public interface IJoint {
	public Quaternion getWorldOrientation() ;

	public Quaternion getLocalOrientation() ;

	public JointType getType() ;

	public IJoint getParent();

	public void setParent(IJoint parent);
	
}
