package imuanalyzer.ui.jmonkey;

import imuanalyzer.signalprocessing.IBoxplotData;
import imuanalyzer.signalprocessing.VectorLine;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;

/**
 * threedimensional boxplot representation
 * with optional additional markers
 * @author Christopher-Eyk Hrabia
 *
 */
public class Boxplot3d extends Node {

	private static final Logger LOGGER = Logger.getLogger(Boxplot3d.class
			.getName());

	private static final float SEGMENTHEIGTH = 0.05f;
	private static final float WHISKERRADIUS = 0.2f;
	private static final float SPECIALRADIUS = 0.3f;
	private static final float MEDIANRADIUS = WHISKERRADIUS;
	private static final float OUTLINERSIDELENGTH = WHISKERRADIUS / 3;
	private static final float SEGMENTOFFSET = 0.05f;

	ArrayList<IBoxplotData> statistics;

	Geometry line;

	Geometry specialPointsLine;

	ArrayList<Geometry> cylinders = new ArrayList<Geometry>();
	ArrayList<Geometry> boxes = new ArrayList<Geometry>();

	ColorRGBA mainColor = ColorRGBA.Red;
	ColorRGBA medianColor = ColorRGBA.Black;
	ColorRGBA boxColor = ColorRGBA.Orange;
	ColorRGBA extremaColor = ColorRGBA.Pink;
	ColorRGBA outlinerColor = ColorRGBA.Cyan;
	ColorRGBA specialLineColor = ColorRGBA.Gray;
	ColorRGBA specialPointColor = ColorRGBA.Red;

	AssetManager assetManager;

	public Boxplot3d(AssetManager assetManager) {
		this.assetManager = assetManager;

		ArrayList<Vector3f> lineBuffer = new ArrayList<Vector3f>();

		line = Utils.CreateLine(assetManager, lineBuffer, mainColor, false, 4);

		this.attachChild(line);

		specialPointsLine = Utils.CreateLine(assetManager, lineBuffer,
				mainColor, false, 4);

		this.attachChild(specialPointsLine);

	}

	public void clear() {
		ArrayList<Vector3f> lineBuffer = new ArrayList<Vector3f>();
		Utils.updateLine(specialPointsLine, lineBuffer, false, specialLineColor);
		Utils.updateLine(line, lineBuffer, false, mainColor);
		for (Geometry g : boxes) {
			g.removeFromParent();
		}
		boxes.clear();
		for (Geometry g : cylinders) {
			g.removeFromParent();
		}
		cylinders.clear();
	}

	private Geometry getCylinder(ArrayList<Geometry> cylinders, Node toAttach,
			int index, ColorRGBA color, float height, float radius,
			boolean closed) {
		Geometry cylinder = null;

		if (index < cylinders.size()) {
			cylinder = cylinders.get(index);
			Material mat = cylinder.getMaterial();
			mat.setColor("Color", color);
			mat.setColor("GlowColor", color);
			Cylinder mesh = (Cylinder) cylinder.getMesh();
			mesh.updateGeometry(10, 10, radius, radius, height, closed, false);
			cylinder.updateModelBound();
		} else { // add newone
			cylinder = new Geometry("cylinder", new Cylinder(10, 10, radius,
					height, closed));

			Material mat = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
			// mat.getAdditionalRenderState().setFaceCullMode(
			// FaceCullMode.Off);
			mat.setColor("Color", color);
			mat.setColor("GlowColor", color);
			cylinder.setMaterial(mat);

			cylinders.add(cylinder);
			toAttach.attachChild(cylinder);
		}

		return cylinder;
	}

	private Geometry getBox(int index, ColorRGBA color, float height,
			float radius) {
		Geometry box = null;

		if (index < boxes.size()) {
			box = boxes.get(index);
			Material mat = box.getMaterial();
			mat.setColor("Color", color);
			mat.setColor("GlowColor", color);
			Box mesh = (Box) box.getMesh();
			mesh.updateGeometry(new Vector3f(), radius, radius, height);
			box.updateModelBound();
		} else { // add newone
			box = new Geometry("cylinder", new Box(radius, radius, height));

			Material mat = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
			// mat.getAdditionalRenderState().setFaceCullMode(
			// FaceCullMode.Off);
			mat.setColor("Color", color);
			mat.setColor("GlowColor", color);
			box.setMaterial(mat);

			boxes.add(box);
			this.attachChild(box);
		}

		return box;
	}

