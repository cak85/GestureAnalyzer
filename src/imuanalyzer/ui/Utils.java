package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand.JointType;

import com.jme3.material.RenderState;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;

public class Utils {

	public static Quaternion getJMEQuad(imuanalyzer.filter.Quaternion quad) {
		return new Quaternion((float) quad.get(1) * -1, (float) quad.get(2)
				* -1, (float) quad.get(3), (float) quad.get(0));
	}

	public static imuanalyzer.filter.Quaternion getSensorQuad(Quaternion quad) {
		return new imuanalyzer.filter.Quaternion(quad.getW(), quad.getX() * -1,
				quad.getY() * -1, quad.getZ());
	}

	public static void switchVisibility(Geometry geom) {
		RenderState state = geom.getMaterial().getAdditionalRenderState();
		System.out.println("CurrentCulling" + state.getFaceCullMode());
		if (state.getFaceCullMode() == FaceCullMode.Off) {
			state.setFaceCullMode(FaceCullMode.FrontAndBack);
		} else {
			state.setFaceCullMode(FaceCullMode.Off);
		}
	}

	public static void setVisible(Geometry geom, boolean visible) {
		RenderState state = geom.getMaterial().getAdditionalRenderState();
		if (visible) {
			state.setFaceCullMode(FaceCullMode.Off);			
		} else {
			state.setFaceCullMode(FaceCullMode.FrontAndBack);
		}
	}

	/**
	 * This method assumes that all geometries with reference to a bone have a
	 * postfix with .JOINTTYPE
	 * 
	 * @param geom
	 * @return
	 */
	public static JointType getJointTypeFromGeometry(Geometry geom) {
		String name = geom.getName();

		int pointIdx = name.indexOf(".");

		if (pointIdx > -1) {

			name = name.substring(pointIdx + 1);

			JointType type = null;
			try {
				type = JointType.valueOf(name);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return type;
		} else {
			return null;
		}
	}
}
