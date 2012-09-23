package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * Class for holding customizable feeling data It is possible to configure
 * number and range of scales data will be safed in database
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class FeelingScale implements IRecordDataNotify {

	private static final Logger LOGGER = Logger.getLogger(FeelingScale.class
			.getName());

	int min;
	int max;
	ArrayList<Integer> currentValues;

	Database db;

	ArrayList<String> descriptions = new ArrayList<String>();

	/**
	 * Default construction with one scale
	 */
	public FeelingScale() {
		this("Feeling", -5, 5, 1);
	}

	/**
	 * Constructor
	 * 
	 * @param description
	 *            description of available scales. Description must be separated
	 *            by ;
	 * @param min
	 *            minimum value of scale
	 * @param max
	 *            maximum value of scale
	 * @param nrOfValues
	 *            of scales/axis
	 */
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

	/**
	 * Change number of available scales
	 * 
	 * @param count
	 *            new number
	 */
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

	/**
	 * Get current minimum scale value
	 * 
	 * @return
	 */
	public int getMin() {
		return min;
	}

	/**
	 * set current minimum scale value
	 * 
	 * @param min
	 */
	public void setMin(int min) {
		this.min = min;
		db.setFeeling(this);
	}

	/**
	 * get current maximum scale value
	 * 
	 * @return
	 */
	public int getMax() {
		return max;
	}

	/**
	 * set current maximum scale value
	 * 
	 * @param max
	 */
	public void setMax(int max) {
		this.max = max;
		db.setFeeling(this);
	}

	/**
	 * Get values in percent
	 * 
	 * @param index
	 * @return
	 */
	public double getPercentValue(int index) {
		return (double) (currentValues.get(index) - min) / (double) (max - min);
	}

	/**
	 * Get notification about new data
	 */
	@Override
	public synchronized void notifyRecordNewData(Date timestamp) {
		db.writeFeelingData(this, timestamp);
	}

	/**
	 * get current values
	 * 
	 * @return
	 */
	public ArrayList<Integer> getCurrentValues() {
		return currentValues;
	}

	/**
	 * Set current values
	 * 
	 * @param currentValues
	 */
	public void setCurrentValues(ArrayList<Integer> currentValues) {
		this.currentValues = currentValues;
	}

	/**
	 * Add new value/scale/axis
	 * 
	 * @param value
	 */
	public void addValue(int value) {
		currentValues.add(value);
	}

	/**
	 * Add new value/scale/axis in percent
	 * 
	 * @param value
	 */
	public void addValueInPercent(double value) {
		addValue(percentToValue(value));
	}

	/**
	 * Convert current value into percent based on scale range
	 * 
	 * @param value
	 * @return value in percent
	 */
	private int percentToValue(double value) {
		return (int) (min + (max - min) * value);
	}

	/**
	 * Set one value in percent
	 * 
	 * @param i
	 * @param value
	 */
	public void setValueInPercent(int i, double value) {
		if (currentValues.size() <= i) {
			currentValues.add(percentToValue(value));
		} else {
			currentValues.set(i, percentToValue(value));
		}
	}

	/**
	 * Change/set all values, ranges and descriptions
	 * 
	 * @param feeling
	 */
	public void setAllValues(FeelingScale feeling) {
		this.max = feeling.max;
		this.min = feeling.min;
		this.descriptions = feeling.descriptions;
		this.currentValues = feeling.currentValues;
	}

	/**
	 * create data from multiple data to one field, a bit hacky but works...
	 */
	public String getAllDescriptions() {
		StringBuilder all = new StringBuilder();
		for (int i = 0; i < descriptions.size(); i++) {
			all.append(descriptions.get(i));
			all.append(";");
		}

		return all.toString();
	}

	/**
	 * Get description of scale with index
	 * 
	 * @param id
	 *            btw. index
	 * @return
	 */
	public String getDescription(int id) {
		if (id < descriptions.size()) {
			return descriptions.get(id);
		} else {
			return "";
		}
	}

	/**
	 * Change description of one scale
	 * 
	 * @param id
	 * @param description
	 */
	public void setDescription(int id, String description) {

		if (id < descriptions.size()) {
			descriptions.set(id, description);
		} else {
			descriptions.add(description);
		}
		// LOGGER.debug("new feeling description: " + description);
		db.setFeeling(this);
	}
}
