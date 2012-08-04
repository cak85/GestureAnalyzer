package imuanalyzer.signalprocessing;

public interface IAnalysisExtension {
	void update(Hand hand, int handIdx);
	
	void finished();
}