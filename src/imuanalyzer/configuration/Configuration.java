package imuanalyzer.configuration;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import imuanalyzer.data.Database;
import imuanalyzer.filter.FilterFactory.FilterTypes;

public class Configuration {

	private static final Logger LOGGER = Logger.getLogger(Configuration.class
			.getName());

	FilterTypes filtertype = FilterTypes.QUATERNION_COMPLEMENTARY;

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

		filtertype = db.getFilterType();

		if (filtertype == null) {
			filtertype = FilterTypes.QUATERNION_COMPLEMENTARY;
		}
	}

	public FilterTypes getFilterType() {
		return filtertype;
	}

	public void setFilterType(FilterTypes type) {
		db.setFilterType(type);
		filtertype = type;
	}

}
