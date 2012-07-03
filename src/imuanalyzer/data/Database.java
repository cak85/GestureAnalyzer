package imuanalyzer.data;

import imuanalyzer.device.ImuRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.FeelingScale;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.JointRelation;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.SensorVector;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.h2.tools.Csv;

public class Database {

	final static String DATABASE_NAME = "IMUAnalyzerData";

	final static String IMU_DATA_TABLE_NAME = "Data";
	final static String IMU_DATA_TABLE_TIME = "Time";
	final static String IMU_DATA_TABLE_SAMPLEPERIOD = "Sampleperiod";
	final static String IMU_DATA_TABLE_SENSOR_ID = "SensorId";
	final static String IMU_DATA_TABLE_ACCELEROMETER_PRE = "Accel_";
	final static String IMU_DATA_TABLE_GYROSCOPE_PRE = "Gyro_";
	final static String IMU_DATA_TABLE_MAGNETOMETER_PRE = "Magneto_";

	final static String COMFORT_TABLE_NAME = "FeelingData";
	final static String COMFORT_TABLE_TIME = "Time";
	final static String COMFORT_TABLE_VALUE = "Value";
	final static String COMFORT_TABLE_NUMBER = "Number";

	final static String IMU_MARKER_TABLE_NAME = "Marker";
	final static String IMU_MARKER_TABLE_ID = "Id";
	final static String IMU_MARKER_TABLE_START_TIME = "StartTime";
	final static String IMU_MARKER_TABLE_END_TIME = "EndTime";
	final static String IMU_MARKER_TABLE_MARKER_NAME = "Name";
	final static String IMU_MARKER_TABLE_MARKER_DESCRIPTION = "Description";

	final static String IMU_JOINTMAPPING_TABLE_NAME = "JointMapping";
	final static String IMU_JOINTMAPPING_TABLE_MARKER_ID = "MarkerId";
	final static String IMU_JOINTMAPPING_TABLE_SENSOR_ID = "SensorId";
	final static String IMU_JOINTMAPPING_TABLE_JOINT_ID = "JointId";

	final static String IMU_RELATION_TABLE_NAME = "JointRelations";
	final static String IMU_RELATION_TABLE_JOINT_ID_DEPT = "JointIdDept";
	final static String IMU_RELATION_TABLE_FACTOR = "Factor";
	final static String IMU_RELATION_TABLE_JOINT_ID_INDEPT = "JointIdIndept";
	final static String IMU_RELATION_TABLE_ID = "ID";

	final static String IMU_CONFIGURATION_TABLE_NAME = "Configuration";
	final static String IMU_CONFIGURATION_TABLE_ID = "Id";
	final static String IMU_CONFIGURATION_TABLE_FILTER_ID = "FilterId";

	final static String FEELING_CONFIGURATION_TABLE_NAME = "FeelingConfig";
	final static String FEELING_CONFIGURATION_TABLE_ID = "Id";
	final static String FEELING_CONFIGURATION_DESCRIPTION = "Description";
	final static String FEELING_CONFIGURATION_NR_VALUES = "ValueNumber";
	final static String FEELING_CONFIGURATION_MAX_VALUES = "Max";
	final static String FEELING_CONFIGURATION_MIN_VALUES = "Min";

	// OP stands for Orierntation and Position because these tables are quite
	// similar
	final static String IMU_POSITION_TABLE_NAME = "InitialPosition";
	final static String IMU_ORIENTATION_TABLE_NAME = "InitialOrientation";
	final static String IMU_OP_TABLE_MARKER_ID = "MarkerId";
	final static String IMU_OP_TABLE_JOINT_ID = "JointId";
	final static String IMU_OP_TABLE_QUAT_W = "QuatW";
	final static String IMU_OP_TABLE_QUAT_X = "QuatX";
	final static String IMU_OP_TABLE_QUAT_Y = "QuatY";
	final static String IMU_OP_TABLE_QUAT_Z = "QuatZ";

	private static final Logger LOGGER = Logger.getLogger(Database.class
			.getName());

	protected static Database instance = null;

	public static Database getInstance() throws SQLException {
		if (instance == null) {
			instance = new Database();
		}

		return instance;
	}

	Connection conn;

	static final StringBuilder writeImuData = new StringBuilder("insert into ")
			.append(IMU_DATA_TABLE_NAME).append(" (")
			.append(IMU_DATA_TABLE_TIME).append(",")
			.append(IMU_DATA_TABLE_SAMPLEPERIOD).append(",")
			.append(IMU_DATA_TABLE_SENSOR_ID).append(",")
			.append(IMU_DATA_TABLE_ACCELEROMETER_PRE).append("x,")
			.append(IMU_DATA_TABLE_ACCELEROMETER_PRE).append("y,")
			.append(IMU_DATA_TABLE_ACCELEROMETER_PRE).append("z,")
			.append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("x,")
			.append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("y,")
			.append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("z,")
			.append(IMU_DATA_TABLE_MAGNETOMETER_PRE).append("x,")
			.append(IMU_DATA_TABLE_MAGNETOMETER_PRE).append("y,")
			.append(IMU_DATA_TABLE_MAGNETOMETER_PRE).append("z,")
			.append(") values (?,?,?,?,?,?,?,?,?,?,?,?)");

