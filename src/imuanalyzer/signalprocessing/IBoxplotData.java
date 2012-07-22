package imuanalyzer.signalprocessing;

import java.util.ArrayList;

public interface IBoxplotData {
	
	public float getMedian() ;

	public float getMax() ;

	public float getMin() ;

	public float getUpperQuantile();

	public float getLowerQuantile() ;

	public IStatisticsValue getMaxObj() ;

	public IStatisticsValue getMinObj() ;
	
	public IStatisticsValue getAvgObj();
	
	public ArrayList<IStatisticsValue> getOutliners();
	
	public String getDescription();
	
	ArrayList<Float> getSpecialPoints();
}
