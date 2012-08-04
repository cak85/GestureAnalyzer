package imuanalyzer.signalprocessing;

import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.math.AngleHelper;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class JointAngleCollector implements IAnalysisExtension {

	ArrayList<Hand> hands;

	protected ArrayList<ArrayBlockingQueue<double[]>> values = new ArrayList<ArrayBlockingQueue<double[]>>();

	protected double[] currentAngles1 = { 0, 0, 0 };

	protected double[] currentAngles2 = { 0, 0, 0 };

	JointType type1;
	JointType type2;

	String description;

	float minRelationOffset = 0;

	public JointAngleCollector(String namePostfix, final Hand hand,
			final JointType type1, final JointType type2, final int valueLimit) {

		this.type1 = type1;
		this.type2 = type2;

		Joint j1 = hand.getJoint(type1);

		Joint j2 = hand.getJoint(type2);

		Restriction r1 = j1.getRestriction();
		Restriction r2 = j2.getRestriction();

		if (r1.isRollAllowed() && r2.isRollAllowed()) {
			values.add(new ArrayBlockingQueue<double[]>(valueLimit));
		}

		if (r1.isPitchAllowed() && r2.isPitchAllowed()) {
			values.add(new ArrayBlockingQueue<double[]>(valueLimit));
		}

		if (r1.isYawAllowed() && r2.isYawAllowed()) {
			values.add(new ArrayBlockingQueue<double[]>(valueLimit));
		}

		description = "Relation " + type1 + " / " + type2 + " " + namePostfix;

	}

	public synchronized void update(Hand hand, int idx) {

		double[] angles1 = hand.getJoint(type1).getRotationBetweenParent()
				.getAnglesRad();

		double[] angles2 = hand.getJoint(type2).getRotationBetweenParent()
				.getAnglesRad();

		if (Math.abs(angles1[0] - currentAngles1[0]) > 0.0001
				|| Math.abs(angles1[1] - currentAngles1[1]) > 0.0001
				|| Math.abs(angles1[2] - currentAngles1[2]) > 0.0001
				|| Math.abs(angles2[0] - currentAngles2[0]) > 0.0001
				|| Math.abs(angles2[1] - currentAngles2[1]) > 0.0001
				|| Math.abs(angles2[2] - currentAngles2[2]) > 0.0001) {
			
			currentAngles1 = angles1;
			currentAngles2 = angles2;
			int i = 0;
			for (ArrayBlockingQueue<double[]> currentValues : values) {

				double x = Math.abs(AngleHelper.degFromRad(angles1[i]));
				double y = Math.abs(AngleHelper.degFromRad(angles2[i]));

				if (x > minRelationOffset && y > minRelationOffset) {

					// delete oldest if necessary
					if (currentValues.remainingCapacity() == 0) {
						currentValues.poll();
					}

					currentValues.add(new double[] { x, y });
				}

				i++;
			}

		}
	}

	@Override
	public void finished() {
	}

	/**
	 * @return the values
	 */
	public ArrayList<ArrayBlockingQueue<double[]>> getValues() {
		return values;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(ArrayList<ArrayBlockingQueue<double[]>> values) {
		this.values = values;
	}

	/**
	 * @return the type1
	 */
	public JointType getType1() {
		return type1;
	}

	/**
	 * @param type1
	 *            the type1 to set
	 */
	public void setType1(JointType type1) {
		this.type1 = type1;
	}

	/**
	 * @return the type2
	 */
	public JointType getType2() {
		return type2;
	}

	/**
	 * @param type2
	 *            the type2 to set
	 */
	public void setType2(JointType type2) {
		this.type2 = type2;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the minRelationOffset
	 */
	public float getMinRelationOffset() {
		return minRelationOffset;
	}

	/**
	 * @param minRelationOffset
	 *            the minRelationOffset to set
	 */
	public void setMinRelationOffset(float minRelationOffset) {
		this.minRelationOffset = minRelationOffset;
	}
}
