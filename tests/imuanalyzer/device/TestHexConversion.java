package imuanalyzer.device;

import org.junit.Before;
import org.junit.Test;

public class TestHexConversion {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHexConversion() {
		String samplePeriodToken = "7090";
		Long bits = Long.valueOf(samplePeriodToken, 16);

		int value = bits.intValue();

		System.out.println("Samp Mikro: " + value);
		System.out.println("Samp MikroHex: " + Integer.toHexString(value));

		double samplePeriod = (bits.intValue() / 1000000.0);
		System.out.println("Samp Sek: " + samplePeriod);
	}
}
