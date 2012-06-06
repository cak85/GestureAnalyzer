package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;

import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

public class ComfortScale implements IRecordDataNotify {
	
	private static final Logger LOGGER = Logger.getLogger(ComfortScale.class
			.getName());

	int min;
	int max;
	int currentValue;
	
	Database db;
	
	public ComfortScale(int min,int max,int currentValue){
		this.min=min;
		this.max=max;
		this.currentValue=currentValue;
		
		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(int currentValue) {
		this.currentValue = currentValue;
	}

	@Override
	public synchronized void notifyRecordNewData(Date timestamp) {
		db.writeComfortData(this, timestamp);		
	}
}
