package imuanalyzer.device;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * Hardware access implementation for accessing MARG sensors Reads and parses
 * data from MARG sensors via serial
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class MARGReader implements SerialPortEventListener, IMARGDataProvider {

	private static final boolean READ_GYRO_TEMP = true;

	private static final Logger LOGGER = Logger.getLogger(MARGReader.class
			.getName());

	private static final int BAUDRATE = 115200;
	private static final int TIMEOUT = 2000;

	private static final char COMMAND_DISABLE_DEBUG = 'd';
	private static final char COMMAND_ENABLE_CONTINOUS_MODE = 'C';
	private static final char COMMAND_DISABLE_CONTINOUS_MODE = 'c';
	private static final char COMMAND_CALIBRATION = 'K';

	private static final int NUMBER_OF_SEPARATED_VALUES = 9 + (READ_GYRO_TEMP ? 1
			: 0);

	private static final int MAX_ERROR_TRESHHOLD = 5;
	private static final int ERROR_TRESHHOLD_RING_LENGTH = 10;

	private int numberOfIMUs;

	private SerialPort serialPort;

	private InputStream inSerial;
	private OutputStream outSerial;
	private byte[] buffer = new byte[1024];

	private MARGEventManager eventManager = new MARGEventManager();

	private Boolean continousMode = true;

	private String portName;

	private SerialListener listener;

	ArrayList<GyroTempCorrection> tempCorrectFactors = new ArrayList<GyroTempCorrection>();

	protected ArrayList<IMARGReaderStatusListener> notifiers = new ArrayList<IMARGReaderStatusListener>();

	public MARGReader(String portName, int numberOfIMUs, Boolean continousMode)
			throws Exception {
		this.numberOfIMUs = numberOfIMUs;
		this.continousMode = continousMode;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					close();
				} catch (Exception e) {
					LOGGER.error(e.toString());
				}
			}
		});
		initTempCorrect();
	}

	/**
	 * Hardcoded temperature corrections valid for current painted sensor
	 * numbers
	 */
	private void initTempCorrect() {
		tempCorrectFactors.add(new GyroTempCorrection(new double[] { -1.597,
				-1.464, 3.325 }, new double[] { 40.387, 111.920, -85.487 }));
		tempCorrectFactors.add(new GyroTempCorrection(new double[] { -0.055,
				-0.033, 1.858 }, new double[] { -75.041, 27.825, -37.805 }));
		tempCorrectFactors.add(new GyroTempCorrection(new double[] { -0.296,
				0.782, 0.257 }, new double[] { -9.419, -1.178, 8.978 }));
		tempCorrectFactors.add(new GyroTempCorrection(new double[] { 3.437,
				2.252, -2.890 }, new double[] { -122.628, -123.062, 48.051 }));
	}

	private void init(String portName, Boolean continousMode) throws Exception,
			IOException {
		connect(portName);
		outSerial.write(COMMAND_DISABLE_DEBUG);
		if (continousMode) {
			// set continous mode
			outSerial.write(COMMAND_ENABLE_CONTINOUS_MODE);
		}
	}

	private void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);

		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),
					TIMEOUT);

			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(BAUDRATE, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				serialPort.setDTR(false);

				inSerial = serialPort.getInputStream();
				outSerial = serialPort.getOutputStream();

				// polling is more efficient in our case
				// serialPort.addEventListener(this);
				// serialPort.notifyOnDataAvailable(true);

				listener = new SerialListener();

				new Thread(listener).start();
			}
		}
	}

	float decodeFloatFromHex(String inString) {
		byte[] inData = new byte[4];

		inData[0] = (byte) Integer.parseInt(inString.substring(0, 2), 16);
		inData[1] = (byte) Integer.parseInt(inString.substring(2, 4), 16);
		inData[2] = (byte) Integer.parseInt(inString.substring(4, 6), 16);
		inData[3] = (byte) Integer.parseInt(inString.substring(6, 8), 16);

		int intbits = (inData[3] << 24) | ((inData[2] & 0xff) << 16)
				| ((inData[1] & 0xff) << 8) | (inData[0] & 0xff);

		return Float.intBitsToFloat(intbits);
	}

	int processCounter = 0;
	int errorCounter = 0;

	/**
	 * Read data from IMUs
	 */
	public void serialEvent(SerialPortEvent arg0) {
		readSerial();
	}

	protected void readSerial() {
		int data;

		try {
			int len = 0;
			while ((data = inSerial.read()) > -1) {
				if (data == '\n') {

					processCounter++;
					if (processCounter % ERROR_TRESHHOLD_RING_LENGTH == 0) {
						errorCounter = 0;
					}

					String transferedData = new String(buffer, 0, len);
					StringTokenizer tokenizer = new StringTokenizer(
							transferedData, ";");
					// System.out.println(transferedData);

					ArrayList<String> tokenList = new ArrayList<String>();
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						if (token.equals("ERR")) {
							errorCounter++;
							if (errorCounter > MAX_ERROR_TRESHHOLD) {
								String message = "The device is reporting many transfer errors - the connection is closed. Please check the hardware!";
								for (IMARGReaderStatusListener notifier : notifiers) {
									notifier.notifyImuReaderError(message);
								}
							} else {
								LOGGER.debug("Transfer error #: "
										+ errorCounter);
							}
							break;
						}
						tokenList.add(token);
					}

					if (tokenList.size() >= ((NUMBER_OF_SEPARATED_VALUES
							* numberOfIMUs + 1))) {
						MARGRawData[] imuData = new MARGRawData[numberOfIMUs];
						for (int i = 0; i < numberOfIMUs; i++) {
							int tokenIndex = i * NUMBER_OF_SEPARATED_VALUES;
							try {
								MARGRawData current = new MARGRawData();

								imuData[i] = current;

								int index = 0;

								current.setId(i);

								Long bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getAccelerometer().x = bits.intValue();
								index++;
								bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getAccelerometer().y = bits.intValue();
								index++;
								bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getAccelerometer().z = bits.intValue();
								index++;

								bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getGyroskope().x = bits.intValue();
								index++;
								bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getGyroskope().y = bits.intValue();
								index++;
								bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getGyroskope().z = bits.intValue();
								index++;

								if (READ_GYRO_TEMP) {
									bits = Long.valueOf(
											tokenList.get(tokenIndex + index),
											16);
									current.setRawTemp(bits.intValue());
									index++;
									// correct by temp
									tempCorrectFactors.get(i).correctGyro(
											current);
								}

								bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getMagnetometer().x = bits.intValue();
								index++;
								bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getMagnetometer().y = bits.intValue();
								index++;
								bits = Long.valueOf(
										tokenList.get(tokenIndex + index), 16);
								current.getMagnetometer().z = bits.intValue();
							} catch (NumberFormatException e) {
								LOGGER.error(e);
								break; // skip current data line
							}
						}
						String samplePeriodToken = tokenList
								.get(NUMBER_OF_SEPARATED_VALUES * numberOfIMUs);
						samplePeriodToken = samplePeriodToken.substring(0,
								samplePeriodToken.length() - 1); // remove \n
						Long bits = Long.valueOf(samplePeriodToken, 16);
						// convert from mikroseconds to seconds
						double samplePeriod = (bits.intValue() / 1000000.0);

						eventManager.fireEvent(new MARGEvent(imuData,
								samplePeriod));

					} else {
						LOGGER.warn("Incomplete command: \n" + transferedData);
						// Ignore incomplete or wrong transfered data
					}
					break;
				}
				buffer[len++] = (byte) data;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void close() {
		// disable continous mode
		try {
			if (portName != null) {
				if (listener != null) {
					listener.stopListener();
				}
				outSerial.write(COMMAND_DISABLE_CONTINOUS_MODE);
				serialPort.removeEventListener();
				serialPort.close();
				portName = null;
			}
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	public int getNumberOfIMUs() {
		return numberOfIMUs;
	}

	/**
	 * Start device calibration
	 */
	public void calibrate() {
		try {
			if (outSerial != null) {
				outSerial.write(COMMAND_DISABLE_CONTINOUS_MODE);
				outSerial.write(COMMAND_CALIBRATION);
				outSerial.write(COMMAND_ENABLE_CONTINOUS_MODE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MARGEventManager getEventManager() {
		return eventManager;
	}

	public void connectToPort(String portName) throws Exception {
		close();
		this.portName = portName;

		init(portName, continousMode);
	}

	public String getPortName() {
		return portName;
	}

	@Override
	public boolean isConnected() {
		return (portName != null);
	}

	@Override
	public void registerStatusListener(IMARGReaderStatusListener notifier) {
		if (!notifiers.contains(notifier)) {
			notifiers.add(notifier);
		}
	}

	@Override
	public void deregisterStatusListener(IMARGReaderStatusListener notifier) {
		notifiers.remove(notifier);
	}

	/**
	 * Worker thread for polling the serial line
	 * 
	 * @author Christopher-Eyk Hrabia
	 * 
	 */
	private class SerialListener implements Runnable {

		boolean stop = false;

		@Override
		public void run() {
			while (!stop) {
				readSerial();
			}
		}

		public synchronized void stopListener() {
			stop = true;
		}

	}

}
