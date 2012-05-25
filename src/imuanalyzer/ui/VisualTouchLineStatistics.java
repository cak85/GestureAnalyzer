package imuanalyzer.ui;

import imuanalyzer.signalprocessing.TouchLine;
import imuanalyzer.signalprocessing.TouchLineStatistics;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

public class VisualTouchLineStatistics extends Node {

	private static final Logger LOGGER = Logger
			.getLogger(VisualTouchLineStatistics.class.getName());

	ArrayList<TouchLineStatistics> statistics;

	Geometry line = null;
	Geometry box = null;
	Geometry extrema = null;
	Geometry median = null;

	ColorRGBA mainColor = ColorRGBA.Red;
	ColorRGBA medianColor = ColorRGBA.Black;

	public VisualTouchLineStatistics(AssetManager assetManager) {
		ArrayList<Vector3f> lineBuffer = new ArrayList<Vector3f>();

		line = Utils.CreateLine(assetManager, lineBuffer, mainColor, false, 1);
		box = Utils.CreateLine(assetManager, lineBuffer,
				ColorRGBA.Green, false, 4);
		median = Utils.CreateLine(assetManager, lineBuffer, medianColor, false,
				10);
		
		extrema= Utils.CreateLine(assetManager, lineBuffer,
				ColorRGBA.Blue, false, 20);

		this.attachChild(extrema);
		this.attachChild(line);
		this.attachChild(box);
		this.attachChild(median);
	}

	private void updateData() {
		ArrayList<TouchLine> lines = new ArrayList<TouchLine>();
		ArrayList<ArrayList<Vector3f>> medians = new ArrayList<ArrayList<Vector3f>>();
		ArrayList<ArrayList<Vector3f>> boxes = new ArrayList<ArrayList<Vector3f>>();
		ArrayList<ArrayList<Vector3f>> extremaPoints = new ArrayList<ArrayList<Vector3f>>();
		for (TouchLineStatistics t : statistics) {
			TouchLine maxLine = t.getMaxObj();
			lines.add(maxLine);
			ArrayList<Vector3f> maxlineBuffer = maxLine.getLineBuffer();
			if (maxlineBuffer.size() > 5) { // arbitrary number ....TODO

				// /MEDIAN
				medians.add(getLineSegment(maxlineBuffer, t.getMedian()));
				LOGGER.debug("Median: "+t.getMedian());
				// Extrema
				extremaPoints.add(getLineSegment(maxlineBuffer, t.getMax()));
				extremaPoints.add(getLineSegment(maxlineBuffer, t.getMin()));
				LOGGER.debug("Extrema low: "+t.getMin());
				LOGGER.debug("Extrema high: "+t.getMax());
				
				LOGGER.debug("Maxline length " + maxLine.getLength());

				// / BOX
				Vector3f pos1 = new Vector3f();
				Vector3f pos2 = new Vector3f();
				int boxBegin = getPosFromLength(maxlineBuffer,
						t.getLowerQuantile(), pos1);
				int boxEnd = getPosFromLength(maxlineBuffer,
						t.getUpperQuantile(), pos2);
				LOGGER.debug("Quantile low: "+t.getLowerQuantile());
				LOGGER.debug("Quantile high: "+t.getUpperQuantile());

				ArrayList<Vector3f> boxLine = new ArrayList<Vector3f>();
				boxLine.add(pos1);

				for (int i = boxBegin + 1; i < boxEnd; i++) {
					boxLine.add(maxlineBuffer.get(i));
				}

				boxLine.add(pos2);

				boxes.add(boxLine);
			}

		}
		System.out.println("Box " + boxes);
		System.out.println("Extrema " + extremaPoints);
		System.out.println("Median " + medians);
		
		Utils.updateLinesTouch(line, lines);
		Utils.updateLines(box, boxes);
		Utils.updateLines(median, medians);
		Utils.updateLines(extrema, extremaPoints);

	}

	/**
	 * paint a line segment of 2 points around length point position
	 */
	private ArrayList<Vector3f> getLineSegment(ArrayList<Vector3f> lineBuffer,
			float lengthPosition) {
		Vector3f segmentStart = new Vector3f();
		int idx = getPosFromLength(lineBuffer, lengthPosition, segmentStart);

		Vector3f segmentEnd;
		if (idx < (lineBuffer.size() - 1)) {
			segmentEnd = segmentStart.add(lineBuffer.get(idx + 1)
					.subtract(segmentStart).normalize().mult(0.1f));
		} else {
			segmentEnd = segmentStart;
			segmentStart = segmentStart.add(lineBuffer.get(idx-1)
					.subtract(segmentStart).normalize().mult(0.1f));
		}

		ArrayList<Vector3f> segmentLine = new ArrayList<Vector3f>();
		segmentLine.add(segmentStart);
		segmentLine.add(segmentEnd);

		return segmentLine;
	}

	/**
	 * Calculate idx from length position in vector line
	 * 
	 * @param lineBuffer
	 * @param length
	 * @return
	 */
	private int getPosFromLength(ArrayList<Vector3f> lineBuffer, float length,
			Vector3f pos) {
		int idx = 0;

		float measuredLength = 0;

		pos.set(lineBuffer.get(0));
		for (idx = 1; idx < lineBuffer.size(); idx++) {
			Vector3f diff = lineBuffer.get(idx).subtract(
					lineBuffer.get(idx - 1));
			float addLength = diff.length();
			measuredLength += addLength;
			if (measuredLength >length) {
				pos = lineBuffer.get(idx);
//				pos.set(pos.subtract(diff.normalize().mult(
//						measuredLength - length)));

				idx = idx - 1;
				break;
			}
		}
		
		if(idx==lineBuffer.size()){
			idx--;
			pos.set(lineBuffer.get(idx));
		}

		return idx;
	}

	public ArrayList<TouchLineStatistics> getStatistics() {
		return statistics;
	}

	public void setStatistics(ArrayList<TouchLineStatistics> statistics) {
		this.statistics = statistics;
		updateData();
	}

}