	static final StringBuilder writeComfortData = new StringBuilder(
			"insert into ").append(COMFORT_TABLE_NAME).append(" (")
			.append(COMFORT_TABLE_TIME).append(",")
			.append(COMFORT_TABLE_NUMBER).append(",")
			.append(COMFORT_TABLE_VALUE).append(",").append(") values (?,?,?)");

	protected Database() throws SQLException {
		conn = DriverManager.getConnection("jdbc:h2:" + DATABASE_NAME
				+ ";DB_CLOSE_ON_EXIT=FALSE", "sa", "");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error(e.toString());
				}
			}
		});

		createTables();

	}

	public void execute(String query) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute(query);
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	public void createTables() {

		StringBuilder createString;

		Marker defaultMarker = Marker.getDefaultMarker();

		if (!existsTable(IMU_DATA_TABLE_NAME)) {
			// create DATATABLE
			createString = new StringBuilder("create table ")
					.append(IMU_DATA_TABLE_NAME).append(" (")
					.append(IMU_DATA_TABLE_TIME).append(" TIMESTAMP, ")
					.append(IMU_DATA_TABLE_SAMPLEPERIOD).append(" DOUBLE, ")
					.append(IMU_DATA_TABLE_SENSOR_ID).append(" INT, ")
					.append(IMU_DATA_TABLE_ACCELEROMETER_PRE)
					.append("x DOUBLE, ")
					.append(IMU_DATA_TABLE_ACCELEROMETER_PRE)
					.append("y DOUBLE, ")
					.append(IMU_DATA_TABLE_ACCELEROMETER_PRE)
					.append("z DOUBLE, ").append(IMU_DATA_TABLE_GYROSCOPE_PRE)
					.append("x DOUBLE, ").append(IMU_DATA_TABLE_GYROSCOPE_PRE)
					.append("y DOUBLE, ").append(IMU_DATA_TABLE_GYROSCOPE_PRE)
					.append("z DOUBLE, ")
					.append(IMU_DATA_TABLE_MAGNETOMETER_PRE)
					.append("x DOUBLE, ")
					.append(IMU_DATA_TABLE_MAGNETOMETER_PRE)
					.append("y DOUBLE, ")
					.append(IMU_DATA_TABLE_MAGNETOMETER_PRE)
					.append("z DOUBLE, ").append(");");
			execute(createString.toString());
		}

		if (!existsTable(COMFORT_TABLE_NAME)) {
			// create comfort table
			createString = new StringBuilder("create table ")
					.append(COMFORT_TABLE_NAME).append(" (")
					.append(COMFORT_TABLE_TIME).append(" TIMESTAMP, ")
					.append(COMFORT_TABLE_NUMBER).append(" INT, ")
					.append(COMFORT_TABLE_VALUE).append(" DOUBLE, ")
					.append(");");
			execute(createString.toString());
		}

		if (!existsTable(IMU_MARKER_TABLE_NAME)) {
			// create marker table
			createString = new StringBuilder("create table ")
					.append(IMU_MARKER_TABLE_NAME).append(" (")
					.append(IMU_MARKER_TABLE_ID)
					.append(" BIGINT IDENTITY PRIMARY KEY, ")
					.append(IMU_MARKER_TABLE_START_TIME).append(" TIMESTAMP, ")
					.append(IMU_MARKER_TABLE_END_TIME).append(" TIMESTAMP, ")
					.append(IMU_MARKER_TABLE_MARKER_NAME).append(" VARCHAR, ")
					.append(IMU_MARKER_TABLE_MARKER_DESCRIPTION)
					.append(" VARCHAR, ").append(");");
			execute(createString.toString());
			setMarker(defaultMarker);
		}

		if (!existsTable(IMU_JOINTMAPPING_TABLE_NAME)) {
			// create joint mapping table
			createString = new StringBuilder("create table ")
					.append(IMU_JOINTMAPPING_TABLE_NAME).append(" (")
					.append(IMU_JOINTMAPPING_TABLE_MARKER_ID)
					.append(" BIGINT, ")
					.append(IMU_JOINTMAPPING_TABLE_SENSOR_ID).append(" INT, ")
					.append(IMU_JOINTMAPPING_TABLE_JOINT_ID).append(" INT, ")
					.append("FOREIGN KEY(")
					.append(IMU_JOINTMAPPING_TABLE_MARKER_ID)
					.append(") REFERENCES ").append(IMU_MARKER_TABLE_NAME)
					.append(" (").append(IMU_MARKER_TABLE_ID)
					.append(") ON DELETE CASCADE").append(");");
			execute(createString.toString());
			for (JointType j : JointType.values()) {
				setJointSensorMapping(defaultMarker, j, -1);
			}
		}

		if (!existsTable(IMU_RELATION_TABLE_NAME)) {
			// create joint relation table
			createString = new StringBuilder("create table ")
					.append(IMU_RELATION_TABLE_NAME).append(" (")
					.append(IMU_RELATION_TABLE_ID)
					.append(" BIGINT IDENTITY PRIMARY KEY, ")
					.append(IMU_RELATION_TABLE_JOINT_ID_DEPT).append(" INT, ")
					.append(IMU_RELATION_TABLE_FACTOR).append(" REAL, ")
					.append(IMU_RELATION_TABLE_JOINT_ID_INDEPT).append(" INT ")
					.append(");");
			execute(createString.toString());
			// init default
			// define relation between joints
			JointRelation relation = new JointRelation(new Joint(null,
					JointType.INDEX_TOP, null), new Joint(null,
					JointType.INDEX_MID, null), 2f / 3f);
			setJointRelation(relation);
			relation = new JointRelation(new Joint(null, JointType.MIDDLE_TOP,
					null), new Joint(null, JointType.MIDDLE_MID, null), 2f / 3f);
			setJointRelation(relation);
			relation = new JointRelation(new Joint(null, JointType.RING_TOP,
					null), new Joint(null, JointType.RING_MID, null), 2f / 3f);
			setJointRelation(relation);
			relation = new JointRelation(new Joint(null, JointType.LITTLE_TOP,
					null), new Joint(null, JointType.LITTLE_MID, null), 2f / 3f);
			setJointRelation(relation);
			relation = new JointRelation(new Joint(null, JointType.THUMB_TOP,
					null), new Joint(null, JointType.THUMB_MID, null), 2f / 3f);
			setJointRelation(relation);

		}

		if (!existsTable(IMU_ORIENTATION_TABLE_NAME)) {
			// create initial orientation table
			createString = new StringBuilder("create table ")
					.append(IMU_ORIENTATION_TABLE_NAME).append(" (")
					.append(IMU_OP_TABLE_MARKER_ID).append(" BIGINT, ")
					.append(IMU_OP_TABLE_JOINT_ID).append(" INT, ")
					.append(IMU_OP_TABLE_QUAT_W).append(" DOUBLE, ")
					.append(IMU_OP_TABLE_QUAT_X).append(" DOUBLE, ")
					.append(IMU_OP_TABLE_QUAT_Y).append(" DOUBLE, ")
					.append(IMU_OP_TABLE_QUAT_Z).append(" DOUBLE, ")
					.append("FOREIGN KEY(")
					.append(IMU_JOINTMAPPING_TABLE_MARKER_ID)
					.append(") REFERENCES ").append(IMU_MARKER_TABLE_NAME)
					.append(" (").append(IMU_MARKER_TABLE_ID)
					.append(") ON DELETE CASCADE").append(");");
			execute(createString.toString());
		}

		if (!existsTable(IMU_POSITION_TABLE_NAME)) {
			// create initial position table
			createString = new StringBuilder("create table ")
					.append(IMU_POSITION_TABLE_NAME).append(" (")
					.append(IMU_OP_TABLE_MARKER_ID).append(" BIGINT, ")
					.append(IMU_OP_TABLE_JOINT_ID).append(" INT, ")
					.append(IMU_OP_TABLE_QUAT_W).append(" DOUBLE, ")
					.append(IMU_OP_TABLE_QUAT_X).append(" DOUBLE, ")
					.append(IMU_OP_TABLE_QUAT_Y).append(" DOUBLE, ")
					.append(IMU_OP_TABLE_QUAT_Z).append(" DOUBLE, ")
					.append("FOREIGN KEY(")
					.append(IMU_JOINTMAPPING_TABLE_MARKER_ID)
					.append(") REFERENCES ").append(IMU_MARKER_TABLE_NAME)
					.append(" (").append(IMU_MARKER_TABLE_ID)
					.append(") ON DELETE CASCADE").append(");");
			execute(createString.toString());
		}

		if (!existsTable(IMU_CONFIGURATION_TABLE_NAME)) {
			// create configuration table
			createString = new StringBuilder("create table ")
					.append(IMU_CONFIGURATION_TABLE_NAME).append(" (")
					.append(IMU_CONFIGURATION_TABLE_ID)
					.append(" BIGINT IDENTITY PRIMARY KEY, ")
					.append(IMU_CONFIGURATION_TABLE_FILTER_ID).append(" INT, ")
					.append(");");
			execute(createString.toString());
			setFilterType(FilterTypes.QUATERNION_COMPLEMENTARY);
		}

		if (!existsTable(FEELING_CONFIGURATION_TABLE_NAME)) {
			// create configuration table
			createString = new StringBuilder("create table ")
					.append(FEELING_CONFIGURATION_TABLE_NAME).append(" (")
					.append(FEELING_CONFIGURATION_TABLE_ID)
					.append(" BIGINT IDENTITY PRIMARY KEY, ")
					.append(FEELING_CONFIGURATION_DESCRIPTION)
					.append(" VARCHAR, ")
					.append(FEELING_CONFIGURATION_NR_VALUES).append(" INT, ")
					.append(FEELING_CONFIGURATION_MIN_VALUES).append(" INT, ")
					.append(FEELING_CONFIGURATION_MAX_VALUES).append(" INT, ")
					.append(");");
			execute(createString.toString());
			setFeeling(new FeelingScale());// default
		}
	}

	private boolean existsTable(String tablename) {
		DatabaseMetaData dmd;
		try {
			dmd = conn.getMetaData();

			ResultSet results = dmd.getTables(conn.getCatalog(), null, null,
					null);

			while (results.next()) {
				String tableName = results.getString("TABLE_NAME");
				// checking for one table should be enough
				if (tableName.equals(tablename.toUpperCase())) {
					return true;
				}
			}

		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return false;
	}

	public void writeFeelingData(FeelingScale comfort, java.util.Date timestamp) {

		for (int i = 0; i < comfort.getCurrentValues().size(); i++) {
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(writeComfortData.toString());
				stmt.setTimestamp(1, new Timestamp(timestamp.getTime()));
				stmt.setInt(2, i);
				stmt.setDouble(3, comfort.getPercentValue(i));
				stmt.execute();
				stmt.close();
			} catch (SQLException ex) {
				LOGGER.error(ex);
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						LOGGER.error(e);
					}
				}
			}
		}
	}

	public void deleteFeelingData(Marker marker) {

		PreparedStatement statement = null;

		StringBuilder getType = new StringBuilder("delete from ")
				.append(COMFORT_TABLE_NAME).append(" where ")
				.append(COMFORT_TABLE_TIME).append(" between ? and ?");

		try {
			statement = conn.prepareStatement(getType.toString());
			Timestamp t1;
			if (marker.start != null) {
				t1 = new Timestamp(marker.start.getTime());
			} else {
				t1 = new Timestamp(new Date().getTime());
			}
			Timestamp t2;
			if (marker.start != null) {
				t2 = new Timestamp(marker.end.getTime());
			} else {
				t2 = new Timestamp(new Date().getTime());
			}
			statement.setTimestamp(1, t1);
			statement.setTimestamp(2, t2);
			statement.executeUpdate();

		} catch (final SQLException e) {
			LOGGER.error(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	public FeelingScale selectFeelingData(java.util.Date timestamp,
			FeelingScale comfortScale) {
		FeelingScale result = null;

		StringBuilder select = new StringBuilder("select ")
				.append(COMFORT_TABLE_NUMBER).append(" , ")
				.append(COMFORT_TABLE_VALUE).append(" from ")
				.append(COMFORT_TABLE_NAME).append(" where ")
				.append(COMFORT_TABLE_TIME)
				.append(" between ? and ? ORDER BY ")
				.append(COMFORT_TABLE_NUMBER);

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(select.toString());
			stmt.setTimestamp(1, new Timestamp(timestamp.getTime()));
			stmt.setTimestamp(2, new Timestamp(timestamp.getTime()));
			ResultSet res = stmt.executeQuery();
			while (res.next()) {
				comfortScale.setValueInPercent(
						res.getInt(COMFORT_TABLE_NUMBER),
						res.getDouble(COMFORT_TABLE_VALUE));
			}
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return result;
	}

	public void writeImuData(int id, SensorVector accel, SensorVector gyro,
			SensorVector magneto, double samplePeriod, java.util.Date timestamp) {

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(writeImuData.toString());
			stmt.setTimestamp(1, new Timestamp(timestamp.getTime()));
			stmt.setDouble(2, samplePeriod);
			stmt.setDouble(3, id);
			stmt.setDouble(4, accel.x);
			stmt.setDouble(5, accel.y);
			stmt.setDouble(6, accel.z);
			stmt.setDouble(7, gyro.x);
			stmt.setDouble(8, gyro.y);
			stmt.setDouble(9, gyro.z);
			stmt.setDouble(10, magneto.x);
			stmt.setDouble(11, magneto.y);
			stmt.setDouble(12, magneto.z);
			stmt.execute();
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	public void deleteImuData(Marker marker) {

		PreparedStatement statement = null;

		StringBuilder getType = new StringBuilder("delete from ")
				.append(IMU_DATA_TABLE_NAME).append(" where ")
				.append(IMU_DATA_TABLE_TIME).append(" between ? and ?");

		try {
			statement = conn.prepareStatement(getType.toString());
			Timestamp t1;
			if (marker.start != null) {
				t1 = new Timestamp(marker.start.getTime());
			} else {
				t1 = new Timestamp(new Date().getTime());
			}
			Timestamp t2;
			if (marker.start != null) {
				t2 = new Timestamp(marker.end.getTime());
			} else {
				t2 = new Timestamp(new Date().getTime());
			}
			statement.setTimestamp(1, t1);
			statement.setTimestamp(2, t2);
			statement.executeUpdate();

		} catch (final SQLException e) {
			LOGGER.error(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException e) {
					LOGGER.error(e);
				}
			}
		}

	}

	public ResultSet selectImuData(PreparedStatement statement, Marker marker)
			throws SQLException {

		ResultSet rs = null;

		StringBuilder select = new StringBuilder("select ")
				.append(IMU_DATA_TABLE_SENSOR_ID).append(",")
				.append(IMU_DATA_TABLE_TIME).append(",")
				.append(IMU_DATA_TABLE_SAMPLEPERIOD).append(",")
				.append(IMU_DATA_TABLE_ACCELEROMETER_PRE).append("x")
				.append(",").append(IMU_DATA_TABLE_ACCELEROMETER_PRE)
				.append("y").append(",")
				.append(IMU_DATA_TABLE_ACCELEROMETER_PRE).append("z")
				.append(",").append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("x")
				.append(",").append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("y")
				.append(",").append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("z")
				.append(",").append(IMU_DATA_TABLE_MAGNETOMETER_PRE)
				.append("x").append(",")
				.append(IMU_DATA_TABLE_MAGNETOMETER_PRE).append("y")
				.append(",").append(IMU_DATA_TABLE_MAGNETOMETER_PRE)
				.append("z").append(" from ").append(IMU_DATA_TABLE_NAME)
				.append(" where ").append(IMU_DATA_TABLE_TIME)
				.append(" between ? and ?");

		statement = conn.prepareStatement(select.toString());
		statement.setTimestamp(1, new Timestamp(marker.start.getTime()));
		statement.setTimestamp(2, new Timestamp(marker.end.getTime()));
		rs = statement.executeQuery();

		return rs;
	}

	public ArrayList<ImuRawData> getImuData(Marker marker) {
		ArrayList<ImuRawData> data = new ArrayList<ImuRawData>();

		PreparedStatement statement = null;
		try {
			ResultSet rs = selectImuData(statement, marker);

			while (rs.next()) {
				SensorVector accelerometer = new SensorVector(
						rs.getDouble(IMU_DATA_TABLE_ACCELEROMETER_PRE + "x"),
						rs.getDouble(IMU_DATA_TABLE_ACCELEROMETER_PRE + "y"),
						rs.getDouble(IMU_DATA_TABLE_ACCELEROMETER_PRE + "z"));

				SensorVector gyroskope = new SensorVector(
						rs.getDouble(IMU_DATA_TABLE_GYROSCOPE_PRE + "x"),
						rs.getDouble(IMU_DATA_TABLE_GYROSCOPE_PRE + "y"),
						rs.getDouble(IMU_DATA_TABLE_GYROSCOPE_PRE + "z"));

				SensorVector magnetometer = new SensorVector(
						rs.getDouble(IMU_DATA_TABLE_MAGNETOMETER_PRE + "x"),
						rs.getDouble(IMU_DATA_TABLE_MAGNETOMETER_PRE + "y"),
						rs.getDouble(IMU_DATA_TABLE_MAGNETOMETER_PRE + "z"));

				ImuRawData dataItem = new ImuRawData(
						rs.getInt(IMU_DATA_TABLE_SENSOR_ID),
						rs.getTimestamp(IMU_DATA_TABLE_TIME),
						rs.getDouble(IMU_DATA_TABLE_SAMPLEPERIOD),
						accelerometer, gyroskope, magnetometer);
				data.add(dataItem);
			}

		} catch (final SQLException e) {
			LOGGER.error(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException e) {
					LOGGER.error(e);
				}
			}
		}

		return data;
	}

	/**
	 * get count of imu data entries in db
	 * 
	 * @param marker
	 * @return
	 */
	public int getCount(Marker marker) {
		int count = 0;

		PreparedStatement statement = null;
		try {
			ResultSet rs = null;

			StringBuilder select = new StringBuilder("select count(")
					.append(IMU_DATA_TABLE_SENSOR_ID).append(")")
					.append(" from ").append(IMU_DATA_TABLE_NAME)
					.append(" where ").append(IMU_DATA_TABLE_TIME)
					.append(" between ? and ?");

			statement = conn.prepareStatement(select.toString());
			statement.setTimestamp(1, new Timestamp(marker.start.getTime()));
			statement.setTimestamp(2, new Timestamp(marker.end.getTime()));
			rs = statement.executeQuery();

			if (rs.next()) {
				count = rs.getInt(1);
			}

		} catch (final SQLException e) {
			LOGGER.error(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException e) {
					LOGGER.error(e);
				}
			}
		}

		return count;

	}

	public void writeImuDataToCsv(Marker marker, String filename) {
		PreparedStatement statement = null;
		try {
			ResultSet rs = selectImuData(statement, marker);
			new Csv().write(filename, rs, null);
		} catch (final SQLException e) {
			LOGGER.error(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	public FilterTypes getFilterType() {
		FilterTypes currentType = null;

		Statement statement = null;
		ResultSet rs = null;

		StringBuilder select = new StringBuilder("select ")
				.append(IMU_CONFIGURATION_TABLE_FILTER_ID).append(" from ")
				.append(IMU_CONFIGURATION_TABLE_NAME);

		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(select.toString());
			if (rs.next()) {
				currentType = FilterTypes.values()[rs.getInt(1)];
			} // end while
		} catch (final SQLException e) {
			LOGGER.error(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException e) {
					LOGGER.error(e);
				}
			}
		}

		return currentType;
	}

	private boolean updateFilterType(FilterTypes type, int id) {

		boolean ret = false;

		StringBuilder update = new StringBuilder("update ")
				.append(IMU_CONFIGURATION_TABLE_NAME).append(" set ")
				.append(IMU_CONFIGURATION_TABLE_FILTER_ID).append("=? where ")
				.append(IMU_CONFIGURATION_TABLE_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(update.toString());
			stmt.setInt(1, type.ordinal());
			stmt.setLong(2, id);
			ret = stmt.executeUpdate() > 0;
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	public void setFilterType(FilterTypes type) {

		if (updateFilterType(type, 1)) {
			return;
		}

		StringBuilder insert = new StringBuilder("insert into ")
				.append(IMU_CONFIGURATION_TABLE_NAME).append(" (")
				.append(IMU_CONFIGURATION_TABLE_FILTER_ID)
				.append(") values (?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insert.toString());
			stmt.setInt(1, type.ordinal());
			stmt.execute();
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	private boolean updateMarkerById(Marker marker) {

		boolean ret = false;

		StringBuilder update = new StringBuilder("update ")
				.append(IMU_MARKER_TABLE_NAME).append(" set ")
				.append(IMU_MARKER_TABLE_MARKER_NAME).append("=? ").append(",")
				.append(IMU_MARKER_TABLE_MARKER_DESCRIPTION).append("=? ")
				.append(",").append(IMU_MARKER_TABLE_START_TIME).append("=? ")
				.append(",").append(IMU_MARKER_TABLE_END_TIME).append("=? ")
				.append("where ").append(IMU_MARKER_TABLE_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(update.toString());
			stmt.setString(1, marker.getName());
			stmt.setString(2, marker.getDescription());
			stmt.setTimestamp(3, new Timestamp(marker.getStart().getTime()));
			stmt.setTimestamp(4, new Timestamp(marker.getEnd().getTime()));
			stmt.setLong(5, marker.getId());
			ret = stmt.executeUpdate() > 0;
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	public boolean removeMarker(Marker marker) {

		if (marker.getId() < 1) {
			return false;
		}

		boolean ret = false;

		StringBuilder delete = new StringBuilder("delete from ")
				.append(IMU_MARKER_TABLE_NAME).append(" where ")
				.append(IMU_MARKER_TABLE_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(delete.toString());
			stmt.setLong(1, marker.getId());
			ret = stmt.executeUpdate() > 0;
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	public ArrayList<Marker> getAvailableMarkers() {
		ArrayList<Marker> markers = new ArrayList<Marker>();

		Statement statement = null;
		ResultSet rs = null;

		StringBuilder select = new StringBuilder("select ")
				.append(IMU_MARKER_TABLE_ID).append(",")
				.append(IMU_MARKER_TABLE_MARKER_NAME).append(",")
				.append(IMU_MARKER_TABLE_MARKER_DESCRIPTION).append(",")
				.append(IMU_MARKER_TABLE_START_TIME).append(",")
				.append(IMU_MARKER_TABLE_END_TIME).append(" from ")
				.append(IMU_MARKER_TABLE_NAME).append(" where ")
				.append(IMU_MARKER_TABLE_ID).append("!=1");

		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(select.toString());
			while (rs.next()) {
				Marker newMarker = new Marker(
						rs.getString(IMU_MARKER_TABLE_MARKER_NAME),
						rs.getString(IMU_MARKER_TABLE_MARKER_DESCRIPTION),
						rs.getLong(IMU_MARKER_TABLE_ID),
						rs.getTimestamp(IMU_MARKER_TABLE_START_TIME),
						rs.getTimestamp(IMU_MARKER_TABLE_END_TIME));
				markers.add(newMarker);
			} // end while
		} catch (final SQLException e) {
			LOGGER.error(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException e) {
					LOGGER.error(e);
				}
			}
		}

		return markers;
	}

	public void setMarker(Marker marker) {

		if (marker.getId() > 0) {
			if (updateMarkerById(marker)) {
				return;
			}
		}

		StringBuilder insert = new StringBuilder("insert into ")
				.append(IMU_MARKER_TABLE_NAME).append(" (")
				.append(IMU_MARKER_TABLE_MARKER_NAME).append(",")
				.append(IMU_MARKER_TABLE_MARKER_DESCRIPTION).append(",")
				.append(IMU_MARKER_TABLE_START_TIME).append(",")
				.append(IMU_MARKER_TABLE_END_TIME).append(") values (?,?,?,?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insert.toString(),
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, marker.getName());
			stmt.setString(2, marker.getDescription());
			stmt.setTimestamp(3, new Timestamp(marker.getStart().getTime()));
			stmt.setTimestamp(4, new Timestamp(marker.getEnd().getTime()));
			stmt.execute();

			ResultSet results = stmt.getGeneratedKeys();
			if (results.next()) {
				marker.setId(results.getLong(1));
			}

			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	private boolean updateInitialQuaternion(String tablename, Marker marker,
			JointType jointType, Quaternion quaternion) {

		boolean ret = false;

		StringBuilder update = new StringBuilder("update ").append(tablename)
				.append(" set ").append(IMU_OP_TABLE_QUAT_W).append("=? ,")
				.append(IMU_OP_TABLE_QUAT_X).append("=? ,")
				.append(IMU_OP_TABLE_QUAT_Y).append("=? ,")
				.append(IMU_OP_TABLE_QUAT_Z).append("=? ").append(" where ")
				.append(IMU_OP_TABLE_MARKER_ID).append("=? and ")
				.append(IMU_OP_TABLE_JOINT_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(update.toString());
			stmt.setDouble(1, quaternion.getW());
			stmt.setDouble(2, quaternion.getX());
			stmt.setDouble(3, quaternion.getY());
			stmt.setDouble(4, quaternion.getZ());
			stmt.setLong(5, marker.getId());
			stmt.setInt(6, jointType.ordinal());
			ret = stmt.executeUpdate() > 0;
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	public void setInitialPosition(Marker marker, JointType jointType,
			Quaternion position) {
		setInitialQuaternion(IMU_POSITION_TABLE_NAME, marker, jointType,
				position);
	}

	public void setInitialOrientation(Marker marker, JointType jointType,
			Quaternion orientation) {
		setInitialQuaternion(IMU_ORIENTATION_TABLE_NAME, marker, jointType,
				orientation);
	}

	public void setInitialQuaternion(String tablename, Marker marker,
			JointType jointType, Quaternion quaternion) {
		if (updateInitialQuaternion(tablename, marker, jointType, quaternion)) {
			return;
		}

		StringBuilder insert = new StringBuilder("insert into ")
				.append(tablename).append(" (").append(IMU_OP_TABLE_MARKER_ID)
				.append(",").append(IMU_OP_TABLE_JOINT_ID).append(",")
				.append(IMU_OP_TABLE_QUAT_W).append(",")
				.append(IMU_OP_TABLE_QUAT_X).append(",")
				.append(IMU_OP_TABLE_QUAT_Y).append(",")
				.append(IMU_OP_TABLE_QUAT_Z).append(",")
				.append(") values (?,?,?,?,?,?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insert.toString());
			stmt.setLong(1, marker.getId());
			stmt.setInt(2, jointType.ordinal());
			stmt.setDouble(3, quaternion.getW());
			stmt.setDouble(4, quaternion.getX());
			stmt.setDouble(5, quaternion.getY());
			stmt.setDouble(6, quaternion.getZ());
			stmt.execute();
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	public Quaternion getInitialOrientation(Marker marker, JointType jointType) {
		return getInitialQuaternion(IMU_ORIENTATION_TABLE_NAME, marker,
				jointType);
	}

	public Quaternion getInitialPosition(Marker marker, JointType jointType) {
		return getInitialQuaternion(IMU_POSITION_TABLE_NAME, marker, jointType);
	}

	public Quaternion getInitialQuaternion(String tablename, Marker marker,
			JointType jointType) {

		Quaternion ret = new Quaternion();

		StringBuilder select = new StringBuilder("select ")
				.append(IMU_OP_TABLE_QUAT_W).append(", ")
				.append(IMU_OP_TABLE_QUAT_X).append(", ")
				.append(IMU_OP_TABLE_QUAT_Y).append(", ")
				.append(IMU_OP_TABLE_QUAT_Z).append(" from ").append(tablename)
				.append(" where ").append(IMU_OP_TABLE_MARKER_ID)
				.append("=? and ").append(IMU_OP_TABLE_JOINT_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(select.toString());
			stmt.setLong(1, marker.getId());
			stmt.setInt(2, jointType.ordinal());
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				ret.setQ1(res.getDouble(1));
				ret.setQ2(res.getDouble(2));
				ret.setQ3(res.getDouble(3));
				ret.setQ4(res.getDouble(4));
			}
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	private boolean updateJointSensorMapping(Marker marker,
			JointType jointType, int sensorId) {

		boolean ret = false;

		StringBuilder update = new StringBuilder("update ")
				.append(IMU_JOINTMAPPING_TABLE_NAME).append(" set ")
				.append(IMU_JOINTMAPPING_TABLE_SENSOR_ID).append("=? ")
				.append(" where ").append(IMU_JOINTMAPPING_TABLE_MARKER_ID)
				.append("=? and ").append(IMU_JOINTMAPPING_TABLE_JOINT_ID)
				.append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(update.toString());
			stmt.setInt(1, sensorId);
			stmt.setLong(2, marker.getId());
			stmt.setInt(3, jointType.ordinal());
			ret = stmt.executeUpdate() > 0;
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	public void setJointSensorMapping(Marker marker, JointType jointType,
			int sensorId) {
		if (updateJointSensorMapping(marker, jointType, sensorId)) {
			return;
		}

		StringBuilder insert = new StringBuilder("insert into ")
				.append(IMU_JOINTMAPPING_TABLE_NAME).append(" (")
				.append(IMU_JOINTMAPPING_TABLE_MARKER_ID).append(",")
				.append(IMU_JOINTMAPPING_TABLE_SENSOR_ID).append(",")
				.append(IMU_JOINTMAPPING_TABLE_JOINT_ID).append(",")
				.append(") values (?,?,?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insert.toString());
			stmt.setLong(1, marker.getId());
			stmt.setInt(2, sensorId);
			stmt.setInt(3, jointType.ordinal());
			stmt.execute();
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	public int getJointSensorMapping(Marker marker, JointType jointType) {

		int retID = -1;

		StringBuilder select = new StringBuilder("select ")
				.append(IMU_JOINTMAPPING_TABLE_SENSOR_ID).append(" from ")
				.append(IMU_JOINTMAPPING_TABLE_NAME).append(" where ")
				.append(IMU_JOINTMAPPING_TABLE_MARKER_ID).append("=? and ")
				.append(IMU_JOINTMAPPING_TABLE_JOINT_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(select.toString());
			stmt.setLong(1, marker.getId());
			stmt.setInt(2, jointType.ordinal());
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				retID = res.getInt(1);
			}
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return retID;
	}

	public ArrayList<JointRelation> getJointRelation(Hand hand,
			Joint jointInDept) {

		ArrayList<JointRelation> relations = new ArrayList<JointRelation>();

		StringBuilder select = new StringBuilder("select ")
				.append(IMU_RELATION_TABLE_JOINT_ID_DEPT).append(", ")
				.append(IMU_RELATION_TABLE_FACTOR).append(" from ")
				.append(IMU_RELATION_TABLE_NAME).append(" where ")
				.append(IMU_RELATION_TABLE_JOINT_ID_INDEPT).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(select.toString());
			stmt.setInt(1, jointInDept.getType().ordinal());
			ResultSet res = stmt.executeQuery();

			while (res.next()) {
				Joint jointdept = hand.getJoint(JointType.values()[res
						.getInt(IMU_RELATION_TABLE_JOINT_ID_DEPT)]);
				float factor = res.getFloat(IMU_RELATION_TABLE_FACTOR);
				relations
						.add(new JointRelation(jointdept, jointInDept, factor));
			}
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return relations;
	}

	private boolean updateJointRelation(JointRelation relation) {

		boolean ret = false;

		StringBuilder update = new StringBuilder("update ")
				.append(IMU_RELATION_TABLE_NAME).append(" set ")
				.append(IMU_RELATION_TABLE_JOINT_ID_INDEPT).append("=? ,")
				.append(IMU_RELATION_TABLE_FACTOR).append("=?")
				.append(" where ").append(IMU_RELATION_TABLE_JOINT_ID_DEPT)
				.append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(update.toString());
			stmt.setInt(1, relation.getIndependent().getType().ordinal());
			stmt.setFloat(2, relation.getFactor());
			stmt.setInt(3, relation.getDependent().getType().ordinal());
			ret = stmt.executeUpdate() > 0; // updated or not
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	public void setJointRelation(JointRelation relation) {
		if (updateJointRelation(relation)) {
			return;
		}

		StringBuilder insert = new StringBuilder("insert into ")
				.append(IMU_RELATION_TABLE_NAME).append(" (")
				.append(IMU_RELATION_TABLE_JOINT_ID_DEPT).append(",")
				.append(IMU_RELATION_TABLE_FACTOR).append(",")
				.append(IMU_RELATION_TABLE_JOINT_ID_INDEPT)
				.append(") values (?,?,?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insert.toString());
			stmt.setInt(1, relation.getDependent().getType().ordinal());
			stmt.setFloat(2, relation.getFactor());
			stmt.setInt(3, relation.getIndependent().getType().ordinal());
			stmt.execute();
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	public boolean deleteJointRelation(JointRelation relation) {

		boolean ret = false;

		StringBuilder update = new StringBuilder("delete from ")
				.append(IMU_RELATION_TABLE_NAME).append(" where ")
				.append(IMU_RELATION_TABLE_JOINT_ID_INDEPT).append("=? and ")
				.append(IMU_RELATION_TABLE_JOINT_ID_DEPT).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(update.toString());
			stmt.setInt(1, relation.getIndependent().getType().ordinal());
			stmt.setInt(2, relation.getDependent().getType().ordinal());
			ret = stmt.executeUpdate() > 0; // updated or not
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	public FeelingScale getFeelingScale() {
		FeelingScale feeling = null;

		PreparedStatement statement = null;
		ResultSet rs = null;

		StringBuilder select = new StringBuilder("select ")
				.append(FEELING_CONFIGURATION_DESCRIPTION).append(",")
				.append(FEELING_CONFIGURATION_NR_VALUES).append(",")
				.append(FEELING_CONFIGURATION_MIN_VALUES).append(",")
				.append(FEELING_CONFIGURATION_MAX_VALUES).append(" from ")
				.append(FEELING_CONFIGURATION_TABLE_NAME).append(" where ")
				.append(FEELING_CONFIGURATION_TABLE_ID).append(" =? ");

		try {
			statement = conn.prepareStatement(select.toString());
			statement.setInt(1, 1);// default id 1
			rs = statement.executeQuery();
			if (rs.next()) {
				String description = rs
						.getString(FEELING_CONFIGURATION_DESCRIPTION);
				int max = rs.getInt(FEELING_CONFIGURATION_MAX_VALUES);
				int min = rs.getInt(FEELING_CONFIGURATION_MIN_VALUES);
				int nrValues = rs.getInt(FEELING_CONFIGURATION_NR_VALUES);

				feeling = new FeelingScale(description, min, max, nrValues);
			} else {
				feeling = new FeelingScale();
			}
		} catch (final SQLException e) {
			LOGGER.error(e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (final SQLException e) {
					LOGGER.error(e);
				}
			}
		}

		return feeling;
	}

	private boolean updateFeelingScale(FeelingScale feeling) {

		boolean ret = false;

		StringBuilder update = new StringBuilder("update ")
				.append(FEELING_CONFIGURATION_TABLE_NAME).append(" set ")
				.append(FEELING_CONFIGURATION_NR_VALUES).append("=? ,")
				.append(FEELING_CONFIGURATION_MIN_VALUES).append("=? ,")
				.append(FEELING_CONFIGURATION_MAX_VALUES).append("=? ,")
				.append(FEELING_CONFIGURATION_DESCRIPTION).append("=?")
				.append(" where ").append(FEELING_CONFIGURATION_TABLE_ID)
				.append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(update.toString());
			stmt.setInt(1, feeling.getCurrentValues().size());
			stmt.setInt(2, feeling.getMin());
			stmt.setInt(3, feeling.getMax());
			stmt.setString(4, feeling.getAllDescriptions());
			stmt.setInt(5, 1); // at the moment we will not have more than one
								// entry
			ret = stmt.executeUpdate() > 0; // updated or not
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
		return ret;
	}

	public void setFeeling(FeelingScale feeling) {

		if (updateFeelingScale(feeling)) {
			return;
		}

		StringBuilder insert = new StringBuilder("insert into ")
				.append(FEELING_CONFIGURATION_TABLE_NAME).append(" (")
				.append(FEELING_CONFIGURATION_NR_VALUES).append(",")
				.append(FEELING_CONFIGURATION_MIN_VALUES).append(",")
				.append(FEELING_CONFIGURATION_MAX_VALUES).append(",")
				.append(FEELING_CONFIGURATION_DESCRIPTION)
				.append(") values (?,?,?,?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insert.toString());
			stmt.setInt(1, feeling.getCurrentValues().size());
			stmt.setInt(2, feeling.getMin());
			stmt.setInt(3, feeling.getMax());
			stmt.setString(4, feeling.getAllDescriptions());
			stmt.execute();
			stmt.close();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
		}
	}

}
