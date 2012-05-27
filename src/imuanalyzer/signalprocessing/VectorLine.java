package imuanalyzer.signalprocessing;

import java.util.ArrayList;

import com.jme3.math.Vector3f;

public class VectorLine implements Comparable<VectorLine> {
	ArrayList<Vector3f> lineBuffer = new ArrayList<Vector3f>();

	float length = 0;

	public VectorLine() {
		this(new ArrayList<Vector3f>());
	}

	public VectorLine(ArrayList<Vector3f> lineBuffer) {
		this(lineBuffer, 0);
	}

	public VectorLine(ArrayList<Vector3f> lineBuffer, float length) {
		this.lineBuffer = lineBuffer;
		this.length = length;
	}

	public void updateLength() {
		length = 0;
		for (int i = 1; i < lineBuffer.size(); i++) {
			length += lineBuffer.get(i).subtract(lineBuffer.get(i - 1))
					.length();
		}
	}

	public ArrayList<Vector3f> getLineBuffer() {
		return lineBuffer;
	}

	public void setLineBuffer(ArrayList<Vector3f> lineBuffer) {
		this.lineBuffer = lineBuffer;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public void addLength(float addLength) {
		this.length += addLength;
	}

	@Override
	public int compareTo(VectorLine o) {
		if (this.length > o.length) {
			return 1;
		} else if (this.length < o.length) {
			return -1;
		} else {
			return 0;
		}
	}

}
