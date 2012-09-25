package imuanalyzer.signalprocessing;

import imuanalyzer.ui.IInfoContent;
import imuanalyzer.utils.math.LowPassQuad;
import imuanalyzer.utils.math.Quaternion;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.jme3.math.Vector3f;

/**
 * Stores and calculates data of touch analysis
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class TouchAnalysis implements IInfoContent {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(TouchAnalysis.class
			.getName());

	final static double MINCUTOFFDIRECTION = 0.03f;

	Hand hand;

	Joint observedJoint;

	Quaternion currentDirection = new Quaternion();

	ArrayList<VectorLine> lines = new ArrayList<VectorLine>();

	float maxLengthTouch = 0;

	int maxIdTouch = 0;

	Quaternion lastPos = new Quaternion();

	private Object clearLock = new Object();

	VectorLine currentLine;

	LowPassQuad directionLowPass = new LowPassQuad(0.7f);

	String infoName;

	/**
	 * Constructor touch analysis
	 * 
	 * @param hand
	 *            for starting analysis
	 * @param observedJoint
	 *            joint which will be observed
	 */
	public TouchAnalysis(Hand hand, Joint observedJoint) {
		this.hand = hand;
		this.observedJoint = observedJoint;
		lastPos = observedJoint.getFingerTouchPosition();
		currentLine = new VectorLine();
		lines.add(currentLine);
		infoName = "Touch " + observedJoint.getInfoName();
	}

	/**
	 * Clear all analysis data
	 */
	public void clear() {
		synchronized (clearLock) {
			currentLine = new VectorLine();
			lines.add(currentLine);
			maxIdTouch = 0;
			currentDirection.set(1, 0, 0, 0);
			maxLengthTouch = 0;
			lastPos = observedJoint.getFingertipPosition();
		}
	}

	/**
	 * Will be upadted from external, checks if observed joint or a parent is
	 * updated before doing further calculation
	 * 
	 * @param updatedJoint
	 */
	public void update(Joint updatedJoint) {

		if (updatedJoint != observedJoint
				&& !observedJoint.hasParent(updatedJoint)) {
			return;
		}

		Quaternion newPos = observedJoint.getFingerTouchPosition();

		synchronized (clearLock) {
			if (!lastPos.equals(newPos)) {

				Quaternion newDirection = newPos.minus(lastPos);

				// newDirection.print(3);

				// low pass
				newDirection = directionLowPass.filter(newDirection);

				// newDirection.print(8);

				float directionLength = (float) newDirection.getNorm();

				// System.out.printf("%.8f",directionLength);

				if (directionLength > 0.0001) {

					int signChangeCounter = 0;
					// check direction change
					if (Math.abs(newDirection.getX()) > MINCUTOFFDIRECTION
							&& newDirection.getX() > 0
							&& currentDirection.getX() < 0
							|| currentDirection.getX() > 0
							&& newDirection.getX() < 0) {
						signChangeCounter++;
						// LOGGER.debug("x sign changed");
					}
					if (Math.abs(newDirection.getY()) > MINCUTOFFDIRECTION
							&& newDirection.getY() > 0
							&& currentDirection.getY() < 0
							|| currentDirection.getY() > 0
							&& newDirection.getY() < 0) {
						signChangeCounter++;
						// LOGGER.debug("y sign changed");
					}
					if (Math.abs(newDirection.getZ()) > MINCUTOFFDIRECTION
							&& newDirection.getZ() > 0
							&& currentDirection.getZ() < 0
							|| currentDirection.getZ() > 0
							&& newDirection.getZ() < 0) {
						signChangeCounter++;
						// LOGGER.debug("z sign changed");
					}

					if (signChangeCounter > 1) {
						// LOGGER.debug("Direction changed!");
						currentLine = new VectorLine();
						lines.add(currentLine);
					}
					currentDirection = newDirection;

					lastPos = newPos;
				}

				// LOGGER.debug(currentDirection);

				currentLine.addLength(directionLength);

				if (currentLine.getLength() > maxLengthTouch) {
					maxLengthTouch = currentLine.getLength();
					maxIdTouch = lines.size() - 1;
					// LOGGER.debug("new max line id:" + maxIdTouch);
					// LOGGER.debug("Max Length: " + maxLengthTouch);
				}

				// conversion to jme representation not sure why it is different
				// from
				// that one in utils ....?
				currentLine.getLineBuffer().add(
						new Vector3f((float) newPos.getX() * -1, (float) newPos
								.getY(), (float) newPos.getZ() * -1));

			}
		}
	}

	/**
	 * Get all lines from this touch analysis
	 * 
	 * @return
	 */
	public ArrayList<VectorLine> getAllLines() {
		return lines;
	}

	/**
	 * Get the maximum line
	 * 
	 * @return
	 */
	public VectorLine getMaxLine() {
		return lines.get(maxIdTouch);
	}

	/**
	 * Get the currently recognized line
	 * 
	 * @return
	 */
	public VectorLine getCurrentLine() {

		return currentLine;
	}

	/**
	 * Get observed joint
	 * 
	 * @return
	 */
	public Joint getObservedJoint() {
		return observedJoint;
	}

	/**
	 * Get line id of max line
	 * 
	 * @return
	 */
	public int getMaxIdTouch() {
		return maxIdTouch;
	}

	/**
	 * Get maximum touch length
	 * 
	 * @return
	 */
	public float getMaxLengthTouch() {
		return maxLengthTouch;
	}

	@Override
	public String getInfoName() {
		return infoName;
	}

	@Override
	public String getInfoValue() {
		return "current:" + String.format("%.2f", currentLine.getLength())
				+ " max:" + String.format("%.2f", maxLengthTouch);
	}

}
