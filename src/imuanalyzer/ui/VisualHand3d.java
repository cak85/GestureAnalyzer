package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.ArrayList;
import java.util.EnumMap;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.SkeletonDebugger;

public class VisualHand3d extends Node {

	public enum HandOrientation {
		LEFT, RIGHT
	};

	HandOrientation orientation;

	Node model;

	private SkeletonDebugger skeletonDebug;

	EnumMap<JointType, Bone> bones = new EnumMap<JointType, Bone>(
			JointType.class);

	ArrayList<Geometry> subgeometries = new ArrayList<Geometry>();

	public VisualHand3d(AssetManager assetManager) {
		this(assetManager, HandOrientation.RIGHT, false);

	}

	public VisualHand3d(AssetManager assetManager, HandOrientation orientation,
			boolean showSkeleton) {
		Spatial loadedAsset = assetManager
				.loadModel("Models/Hand/Hand.j3o");

		model = findNodeByName((Node) loadedAsset, "Hand-ogremesh");
		init(assetManager, orientation, showSkeleton);
	}

	private void init(AssetManager assetManager, HandOrientation orientation,
			boolean showSkeleton) {
		this.orientation = orientation;

		if (orientation == HandOrientation.LEFT) {
			model.scale(-1, 1, 1);
		}

		this.attachChild(model);

		// TODO try if better performance on doing this asychronous
		findGeometries(model);

		// Everything necessary for disabling Animation and enabling control
		// over skeleton
		AnimControl control = model.getControl(AnimControl.class);
		control.setEnabled(false);
		// HACK dont know why it is necessary to add this control...but it works
		KinematicRagdollControl ragdoll = new KinematicRagdollControl(0.5f);
		model.addControl(ragdoll);
		ragdoll.setEnabled(false);

		Skeleton skeleton = control.getSkeleton();

		if (showSkeleton) {
			// Skeleton Debug
			skeletonDebug = new SkeletonDebugger("Armature", skeleton);
			Material mat2 = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
			mat2.setColor("m_Color", ColorRGBA.Green);
			mat2.getAdditionalRenderState().setDepthTest(false);
			skeletonDebug.setMaterial(mat2);
			model.attachChild(skeletonDebug);
		}

		bones.put(JointType.KT, skeleton.getBone("Bone.KT"));
		bones.put(JointType.KM, skeleton.getBone("Bone.KM"));
		bones.put(JointType.KD, skeleton.getBone("Bone.KD"));
		bones.put(JointType.RT, skeleton.getBone("Bone.RT"));
		bones.put(JointType.RM, skeleton.getBone("Bone.RM"));
		bones.put(JointType.RD, skeleton.getBone("Bone.RD"));
		bones.put(JointType.MT, skeleton.getBone("Bone.MT"));
		bones.put(JointType.MM, skeleton.getBone("Bone.MM"));
		bones.put(JointType.MD, skeleton.getBone("Bone.MD"));
		bones.put(JointType.ZT, skeleton.getBone("Bone.ZT"));
		bones.put(JointType.ZM, skeleton.getBone("Bone.ZM"));
		bones.put(JointType.ZD, skeleton.getBone("Bone.ZD"));
		bones.put(JointType.DT, skeleton.getBone("Bone.DT"));
		bones.put(JointType.DM, skeleton.getBone("Bone.DM"));
		bones.put(JointType.DD, skeleton.getBone("Bone.DD"));
		bones.put(JointType.HR, skeleton.getBone("Bone"));
	}

	private void findGeometries(Node node) {
		for (Spatial s : node.getChildren()) {
			if (s instanceof Node) {
				findGeometries((Node) s);
			} else if (s instanceof Geometry) {
				subgeometries.add((Geometry) s);
			}
		}
	}

	private Node findNodeByName(Node node, String name) {
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

	public HandOrientation getOrientation() {
		return orientation;
	}

	public void setOrientation(HandOrientation orientation) {
		if (this.orientation != orientation) {
			model.scale(-1, 1, 1);
			skeletonDebug.scale(-1, 1, 1);
		}

		this.orientation = orientation;
	}

	public void setBoneRotationRel(JointType finger, Quaternion quad) {
		Bone bone = bones.get(finger);
		setTransform(bone, bone.getLocalPosition(),
				quad.mult(bone.getLocalRotation()));
	}

	public void setBoneRotationAbs(JointType finger, Quaternion quad) {
		Bone bone = bones.get(finger);
		setTransform(bone, bone.getModelSpacePosition(), quad);
	}

	public Quaternion getBoneRotation(JointType finger) {
		Bone bone = bones.get(finger);
		return bone.getLocalRotation();
	}

	public Vector3f getBonePosition(JointType finger) {
		Bone bone = bones.get(finger);
		return bone.getLocalPosition();
	}

	/**
	 * Updates a bone position and rotation.
	 * 
	 * @param bone
	 *            the bone
	 * @param pos
	 *            the position
	 * @param rot
	 *            the rotation
	 */
	public static void setTransform(Bone bone, Vector3f pos, Quaternion rot) {
		setTransform(bone, pos, rot, true);
	}

	/**
	 * Updates a bone position and rotation.
	 * 
	 * @param bone
	 *            the bone
	 * @param pos
	 *            the position
	 * @param rot
	 *            the rotation
	 */
	public static void setTransform(Bone bone, Vector3f pos, Quaternion rot,
			boolean restoreBoneControl) {
		// we ensure that we have the control

		bone.setUserControl(true);
		// we set te user transforms of the bone
		bone.setUserTransformsWorld(pos, rot);
		for (Bone childBone : bone.getChildren()) {
			Transform t = childBone.getCombinedTransform(pos, rot);
			setTransform(childBone, t.getTranslation(), t.getRotation(),
					restoreBoneControl);

		}
		// we give back the control to the keyframed animation
		if (restoreBoneControl) {
			bone.setUserControl(false);
		}
	}

	public Node getModel() {
		return model;
	}

	public void setOpacity(float opa) {
		for (Geometry geo : subgeometries) {
			Material mat = geo.getMaterial();
			mat.setColor("Diffuse", new ColorRGBA(1.0f, 1.0f, 1.0f, opa));
			mat.setColor("Ambient", new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
			mat.setBoolean("UseMaterialColors", true);
			mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			mat.setFloat("Shininess", 0.5f);
			geo.setMaterial(mat);
			geo.setQueueBucket(Bucket.Transparent);

		}
	}

	public void setVisible(JointType type, boolean visible) {
		for (Geometry geo : subgeometries) {
			if (geo.getName().contains(type.toString())) {
				Utils.setVisible(geo, visible);
			}
		}
	}
}
