package imuanalyzer.ui;

import imuanalyzer.data.Database;
import imuanalyzer.data.DatasetMetadata;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.Analysis;
import imuanalyzer.signalprocessing.Analysis.AnalysesMode;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.JointAngleCollector;
import imuanalyzer.utils.math.LinearRegression;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Before;
import org.junit.Test;

public class MarkerControlTest {

	Database db;

	@Before
	public void setUp() throws Exception {
		db = Database.getInstance();
	}

	protected int getMaxDataLength(ArrayList<DatasetMetadata> selectedMarkers) {
		int maxCount = 0;
		for (DatasetMetadata m : selectedMarkers) {
			maxCount = Math.max(maxCount, db.getCount(m));
		}
		return maxCount;
	}

	@Test
	public void automaticMassRegressionTest() {
		System.out.println("TOP and MID Test");

		final String[] fingers = new String[5];
		fingers[0] = "L";
		fingers[1] = "R";
		fingers[2] = "M";
		fingers[3] = "I";
		fingers[4] = "T";

		final JointType[] fingersTyp1 = new JointType[5];
		fingersTyp1[0] = JointType.LITTLE_MID;
		fingersTyp1[1] = JointType.RING_MID;
		fingersTyp1[2] = JointType.MIDDLE_MID;
		fingersTyp1[3] = JointType.INDEX_MID;
		fingersTyp1[4] = JointType.THUMB_MID;

		final JointType[] fingersTyp2 = new JointType[5];
		fingersTyp2[0] = JointType.LITTLE_TOP;
		fingersTyp2[1] = JointType.RING_TOP;
		fingersTyp2[2] = JointType.MIDDLE_TOP;
		fingersTyp2[3] = JointType.INDEX_TOP;
		fingersTyp2[4] = JointType.THUMB_TOP;

		final String[] hands = new String[2];
		hands[0] = "L";
		hands[1] = "R";

		ArrayList<String> names = new ArrayList<String>();
		names.add("chris");
		names.add("baerbel");
		names.add("frank");
		names.add("anne");
		names.add("gerriet");
		names.add("daniel");
		names.add("sonny");
		names.add("katrin");
		names.add("renate");
		names.add("hardy");
		names.add("Christian");
		names.add("Mathias");
		names.add("Victor");
		names.add("Ly");
		names.add("Sascha");
		names.add("Juliane");
		names.add("Thomas");
		names.add("Sabine");
		names.add("Frank");

		for (int n = 0; n < names.size(); n++) {

			for (int f = 0; f < fingers.length; f++) {

				for (int h = 0; h < hands.length; h++) {

					String searchRegexStr = names.get(n) + " " + hands[h] + " "
							+ fingers[f] + ".*";

					int numberOfItemsToSum = 4;

					calcAllRegression(names.get(n), hands[h], fingers[f],
							fingersTyp1[f], fingersTyp2[f], searchRegexStr,
							numberOfItemsToSum);
				}

			}
		}
	}
	
	@Test
	public void automaticMassRegressionTest2() {
		
		System.out.println("Bottom and MID Test");

		final String[] fingers = new String[5];
		fingers[0] = "L";
		fingers[1] = "R";
		fingers[2] = "M";
		fingers[3] = "I";
		fingers[4] = "T";

		final JointType[] fingersTyp1 = new JointType[5];
		fingersTyp1[0] = JointType.LITTLE_MID;
		fingersTyp1[1] = JointType.RING_MID;
		fingersTyp1[2] = JointType.MIDDLE_MID;
		fingersTyp1[3] = JointType.INDEX_MID;
		fingersTyp1[4] = JointType.THUMB_MID;

		final JointType[] fingersTyp2 = new JointType[5];
		fingersTyp2[0] = JointType.LITTLE_BOTTOM;
		fingersTyp2[1] = JointType.RING_BOTTOM;
		fingersTyp2[2] = JointType.MIDDLE_BOTTOM;
		fingersTyp2[3] = JointType.INDEX_BOTTOM;
		fingersTyp2[4] = JointType.THUMB_BOTTOM;

		final String[] hands = new String[2];
		hands[0] = "L";
		hands[1] = "R";

		ArrayList<String> names = new ArrayList<String>();
		names.add("chris");
		names.add("baerbel");
		names.add("frank");
		names.add("anne");
		names.add("gerriet");
		names.add("daniel");
		names.add("sonny");
		names.add("katrin");
		names.add("renate");
		names.add("hardy");
		names.add("Christian");
		names.add("Mathias");
		names.add("Victor");
		names.add("Ly");
		names.add("Sascha");
		names.add("Juliane");
		names.add("Thomas");
		names.add("Sabine");
		names.add("Frank");

		for (int n = 0; n < names.size(); n++) {

			for (int f = 0; f < fingers.length; f++) {

				for (int h = 0; h < hands.length; h++) {

					String searchRegexStr = names.get(n) + " " + hands[h] + " "
							+ fingers[f] + ".*";

					int numberOfItemsToSum = 4;

					calcAllRegression(names.get(n), hands[h], fingers[f],
							fingersTyp1[f], fingersTyp2[f], searchRegexStr,
							numberOfItemsToSum);
				}

			}
		}
	}

