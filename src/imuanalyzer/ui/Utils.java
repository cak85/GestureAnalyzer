package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.VectorLine;

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
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class Utils {

	public static Geometry CreateLine(AssetManager assetManager,
			ArrayList<Vector3f> points, ColorRGBA color, boolean isClosedLoop,
			float lineWidth) {
		// Vertex positions in space
		Vector3f[] vertices = new Vector3f[points.size()];
		vertices = points.toArray(vertices);

		// Indexes. We define the order in which mesh should be constructed
		int numIndexes = 2 * vertices.length;
		int numLines = numIndexes / 2;
		int padding = 0;
		if (!isClosedLoop) {
			padding = 1;
		}
		short[] indexes = new short[numIndexes];
		for (int i = 0; i < numLines - padding; i++) {
			indexes[2 * i] = (short) i;
			indexes[2 * i + 1] = (short) ((i + 1) % numLines);
		}

		// Setting buffers
		Mesh lineMesh = new Mesh();
		lineMesh.setMode(Mesh.Mode.Lines);
		lineMesh.setLineWidth(lineWidth);
		lineMesh.setBuffer(Type.Position, 3,
				BufferUtils.createFloatBuffer(vertices));
		lineMesh.setBuffer(Type.Index, 1,
				BufferUtils.createShortBuffer(indexes));
		lineMesh.updateBound();

		Geometry lineGeom = new Geometry("lineMesh", lineMesh);
		Material matWireframe = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		matWireframe.getAdditionalRenderState().setFaceCullMode(
				FaceCullMode.Off);
		matWireframe.setColor("Color", color.clone());
		//matWireframe.getAdditionalRenderState().setWireframe(true);
		lineGeom.setMaterial(matWireframe);
		return lineGeom;
	}

	public static void updateLine(Geometry line, ArrayList<Vector3f> points,
			boolean isClosedLoop) {
		// Vertex positions in space
		Vector3f[] vertices = new Vector3f[points.size()];
		vertices = points.toArray(vertices);

		if (vertices.length < 2) {
			return;
		}

		// Indexes. We define the order in which mesh should be constructed
		int numIndexes = 2 * vertices.length;
		int numLines = numIndexes / 2;
		int padding = 0;
		if (!isClosedLoop) {
			padding = 1;
			numIndexes -= 2;
		}
		short[] indexes = new short[numIndexes];

		for (int i = 0; i < numLines - padding; i++) {
			indexes[2 * i] = (short) i;
			indexes[2 * i + 1] = (short) ((i + 1) % numLines);
		}

		updateGeometryMesh(line, vertices, indexes);
	}

	public static void updateLines(Geometry line,
			ArrayList<ArrayList<Vector3f>> points) {

		// calculate size
		int size = 0;
		int numberOfLines = 0;
		for (ArrayList<Vector3f> p : points) {
			int tmpSize = p.size();
			if (tmpSize > 1) {
				size += p.size();
				numberOfLines++;
			}
		}

		if (size < 2) {
			return;
		}

		// Vertex positions in space
		Vector3f[] vertices = new Vector3f[size];

		// Indexes. We define the order in which mesh should be constructed
		int numIndexes = 2 * (size) - 2 * numberOfLines;

		short[] indexes = new short[numIndexes];

		ArrayList<Vector3f> tmpVertices = new ArrayList<Vector3f>();

		int iStart = 0;
		for (int j = 0; j < numberOfLines; j++) {
			ArrayList<Vector3f> p = points.get(j);
			int end = iStart + p.size() - 1;
			for (int i = iStart; i < end; i++) {
				int vecIdx = i + j;
				indexes[2 * i] = (short) vecIdx;
				int secondIdx=2 * i + 1;
				if (secondIdx<indexes.length) {
					indexes[secondIdx] = (short) ((vecIdx + 1));
				}
			}
			iStart = end;
			tmpVertices.addAll(p);
		}
		vertices = tmpVertices.toArray(vertices);	

		updateGeometryMesh(line, vertices, indexes);
	}
	
	public static void updateLinesTouch(Geometry line,
			ArrayList<VectorLine> lines) {

		// calculate size
		int size = 0;
		int numberOfLines = 0;
		for (VectorLine t :lines) {
			int tmpSize = t.getLineBuffer().size();
			if (tmpSize > 1) {
				size += tmpSize;
				numberOfLines++;
			}
		}

		Vector3f[] vertices;
		short[] indexes;
		
		if (size > 2) {
			
		// Vertex positions in space
		vertices = new Vector3f[size];

		// Indexes. We define the order in which mesh should be constructed
		int numIndexes = 2 * (size) - 2 * numberOfLines;

		indexes = new short[numIndexes];

		ArrayList<Vector3f> tmpVertices = new ArrayList<Vector3f>();

		int iStart = 0;
		for (int j = 0; j < numberOfLines; j++) {
			ArrayList<Vector3f> p = lines.get(j).getLineBuffer();
			int end = iStart + p.size() - 1;
			for (int i = iStart; i < end; i++) {
				int vecIdx = i + j;
				indexes[2 * i] = (short) vecIdx;
				int secondIdx=2 * i + 1;
				if (secondIdx<indexes.length) {
					indexes[secondIdx] = (short) ((vecIdx + 1));
				}
			}
			iStart = end;
			tmpVertices.addAll(p);
		}
		vertices = tmpVertices.toArray(vertices);	
		
		}else{
			vertices = new Vector3f[0];
			indexes = new short[0];
		}

		updateGeometryMesh(line, vertices, indexes);
	}

	public static void updateGeometryMesh(Geometry geo, Vector3f[] vertices,
			short[] indexes) {
		Mesh lineMesh = geo.getMesh();

		lineMesh.setBuffer(Type.Position, 3,
				BufferUtils.createFloatBuffer(vertices));
		lineMesh.setBuffer(Type.Index, 1,
				BufferUtils.createShortBuffer(indexes));
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

	public static Vector3f getPosition(imuanalyzer.filter.Quaternion quad) {
		return new Vector3f((float) quad.getX(), (float) quad.getY(),
				(float) quad.getZ());
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

	public static Node findNodeByName(Node node, String name) {
		for (Spatial s : node.getChildren()) {
			if (s instanceof Node) {
				Node n = (Node) s;
				if (n.getName().equals(name)) {
					return n;
				} else {
					Node n2 = findNodeByName(n, name);
					if (n2 != null) {
						return n2;
					}
				}

			}
		}
		return null;
	}
}
