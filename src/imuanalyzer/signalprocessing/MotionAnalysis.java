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

	int maxId = 0;
	int minId = 0;

	public MotionAnalysis(Hand hand, Joint observedJoint) {
		this.hand = hand;
		this.observedJoint = observedJoint;
		addInitialSavedMove();
	}

	public void clear() {
		savedMovementFlow.clear();
		addInitialSavedMove();
	}

	public void update(Joint updatedBy) {

		for (MovementStep s : savedMovementFlow) {
			s.getMove().updateWorldOrientation();
		}

		// System.out.println("Type updatedBy = " + updatedBy.getType());

		// check if updateBy is parent of currently observed joint
		// if yes its not neccessary to update
		if (observedJoint.hasParent(updatedBy)) {
			return;
		}

		StoredJointState newState = new StoredJointState(observedJoint,
				observedJoint.parent, true);

		if (savedMovementFlow.size() > 0) {
			StoredJointState lastState = savedMovementFlow.getLast().getMove();

			if (!newState.hasAngelDifferenceGreaterThan(lastState,
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

		//get right parent
		// save extrema
		checkExtrema(newState);

		// LOGGER.debug("Created new one");
		savedMovementFlow.addLast(new MovementStep(newState));
	}

	private void checkExtrema(StoredJointState newState) {

		if (newState.compareTo(getMinMovementStep().move ) < 0) {
			minId = savedMovementFlow.size();
		}
		if (newState.compareTo(getMaxMovementStep().move) > 0) {
			maxId = savedMovementFlow.size();
		}
	}

	/**
	 * Add first saved movement
	 */
	private void addInitialSavedMove() {
		StoredJointState newState = new StoredJointState(observedJoint,
				observedJoint.parent, true);

		minId = 0;
		maxId = 0;

		savedMovementFlow.addLast(new MovementStep(newState));

	}

	public MovementStep getMaxMovementStep() {
		return savedMovementFlow.get(maxId);
	}

	public MovementStep getMinMovementStep() {
		return savedMovementFlow.get(minId);
	}

	public LinkedList<MovementStep> getSavedMovementFlow() {
		return savedMovementFlow;
	}

	public Joint getObservedJoint() {
		return observedJoint;
	}

	public int getMinIdMotion() {
		return minId;
	}

	public int getMaxIdMotion() {
		return maxId;
	}

}
