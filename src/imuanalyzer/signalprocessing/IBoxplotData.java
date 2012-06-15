package imuanalyzer.signalprocessing;

import java.util.ArrayList;

public interface IBoxplotData {
	public float getMedian() ;

	public float getMax() ;

	public float getMin() ;

	public float getUpperQuantile();

	public float getLowerQuantile() ;

	public Object getMaxObj() ;

	public Object getMinObj() ;
	
	public ArrayList<Object> getOutliners();
	
	public String getDescription();
}
