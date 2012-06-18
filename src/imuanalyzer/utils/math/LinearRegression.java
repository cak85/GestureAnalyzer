package imuanalyzer.utils.math;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * Takes in a sequence of pairs of real numbers and computes the best fit (least
 * squares) line y = ax + b through the set of points. Also computes the
 * correlation coefficient and the standard errror of the regression
 * coefficients.
 * 
 */
public class LinearRegression {
	
	private static final Logger LOGGER = Logger.getLogger(LinearRegression.class
			.getName());


	protected double R2 = 0;
	protected double stdErrorBeta0 = 0;
	protected double stdErrorBeta1 = 0;
	protected double beta0 = 0;
	protected double beta1 = 0;
	protected double SSTO = 0;
	protected double SSE = 0;
	protected double SSR = 0;

	public LinearRegression(Iterable<double[]> values) {
		calculate(values);
	}

	private void calculate(Iterable<double[]> values) {
		// first pass: read in data, compute xbar and ybar
		double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;

		int numberOfElements = 0;


		for(double[] v:values){
			numberOfElements++;
			sumx += v[0];
			sumx2 += v[0] * v[0];
			sumy += v[1];
		}
		double xbar = sumx / numberOfElements;
		double ybar = sumy / numberOfElements;

		// second pass: compute summary statistics
		double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
			for(double[] v:values){
			xxbar += (v[0] - xbar) * (v[0] - xbar);
			yybar += (v[1] - ybar) * (v[1] - ybar);
			xybar += (v[0] - xbar) * (v[1] - ybar);
		}
		beta1 = xybar / xxbar;
		beta0 = ybar - beta1 * xbar;

		// print results
		//LOGGER.debug("y   = " + beta1 + " * x + " + beta0);

		// analyze results
		int df = numberOfElements - 2;
		double rss = 0.0; // residual sum of squares
		for(double[] v:values){
			double fit = beta1 * v[0] + beta0;
			rss += (fit - v[1]) * (fit - v[1]);
			SSR += (fit - ybar) * (fit - ybar);
		}
		R2 = SSR / yybar;
		double svar = rss / df;
		double svar1 = svar / xxbar;
		double svar0 = svar / numberOfElements + xbar * xbar * svar1;
		//LOGGER.debug("R^2                 = " + R2);

		stdErrorBeta1 = Math.sqrt(svar1);
		//LOGGER.debug("std error of beta_1 = " + stdErrorBeta1);
		// LOGGER.debug("std error of beta_0 = " + Math.sqrt(svar0));
		svar0 = svar * sumx2 / (numberOfElements * xxbar);
		stdErrorBeta0 = Math.sqrt(svar0);
		//LOGGER.debug("std error of beta_0 = " + stdErrorBeta0);

		SSTO = yybar;
		//LOGGER.debug("SSTO = " + SSTO);
		SSE = rss;
		//LOGGER.debug("SSE  = " + SSE);
		//LOGGER.debug("SSR  = " + SSR);
	}

	public double getR2() {
		return R2;
	}

	public double getStdErrorBeta0() {
		return stdErrorBeta0;
	}

	public double getStdErrorBeta1() {
		return stdErrorBeta1;
	}

	/**
	 * corresponds to n in y=mx+n
	 * @return
	 */
	public double getBeta0() {
		return beta0;
	}

	/**
	 * corresponds to m in y=mx+n
	 * @return
	 */
	public double getBeta1() {
		return beta1;
	}

	/**
	 * (yi-yquer)^2
	 * @return
	 */
	public double getSSTO() {
		return SSTO;
	}

	public double getSSE() {
		return SSE;
	}

	public double getSSR() {
		return SSR;
	}
}