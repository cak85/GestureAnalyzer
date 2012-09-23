package imuanalyzer.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * IIR filter implementation Based on
 * http://code.google.com/p/9dof-orientation-estimation/ coefficients are taken
 * by matlab
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class IIRFilter {
	private List<Double> a;
	private List<Double> b;

	/**
	 * default Filter
	 */
	public IIRFilter() {

		b = new ArrayList<Double>();
		b.add(0.002899695497431);
		b.add(-0.006626465760968);
		b.add(0.004033620976099);
		b.add(0.004033620976099);
		b.add(-0.006626465760968);
		b.add(0.002899695497431);

		a = new ArrayList<Double>();
		a.add(1.000000000000000);
		a.add(-4.229081817661462);
		a.add(7.205853343227314);
		a.add(-6.177477993982333);
		a.add(2.662714482809827);
		a.add(-0.461394312968222);

	}

	/**
	 * Define filter coefficients
	 * 
	 * @param a
	 * @param b
	 */
	public IIRFilter(List<Double> a, List<Double> b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * Use filter for given number of values
	 * 
	 * @param x
	 * @param y
	 */
	public void Applyfilter(List<Double> x, List<Double> y) {
		int ord = a.size() - 1;
		int np = x.size() - 1;

		if (np < ord) {
			for (int k = 0; k < ord - np; k++)
				x.add(0.0);
			np = ord;
		}

		y.clear();

		for (int k = 0; k < np + 1; k++) {
			y.add(0.0);
		}
		int i, j;
		y.set(0, b.get(0) * x.get(0));
		for (i = 1; i < ord + 1; i++) {
			y.set(i, 0.0);
			for (j = 0; j < i + 1; j++)
				y.set(i, y.get(i) + b.get(j) * x.get(i - j));
			for (j = 0; j < i; j++)
				y.set(i, y.get(i) - a.get(j + 1) * y.get(i - j - 1));
		}
		/* end of initial part */
		for (i = ord + 1; i < np + 1; i++) {
			y.set(i, 0.0);
			for (j = 0; j < ord + 1; j++)
				y.set(i, y.get(i) + b.get(j) * x.get(i - j));
			for (j = 0; j < ord; j++)
				y.set(i, y.get(i) - a.get(j + 1) * y.get(i - j - 1));
		}
	}
}
