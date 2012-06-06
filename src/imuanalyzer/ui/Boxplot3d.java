package imuanalyzer.ui;

import imuanalyzer.signalprocessing.IBoxplotData;
import imuanalyzer.signalprocessing.VectorLine;
import imuanalyzer.signalprocessing.VectorLineStatistics;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

public class Boxplot3d extends Node {

	private static final Logger LOGGER = Logger.getLogger(Boxplot3d.class
			.getName());

	private static final float SEGMENTHEIGTH = 0.05f;
	private static final float SEGMENTRADIUS = 0.2f;
	private static final float SEGMENTOFFSET = 0.05f;

	ArrayList<IBoxplotData> statistics;

	Geometry line = null;

	ArrayList<Geometry> cylinders = new ArrayList<Geometry>();

	ColorRGBA mainColor = ColorRGBA.Red;
	ColorRGBA medianColor = ColorRGBA.Black;
	ColorRGBA boxColor = ColorRGBA.Orange;
	ColorRGBA extremaColor = ColorRGBA.Pink;

	AssetManager assetManager;

	public Boxplot3d(AssetManager assetManager) {
		this.assetManager = assetManager;

		ArrayList<Vector3f> lineBuffer = new ArrayList<Vector3f>();

		line = Utils.CreateLine(assetManager, lineBuffer, mainColor, false, 4);

		this.attachChild(line);
	}

	private Geometry getCylinder(int index, ColorRGBA color, float height,
			float radius) {
		Geometry cylinder = null;

		if (index < cylinders.size()) {
			cylinder = cylinders.get(index);
			cylinder.getMaterial().setColor("Color", color);
			Cylinder mesh = (Cylinder) cylinder.getMesh();
			mesh.updateGeometry(10, 10, radius, radius, height, true, false);
			cylinder.updateModelBound();
		} else { // add newone
			cylinder = new Geometry("cylinder", new Cylinder(10, 10, radius,
					height, true));

			Material mat = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
			// mat.getAdditionalRenderState().setFaceCullMode(
			// FaceCullMode.Off);
			mat.setColor("Color", color);
			cylinder.setMaterial(mat);

			cylinders.add(cylinder);
			this.attachChild(cylinder);
		}

		return cylinder;
	}

	private void updateData() {
		ArrayList<VectorLine> lines = new ArrayList<VectorLine>();

		for (int i = 0; i < statistics.size(); i++) {
			IBoxplotData t = statistics.get(i);
			VectorLine maxLine = (VectorLine) t.getMaxObj();
			lines.add(maxLine);
			LOGGER.debug("Maxline length " + maxLine.getLength());

			int cylinderIndex = 0;

			ArrayList<Vector3f> maxlineBuffer = maxLine.getLineBuffer();
			if (maxlineBuffer.size() > 5) { // TODO arbitrary number ....

				Vector3f pos = new Vector3f();
				Vector3f direction = new Vector3f();
				Quaternion rotation = new Quaternion();
				Vector3f up = new Vector3f(0, 1, 0);

				// /MEDIAN
				Geometry median = getCylinder(cylinderIndex++, medianColor,
						SEGMENTHEIGTH, SEGMENTRADIUS + SEGMENTOFFSET);
				LOGGER.debug("Median: " + t.getMedian());
				getPosAndDirectionLength(maxlineBuffer, t.getMedian(), pos,
						direction);
				median.setLocalTranslation(pos);
				rotation.lookAt(direction, up);
				median.setLocalRotation(rotation);

				// Extrema
				Geometry max = getCylinder(cylinderIndex++, extremaColor,
						SEGMENTHEIGTH, SEGMENTRADIUS);
				LOGGER.debug("Extrema high: " + t.getMax());
				getPosAndDirectionLength(maxlineBuffer, t.getMax(), pos,
						direction);
				max.setLocalTranslation(pos);
				rotation.lookAt(direction, up);
				max.setLocalRotation(rotation);

				Geometry min = getCylinder(cylinderIndex++, extremaColor,
						SEGMENTHEIGTH, SEGMENTRADIUS);
				LOGGER.debug("Extrema low: " + t.getMin());
				getPosAndDirectionLength(maxlineBuffer, t.getMin(), pos,
						direction);
				min.setLocalTranslation(pos);
				rotation.lookAt(direction, up);
				min.setLocalRotation(rotation);

				// / BOX
				LOGGER.debug("Quantile low: " + t.getLowerQuantile());
				LOGGER.debug("Quantile high: " + t.getUpperQuantile());
				Vector3f pos1 = new Vector3f();
				Vector3f pos2 = new Vector3f();
				int boxBegin = getPosFromLength(maxlineBuffer,
						t.getLowerQuantile(), pos1);
				int boxEnd = getPosFromLength(maxlineBuffer,
						t.getUpperQuantile(), pos2);

				ArrayList<Vector3f> boxLine = new ArrayList<Vector3f>();
				boxLine.add(pos1);

				// create subline of box points
				for (int j = boxBegin + 1; j < boxEnd; j++) {
					boxLine.add(maxlineBuffer.get(j));
				}
				boxLine.add(pos2);

				for (int j = 1; j < boxLine.size(); j++) {
					addCylinderBetweenTwoPoits(cylinderIndex++,
							boxLine.get(j - 1), boxLine.get(j));
				}

			}

		}

		Utils.updateLinesVec(line, lines, mainColor);
	}

