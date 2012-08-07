package imuanalyzer.filter;

public interface IFilterListener {
	Quaternion updateOrientation(Quaternion quad);
	
	Quaternion getCurrentWRFilterOrientation();
	
	void updateAcceleration(Quaternion quad);
	
	Quaternion getInitialOrientation();
	
	void updateMove(Quaternion quad);
	
	int getPriority();
	
	int getSensorID();
}
