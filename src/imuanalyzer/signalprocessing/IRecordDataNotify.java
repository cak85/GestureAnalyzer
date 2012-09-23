package imuanalyzer.signalprocessing;

import java.util.Date;

/**
 * Interface for getting updated by new recorded data
 * @author Christopher-Eyk Hrabia
 *
 */
public interface IRecordDataNotify {
	void notifyRecordNewData(Date timestamp);
}