	private void updateData() {
		ArrayList<VectorLine> lines = new ArrayList<VectorLine>();

		for (int i = 0; i < statistics.size(); i++) {
			IBoxplotData t = statistics.get(i);

			// one option with based on max line
			//VectorLine boxBaseLine = (VectorLine) t.getMaxObj();
			// better option with using calculated avg line for base
			VectorLine boxBaseLine = (VectorLine) t.getAvgObj();
			lines.add(boxBaseLine);
			LOGGER.debug("Boxbaseline length " + boxBaseLine.getLength());

			int cylinderIndex = 0;
			int boxIndex = 0;

			ArrayList<Vector3f> boxBaselineBuffer = boxBaseLine.getLineBuffer();
			// do not paint lines with less than x points
			if (boxBaselineBuffer.size() > 5) {
 
				Vector3f pos = new Vector3f();
				Vector3f direction = new Vector3f();
				Quaternion rotation = new Quaternion();
				Vector3f up = new Vector3f(0, 1, 0);

				// /MEDIAN
				Geometry median = getCylinder(cylinders, this, cylinderIndex++,
						medianColor, SEGMENTHEIGTH, MEDIANRADIUS
								+ SEGMENTOFFSET, true);
				LOGGER.debug("Median: " + t.getMedian());
				getPosAndDirectionLength(boxBaselineBuffer, t.getMedian(), pos,
						direction);
				median.setLocalTranslation(pos);
				rotation.lookAt(direction, up);
				median.setLocalRotation(rotation);

				// whisker
				Geometry max = getCylinder(cylinders, this, cylinderIndex++,
						extremaColor, SEGMENTHEIGTH, WHISKERRADIUS, true);
				LOGGER.debug("Whisker high: " + t.getMax());
				getPosAndDirectionLength(boxBaselineBuffer, t.getMax(), pos,
						direction);
				max.setLocalTranslation(pos);
				rotation.lookAt(direction, up);
				max.setLocalRotation(rotation);

				Geometry min = getCylinder(cylinders, this, cylinderIndex++,
						extremaColor, SEGMENTHEIGTH, WHISKERRADIUS, true);
				LOGGER.debug("Whisker low: " + t.getMin());
				getPosAndDirectionLength(boxBaselineBuffer, t.getMin(), pos,
						direction);
				min.setLocalTranslation(pos);
				rotation.lookAt(direction, up);
				min.setLocalRotation(rotation);

				// Outlier
				for (Object o : t.getOutliers()) {
					VectorLine outLiner = (VectorLine) o;
					Geometry gOut = getBox(boxIndex++, outlinerColor,
							SEGMENTHEIGTH, OUTLINERSIDELENGTH);
					LOGGER.debug("Outliner: " + outLiner.getLength());
					getPosAndDirectionLength(boxBaselineBuffer,
							outLiner.getLength(), pos, direction);
					gOut.setLocalTranslation(pos);
					rotation.lookAt(direction, up);
					gOut.setLocalRotation(rotation);
				}

				// BOX
				LOGGER.debug("Quantile low: " + t.getLowerQuantile());
				LOGGER.debug("Quantile high: " + t.getUpperQuantile());
				Vector3f pos1 = new Vector3f();
				Vector3f pos2 = new Vector3f();
				int boxBegin = getPosFromLength(boxBaselineBuffer,
						t.getLowerQuantile(), pos1);
				int boxEnd = getPosFromLength(boxBaselineBuffer,
						t.getUpperQuantile(), pos2);

				ArrayList<Vector3f> boxLine = new ArrayList<Vector3f>();
				boxLine.add(pos1);

				// create subline of box points
				for (int j = boxBegin + 1; j < boxEnd; j++) {
					boxLine.add(boxBaselineBuffer.get(j));
				}
				boxLine.add(pos2);

				for (int j = 1; j < boxLine.size(); j++) {
					addCylinderBetweenTwoPoits(cylinderIndex++,
							boxLine.get(j - 1), boxLine.get(j));
				}

				// special points
				if (t.getSpecialPoints().size() > 0) {
					// generate special points line
					ArrayList<Vector3f> specialPoints = new ArrayList<Vector3f>();
					// generate special line
					for (int j = 0; j < boxBaselineBuffer.size(); j++) {
						specialPoints.add(boxBaselineBuffer.get(j));
					}

					for (Float p : t.getSpecialPoints()) {
						Geometry special = getCylinder(cylinders, this,
								cylinderIndex++, specialPointColor,
								SEGMENTHEIGTH, SPECIALRADIUS, true);
						getPosAndDirectionLength(specialPoints, p, pos,
								direction);
						special.setLocalTranslation(pos);
						rotation.lookAt(direction, up);
						special.setLocalRotation(rotation);
					}

					Utils.updateLine(specialPointsLine, specialPoints, false,
							specialLineColor);
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

		Geometry box = getCylinder(cylinders, this, cylinderIndex, boxColor,
				direction.length(), WHISKERRADIUS, true);

		box.setLocalTranslation(pos);
		rotation.lookAt(direction, up);
		box.setLocalRotation(rotation);
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
		clear();
		if (statistics != null) {
			updateData();
		}
	}

	/**
	 * @return the specialPointsLine
	 */
	public Geometry getSpecialPointsLine() {
		return specialPointsLine;
	}

	/**
	 * @param specialPointsLine the specialPointsLine to set
	 */
	public void setSpecialPointsLine(Geometry specialPointsLine) {
		this.specialPointsLine = specialPointsLine;
	}

	/**
	 * @return the mainColor
	 */
	public ColorRGBA getMainColor() {
		return mainColor;
	}

	/**
	 * @param mainColor the mainColor to set
	 */
	public void setMainColor(ColorRGBA mainColor) {
		this.mainColor = mainColor;
	}

	/**
	 * @return the medianColor
	 */
	public ColorRGBA getMedianColor() {
		return medianColor;
	}

	/**
	 * @param medianColor the medianColor to set
	 */
	public void setMedianColor(ColorRGBA medianColor) {
		this.medianColor = medianColor;
	}

	/**
	 * @return the boxColor
	 */
	public ColorRGBA getBoxColor() {
		return boxColor;
	}

	/**
	 * @param boxColor the boxColor to set
	 */
	public void setBoxColor(ColorRGBA boxColor) {
		this.boxColor = boxColor;
	}

	/**
	 * @return the extremaColor
	 */
	public ColorRGBA getExtremaColor() {
		return extremaColor;
	}

	/**
	 * @param extremaColor the extremaColor to set
	 */
	public void setExtremaColor(ColorRGBA extremaColor) {
		this.extremaColor = extremaColor;
	}

	/**
	 * @return the outlinerColor
	 */
	public ColorRGBA getOutlinerColor() {
		return outlinerColor;
	}

	/**
	 * @param outlinerColor the outlinerColor to set
	 */
	public void setOutlinerColor(ColorRGBA outlinerColor) {
		this.outlinerColor = outlinerColor;
	}

	/**
	 * @return the specialLineColor
	 */
	public ColorRGBA getSpecialLineColor() {
		return specialLineColor;
	}

	/**
	 * @param specialLineColor the specialLineColor to set
	 */
	public void setSpecialLineColor(ColorRGBA specialLineColor) {
		this.specialLineColor = specialLineColor;
	}

	/**
	 * @return the specialPointColor
	 */
	public ColorRGBA getSpecialPointColor() {
		return specialPointColor;
	}

	/**
	 * @param specialPointColor the specialPointColor to set
	 */
	public void setSpecialPointColor(ColorRGBA specialPointColor) {
		this.specialPointColor = specialPointColor;
	}

}
