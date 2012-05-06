package imuanalyzer.filter;

public interface IFilterListener {
	Quaternion update(Quaternion quad);
	
	Quaternion getInitialOrientation();
}
