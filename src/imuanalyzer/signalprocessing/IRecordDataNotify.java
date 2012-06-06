package imuanalyzer.signalprocessing;

import java.util.Date;

public interface IRecordDataNotify {
	void notifyRecordNewData(Date timestamp);
}
