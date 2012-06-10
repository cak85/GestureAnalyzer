package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class ComfortScale implements IRecordDataNotify {

	private static final Logger LOGGER = Logger.getLogger(ComfortScale.class
			.getName());

	int min;
	int max;
	ArrayList<Integer> currentValues;

	Database db;

	public ComfortScale(int min, int max, ArrayList<Integer> currentValues) {
		this.min = min;
		this.max = max;
		this.currentValues = currentValues;

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
	
	public double getPercentValue(int value){
		return (double)(value-min)/(double)(max-min);
	}

	@Override
	public synchronized void notifyRecordNewData(Date timestamp) {
		db.writeComfortData(this, timestamp);
	}

	public ArrayList<Integer> getCurrentValues() {
		return currentValues;
	}

	public void setCurrentValues(ArrayList<Integer> currentValues) {
		this.currentValues = currentValues;
	}
	
	public void addValue(int value){
		currentValues.add(value);
	}
	
	public void addValueInPercent(double value){
		addValue(percentToValue(value));
	}
	
	private int percentToValue(double value){
		return (int)(min+(max-min)*value);
	}
	
	public void setValueInPercent(int i,double value){
		if(currentValues.size()>=i){
			currentValues.add(percentToValue(value));
		}else{
			currentValues.set(i, percentToValue(value));
		}
	}
}
