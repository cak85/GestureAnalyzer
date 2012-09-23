package imuanalyzer.filter;

import imuanalyzer.utils.math.Quaternion;

/**
 * Interface for a user class of calculated inertial data The update method will
 * be called on new available data
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public interface IFilterListener {

	/**
	 * Is called after every update of orientation in filter
	 * 
	 * @param quad
	 * @return
	 */
	Quaternion updateOrientation(Quaternion quad);

	/**
	 * Get world orientation
	 * 
	 * @return
	 */
	Quaternion getCurrentWRFilterOrientation();

	/**
	 * Called on accelaration update
	 * 
	 * @param quad
	 */
	void updateAcceleration(Quaternion quad);

	/**
	 * Get initial world orientation of managed object
	 * 
	 * @return initial orientation
	 */
	Quaternion getInitialOrientation();

	/**
	 * Called on movement update
	 * 
	 * @param quad
	 */
	void updateMove(Quaternion quad);

	/**
	 * Get order of orientation if exists an hierarchy
	 * 
	 * @return numeric priority
	 */
	int getPriority();

	/**
	 * Get ID of used sensor
	 * 
	 * @return sensor id as number
	 */
	int getSensorID();
}
