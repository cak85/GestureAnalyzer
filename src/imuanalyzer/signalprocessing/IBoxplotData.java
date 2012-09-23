package imuanalyzer.signalprocessing;

import java.util.ArrayList;

/**
 * Interface for providing all necessary data to paint a boxplot
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IBoxplotData {

	public float getMedian();

	public float getMax();

	public float getMin();

	public float getUpperQuantile();

	public float getLowerQuantile();

	public IStatisticsValue getMaxObj();

	public IStatisticsValue getMinObj();

	public IStatisticsValue getAvgObj();

	public ArrayList<IStatisticsValue> getOutliersUpper();

	public ArrayList<IStatisticsValue> getOutliersLower();

	public ArrayList<IStatisticsValue> getOutliers();

	/**
	 * Get dataset description
	 * @return
	 */
	public String getDescription();

	/**
	 * Get point which will be extra highligted besides the general ones
	 * @return
	 */
	ArrayList<Float> getSpecialPoints();
}
