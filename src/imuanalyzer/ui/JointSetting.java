package imuanalyzer.ui;

import com.jme3.math.ColorRGBA;

import imuanalyzer.signalprocessing.Hand.JointType;

/**
 * Class for storing settings from one joint
 * like colors and visibility
 * @author Christopher-Eyk Hrabia
 *
 */
public class JointSetting {
	JointType type;

	public JointType getType() {
		return type;
	}

	ColorRGBA liveMotionColor = ColorRGBA.White.clone();

	ColorRGBA storedMotionColor = ColorRGBA.White.clone();

	ColorRGBA liveMotionMaxLineColor = ColorRGBA.Green.clone();

	ColorRGBA liveMotionMinLineColor = ColorRGBA.Orange.clone();

	ColorRGBA liveTouchCurrentColor = ColorRGBA.Cyan.clone();

	ColorRGBA liveTouchMaxColor = ColorRGBA.Blue.clone();

	boolean visible = true;

	public JointSetting(JointType type) {
		this.type = type;
	}

	/**
	 * Reset all colors
	 */
	public void resetColor() {
		liveMotionColor = ColorRGBA.White.clone();

		storedMotionColor = ColorRGBA.White.clone();

		liveMotionMaxLineColor = ColorRGBA.Green.clone();

		liveMotionMinLineColor = ColorRGBA.Orange.clone();

		liveTouchCurrentColor = ColorRGBA.Cyan.clone();

		liveTouchMaxColor = ColorRGBA.Blue.clone();
	}

	public ColorRGBA getLiveMotionColor() {
		return liveMotionColor;
	}

	public void setLiveMotionColor(ColorRGBA liveMotionColor) {
		this.liveMotionColor = liveMotionColor.clone();
	}

	public ColorRGBA getStoredMotionColor() {
		return storedMotionColor;
	}

	public void setStoredMotionColor(ColorRGBA storedMotionColor) {
		this.storedMotionColor = storedMotionColor.clone();
	}

	public ColorRGBA getLiveMotionMaxLineColor() {
		return liveMotionMaxLineColor;
	}

	public void setLiveMotionMaxLineColor(ColorRGBA liveMotionMaxLineColor) {
		this.liveMotionMaxLineColor = liveMotionMaxLineColor.clone();
	}

	public ColorRGBA getLiveMotionMinLineColor() {
		return liveMotionMinLineColor;
	}

	public void setLiveMotionMinLineColor(ColorRGBA liveMotionMinLineColor) {
		this.liveMotionMinLineColor = liveMotionMinLineColor.clone();
	}

	public ColorRGBA getLiveTouchCurrentColor() {
		return liveTouchCurrentColor;
	}

	public void setLiveTouchCurrentColor(ColorRGBA liveTouchCurrentColor) {
		this.liveTouchCurrentColor = liveTouchCurrentColor.clone();
	}

	public ColorRGBA getLiveTouchMaxColor() {
		return liveTouchMaxColor;
	}

	public void setLiveTouchMaxColor(ColorRGBA liveTouchMaxColor) {
		this.liveTouchMaxColor = liveTouchMaxColor.clone();
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
