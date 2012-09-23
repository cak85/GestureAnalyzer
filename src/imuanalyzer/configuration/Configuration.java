package imuanalyzer.configuration;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import imuanalyzer.data.Database;
import imuanalyzer.filter.FilterFactory.FilterTypes;

/**
 * Wrapper class for holding some configuration implemented as singleton
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class Configuration {

	private static final Logger LOGGER = Logger.getLogger(Configuration.class
			.getName());

	FilterTypes filtertype = FilterTypes.CF_QUATERNION;

	Database db;

	protected static Configuration instance;

	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return instance;
	}

	protected Configuration() {
		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e.toString());
		}

		try {
			filtertype = db.getFilterType();
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							null,
							"Could not start application, database is locked by another instance",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		if (filtertype == null) {
			filtertype = FilterTypes.CF_QUATERNION;
		}
	}

	/**
	 * Get current used filter type
	 * 
	 * @return
	 */
	public FilterTypes getFilterType() {
		return filtertype;
	}

	/**
	 * Set current used filter type
	 * 
	 * @param type
	 */
	public void setFilterType(FilterTypes type) {
		db.setFilterType(type);
		filtertype = type;
	}

}
