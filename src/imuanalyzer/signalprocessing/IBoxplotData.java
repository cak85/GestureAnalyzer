package imuanalyzer.signalprocessing;

public interface IBoxplotData {
	public float getMedian() ;

	public float getMax() ;

	public float getMin() ;

	public float getUpperQuantile();

	public float getLowerQuantile() ;

	public Object getMaxObj() ;

	public Object getMinObj() ;
}
