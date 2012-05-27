package imuanalyzer.signalprocessing;

import java.util.ArrayList;

import com.jme3.math.Vector3f;

public class Helper {
	public static ArrayList<VectorLine> getExampleTouchlineOdd() {
		ArrayList<VectorLine> touchlines = new ArrayList<VectorLine>();

		ArrayList<Vector3f> p1 = new ArrayList<Vector3f>();
		p1.add(new Vector3f(1, 1, 0));
		p1.add(new Vector3f(1, 2, 0));
		p1.add(new Vector3f(1, 3, 0));
		p1.add(new Vector3f(1, 4, 0));
		p1.add(new Vector3f(1, 5, 0));
		VectorLine t1 = new VectorLine(p1);
		touchlines.add(t1);

		ArrayList<Vector3f> p2 = new ArrayList<Vector3f>();
		p2.add(new Vector3f(2, 1, 0));
		p2.add(new Vector3f(2, 2, 0));
		p2.add(new Vector3f(2, 3, 0));
		p2.add(new Vector3f(2, 4, 0));
		p2.add(new Vector3f(2, 5, 0));
		p2.add(new Vector3f(2, 6, 0));
		VectorLine t2 = new VectorLine(p2);
		touchlines.add(t2);

		ArrayList<Vector3f> p3 = new ArrayList<Vector3f>();
		p3.add(new Vector3f(1, 1, 0));
		p3.add(new Vector3f(2, 1, 0));
		p3.add(new Vector3f(3, 1, 0));
		VectorLine t3 = new VectorLine(p3);
		touchlines.add(t3);

		ArrayList<Vector3f> p4 = new ArrayList<Vector3f>();
		p4.add(new Vector3f(5, 1, 0));
		p4.add(new Vector3f(5, 2, 0));
		VectorLine t4 = new VectorLine(p4);
		touchlines.add(t4);

		ArrayList<Vector3f> p5 = new ArrayList<Vector3f>();
		p5.add(new Vector3f(8, 1, 0));
		p5.add(new Vector3f(8, 2, 0));
		p5.add(new Vector3f(8, 3, 0));
		p5.add(new Vector3f(8, 4, 0));
		VectorLine t5 = new VectorLine(p5);
		touchlines.add(t5);
		return touchlines;
	}
	
	public static ArrayList<VectorLine> getExampleTouchlineEven() {
		ArrayList<VectorLine> touchlines = new ArrayList<VectorLine>();

		ArrayList<Vector3f> p1 = new ArrayList<Vector3f>();
		p1.add(new Vector3f(1, 1, 0));
		p1.add(new Vector3f(1, 2, 0));
		p1.add(new Vector3f(1, 3, 0));
		p1.add(new Vector3f(1, 4, 0));
		p1.add(new Vector3f(1, 5, 0));
		VectorLine t1 = new VectorLine(p1);
		touchlines.add(t1);

		ArrayList<Vector3f> p2 = new ArrayList<Vector3f>();
		p2.add(new Vector3f(2, 1, 0));
		p2.add(new Vector3f(2, 2, 0));
		p2.add(new Vector3f(2, 3, 0));
		p2.add(new Vector3f(2, 4, 0));
		p2.add(new Vector3f(3, 4, 0));
		p2.add(new Vector3f(4, 4, 0));
		VectorLine t2 = new VectorLine(p2);
		touchlines.add(t2);

		ArrayList<Vector3f> p3 = new ArrayList<Vector3f>();
		p3.add(new Vector3f(1, 1, 0));
		p3.add(new Vector3f(2, 1, 0));
		p3.add(new Vector3f(3, 1, 0));
		VectorLine t3 = new VectorLine(p3);
		touchlines.add(t3);

		ArrayList<Vector3f> p4 = new ArrayList<Vector3f>();
		p4.add(new Vector3f(5, 1, 0));
		p4.add(new Vector3f(5, 1.5f, 0));
		VectorLine t4 = new VectorLine(p4);
		touchlines.add(t4);

		ArrayList<Vector3f> p5 = new ArrayList<Vector3f>();
		p5.add(new Vector3f(8, 1, 0));
		p5.add(new Vector3f(8, 2, 0));
		p5.add(new Vector3f(8, 3, 0));
		p5.add(new Vector3f(8, 4, 0));
		VectorLine t5 = new VectorLine(p5);
		touchlines.add(t5);
		return touchlines;
	}
}
