package imuanalyzer.filter;

public interface ITuneFilter {

	int getNumberOfParameters();

	double getParameter(int index);

	void setParameter(int index, double value);
	
	double getMaxValueFromParameter(int index);
	
	double getMinValueFromParameter(int index);
	
	String getParameterName(int index);

}
