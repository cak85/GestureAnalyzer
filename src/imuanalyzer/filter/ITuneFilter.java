package imuanalyzer.filter;

/**
 * Interfaces for accessing filter parameters
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public interface ITuneFilter {

	/**
	 * Get number of available parameters
	 * 
	 * @return number of parameters
	 */
	int getNumberOfParameters();

	/**
	 * Get parameter with index
	 * 
	 * @param index
	 * @return parameter value
	 */
	double getParameter(int index);

	/**
	 * Set new value to parameter with given index
	 * 
	 * @param index
	 * @param value
	 */
	void setParameter(int index, double value);

	/**
	 * Get the maximum value of parameter with index
	 * 
	 * @param index
	 * @return
	 */
	double getMaxValueFromParameter(int index);

	/**
	 * Get the minimum value of parameter with index
	 * 
	 * @param index
	 * @return
	 */
	double getMinValueFromParameter(int index);

	/**
	 * Get the parameter name with index
	 * 
	 * @param index
	 * @return parameter name / description
	 */
	String getParameterName(int index);
	
	/**
	 * Get the parameter description by index
	 * 
	 * @param index
	 * @return parameter name / description
	 */
	String getParameterDescription(int index);

}
