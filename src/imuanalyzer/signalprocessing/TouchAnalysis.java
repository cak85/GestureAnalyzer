package imuanalyzer.signalprocessing;

import imuanalyzer.filter.Quaternion;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.jme3.math.Vector3f;

public class TouchAnalysis {

	private static final Logger LOGGER = Logger.getLogger(TouchAnalysis.class
			.getName());

	Hand hand;

	Joint observedJoint;

	Quaternion currentDirection = new Quaternion();

	ArrayList<ArrayList<Vector3f>> lines = new ArrayList<ArrayList<Vector3f>>();

	float currentLengthTouch = 0;
	float maxLengthTouch = 0;

	int maxIdTouch = 0;

	Quaternion lastPos = new Quaternion();
	ArrayList<Vector3f> lineBuffer = new ArrayList<Vector3f>();

	private Object clearLock = new Object();

	public TouchAnalysis(Hand hand, Joint observedJoint) {
		this.hand = hand;
		this.observedJoint = observedJoint;
		lines.add(lineBuffer);
	}

	public void clear() {
		synchronized (clearLock) {
			lines.clear();
			lineBuffer.clear();
			lines.add(lineBuffer);
			maxIdTouch = 0;
			currentDirection.set(1, 0, 0, 0);
			currentLengthTouch = 0;
			maxLengthTouch = 0;
			lastPos.set(1, 0, 0, 0);
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

				// TODO does not work like expected
				Quaternion newDirection = newPos.minus(lastPos);

				// newDirection.print(8);

				float directionLength = (float) newDirection.getNorm();

				// System.out.printf("%.8f",directionLength);

				if (directionLength > 0.0001) {

					int signChangeCounter = 0;
					// check direction change
					if (newDirection.getX() >= 0 && currentDirection.getX() < 0
							|| currentDirection.getX() >= 0
							&& newDirection.getX() < 0) {
						signChangeCounter++;
						// LOGGER.debug("x sign changed");
					}
					if (newDirection.getY() >= 0 && currentDirection.getY() < 0
							|| currentDirection.getY() >= 0
							&& newDirection.getY() < 0) {
						signChangeCounter++;
						// LOGGER.debug("y sign changed");
					}
					if (newDirection.getZ() >= 0 && currentDirection.getZ() < 0
							|| currentDirection.getZ() >= 0
							&& newDirection.getZ() < 0) {
						signChangeCounter++;
						// LOGGER.debug("z sign changed");
					}

					if (signChangeCounter > 1) {
						LOGGER.debug("Direction changed!");
						currentLengthTouch = 0;
						lineBuffer = new ArrayList<Vector3f>();
						lines.add(lineBuffer);
					}
				}

				currentDirection = newDirection;

				currentLengthTouch += directionLength;

				if (currentLengthTouch > maxLengthTouch) {
					maxLengthTouch = currentLengthTouch;
					maxIdTouch = lines.size() - 1;
				}

				lastPos = newPos;

				// conversion to jme representation not sure why it is different
				// from
				// that one in utils ....?
				lineBuffer.add(new Vector3f((float) newPos.getX() * -1,
						(float) newPos.getY(), (float) newPos.getZ() * -1));

			}
		}
	}

	public ArrayList<ArrayList<Vector3f>> getAllLineBuffer() {
		return lines;
	}

	public ArrayList<Vector3f> getMaxLineBuffer() {
		return lines.get(maxIdTouch);
	}

	public ArrayList<Vector3f> getCurrentLineBuffer() {

		return lineBuffer;
	}

	public Joint getObservedJoint() {
		return observedJoint;
	}
	
	public int getMaxIdTouch() {
		return maxIdTouch;
	}

}
