package imuanalyzer.device;


import org.junit.Before;
import org.junit.Test;

public class ImuReaderHexParseTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test 
	public void testParsing(){
		System.out.println(Long.valueOf("FFFFFF8E",16).intValue());
		System.out.println(Long.valueOf("8E",16).intValue());
	}

}
