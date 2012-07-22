package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class FeelingScale implements IRecordDataNotify {

	private static final Logger LOGGER = Logger.getLogger(FeelingScale.class
			.getName());

	int min;
	int max;
	ArrayList<Integer> currentValues;

	Database db;

	ArrayList<String> descriptions = new ArrayList<String>();

	public FeelingScale() {
		this("Feeling", -5, 5, 1);
	}

	public FeelingScale(String description, int min, int max, int nrOfValues) {

		// parse multiple data from one field, a bit hacky but ...
		StringTokenizer tokenizer = new StringTokenizer(description, ";");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			descriptions.add(token);
		}

		this.min = min;
		this.max = max;
		this.currentValues = new ArrayList<Integer>();
		for (int i = 0; i < nrOfValues; i++) {
			currentValues.add(0);
		}

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}

	public void setNrOfValues(int count) {
		// adjust item size
		while (currentValues.size() < count) {
			currentValues.add(0);
		}
		while (currentValues.size() > count) {
			currentValues.remove(currentValues.size() - 1);
		}
		db.setFeeling(this);
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
		db.setFeeling(this);
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
		db.setFeeling(this);
	}

	public double getPercentValue(int index) {
		return (double) (currentValues.get(index) - min) / (double) (max - min);
	}

	@Override
	public synchronized void notifyRecordNewData(Date timestamp) {
		db.writeFeelingData(this, timestamp);
	}

	public ArrayList<Integer> getCurrentValues() {
		return currentValues;
	}

	public void setCurrentValues(ArrayList<Integer> currentValues) {
		this.currentValues = currentValues;
	}

	public void addValue(int value) {
		currentValues.add(value);
	}

	public void addValueInPercent(double value) {
		addValue(percentToValue(value));
	}

	private int percentToValue(double value) {
		return (int) (min + (max - min) * value);
	}

	public void setValueInPercent(int i, double value) {
		if (currentValues.size() <= i) {
			currentValues.add(percentToValue(value));
		} else {
			currentValues.set(i, percentToValue(value));
		}
	}

	public void setAllValues(FeelingScale feeling) {
		this.max = feeling.max;
		this.min = feeling.min;
		this.descriptions = feeling.descriptions;
		this.currentValues = feeling.currentValues;
	}

	// create data from multiple data to one field, a bit hacky but ...
	public String getAllDescriptions() {
		StringBuilder all = new StringBuilder();
		for (int i = 0; i < descriptions.size(); i++) {
			all.append(descriptions.get(i));
			all.append(";");
		}

		return all.toString();
	}

	public String getDescription(int id) {
		if (id < descriptions.size()) {
			return descriptions.get(id);
		} else {
			return "";
		}
	}

	public void setDescription(int id, String description) {

		if (id < descriptions.size()) {
			descriptions.set(id, description);
		} else {
			descriptions.add(description);
		}
		LOGGER.debug("new feeling description: " + description);
		db.setFeeling(this);
	}
}
