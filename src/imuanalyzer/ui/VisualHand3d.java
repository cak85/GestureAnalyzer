package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

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

	public static final float MAX_OPACITY = 0.95f;

	private static ExecutorService executor = Executors.newCachedThreadPool();

	private static final Logger LOGGER = Logger.getLogger(VisualHand3d.class
			.getName());

	public static final ColorRGBA SKELETON_COLOR = ColorRGBA.White;

	public enum HandOrientation {
		LEFT, RIGHT
	};

	public enum ModelQuality {
		LOW, HIGH
	}

	HandOrientation orientation;

	Node model;

	private SkeletonDebugger skeletonDebug;

	EnumMap<JointType, Bone> bones = new EnumMap<JointType, Bone>(
			JointType.class);

	EnumMap<JointType, ArrayList<Geometry>> geometries = new EnumMap<JointType, ArrayList<Geometry>>(
			JointType.class);

	ArrayList<Geometry> geomtryList = new ArrayList<Geometry>();

	EnumMap<JointType, Quaternion> loadedOrientation = new EnumMap<JointType, Quaternion>(
			JointType.class);

	public VisualHand3d(AssetManager assetManager, HandOrientation orientation,
			boolean showSkeleton, ModelQuality quality) {

		String modelPath = getModelPath(quality);

		Spatial loadedAsset = assetManager.loadModel(modelPath);

		model = Utils.findNodeByName((Node) loadedAsset, "Hand-ogremesh");
		init(assetManager, orientation, showSkeleton);
	}

	/**
	 * Selects model based on given quality
	 * 
	 * @param quality
	 * @return
	 */
	protected String getModelPath(ModelQuality quality) {
		String modelPath;
		switch (quality) {
		case LOW:
			modelPath = "Models/Hand/Hand.j3o";
			break;
		case HIGH:
		default:
			modelPath = "Models/HandHi/Hand.j3o";
			break;
		}
		return modelPath;
	}

	private void init(AssetManager assetManager, HandOrientation orientation,
			boolean addSkeleton) {
		this.orientation = orientation;

		if (orientation == HandOrientation.LEFT) {
			model.scale(-1, 1, 1);
		}

		this.attachChild(model);

		for (JointType t : JointType.values()) {
			geometries.put(t, new ArrayList<Geometry>());
		}

		Future<?> geometryFuture = executor.submit(new Runnable() {

			@Override
			public void run() {
				findGeometries(model);
			}
		});

		// Everything necessary for disabling Animation and enabling control
		// over skeleton
		AnimControl control = model.getControl(AnimControl.class);
		control.setEnabled(false);
		// HACK don't know why it is necessary to add this control...but it
		// works
		// if I do not add this control the model structure collaspes
		KinematicRagdollControl ragdoll = new KinematicRagdollControl(0.5f);
		model.addControl(ragdoll);
		ragdoll.setEnabled(false);

		Skeleton skeleton = control.getSkeleton();

		if (addSkeleton) {
			// Skeleton Debug
			skeletonDebug = new SkeletonDebugger("Armature", skeleton);
			Material mat2 = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
			mat2.setColor("m_Color", SKELETON_COLOR.clone());
			mat2.getAdditionalRenderState().setDepthTest(false);
			skeletonDebug.setMaterial(mat2);
			model.attachChild(skeletonDebug);
		}

		bones.put(JointType.LITTLE_TOP, skeleton.getBone("Bone.KT"));
		bones.put(JointType.LITTLE_MID, skeleton.getBone("Bone.KM"));
		bones.put(JointType.LITTLE_BOTTOM, skeleton.getBone("Bone.KD"));
		bones.put(JointType.RING_TOP, skeleton.getBone("Bone.RT"));
		bones.put(JointType.RING_MID, skeleton.getBone("Bone.RM"));
		bones.put(JointType.RING_BOTTOM, skeleton.getBone("Bone.RD"));
		bones.put(JointType.MIDDLE_TOP, skeleton.getBone("Bone.MT"));
		bones.put(JointType.MIDDLE_MID, skeleton.getBone("Bone.MM"));
		bones.put(JointType.MIDDLE_BOTTOM, skeleton.getBone("Bone.MD"));
		bones.put(JointType.INDEX_TOP, skeleton.getBone("Bone.ZT"));
		bones.put(JointType.INDEX_MID, skeleton.getBone("Bone.ZM"));
		bones.put(JointType.INDEX_BOTTOM, skeleton.getBone("Bone.ZD"));
		bones.put(JointType.THUMB_TOP, skeleton.getBone("Bone.DT"));
		bones.put(JointType.THUMB_MID, skeleton.getBone("Bone.DM"));
		bones.put(JointType.THUMB_BOTTOM, skeleton.getBone("Bone.DD"));
		bones.put(JointType.HAND_ROOT, skeleton.getBone("Bone"));

		// save initial bone rotations
		for (Entry<JointType, Bone> entry : bones.entrySet()) {
			loadedOrientation.put(entry.getKey(), entry.getValue()
					.getLocalRotation());
		}

		// wait for parallel task
		try {
			geometryFuture.get();
		} catch (InterruptedException e) {
			LOGGER.error(e);
			e.printStackTrace();
		} catch (ExecutionException e) {
			LOGGER.error(e);
		}

		// force cull settings
		for (JointType t : JointType.values()) {
			setVisible(t, true);
		}
	}

	public void resetRotations() {
		for (Entry<JointType, Bone> entry : bones.entrySet()) {

			setBoneRotationAbs(entry.getKey(),
					loadedOrientation.get(entry.getKey()));
		}
	}

	private void findGeometries(Node node) {

		for (Spatial s : node.getChildren()) {
			if (s instanceof Node) {
				findGeometries((Node) s);
			} else if (s instanceof Geometry) {
				Geometry geom = (Geometry) s;
				//two possibilities
				JointType type = Utils
						.getJointTypeFromGeomertyByOrderMapping(geom);
				// JointType type =
				// Utils.getJointTypeFromGeometryNamePostfix(geom);
				if (type != null) {
					geometries.get(type).add(geom);
					geomtryList.add(geom);
				}
			}
		}
	}

	public HandOrientation getOrientation() {
		return orientation;
	}

	public void setOrientation(HandOrientation orientation) {

		if (this.orientation != orientation) {
			this.scale(-1, 1, 1);
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

	public void setBoneRotationAbs(JointType finger, Vector3f pos,
			Quaternion quad) {
		Bone bone = bones.get(finger);
		setTransform(bone, pos, quad);
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

	public void setOpacity(float opaStep, int count) {
		setOpacity(ColorRGBA.White, opaStep, count);
	}

	/**
	 * Set hand part color and opacticy
	 * 
	 * @param type
	 * @param color
	 * @param percentOfMax
	 *            percentage of the hardcoded max opacity value
	 */
	public void setOpacity(JointType type, ColorRGBA color, float percentOfMax) {

		ColorRGBA colorAmbient = color.clone();

		ColorRGBA colorDiffuse = colorAmbient.clone();

		colorDiffuse.a = MAX_OPACITY * percentOfMax;

		for (Geometry geo : geometries.get(type)) {
			setGeomOpacity(colorAmbient, colorDiffuse, geo);
		}
	}

	/***
	 * Set color and opacity of hand part based of count and step size
	 * 
	 * @param type
	 * @param color
	 * @param opaStep
	 * @param count
	 */
	public void setOpacity(JointType type, ColorRGBA color, float opaStep,
			int count) {

		ColorRGBA colorAmbient = color.clone();

		ColorRGBA colorDiffuse = colorAmbient.clone();

		colorDiffuse.a = opaStep * count;

		if (colorDiffuse.a > MAX_OPACITY) {
			colorDiffuse.a = MAX_OPACITY;
		}

		for (Geometry geo : geometries.get(type)) {
			setGeomOpacity(colorAmbient, colorDiffuse, geo);
		}
	}

	/***
	 * Set color and opacity of whole hand based of count and step size
	 * 
	 * @param type
	 * @param color
	 * @param opaStep
	 * @param count
	 */
	public void setOpacity(ColorRGBA color, float opaStep, int count) {

		ColorRGBA colorAmbient = color.clone();

		ColorRGBA colorDiffuse = colorAmbient.clone();

		colorDiffuse.a = opaStep * count;

		if (colorDiffuse.a > MAX_OPACITY) {
			colorDiffuse.a = MAX_OPACITY;
		}

		for (Geometry geo : geomtryList) {
			setGeomOpacity(colorAmbient, colorDiffuse, geo);
		}
	}

	protected void setGeomOpacity(ColorRGBA colorAmbient,
			ColorRGBA colorDiffuse, Geometry geo) {
		Material mat = geo.getMaterial();
		mat.setColor("Diffuse", colorDiffuse);
		mat.setColor("Ambient", colorAmbient);
		mat.setBoolean("UseMaterialColors", true);
		mat.getAdditionalRenderState().setBlendMode(BlendMode.AlphaAdditive);
		mat.setFloat("Shininess", 0.5f);
		geo.setMaterial(mat);
		geo.setQueueBucket(Bucket.Transparent);
	}

	public void setVisible(JointType type, boolean visible) {
		for (Geometry geo : geometries.get(type)) {
			if (geo.getName().contains(type.toString())) {
				Utils.setVisible(geo, visible);
			}
		}
	}

	public void setSkeletonVisible(boolean visible) {
		if (skeletonDebug != null) {
			if (visible) {
				skeletonDebug.setCullHint(CullHint.Dynamic);
			} else {
				skeletonDebug.setCullHint(CullHint.Always);
			}
		}
	}

	public ArrayList<Geometry> getJointGeometries(JointType type) {
		return geometries.get(type);
	}

	public void updateCollisionData() {
		for (Geometry geo : geomtryList) {
			geo.getMesh().createCollisionData();
			geo.updateModelBound();
		}
	}
}
