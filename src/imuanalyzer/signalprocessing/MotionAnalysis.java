package imuanalyzer.signalprocessing;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.Utils;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.jme3.math.Vector3f;

public class MotionAnalysis {

	private static final Logger LOGGER = Logger.getLogger(MotionAnalysis.class
			.getName());

	private static final double MIN_ANGLE_DIFFERENCE = 10 * (Math.PI / 180);

	Hand hand;
	Joint observedJoint;

	protected LinkedList<MovementStep> savedMovementFlow = new LinkedList<MovementStep>();
	protected JointType saveMovementStartJoint = JointType.RING_BOTTOM;
	protected Boolean saveMovement = false;
	protected double movementMinDifference = 0.12;

	int maxId = 0;
	int minId = 0;

	VectorLine maxLine = new VectorLine();
	VectorLine minLine = new VectorLine();

	ArrayList<Integer> minIds = new ArrayList<Integer>();

	ArrayList<Integer> maxIds = new ArrayList<Integer>();

	public MotionAnalysis(Hand hand, Joint observedJoint) {
		this.hand = hand;
		this.observedJoint = observedJoint;
		addInitialSavedMove();
	}

	public void clear() {
		savedMovementFlow.clear();
		addInitialSavedMove();
	}
	
	MovementStep lastChanged;

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
		

		if(lastChanged!=null){
			StoredJointState lastState = lastChanged.getMove();

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
			if (m!=lastChanged && m.getMove().equals(newState)) {
				m.incCount();
				LOGGER.debug("Find existing position - Increase count");
				return;
			}
		}

		// get right parent
		// save extrema
		checkExtrema(newState);

		// LOGGER.debug("Created new one");
		lastChanged=new MovementStep(newState);
		savedMovementFlow.addLast(lastChanged);
	}

	private void checkExtrema(StoredJointState newState) {

		if (newState.compareTo(getMinMovementStep().move) < 0) {
			minId = savedMovementFlow.size();
			minIds.add(minId);
		}
		if (newState.compareTo(getMaxMovementStep().move) > 0) {
			maxId = savedMovementFlow.size();
			maxIds.add(maxId);
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
		maxIds.add(0);
		minIds.add(0);

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

	public ArrayList<VectorLine> getMaxLine() {
		return getLineToId(maxIds);
	}

	public ArrayList<VectorLine> getMinLine() {
		return getLineToId(minIds);
	}

	protected ArrayList<VectorLine> getLineToId(ArrayList<Integer> ids) {
		ArrayList<VectorLine> lines = new ArrayList<VectorLine>();

		int numberOfChildren = getMaxMovementStep().getMove().children.size();

		if (numberOfChildren == 0) {
			ArrayList<Vector3f> points = new ArrayList<Vector3f>();

			for (Integer i : ids) {
				MovementStep m = savedMovementFlow.get(i);
				Quaternion quat = m.getMove().getFingerTopPosition();
				points.add(Utils.quatToVecPos(quat));
			}
			lines.add(new VectorLine(points));
		} else {
			for (int j = 0; j < numberOfChildren; j++) {

				ArrayList<Vector3f> points = new ArrayList<Vector3f>();

				for (Integer i : ids) {
					MovementStep m = savedMovementFlow.get(i);
					Quaternion quat = m.getMove().children.get(j)
							.getLatestChild().getFingerTopPosition();
					points.add(Utils.quatToVecPos(quat));
				}
				lines.add(new VectorLine(points));
			}
		}
		return lines;
	}

}
