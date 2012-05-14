package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.ArrayList;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class Utils {
	
	public static Geometry CreateLine(AssetManager assetManager, ArrayList<Vector3f> points, ColorRGBA color, boolean isClosedLoop, float lineWidth)
	   {
	      // Vertex positions in space
	       Vector3f[] vertices = new Vector3f[points.size()];
	       vertices = points.toArray(vertices);
	 
	       // Indexes. We define the order in which mesh should be constructed
	       int numIndexes = 2 * vertices.length;
	       int numLines = numIndexes / 2;
	       int padding = 0;
	       if (!isClosedLoop)
	       {
	           padding = 1;
	       }
	       short[] indexes = new short[numIndexes];
	       for (int i = 0; i < numLines - padding; i++)
	       {
	           indexes[2 * i] = (short) i;
	           indexes[2 * i + 1] = (short) ((i + 1) % numLines);
	       }
	 
	       // Setting buffers
	       Mesh lineMesh = new Mesh();
	       lineMesh.setMode(Mesh.Mode.Lines);
	       lineMesh.setLineWidth(lineWidth);
	       lineMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
	       lineMesh.setBuffer(Type.Index, 1, BufferUtils.createShortBuffer(indexes));
	       lineMesh.updateBound();
	 
	       Geometry lineGeom = new Geometry("lineMesh", lineMesh);
	       Material matWireframe = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	       matWireframe.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
	       matWireframe.setColor("Color", color.clone());
	       matWireframe.getAdditionalRenderState().setWireframe(true);
	       lineGeom.setMaterial(matWireframe);
	       return lineGeom;
	   }
	
	public static void updateLine(Geometry line, ArrayList<Vector3f> points,boolean isClosedLoop){
		// Vertex positions in space
	       Vector3f[] vertices = new Vector3f[points.size()];
	       vertices = points.toArray(vertices);
	 
	       // Indexes. We define the order in which mesh should be constructed
	       int numIndexes = 2 * vertices.length;
	       int numLines = numIndexes / 2;
	       int padding = 0;
	       if (!isClosedLoop)
	       {
	           padding = 1;
	       }
	       short[] indexes = new short[numIndexes];
	       for (int i = 0; i < numLines - padding; i++)
	       {
	           indexes[2 * i] = (short) i;
	           indexes[2 * i + 1] = (short) ((i + 1) % numLines);
	       }
	       
	       Mesh lineMesh = line.getMesh();
	       
	       lineMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
	       lineMesh.setBuffer(Type.Index, 1, BufferUtils.createShortBuffer(indexes));
	       lineMesh.updateBound();
	}

	public static Quaternion getJMEQuad(imuanalyzer.filter.Quaternion quad) {
		return new Quaternion((float) quad.get(1) * -1, (float) quad.get(2)
				* -1, (float) quad.get(3), (float) quad.get(0));
	}

	public static imuanalyzer.filter.Quaternion getSensorQuad(Quaternion quad) {
		return new imuanalyzer.filter.Quaternion(quad.getW(), quad.getX() * -1,
				quad.getY() * -1, quad.getZ());
	}
	
	public static Vector3f getPosition(imuanalyzer.filter.Quaternion quad){
		return new Vector3f((float)quad.getX(), (float)quad.getY(), (float)quad.getZ());
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