	private void addCylinderBetweenTwoPoits(int cylinderIndex, Vector3f pos1,
			Vector3f pos2) {

		Quaternion rotation = new Quaternion();
		Vector3f up = new Vector3f(0, 1, 0);

		Vector3f direction = pos2.subtract(pos1);

		Vector3f pos = pos2.subtract(direction.mult(0.5f)); // get mid position

		Geometry box = getCylinder(cylinderIndex, boxColor, direction.length(),
				SEGMENTRADIUS);

		box.setLocalTranslation(pos);
		rotation.lookAt(direction, up);
		box.setLocalRotation(rotation);
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
			segmentStart = segmentStart.add(lineBuffer.get(idx - 1)
					.subtract(segmentStart).normalize().mult(0.1f));
		}

		ArrayList<Vector3f> segmentLine = new ArrayList<Vector3f>();
		segmentLine.add(segmentStart);
		segmentLine.add(segmentEnd);

		return segmentLine;
	}

	/**
	 * paint a line segment of 2 points around length point position
	 */
	private void getPosAndDirectionLength(ArrayList<Vector3f> lineBuffer,
			float lengthPosition, Vector3f retPos, Vector3f retDirection) {

		int idx = getPosFromLength(lineBuffer, lengthPosition, retPos);

		if (idx < (lineBuffer.size() - 1)) {
			retDirection.set(lineBuffer.get(idx + 1).subtract(retPos));
		} else {
			retDirection.set(retPos.subtract(lineBuffer.get(idx - 1)));
		}
	}

	/**
	 * Calculate idx from length position in vector line
	 * 
	 * @param lineBuffer
	 * @param length
	 * @param pos
	 *            return calculated position
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
			if (measuredLength > length) {
				// important to use set and not changeing the reference!!
				pos.set(lineBuffer.get(idx));
				pos.set(pos.subtract(diff.normalize().mult(
						measuredLength - length)));

				idx = idx - 1;
				break;
			} else if (measuredLength == length) {
				pos.set(lineBuffer.get(idx));
				break;
			}
		}

		if (idx == lineBuffer.size()) {
			idx--;
			pos.set(lineBuffer.get(idx));
		}

		return idx;
	}

	public ArrayList<IBoxplotData> getStatistics() {
		return statistics;
	}

	public void setStatistics(ArrayList<IBoxplotData> statistics) {
		this.statistics = statistics;
		if (statistics != null) {
			updateData();
		}
	}

}
