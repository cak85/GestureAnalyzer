package imuanalyzer.utils;


public class SensorVector {
	public double x;
	public double y;
	public double z;

	public SensorVector() {
		x = 0;
		y = 0;
		z = 0;
	}

	public SensorVector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public SensorVector(SensorVector other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public SensorVector substract(SensorVector otherData) {
		SensorVector ret = new SensorVector();
		ret.x = this.x - otherData.x;
		ret.y = this.y - otherData.y;
		ret.z = this.z - otherData.z;
		return ret;
	}

	public SensorVector add(SensorVector otherData) {
		SensorVector ret = new SensorVector();
		ret.x = this.x + otherData.x;
		ret.y = this.y + otherData.y;
		ret.z = this.z + otherData.z;
		return ret;
	}

	public double abs() {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}

	public double skalar(SensorVector otherData) {
		double skalar;
		skalar = this.x * otherData.x;
		skalar += this.y * otherData.y;
		skalar += this.z * otherData.z;
		return skalar;
	}

	public SensorVector cross(SensorVector otherData) {
		SensorVector ret = new SensorVector();
		ret.x = this.y * otherData.z - this.z * otherData.y;
		ret.y = this.z * otherData.x - this.x * otherData.z;
		ret.z = this.x * otherData.y - this.y * otherData.x;
		return ret;
	}

	public SensorVector mult(double skalar) {
		SensorVector ret = new SensorVector();
		ret.x = this.x * skalar;
		ret.y = this.y * skalar;
		ret.z = this.z * skalar;
		return ret;
	}
	
	public static SensorVector[] copyArray(SensorVector[] array){
		SensorVector[] newArray = new SensorVector[array.length];
		
		for (int i = 0; i < newArray.length; i++) {
			newArray[i] = new SensorVector(array[i]);
		}
		
		return newArray;
	}

}