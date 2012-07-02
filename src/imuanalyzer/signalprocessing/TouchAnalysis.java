package imuanalyzer.signalprocessing;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.ui.IInfoContent;
import imuanalyzer.utils.math.LowPass;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.jme3.math.Vector3f;

public class TouchAnalysis implements IInfoContent {

	private static final Logger LOGGER = Logger.getLogger(TouchAnalysis.class
			.getName());

	Hand hand;

	Joint observedJoint;

	Quaternion currentDirection = new Quaternion();

	ArrayList<VectorLine> lines = new ArrayList<VectorLine>();

	float maxLengthTouch = 0;

	int maxIdTouch = 0;

	Quaternion lastPos = new Quaternion();

	private Object clearLock = new Object();

	VectorLine currentLine;

	LowPass directionLowPass = new LowPass(0.5f);

	String infoName;

	public TouchAnalysis(Hand hand, Joint observedJoint) {
		this.hand = hand;
		this.observedJoint = observedJoint;
		lastPos = observedJoint.getFingertipPosition();
		currentLine = new VectorLine();
		lines.add(currentLine);
		infoName = "Touch " + observedJoint.getInfoName();
	}

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

	public void update(Joint updatedJoint) {

		if (updatedJoint != observedJoint
				&& !observedJoint.hasParent(updatedJoint)) {
			return;
		}

		Quaternion newPos = observedJoint.getFingertipPosition();
		synchronized (clearLock) {
			if (!lastPos.equals(newPos)) {

				Quaternion newDirection = newPos.minus(lastPos);

				// low pass
				newDirection = directionLowPass.filter(newDirection);

				// newDirection.print(8);

				float directionLength = (float) newDirection.getNorm();

				// System.out.printf("%.8f",directionLength);

				if (directionLength > 0.0001) {

					int signChangeCounter = 0;
					// check direction change
					if (newDirection.getX() > 0 && currentDirection.getX() < 0
							|| currentDirection.getX() > 0
							&& newDirection.getX() < 0) {
						signChangeCounter++;
						// LOGGER.debug("x sign changed");
					}
					if (newDirection.getY() > 0 && currentDirection.getY() < 0
							|| currentDirection.getY() > 0
							&& newDirection.getY() < 0) {
						signChangeCounter++;
						// LOGGER.debug("y sign changed");
					}
					if (newDirection.getZ() > 0 && currentDirection.getZ() < 0
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
				}

				// LOGGER.debug(currentDirection);

				currentLine.addLength(directionLength);

				if (currentLine.getLength() > maxLengthTouch) {
					maxLengthTouch = currentLine.getLength();
					maxIdTouch = lines.size() - 1;
					// LOGGER.debug("new max line id:" + maxIdTouch);
					// LOGGER.debug("Max Length: " + maxLengthTouch);
				}

				lastPos = newPos;

				// conversion to jme representation not sure why it is different
				// from
				// that one in utils ....?
				currentLine.getLineBuffer().add(
						new Vector3f((float) newPos.getX() * -1, (float) newPos
								.getY(), (float) newPos.getZ() * -1));

			}
		}
	}

	public ArrayList<VectorLine> getAllLines() {
		return lines;
	}

	public VectorLine getMaxLine() {
		return lines.get(maxIdTouch);
	}

	public VectorLine getCurrentLine() {

		return currentLine;
	}

	public Joint getObservedJoint() {
		return observedJoint;
	}

	public int getMaxIdTouch() {
		return maxIdTouch;
	}

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
