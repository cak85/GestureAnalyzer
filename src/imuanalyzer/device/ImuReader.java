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

public class ImuReader implements SerialPortEventListener,IIMUDataProvider {
	
	private static final Logger LOGGER = Logger
	.getLogger(ImuReader.class.getName());

	private static final int BAUDRATE = 115200;
	private static final int TIMEOUT = 2000;

	private static final char COMMAND_DISABLE_DEBUG = 'd';
	private static final char COMMAND_ENABLE_CONTINOUS_MODE = 'C';
	private static final char COMMAND_DISABLE_CONTINOUS_MODE = 'c';
	private static final char COMMAND_CALIBRATION = 'K';

	private int numberOfIMUs;

	private SerialPort serialPort;

	private InputStream inSerial;
	private OutputStream outSerial;
	private byte[] buffer = new byte[1024];

	private ImuEventManager eventManager = new ImuEventManager();

	private Boolean continousMode = true;

	private String portName;

	public ImuReader(String portName, int numberOfIMUs, Boolean continousMode)
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

				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);

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

	/**
	 * Read data from IMUs
	 */
	public void serialEvent(SerialPortEvent arg0) {
		int data;

		try {
			int len = 0;
			while ((data = inSerial.read()) > -1) {
				if (data == '\n') {
					String transferedData = new String(buffer, 0, len);
					StringTokenizer tokenizer = new StringTokenizer(
							transferedData, ";");

					ArrayList<String> tokenList = new ArrayList<String>();
					while (tokenizer.hasMoreTokens()) {
						tokenList.add(tokenizer.nextToken());
					}

					if (tokenList.size() >= ((9 * numberOfIMUs) + 1)) {
						ImuRawData[] imuData = new ImuRawData[numberOfIMUs];
						for (int i = 0; i < numberOfIMUs; i++) {
							int tokenIndex = i * 9;
							try {
								ImuRawData current = new ImuRawData();

								imuData[i] = current;

								current.setId(i);
								Long bits = Long.parseLong(
										tokenList.get(tokenIndex), 16);
								current.getAccelerometer().x = bits.intValue();
								bits = Long.parseLong(
										tokenList.get(tokenIndex + 1), 16);
								current.getAccelerometer().y = bits.intValue();
								bits = Long.parseLong(
										tokenList.get(tokenIndex + 2), 16);
								current.getAccelerometer().z = bits.intValue();

								current.getGyroskope().x = decodeFloatFromHex(tokenList
										.get(tokenIndex + 3));
								current.getGyroskope().y = decodeFloatFromHex(tokenList
										.get(tokenIndex + 4));
								current.getGyroskope().z = decodeFloatFromHex(tokenList
										.get(tokenIndex + 5));

								current.getMagnetometer().x = decodeFloatFromHex(tokenList
										.get(tokenIndex + 6));
								current.getMagnetometer().y = decodeFloatFromHex(tokenList
										.get(tokenIndex + 7));
								current.getMagnetometer().z = decodeFloatFromHex(tokenList
										.get(tokenIndex + 8));

							} catch (NumberFormatException e) {
								LOGGER.error(e);
								break; // skip current data line
							}
						}

						eventManager.fireEvent(new ImuEvent(imuData));

					} else {
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

	public ImuEventManager getEventManager() {
		return eventManager;
	}

	public void setPortName(String portName) throws Exception {
		close();
		this.portName = portName;

		init(portName, continousMode);
	}

	public String getPortName() {
		return portName;
	}

}
