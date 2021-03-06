package imuanalyzer.ui.jmonkey;

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

/**
 * Helper methods for type conversion and simple object/model creation
 * @author Christopher-Eyk Hrabia
 *
 */
public class Utils {

	/**
	 * Get position vector from Quaternion
	 * @param quat
	 * @return Vector
	 */
	public static Vector3f quatToVecPos(imuanalyzer.utils.math.Quaternion quat) {
		return new Vector3f((float) quat.getX() * -1, (float) quat.getY(),
				(float) quat.getZ() * -1);
	}

	/**
	 * Create a 3d line
	 * @param assetManager
	 * @param vLine
	 * @param color
	 * @param isClosedLoop
	 * @param lineWidth
	 * @return
	 */
	public static Geometry CreateLine(AssetManager assetManager,
			VectorLine vLine, ColorRGBA color, boolean isClosedLoop,
			float lineWidth) {
		return CreateLine(assetManager, vLine.getLineBuffer(), color,
				isClosedLoop, lineWidth);
	}

	/**
	 * Create a 3d line
	 * @param assetManager
	 * @param points
	 * @param color
	 * @param isClosedLoop
	 * @param lineWidth
	 * @return
	 */
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
		matWireframe.setColor("Color", color);
		// matWireframe.getAdditionalRenderState().setWireframe(true);
		lineGeom.setMaterial(matWireframe);
		return lineGeom;
	}

	/**
	 * Create several 3d lines from line vector in one geometry
	 * @param assetManager
	 * @param lines
	 * @param color
	 * @param isClosedLoop
	 * @param lineWidth
	 * @return
	 */
	public static Geometry CreateLinesVec(AssetManager assetManager,
			ArrayList<VectorLine> lines, ColorRGBA color, boolean isClosedLoop,
			float lineWidth) {
		// calculate size
		int size = 0;
		int numberOfLines = 0;
		for (VectorLine t : lines) {
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
					int secondIdx = 2 * i + 1;
					if (secondIdx < indexes.length) {
						indexes[secondIdx] = (short) ((vecIdx + 1));
					}
				}
				iStart = end;
				tmpVertices.addAll(p);
			}
			vertices = tmpVertices.toArray(vertices);

		} else {
			vertices = new Vector3f[0];
			indexes = new short[0];
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
		matWireframe.setColor("Color", color);
		// matWireframe.getAdditionalRenderState().setWireframe(true);
		lineGeom.setMaterial(matWireframe);
		return lineGeom;
	}

	/**
	 * Update 3d line
	 * @param line
	 * @param vLine
	 * @param isClosedLoop
	 * @param color
	 */
	public static void updateLine(Geometry line, VectorLine vLine,
			boolean isClosedLoop, ColorRGBA color) {
		updateLine(line, vLine.getLineBuffer(), isClosedLoop, color);
	}

	/**
	 * Update 3d line
	 * @param line
	 * @param points
	 * @param isClosedLoop
	 * @param color
	 */
	public static void updateLine(Geometry line, ArrayList<Vector3f> points,
			boolean isClosedLoop, ColorRGBA color) {

		line.getMaterial().setColor("Color", color);

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

	/**
	 * Update several lines saved in one geometry
	 * @param line
	 * @param points
	 * @param color
	 */
	public static void updateLines(Geometry line,
			ArrayList<ArrayList<Vector3f>> points, ColorRGBA color) {

		line.getMaterial().setColor("Color", color);

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
				int secondIdx = 2 * i + 1;
				if (secondIdx < indexes.length) {
					indexes[secondIdx] = (short) ((vecIdx + 1));
				}
			}
			iStart = end;
			tmpVertices.addAll(p);
		}
		vertices = tmpVertices.toArray(vertices);

		updateGeometryMesh(line, vertices, indexes);
	}

	/**
	 * Update several lines in one geometry
	 * @param line
	 * @param lines
	 * @param color
	 */
	public static void updateLinesVec(Geometry line,
			ArrayList<VectorLine> lines, ColorRGBA color) {

		line.getMaterial().setColor("Color", color);

		// calculate size
		int size = 0;
		int numberOfLines = 0;
		for (VectorLine t : lines) {
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
					int secondIdx = 2 * i + 1;
					if (secondIdx < indexes.length) {
						indexes[secondIdx] = (short) ((vecIdx + 1));
					}
				}
				iStart = end;
				tmpVertices.addAll(p);
			}
			vertices = tmpVertices.toArray(vertices);

		} else {
			vertices = new Vector3f[0];
			indexes = new short[0];
		}

		updateGeometryMesh(line, vertices, indexes);
	}

	/**
	 * Update geoemetry mesh and bounds
	 * @param geo
	 * @param vertices
	 * @param indexes
	 */
	public static void updateGeometryMesh(Geometry geo, Vector3f[] vertices,
			short[] indexes) {
		Mesh lineMesh = geo.getMesh();

		lineMesh.setBuffer(Type.Position, 3,
				BufferUtils.createFloatBuffer(vertices));
		lineMesh.setBuffer(Type.Index, 1,
				BufferUtils.createShortBuffer(indexes));
		lineMesh.updateBound();
	}

	/**
	 * Convert own quaternion implementation to JME implementation
	 * @param quad
	 * @return
	 */
	public static Quaternion getJMEQuad(imuanalyzer.utils.math.Quaternion quad) {
		return new Quaternion((float) quad.get(1) * -1, (float) quad.get(2)
				* -1, (float) quad.get(3), (float) quad.get(0));
	}

	/**
	 * Convert JME Quaternion representation to own implementation
	 * @param quad
	 * @return
	 */
	public static imuanalyzer.utils.math.Quaternion getSensorQuad(Quaternion quad) {
		return new imuanalyzer.utils.math.Quaternion(quad.getW(), quad.getX() * -1,
				quad.getY() * -1, quad.getZ());
	}

	/**
	 * Get jme vector from quaternion
	 * @param quad
	 * @return
	 */
	public static Vector3f getPosition(imuanalyzer.utils.math.Quaternion quad) {
		return new Vector3f((float) quad.getX(), (float) quad.getY(),
				(float) quad.getZ());
	}

	/**
	 * Swap visibilty of geometry
	 * @param geom
	 */
	public static void swapVisibility(Geometry geom) {
		RenderState state = geom.getMaterial().getAdditionalRenderState();
		System.out.println("CurrentCulling" + state.getFaceCullMode());
		if (state.getFaceCullMode() == FaceCullMode.Off) {
			state.setFaceCullMode(FaceCullMode.FrontAndBack);
		} else {
			state.setFaceCullMode(FaceCullMode.Off);
		}
	}

	/**
	 * Set visibility of geometry
	 * @param geom
	 * @param visible
	 */
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
	 * postfix with .JOINTTYPE this is working automatically with Blender 2.63a
	 * JME3 SDK Nighly build from begin of August 2012 and ORGRE Exporter from
	 * nightly build JME3 SDK
	 * 
	 * @param geom
	 * @return
	 */
	public static JointType getJointTypeFromGeometryNamePostfix(Geometry geom) {
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

	/**
	 * Usable for Blender prior 2.63 with corresponding ogre exporter and older
	 * JME SDK nightly build
	 * 
	 * @param geom
	 * @return
	 */
	public static JointType getJointTypeFromGeomertyByOrderMapping(Geometry geom) {

		String name = geom.getName();

		if (name.equals("Hand-geom-1")) {
			return JointType.HAND_ROOT;
		}
		if (name.equals("Hand-geom-2")) {
			return JointType.THUMB_BOTTOM;
		}
		if (name.equals("Hand-geom-3")) {
			return JointType.THUMB_MID;
		}
		if (name.equals("Hand-geom-4") || name.equals("Hand-geom-17")) {
			return JointType.THUMB_TOP;
		}
		if (name.equals("Hand-geom-5")) {
			return JointType.INDEX_BOTTOM;
		}
		if (name.equals("Hand-geom-6")) {
			return JointType.INDEX_MID;
		}
		if (name.equals("Hand-geom-7") || name.equals("Hand-geom-18")) {
			return JointType.INDEX_TOP;
		}
		if (name.equals("Hand-geom-8")) {
			return JointType.MIDDLE_BOTTOM;
		}
		if (name.equals("Hand-geom-9")) {
			return JointType.MIDDLE_MID;
		}
		if (name.equals("Hand-geom-10") || name.equals("Hand-geom-19")) {
			return JointType.MIDDLE_TOP;
		}
		if (name.equals("Hand-geom-11")) {
			return JointType.RING_BOTTOM;
		}
		if (name.equals("Hand-geom-12")) {
			return JointType.RING_MID;
		}
		if (name.equals("Hand-geom-13") || name.equals("Hand-geom-20")) {
			return JointType.RING_TOP;
		}
		if (name.equals("Hand-geom-14")) {
			return JointType.LITTLE_BOTTOM;
		}
		if (name.equals("Hand-geom-15")) {
			return JointType.LITTLE_MID;
		}
		if (name.equals("Hand-geom-16") || name.equals("Hand-geom-21")) {
			return JointType.LITTLE_TOP;
		}

		return null;
	}

	/**
	 * Search for one node in scenegraph below given node for a node with given
	 * name
	 * 
	 * @param node
	 * @param name
	 * @return null if no node equals given name or first node with name
	 */
	public static Node findNodeByName(Node node, String name) {
		if (node.getName().equals(name)) {
			return node;
		}
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
