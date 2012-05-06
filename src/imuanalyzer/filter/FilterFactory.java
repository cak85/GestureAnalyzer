package imuanalyzer.filter;

import imuanalyzer.filter.filterimpl.AHRSFilter;
import imuanalyzer.filter.filterimpl.AHRSFilterMadgwick;
import imuanalyzer.filter.filterimpl.AHRSFilterMahony;
import imuanalyzer.filter.filterimpl.KalmanFilter;
import imuanalyzer.filter.filterimpl.QuaternionComplementaryFilter;

public class FilterFactory {

	public enum FilterTypes {
		KALMAN, QUATERNION_COMPLEMENTARY,AHRS,AHRSMAHONY,AHRSMADGWICK
	}

	public static Filter getFilter(FilterTypes type) {
		Filter filter;

		switch (type) {
		case KALMAN:
			filter = new KalmanFilter();
			break;
		case AHRS:
			filter = new AHRSFilter();
			break;
		case QUATERNION_COMPLEMENTARY:
			filter = new QuaternionComplementaryFilter();
			break;
		case AHRSMAHONY:
			filter = new AHRSFilterMahony();
			break;
		case AHRSMADGWICK:
			filter = new AHRSFilterMadgwick();
			break;
		default:
			filter = new AHRSFilter();
		}
		return filter;
	}
}
