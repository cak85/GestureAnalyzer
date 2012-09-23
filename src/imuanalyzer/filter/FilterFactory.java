package imuanalyzer.filter;

import imuanalyzer.filter.filterimpl.CFMayhonyWithMagneticDistorsion;
import imuanalyzer.filter.filterimpl.CFMadgwick;
import imuanalyzer.filter.filterimpl.CFFreeIMU;
import imuanalyzer.filter.filterimpl.CFMahony;
import imuanalyzer.filter.filterimpl.KalmanFilter;
import imuanalyzer.filter.filterimpl.MyTemperatureCorrectedFilter;
import imuanalyzer.filter.filterimpl.QuaternionComplementaryFilter;
import imuanalyzer.filter.filterimpl.VaranesoDOF;

/**
 * Create filters by type in this factory
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class FilterFactory {

	public enum FilterTypes {
		KALMAN, CF_QUATERNION, CF_MAHONY_MAGNETIC_DISTORSION, CF_MAHONY, CF_MADGWICK_GRADIENT_DECENT, VARANESO_DOF, CF_FREEIMU, MY_FILTER
	}

	public static Filter getFilter(FilterTypes type) {
		Filter filter;

		switch (type) {
		case KALMAN:
			filter = new KalmanFilter();
			break;
		case CF_MAHONY_MAGNETIC_DISTORSION:
			filter = new CFMayhonyWithMagneticDistorsion();
			break;
		case CF_QUATERNION:
			filter = new QuaternionComplementaryFilter();
			break;
		case CF_MAHONY:
			filter = new CFMahony();
			break;
		case CF_MADGWICK_GRADIENT_DECENT:
			filter = new CFMadgwick();
			break;
		case CF_FREEIMU:
			filter = new CFFreeIMU();
			break;
		case VARANESO_DOF:
			filter = new VaranesoDOF();
			break;
		case MY_FILTER:
			filter = new MyTemperatureCorrectedFilter();
			break;
		default:
			filter = new CFMayhonyWithMagneticDistorsion();
		}
		return filter;
	}
}
