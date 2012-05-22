package imuanalyzer.signalprocessing;

import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.EnumMap;

public class MovementStep {
	StoredJointState move;

	int count;
	
	EnumMap<JointType, StoredJointState> jointSet;

	public MovementStep(StoredJointState move) {
		this.move = move;
		count = 1;
		jointSet = move.getAll();
	}
	
	public void incCount(){
		count++;
	}	
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public StoredJointState getMove() {
		return move;
	}
	
	public EnumMap<JointType, StoredJointState> getJointSet(){
		return jointSet;
	}

}
