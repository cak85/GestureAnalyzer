package imuanalyzer.utils.math;

import org.apache.log4j.Logger;

/**
 * Reduced version of other LinearRegressionImplementation Takes in a sequence
 * of pairs of real numbers and computes the best fit (least squares) line y =
 * ax + b through the set of points. Also computes the correlation coefficient
 * Based on
 * http://introcs.cs.princeton.edu/java/97data/LinearRegression.java.html
 */
public class FastLinearRegression {

	private static final Logger LOGGER = Logger
			.getLogger(FastLinearRegression.class.getName());

	protected double R2 = 0;

	protected double constant = 0;
	protected double coefficient = 0;

	public FastLinearRegression(Iterable<double[]> values) {
		calculate(values);
	}

	/**
	 * Calculate the linerar regression
	 * @param values
	 */
	private void calculate(Iterable<double[]> values) {
		// first pass: read in data, compute xbar and ybar
		double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;

		int numberOfElements = 0;

		for (double[] v : values) {
			numberOfElements++;
			sumx += v[0];
			sumx2 += v[0] * v[0];
			sumy += v[1];
		}
		double xbar = sumx / numberOfElements;
		double ybar = sumy / numberOfElements;

		// second pass: compute summary statistics
		double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
		for (double[] v : values) {
			xxbar += (v[0] - xbar) * (v[0] - xbar);
			yybar += (v[1] - ybar) * (v[1] - ybar);
			xybar += (v[0] - xbar) * (v[1] - ybar);
		}
		coefficient = xybar / xxbar;
		constant = ybar - coefficient * xbar;

		// print results
		LOGGER.debug("y   = " + coefficient + " * x + " + constant);

		double SSR = 0;

		for (double[] v : values) {
			double fit = coefficient * v[0] + constant;
			SSR += (fit - ybar) * (fit - ybar);
		}
		R2 = SSR / yybar;
		LOGGER.debug("R²:"+R2);
	}

	public double getR2() {
		return R2;
	}

	/**
	 * corresponds to n in y=mx+n
	 * 
	 * @return
	 */
	public double getConstant() {
		return constant;
	}

	/**
	 * corresponds to m in y = mx + n
	 * 
	 * @return
	 */
	public double getCoefficient() {
		return coefficient;
	}
}