	// @Test
	public void calcSingle() {
		calcAllRegression("chris", "L", "I", JointType.INDEX_MID,
				JointType.INDEX_TOP, "chris L I .*", 4);
		calcAllRegression("katrin", "R", "I", JointType.INDEX_MID,
				JointType.INDEX_TOP, "katrin R I .*", 4);
		calcAllRegression("renate", "L", "R", JointType.RING_MID,
				JointType.RING_TOP, "renate L R .*", 4);
		calcAllRegression("hardy", "L", "R", JointType.RING_MID,
				JointType.RING_TOP, "hardy L R .*", 4);
		calcAllRegression("hardy", "L", "M", JointType.MIDDLE_MID,
				JointType.MIDDLE_TOP, "hardy L M .*", 4);
		calcAllRegression("hardy", "L", "I", JointType.INDEX_MID,
				JointType.INDEX_TOP, "hardy L I .*", 4);
		calcAllRegression("Christian", "R", "M", JointType.MIDDLE_MID,
				JointType.MIDDLE_TOP, "Christian R M .*", 4);
		calcAllRegression("Sascha", "R", "M", JointType.MIDDLE_MID,
				JointType.MIDDLE_TOP, "Sascha R M .*", 4);
		calcAllRegression("Sascha", "L", "I", JointType.INDEX_MID,
				JointType.INDEX_TOP, "Sascha L I .*", 4);
		calcAllRegression("Juliane", "L", "R", JointType.RING_MID,
				JointType.RING_TOP, "Juliane L R .*", 4);
		calcAllRegression("Juliane", "R", "M", JointType.MIDDLE_MID,
				JointType.MIDDLE_TOP, "Juliane R M .*", 4);
		calcAllRegression("Frank", "L", "R", JointType.RING_MID,
				JointType.RING_TOP, "Frank L R .*", 4);
		calcAllRegression("Frank", "L", "T", JointType.THUMB_MID,
				JointType.THUMB_TOP, "Frank L T .*", 4);
		calcAllRegression("Frank", "R", "R", JointType.RING_MID,
				JointType.RING_TOP, "Frank R R .*", 4);
	}

	protected void calcAllRegression(String name, String hand, String finger,
			JointType type1, JointType type2, String searchRegexStr,
			int numberOfItemsToSum) {
		ArrayList<DatasetMetadata> availableMarker = db.getAvailableMarkers();

		ArrayList<ArrayList<DatasetMetadata>> proceedItems = new ArrayList<ArrayList<DatasetMetadata>>();

		proceedItems.add(new ArrayList<DatasetMetadata>());

		// sort matching datasets
		for (DatasetMetadata m : availableMarker) {
			if (m.getName().matches(searchRegexStr)) {
				ArrayList<DatasetMetadata> current = proceedItems.get(proceedItems
						.size() - 1);
				if (current.size() >= numberOfItemsToSum) {
					current = new ArrayList<DatasetMetadata>();
					proceedItems.add(current);
				}
				current.add(m);
			}
		}

		for (ArrayList<DatasetMetadata> list : proceedItems) {
			for (DatasetMetadata single : list) {
				ArrayList<DatasetMetadata> singleList = new ArrayList<DatasetMetadata>(1);
				singleList.add(single);
				calculateRegression(single.getName(), type1, type2, singleList);
			}
			calculateRegression(name + " " + hand + " " + finger + " Sum;",
					type1, type2, list);
		}
	}

	protected void calculateRegression(String name, JointType type1,
			JointType type2, ArrayList<DatasetMetadata> markerList) {
		Analysis newAnalysis = new Analysis(null);

		Hand hand = new Hand(null, DatasetMetadata.getDefaultMarker());

		int numValues = getMaxDataLength(markerList);

		if (numValues < 1) {
			System.out.println("No data in current dataset:" + name);
			return;
		}

		JointAngleCollector collector = new JointAngleCollector("", hand,
				type1, type2, numValues);

		newAnalysis.calculate(AnalysesMode.WITHOUTPOSTPROCCESIG, markerList,
				FilterTypes.CF_MAHONY_MAGNETIC_DISTORSION, new ArrayList<Hand.JointType>(1),
				new ArrayList<Hand.JointType>(1), new ArrayList<Float>(1),
				collector, false, false, false);

		for (ArrayBlockingQueue<double[]> collecItem : collector.getValues()) {

			LinearRegression regression = new LinearRegression(collecItem);

			double m = regression.getBeta1();

			double r2 = regression.getR2();

			System.out.println(name + ";" + String.format("%.3f", m) + ";"
					+ String.format("%.3f", r2));

		}
	}

