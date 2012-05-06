package imuanalyzer.data;

import imuanalyzer.device.ImuRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.tools.SensorVector;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class Database {

	final static String DATABASE_NAME = "IMUAnalyzerData";

	final static String IMU_DATA_TABLE_NAME = "Data";
	final static String IMU_DATA_TABLE_TIME = "Time";
	final static String IMU_DATA_TABLE_SENSOR_ID = "SensorId";
	final static String IMU_DATA_TABLE_ACCELEROMETER_PRE = "Accel_";
	final static String IMU_DATA_TABLE_GYROSCOPE_PRE = "Gyro_";
	final static String IMU_DATA_TABLE_MAGNETOMETER_PRE = "Magneto_";

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

	final static String IMU_CONFIGURATION_TABLE_NAME = "Configuration";
	final static String IMU_CONFIGURATION_TABLE_ID = "Id";
	final static String IMU_CONFIGURATION_TABLE_FILTER_ID = "FilterId";

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
			.append(") values (?,?,?,?,?,?,?,?,?,?,?)");

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

		DatabaseMetaData dmd;
		try {
			dmd = conn.getMetaData();

			ResultSet results = dmd.getTables(conn.getCatalog(), null, null,
					null);

			while (results.next()) {
				String tableName = results.getString("TABLE_NAME");
				// checking for one table should be enough
				if (tableName.equals(IMU_DATA_TABLE_NAME.toUpperCase())) {
					return;
				}
			}

		} catch (SQLException e) {
			LOGGER.error(e);
		}

		StringBuilder createString;

		// create DATATABLE
		createString = new StringBuilder("create table ")
				.append(IMU_DATA_TABLE_NAME).append(" (")
				.append(IMU_DATA_TABLE_TIME).append(" TIMESTAMP, ")
				.append(IMU_DATA_TABLE_SENSOR_ID).append(" INT, ")
				.append(IMU_DATA_TABLE_ACCELEROMETER_PRE).append("x DOUBLE, ")
				.append(IMU_DATA_TABLE_ACCELEROMETER_PRE).append("y DOUBLE, ")
				.append(IMU_DATA_TABLE_ACCELEROMETER_PRE).append("z DOUBLE, ")
				.append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("x DOUBLE, ")
				.append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("y DOUBLE, ")
				.append(IMU_DATA_TABLE_GYROSCOPE_PRE).append("z DOUBLE, ")
				.append(IMU_DATA_TABLE_MAGNETOMETER_PRE).append("x DOUBLE, ")
				.append(IMU_DATA_TABLE_MAGNETOMETER_PRE).append("y DOUBLE, ")
				.append(IMU_DATA_TABLE_MAGNETOMETER_PRE).append("z DOUBLE, ")
				.append(");");
		execute(createString.toString());

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

		// create joint mapping table
		createString = new StringBuilder("create table ")
				.append(IMU_JOINTMAPPING_TABLE_NAME).append(" (")
				.append(IMU_JOINTMAPPING_TABLE_MARKER_ID).append(" BIGINT, ")
				.append(IMU_JOINTMAPPING_TABLE_SENSOR_ID).append(" INT, ")
				.append(IMU_JOINTMAPPING_TABLE_JOINT_ID).append(" INT, ")
				.append("FOREIGN KEY(")
				.append(IMU_JOINTMAPPING_TABLE_MARKER_ID)
				.append(") REFERENCES ").append(IMU_MARKER_TABLE_NAME)
				.append(" (").append(IMU_MARKER_TABLE_ID).append(")")
				.append(");");
		execute(createString.toString());

		// create configuration table
		createString = new StringBuilder("create table ")
				.append(IMU_CONFIGURATION_TABLE_NAME).append(" (")
				.append(IMU_CONFIGURATION_TABLE_ID)
				.append(" BIGINT IDENTITY PRIMARY KEY, ")
				.append(IMU_CONFIGURATION_TABLE_FILTER_ID).append(" INT, ")
				.append(");");
		execute(createString.toString());

		initTableData();

	}

	private void initTableData() {
		Marker defaultMarker = Marker.getDefaultMarker();
		setMarker(defaultMarker);
		setFilterType(FilterTypes.QUATERNION_COMPLEMENTARY);

		for (JointType j : JointType.values()) {
			setJointSensorMapping(defaultMarker, j, -1);
		}
	}

	public void writeImuData(int id, SensorVector accel, SensorVector gyro,
			SensorVector magneto) {

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(writeImuData.toString());
			stmt.setDate(1, new java.sql.Date(new Date().getTime()));
			stmt.setDouble(2, id);
			stmt.setDouble(3, accel.x);
			stmt.setDouble(4, accel.y);
			stmt.setDouble(5, accel.z);
			stmt.setDouble(6, gyro.x);
			stmt.setDouble(7, gyro.y);
			stmt.setDouble(8, gyro.z);
			stmt.setDouble(9, magneto.x);
			stmt.setDouble(10, magneto.y);
			stmt.setDouble(11, magneto.z);
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

	public ArrayList<ImuRawData> getImuData(Marker marker) {
		ArrayList<ImuRawData> data = new ArrayList<ImuRawData>();

		PreparedStatement statement = null;
		ResultSet rs = null;

		StringBuilder getType = new StringBuilder("select ")
				.append(IMU_DATA_TABLE_SENSOR_ID).append(",")
				.append(IMU_DATA_TABLE_TIME).append(",")
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
				.append(">? and ").append(IMU_DATA_TABLE_TIME).append("<?");

		try {
			statement = conn.prepareStatement(getType.toString());
			statement.setDate(1, marker.start);
			statement.setDate(2, marker.end);
			rs = statement.executeQuery(getType.toString());
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
						rs.getDate(IMU_DATA_TABLE_TIME), accelerometer,
						gyroskope, magnetometer);
				data.add(dataItem);
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

		return data;
	}

	public FilterTypes getFilterType() {
		FilterTypes currentType = null;

		Statement statement = null;
		ResultSet rs = null;

		StringBuilder getType = new StringBuilder("select ")
				.append(IMU_CONFIGURATION_TABLE_FILTER_ID).append(" from ")
				.append(IMU_CONFIGURATION_TABLE_NAME);

		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(getType.toString());
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

		StringBuilder setType = new StringBuilder("update ")
				.append(IMU_CONFIGURATION_TABLE_NAME).append(" set ")
				.append(IMU_CONFIGURATION_TABLE_FILTER_ID).append("=? where ")
				.append(IMU_CONFIGURATION_TABLE_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(setType.toString());
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

		StringBuilder setType = new StringBuilder("insert into ")
				.append(IMU_CONFIGURATION_TABLE_NAME).append(" (")
				.append(IMU_CONFIGURATION_TABLE_FILTER_ID)
				.append(") values (?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(setType.toString());
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

		StringBuilder setType = new StringBuilder("update ")
				.append(IMU_MARKER_TABLE_NAME).append(" set ")
				.append(IMU_MARKER_TABLE_MARKER_NAME).append("=? ").append(",")
				.append(IMU_MARKER_TABLE_MARKER_DESCRIPTION).append("=? ")
				.append(",").append(IMU_MARKER_TABLE_START_TIME).append("=? ")
				.append(",").append(IMU_MARKER_TABLE_END_TIME).append("=? ")
				.append("where ").append(IMU_MARKER_TABLE_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(setType.toString());
			stmt.setString(1, marker.getName());
			stmt.setString(2, marker.getDescription());
			stmt.setDate(3, marker.getStart());
			stmt.setDate(4, marker.getEnd());
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

		StringBuilder setType = new StringBuilder("delete from ")
				.append(IMU_MARKER_TABLE_NAME).append(" where ")
				.append(IMU_MARKER_TABLE_ID).append("=").append(marker.getId());

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(setType.toString());
			stmt.setString(1, marker.getName());
			stmt.setString(2, marker.getDescription());
			stmt.setDate(3, marker.getStart());
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

		StringBuilder getType = new StringBuilder("select ")
				.append(IMU_MARKER_TABLE_ID).append(",")
				.append(IMU_MARKER_TABLE_MARKER_NAME).append(",")
				.append(IMU_MARKER_TABLE_MARKER_DESCRIPTION).append(",")
				.append(IMU_MARKER_TABLE_START_TIME).append(",")
				.append(IMU_MARKER_TABLE_END_TIME).append(" from ")
				.append(IMU_MARKER_TABLE_NAME).append(" where ")
				.append(IMU_MARKER_TABLE_ID).append("!=1");

		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(getType.toString());
			while (rs.next()) {
				Marker newMarker = new Marker(
						rs.getString(IMU_MARKER_TABLE_MARKER_NAME),
						rs.getString(IMU_MARKER_TABLE_MARKER_DESCRIPTION),
						rs.getLong(IMU_MARKER_TABLE_ID),
						rs.getDate(IMU_MARKER_TABLE_START_TIME),
						rs.getDate(IMU_MARKER_TABLE_END_TIME));
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

		StringBuilder setType = new StringBuilder("insert into ")
				.append(IMU_MARKER_TABLE_NAME).append(" (")
				.append(IMU_MARKER_TABLE_MARKER_NAME).append(",")
				.append(IMU_MARKER_TABLE_MARKER_DESCRIPTION).append(",")
				.append(IMU_MARKER_TABLE_START_TIME).append(",")
				.append(IMU_MARKER_TABLE_END_TIME).append(") values (?,?,?,?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(setType.toString());
			stmt.setString(1, marker.getName());
			stmt.setString(2, marker.getDescription());
			stmt.setDate(3, marker.getStart());
			stmt.setDate(4, marker.getEnd());
			stmt.execute();

			DatabaseMetaData dmd = conn.getMetaData();
			ResultSet results = dmd.getPrimaryKeys(conn.getCatalog(), null,
					IMU_MARKER_TABLE_NAME);
			if (results.next()) {
				marker.setId(results.getLong(5));
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

	private boolean updateJointSensorMapping(Marker marker,
			JointType jointType, int sensorId) {

		boolean ret = false;

		StringBuilder setType = new StringBuilder("update ")
				.append(IMU_JOINTMAPPING_TABLE_NAME).append(" set ")
				.append(IMU_JOINTMAPPING_TABLE_SENSOR_ID).append("=? ")
				.append(" where ").append(IMU_JOINTMAPPING_TABLE_MARKER_ID)
				.append("=? and ").append(IMU_JOINTMAPPING_TABLE_JOINT_ID)
				.append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(setType.toString());
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

		StringBuilder setType = new StringBuilder("insert into ")
				.append(IMU_JOINTMAPPING_TABLE_NAME).append(" (")
				.append(IMU_JOINTMAPPING_TABLE_MARKER_ID).append(",")
				.append(IMU_JOINTMAPPING_TABLE_SENSOR_ID).append(",")
				.append(IMU_JOINTMAPPING_TABLE_JOINT_ID).append(",")
				.append(") values (?,?,?)");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(setType.toString());
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

		StringBuilder setType = new StringBuilder("select ")
				.append(IMU_JOINTMAPPING_TABLE_SENSOR_ID).append(" from ")
				.append(IMU_JOINTMAPPING_TABLE_NAME).append(" where ")
				.append(IMU_JOINTMAPPING_TABLE_MARKER_ID).append("=? and ")
				.append(IMU_JOINTMAPPING_TABLE_JOINT_ID).append("=?");

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(setType.toString());
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

}
