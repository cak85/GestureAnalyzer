package imuanalyzer.signalprocessing;

import java.util.ArrayList;

import com.jme3.math.Vector3f;

public class Helper {
	public static ArrayList<TouchLine> getExampleTouchlineOdd() {
		ArrayList<TouchLine> touchlines = new ArrayList<TouchLine>();

		ArrayList<Vector3f> p1 = new ArrayList<Vector3f>();
		p1.add(new Vector3f(1, 1, 0));
		p1.add(new Vector3f(1, 2, 0));
		p1.add(new Vector3f(1, 3, 0));
		p1.add(new Vector3f(1, 4, 0));
		p1.add(new Vector3f(1, 5, 0));
		TouchLine t1 = new TouchLine(p1);
		touchlines.add(t1);

		ArrayList<Vector3f> p2 = new ArrayList<Vector3f>();
		p2.add(new Vector3f(2, 1, 0));
		p2.add(new Vector3f(2, 2, 0));
		p2.add(new Vector3f(2, 3, 0));
		p2.add(new Vector3f(2, 4, 0));
		p2.add(new Vector3f(2, 5, 0));
		p2.add(new Vector3f(2, 6, 0));
		TouchLine t2 = new TouchLine(p2);
		touchlines.add(t2);

		ArrayList<Vector3f> p3 = new ArrayList<Vector3f>();
		p3.add(new Vector3f(1, 1, 0));
		p3.add(new Vector3f(2, 1, 0));
		p3.add(new Vector3f(3, 1, 0));
		TouchLine t3 = new TouchLine(p3);
		touchlines.add(t3);

		ArrayList<Vector3f> p4 = new ArrayList<Vector3f>();
		p4.add(new Vector3f(5, 1, 0));
		p4.add(new Vector3f(5, 2, 0));
		TouchLine t4 = new TouchLine(p4);
		touchlines.add(t4);

		ArrayList<Vector3f> p5 = new ArrayList<Vector3f>();
		p5.add(new Vector3f(8, 1, 0));
		p5.add(new Vector3f(8, 2, 0));
		p5.add(new Vector3f(8, 3, 0));
		p5.add(new Vector3f(8, 4, 0));
		TouchLine t5 = new TouchLine(p5);
		touchlines.add(t5);
		return touchlines;
	}
	
	public static ArrayList<TouchLine> getExampleTouchlineEven() {
		ArrayList<TouchLine> touchlines = new ArrayList<TouchLine>();

		ArrayList<Vector3f> p1 = new ArrayList<Vector3f>();
		p1.add(new Vector3f(1, 1, 0));
		p1.add(new Vector3f(1, 2, 0));
		p1.add(new Vector3f(1, 3, 0));
		p1.add(new Vector3f(1, 4, 0));
		p1.add(new Vector3f(1, 5, 0));
		TouchLine t1 = new TouchLine(p1);
		touchlines.add(t1);

		ArrayList<Vector3f> p2 = new ArrayList<Vector3f>();
		p2.add(new Vector3f(2, 1, 0));
		p2.add(new Vector3f(2, 2, 0));
		p2.add(new Vector3f(2, 3, 0));
		p2.add(new Vector3f(2, 4, 0));
		p2.add(new Vector3f(3, 4, 0));
		p2.add(new Vector3f(4, 4, 0));
		TouchLine t2 = new TouchLine(p2);
		touchlines.add(t2);

		ArrayList<Vector3f> p3 = new ArrayList<Vector3f>();
		p3.add(new Vector3f(1, 1, 0));
		p3.add(new Vector3f(2, 1, 0));
		p3.add(new Vector3f(3, 1, 0));
		TouchLine t3 = new TouchLine(p3);
		touchlines.add(t3);

		ArrayList<Vector3f> p4 = new ArrayList<Vector3f>();
		p4.add(new Vector3f(5, 1, 0));
		p4.add(new Vector3f(5, 1.5f, 0));
		TouchLine t4 = new TouchLine(p4);
		touchlines.add(t4);

		ArrayList<Vector3f> p5 = new ArrayList<Vector3f>();
		p5.add(new Vector3f(8, 1, 0));
		p5.add(new Vector3f(8, 2, 0));
		p5.add(new Vector3f(8, 3, 0));
		p5.add(new Vector3f(8, 4, 0));
		TouchLine t5 = new TouchLine(p5);
		touchlines.add(t5);
		return touchlines;
	}
}