	@Test
	public void automaticMassAngleTest() {

		final String[] fingers = new String[5];
		fingers[0] = "L";
		fingers[1] = "R";
		fingers[2] = "M";
		fingers[3] = "I";
		fingers[4] = "T";

		final JointType[] fingersTyp1 = new JointType[5];
		fingersTyp1[0] = JointType.LITTLE_MID;
		fingersTyp1[1] = JointType.RING_MID;
		fingersTyp1[2] = JointType.MIDDLE_MID;
		fingersTyp1[3] = JointType.INDEX_MID;
		fingersTyp1[4] = JointType.THUMB_MID;

		final JointType[] fingersTyp2 = new JointType[5];
		fingersTyp2[0] = JointType.LITTLE_TOP;
		fingersTyp2[1] = JointType.RING_TOP;
		fingersTyp2[2] = JointType.MIDDLE_TOP;
		fingersTyp2[3] = JointType.INDEX_TOP;
		fingersTyp2[4] = JointType.THUMB_TOP;

		final String[] hands = new String[2];
		hands[0] = "L";
		hands[1] = "R";

		ArrayList<String> names = new ArrayList<String>();
		names.add("chris");
		names.add("baerbel");
		names.add("frank");
		names.add("anne");
		names.add("gerriet");
		names.add("daniel");
		names.add("sonny");
		names.add("katrin");
		names.add("renate");
		names.add("hardy");
		names.add("Christian");
		names.add("Mathias");
		names.add("Victor");
		names.add("Ly");
		names.add("Sascha");
		names.add("Juliane");
		names.add("Thomas");
		names.add("Sabine");
		names.add("Frank");

		try {
			// Create file
			FileWriter fstream = new FileWriter("outAngles.txt");
			BufferedWriter out = new BufferedWriter(fstream);

			for (int n = 0; n < names.size(); n++) {

				for (int f = 0; f < fingers.length; f++) {

					for (int h = 0; h < hands.length; h++) {

						String searchRegexStr = names.get(n) + " " + hands[h]
								+ " " + fingers[f] + ".*";

						int numberOfItemsToSum = 4;

						calcAllAngles(out, n + 1, names.get(n), hands[h],
								fingers[f], fingersTyp1[f], fingersTyp2[f],
								searchRegexStr, numberOfItemsToSum);
					}

				}
				System.out.println("Finished: " + names.get(n));
			}
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	protected void calcAllAngles(BufferedWriter out, int propandNr,
			String name, String hand, String finger, JointType type1,
			JointType type2, String searchRegexStr, int numberOfItemsToSum)
			throws IOException {
		ArrayList<DatasetMetadata> availableMarker = db.getAvailableMarkers();

		ArrayList<ArrayList<DatasetMetadata>> proceedItems = new ArrayList<ArrayList<DatasetMetadata>>();

		proceedItems.add(new ArrayList<DatasetMetadata>());

		// sort matching datasets
		for (DatasetMetadata m : availableMarker) {
			if (m.getName().matches(searchRegexStr)) {
				ArrayList<DatasetMetadata> current = proceedItems.get(proceedItems
						.size() - 1);
				if (current.size() >= numberOfItemsToSum) {
					current = new ArrayList<DatasetMetadata>();
					proceedItems.add(current);
				}
				current.add(m);
			}
		}

		for (ArrayList<DatasetMetadata> list : proceedItems) {
			for (DatasetMetadata single : list) {
				ArrayList<DatasetMetadata> singleList = new ArrayList<DatasetMetadata>(1);
				singleList.add(single);
				calculateAngles(out, "" + propandNr + ";" + single.getName(),
						type1, type2, singleList);
			}
		}
	}

	protected void calculateAngles(BufferedWriter out, String name,
			JointType type1, JointType type2, ArrayList<DatasetMetadata> markerList)
			throws IOException {
		Analysis newAnalysis = new Analysis(null);

		Hand hand = new Hand(null, DatasetMetadata.getDefaultMarker());

		int numValues = getMaxDataLength(markerList);

		if (numValues < 1) {
			// System.out.println("No data in current dataset:" + name);
			return;
		}

		JointAngleCollector collector = new JointAngleCollector("", hand,
				type1, type2, numValues);

		newAnalysis.calculate(AnalysesMode.WITHOUTPOSTPROCCESIG, markerList,
				FilterTypes.CF_MAHONY_MAGNETIC_DISTORSION, new ArrayList<Hand.JointType>(1),
				new ArrayList<Hand.JointType>(1), new ArrayList<Float>(1),
				collector, false, false, false);

		for (ArrayBlockingQueue<double[]> collecItem : collector.getValues()) {

			for (double[] xy : collecItem) {
				out.append(name + ";" + xy[0] + ";" + xy[1] + ";" + xy[1]
						/ xy[0] + "\n");
			}

		}
	}
}
