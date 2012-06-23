package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.swing.OrientationChartFrame;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Creates and manages non dynamic charts
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

	public NonDynamicChartFiller(OrientationChartManager _chartOrientation,
			AccelerationChartManager _chartsAcceleration,
			FeelingChartManager _feelingChart,
			JointRelationChartManager _chartsRelation, int nrHands, int maxData) {
		this.nrHands = nrHands;
		chartOrientation = _chartOrientation;
		chartsAcceleration = _chartsAcceleration;
		feelingChart = _feelingChart;
		chartsRelation = _chartsRelation;

		//relations
		for (JointRelationChartFrame chart : chartsRelation.getRelations()) {
			staticRelationCharts.add(chartsRelation.getStaticChart(chart.type1,
					chart.type2, maxData));
		}
		
		//orientations
		Set<Entry<JointType, OrientationChartFrame>> orientationCharts = chartOrientation
				.getCharts().entrySet();
		for (Entry<JointType, OrientationChartFrame> chart : orientationCharts) {
			staticOrientationCharts.add(chartOrientation.getStaticChart(
					chart.getKey(), orientationCharts.size(), maxData));
		}
		// TODO same for other chart types
	}

	public void update(Hand hand, int idx, Double sumSamplePeriod) {

		if (sumSamplePeriod % JointRelationChartManager.UPDATE_CYCLE < 10)
			for (JointRelationChartFrame chart : staticRelationCharts) {
				chart.setHand(hand);
				chart.update();
			}
	}
}
