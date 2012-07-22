package imuanalyzer.ui.swing.charts;

import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.IAnalysisExtension;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.IBoxplotData;
import imuanalyzer.signalprocessing.RelationStatistics;
import imuanalyzer.ui.Boxplot2d;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Set;

import javax.swing.JFrame;

/**
 * Creates and manages non dynamic charts for analysis chart output
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class NonDynamicChartFiller implements IAnalysisExtension {

	protected OrientationChartManager chartOrientation;
	protected AccelerationChartManager chartsAcceleration;
	protected FeelingChartManager feelingChart;
	protected JointRelationChartManager chartsRelation;
	int nrHands;

	ArrayList<JointRelationChartFrame> staticRelationCharts = new ArrayList<JointRelationChartFrame>();
	ArrayList<OrientationChartFrame> staticOrientationCharts = new ArrayList<OrientationChartFrame>();
	ArrayList<AccelerationChartFrame> staticAccelerationCharts = new ArrayList<AccelerationChartFrame>();
	FeelingChartFrame staticFeelingChart = null;

	boolean showRelationBoxplot;

	public NonDynamicChartFiller(OrientationChartManager _chartOrientation,
			AccelerationChartManager _chartsAcceleration,
			FeelingChartManager _feelingChart,
			JointRelationChartManager _chartsRelation,
			ArrayList<Marker> selectedMarkers, int maxData,
			boolean showRelationBoxplot) {

		this.nrHands = selectedMarkers.size();
		chartOrientation = _chartOrientation;
		chartsAcceleration = _chartsAcceleration;
		feelingChart = _feelingChart;
		chartsRelation = _chartsRelation;
		this.showRelationBoxplot = showRelationBoxplot;

		// relations
		int i = 0;
		for (JointRelationChartFrame chart : chartsRelation.getCharts()) {
			JointRelationChartFrame frame = chartsRelation.getStaticChart(
					selectedMarkers.get(i).getName(), chart.type1, chart.type2,
					maxData);
			staticRelationCharts.add(frame);
			i++;
		}

		// orientations
		Set<Entry<JointType, OrientationChartFrame>> orientationCharts = chartOrientation
				.getCharts().entrySet();
		for (Entry<JointType, OrientationChartFrame> chart : orientationCharts) {
			OrientationChartFrame frame = chartOrientation.getStaticChart(
					chart.getKey(), orientationCharts.size(), maxData);
			staticOrientationCharts.add(frame);
		}

		// acceleration
		for (AccelerationChartFrame chart : chartsAcceleration.getCharts()) {
			AccelerationChartFrame frame = chartsAcceleration.getStaticChart(
					chart.getType(), maxData);
			staticAccelerationCharts.add(frame);
		}

		// feeling
		if (feelingChart.isEnabled()) {
			staticFeelingChart = feelingChart.getStaticChart(maxData);
		}

	}

	public synchronized void update(Hand hand, int idx, Double sumSamplePeriod) {

		if (sumSamplePeriod % JointRelationChartManager.UPDATE_CYCLE < 10) {
			for (JointRelationChartFrame chart : staticRelationCharts) {
				chart.setHand(hand);
				chart.update();
			}
		}
		if (sumSamplePeriod % OrientationChartManager.UPDATE_CYCLE < 10) {
			for (OrientationChartFrame chart : staticOrientationCharts) {
				chart.setHand(hand);
				chart.update(idx);
			}
		}
		if (sumSamplePeriod % AccelerationChartManager.UPDATE_CYCLE < 10) {
			for (AccelerationChartFrame chart : staticAccelerationCharts) {
				chart.setHand(hand);
				chart.update();
			}
		}
		if (staticFeelingChart != null) {
			staticFeelingChart.setHand(hand);
			staticFeelingChart.update();
		}
	}

	@Override
	public void finished() {

		// set visiblity of frames

		// relations
		for (JointRelationChartFrame frame : staticRelationCharts) {
			frame.updateRegression();
			frame.setVisible(true);
		}

		// orientations
		for (JFrame frame : staticOrientationCharts) {
			frame.setVisible(true);
		}

		// acceleration
		for (JFrame frame : staticAccelerationCharts) {
			frame.setVisible(true);
		}
		// feeling
		if (feelingChart.isEnabled()) {
			staticFeelingChart.setVisible(true);
		}

		if (showRelationBoxplot) {
			// calculate boxplot data
			ArrayList<IBoxplotData> boxplotDataSets = new ArrayList<IBoxplotData>();

			for (JointRelationChartFrame chart : staticRelationCharts) {
				int i = 1;
				for (ArrayBlockingQueue<double[]> line : chart.getValues()) {
					ArrayList<Float> elements = new ArrayList<Float>();

					for (double[] touple : line) {
						elements.add((float) (touple[0] / touple[1]));
					}

					RelationStatistics stat = new RelationStatistics(
							chart.getName() + i, elements,
							new ArrayList<Float>());
					boxplotDataSets.add(stat);
					i++;
				}
			}

			// show boxplot
			new Boxplot2d("Joint Relations", boxplotDataSets);

		}
	}
}
