package imuanalyzer.signalprocessing;

import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.LinkedList;

import org.apache.log4j.Logger;

public class MotionAnalysis {
	
	private static final Logger LOGGER = Logger.getLogger(MotionAnalysis.class
			.getName());
	
	private static final double MIN_ANGLE_DIFFERENCE = 10 * (Math.PI / 180);
	
	
	Hand hand;
	Joint observedJoint;
	
	protected LinkedList<MovementStep> savedMovementFlow = new LinkedList<MovementStep>();
	protected JointType saveMovementStartJoint = JointType.RD;
	protected Boolean saveMovement = false;
	protected double movementMinDifference = 0.12;
	
	double[] maxAngles = { 0, 0, 0 };

	double[] minAngles = { 999, 999, 999 };

	double minAngleSum = Math.abs(minAngles[0]) + Math.abs(minAngles[1])
			+ Math.abs(minAngles[2]);

	double maxAngleSum = Math.abs(maxAngles[0]) + Math.abs(maxAngles[1])
			+ Math.abs(maxAngles[2]);

	int minIdMotion = 0;

	int maxIdMotion = 0;
	
	public MotionAnalysis(Hand hand, Joint observedJoint) {
		this.hand=hand;
		this.observedJoint=observedJoint;
		addInitialSavedMove();		
	}
	
	public void clear(){
		savedMovementFlow.clear();
		addInitialSavedMove();	
	}
	
	public void update(Joint updatedBy){

		for (MovementStep s : savedMovementFlow) {
			s.getMove().updateWorldOrientation();
		}
		
		System.out.println("Type updatedBy = " + updatedBy.getType());

		// check if updateBy is parent of currently observed joint
		// if yes its not neccessary to update
		if (observedJoint.hasParent(updatedBy)) {
			return;
		}

		StoredJointState newState = new StoredJointState(
				observedJoint, observedJoint.parent, true);

		if (savedMovementFlow.size() > 0) {
			StoredJointState lastState = savedMovementFlow.getLast()
					.getMove();

			if (!newState.hasAngelurDifferenceGreaterThan(lastState,
					MIN_ANGLE_DIFFERENCE)) {
				// LOGGER.debug("Difference to low");
				return;
			}
		}

		// check if an almost same position is already saved and its not
		// the last one (not increasing counter if we move slightly on
		// position)
		// if yes increase counter
		for (int i = 0; i < savedMovementFlow.size() - 1; i++) {
			MovementStep m = savedMovementFlow.get(i);
			if (m.getMove().equals(newState)) {
				m.incCount();
				// LOGGER.debug("Find existing position - Increase count");
				return;
			}
		}

		// save extrema
		double[] currentAngles = newState.getMaxAngle();

		double currentAngleSum = Math.abs(currentAngles[0])
				+ Math.abs(currentAngles[1]) + Math.abs(currentAngles[2]);

		if (currentAngleSum > maxAngleSum) {
			maxAngles = currentAngles;
			maxAngleSum = currentAngleSum;
			maxIdMotion = savedMovementFlow.size(); // correct because we
													// count up afterwards
			LOGGER.debug("MaxAngleSum: " + (maxAngleSum * 180 / Math.PI));
		}
		if (currentAngleSum < minAngleSum) {
			minAngles = currentAngles;
			minAngleSum = currentAngleSum;
			minIdMotion = savedMovementFlow.size(); // correct because we
													// count up afterwards
			LOGGER.debug("MinAngleSum: " + (minAngleSum * 180 / Math.PI));
		}

		// LOGGER.debug("Created new one");
		savedMovementFlow.addLast(new MovementStep(newState));
	}
	
	/**
	 * Add first saved movement
	 */
	private void addInitialSavedMove() {
		StoredJointState newState = new StoredJointState(observedJoint,
				observedJoint.parent, true);
		savedMovementFlow.addLast(new MovementStep(newState));
	}
	
	public MovementStep getMaxMovementStep() {
		return savedMovementFlow.get(maxIdMotion);
	}

	public MovementStep getMinMovementStep() {
		return savedMovementFlow.get(minIdMotion);
	}

	public LinkedList<MovementStep> getSavedMovementFlow() {
		return savedMovementFlow;
	}
	
	public Joint getObservedJoint() {
		return observedJoint;
	}	
	
	public int getMinIdMotion() {
		return minIdMotion;
	}

	public int getMaxIdMotion() {
		return maxIdMotion;
	}

}
