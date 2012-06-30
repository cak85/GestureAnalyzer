package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Creates and manages non dynamic charts for analysis chart output
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class NonDynamicChartFiller {

	protected OrientationChartManager chartOrientation;
	protected AccelerationChartManager chartsAcceleration;
	protected FeelingChartManager feelingChart;
	protected JointRelationChartManager chartsRelation;
	int nrHands;

	ArrayList<JointRelationChartFrame> staticRelationCharts = new ArrayList<JointRelationChartFrame>();
	ArrayList<OrientationChartFrame> staticOrientationCharts = new ArrayList<OrientationChartFrame>();
	ArrayList<AccelerationChartFrame> staticAccelerationCharts = new ArrayList<AccelerationChartFrame>();
	FeelingChartFrame staticFeelingChart = null;

	public NonDynamicChartFiller(OrientationChartManager _chartOrientation,
			AccelerationChartManager _chartsAcceleration,
			FeelingChartManager _feelingChart,
			JointRelationChartManager _chartsRelation, int nrHands, int maxData) {
		
		this.nrHands = nrHands;
		chartOrientation = _chartOrientation;
		chartsAcceleration = _chartsAcceleration;
		feelingChart = _feelingChart;
		chartsRelation = _chartsRelation;

		// relations
		for (JointRelationChartFrame chart : chartsRelation.getCharts()) {
			JointRelationChartFrame frame = chartsRelation.getStaticChart(
					chart.type1, chart.type2, maxData);
			staticRelationCharts.add(frame);
			frame.setVisible(true);
		}

		// orientations
		Set<Entry<JointType, OrientationChartFrame>> orientationCharts = chartOrientation
				.getCharts().entrySet();
		for (Entry<JointType, OrientationChartFrame> chart : orientationCharts) {
			OrientationChartFrame frame = chartOrientation.getStaticChart(
					chart.getKey(), orientationCharts.size(), maxData);
			frame.setVisible(true);
			staticOrientationCharts.add(frame);
		}
		
		// acceleration
		for (AccelerationChartFrame chart : chartsAcceleration.getCharts()) {
			AccelerationChartFrame frame = chartsAcceleration.getStaticChart(
					chart.getType(), maxData);
			frame.setVisible(true);
			staticAccelerationCharts.add(frame);
		}
		
		// feeling
		if (feelingChart.isEnabled()) {
			staticFeelingChart = feelingChart.getStaticChart(maxData);
			staticFeelingChart.setVisible(true);
		}
	}

	public void update(Hand hand, int idx, Double sumSamplePeriod) {

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
}
