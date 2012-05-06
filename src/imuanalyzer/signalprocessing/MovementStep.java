package imuanalyzer.signalprocessing;

public class MovementStep {
	StoredJointState move;

	int count;

	public MovementStep(StoredJointState move) {
		this.move = move;
		count = 1;
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

}
