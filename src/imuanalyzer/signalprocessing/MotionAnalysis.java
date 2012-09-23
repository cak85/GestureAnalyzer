package imuanalyzer.signalprocessing;

import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.IInfoContent;
import imuanalyzer.ui.jmonkey.Utils;
import imuanalyzer.utils.math.AngleHelper;
import imuanalyzer.utils.math.Quaternion;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.jme3.math.Vector3f;

/**
 * Analysis of finger motion
 * @author Christopher-Eyk Hrabia
 *
 */
public class MotionAnalysis implements IInfoContent {

	private static final Logger LOGGER = Logger.getLogger(MotionAnalysis.class
			.getName());

	private static final double MIN_ANGLE_DIFFERENCE = AngleHelper
			.radFromDeg(10);

	Hand hand;
	Joint observedJoint;

	protected LinkedList<MovementStep> savedMovementFlow = new LinkedList<MovementStep>();
	protected JointType saveMovementStartJoint = JointType.RING_BOTTOM;
	protected Boolean saveMovement = false;
	protected double movementMinDifference = 0.12;

	int maxId = 0;
	int minId = 0;

	ArrayList<Integer> minIds = new ArrayList<Integer>();

	ArrayList<Integer> maxIds = new ArrayList<Integer>();

	ArrayList<VectorLine> maxLines = new ArrayList<VectorLine>();
	ArrayList<VectorLine> minLines = new ArrayList<VectorLine>();

	String infoName;

	public MotionAnalysis(Hand hand, Joint observedJoint) {
		this.hand = hand;
		this.observedJoint = observedJoint;
		addInitialSavedMove();
		infoName = "Motion " + observedJoint.getInfoName();
	}

	public void clear() {
		savedMovementFlow.clear();
		addInitialSavedMove();
		maxLines.clear();
		minLines.clear();
		maxId = 0;
		minId = 0;
	}

	MovementStep lastChanged;

	int maxCount = 0;

	public void update(Joint updatedBy) {

		for (MovementStep s : savedMovementFlow) {
			s.getMove().updateWorldOrientation();
		}

		// check if updateBy is parent of currently observed joint
		// if yes its not neccessary to update
		if (observedJoint.hasParent(updatedBy)) {
			return;
		}

		StoredJointState newState = new StoredJointState(observedJoint,
				observedJoint.parent, true);

		if (lastChanged != null) {
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
			if (m != lastChanged && m.getMove().equals(newState)) {
				m.incCount();
				maxCount = Math.max(maxCount, m.getCount());
				// LOGGER.debug("Find existing position - Increase count");
				return;
			}
		}

		// LOGGER.debug("Created new one");
		lastChanged = new MovementStep(newState);
		savedMovementFlow.addLast(lastChanged);

		// get right parent
		// save extrema
		checkExtrema(newState);
	}

	private void checkExtrema(StoredJointState newState) {

		if (newState.compareTo(getMinMovementStep().move) < 0) {
			minId = savedMovementFlow.size() - 1;
			minIds.add(minId);
		}
		if (newState.compareTo(getMaxMovementStep().move) > 0) {
			maxId = savedMovementFlow.size() - 1;
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
		maxLines = getLineToId(maxIds);
		return maxLines;
	}

	public ArrayList<VectorLine> getMinLine() {
		minLines = getLineToId(minIds);
		return minLines;
	}

	/**
	 * Get Line over given ids of saved motion states
	 * 
	 * @param ids
	 * @return
	 */
	protected ArrayList<VectorLine> getLineToId(ArrayList<Integer> ids) {
		if (savedMovementFlow.size() < 1) {
			return new ArrayList<VectorLine>();
		}
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

	/**
	 * Get the maximum motion passes through one position over all positions
	 * @return
	 */
	public int getMaxCount() {
		return maxCount;
	}

	@Override
	public String getInfoName() {
		return infoName;
	}

	@Override
	public String getInfoValue() {
		StringBuffer info = new StringBuffer();
		int minSize = minLines.size();
		int maxSize = maxLines.size();
		for (int i = 0; i < Math.max(minSize, maxSize); i++) {
			if (i < minSize) {
				info.append("min:");
				info.append(String.format("%.2f", minLines.get(i).getLength()));
			}
			if (i < maxSize) {
				info.append(" max:");
				info.append(String.format("%.2f", maxLines.get(i).getLength()));
			}
		}
		return info.toString();
	}

}
