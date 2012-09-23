package imuanalyzer.ui.jmonkey;

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

/**
 * Threedimensional representation of a rigged human hand
 * 
 * The problem with the rigged skeleton model is, that the hierachie of joints
 * is not based on nodes, the commented code parts tryed to focus on this
 * problem but it is not complete and has the problem that the other code
 * structure in Visual3D does not allow to handle single analysis in the way we
 * no which part of hand is analyzed because of to high abstraction
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
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
		LOW, MID, HIGH
	}

	HandOrientation orientation;

	Node rootNode;

	Node model;

	private SkeletonDebugger skeletonDebug;

	EnumMap<JointType, Bone> bones = new EnumMap<JointType, Bone>(
			JointType.class);

	EnumMap<JointType, ArrayList<Geometry>> geometries = new EnumMap<JointType, ArrayList<Geometry>>(
			JointType.class);

	ArrayList<Geometry> geomtryList = new ArrayList<Geometry>();

	EnumMap<JointType, Quaternion> loadedOrientation = new EnumMap<JointType, Quaternion>(
			JointType.class);

	// EnumMap<JointType, Node> jointNodes = new EnumMap<JointType, Node>(
	// JointType.class);

	ModelQuality quality;

	/**
	 * Constructor
	 * 
	 * @param assetManager
	 *            for loading models
	 * @param orientation
	 *            start orientation (left/right)
	 * @param showSkeleton
	 *            show skeleton of hand on start?
	 * @param quality
	 *            model quality
	 * @param integrateSkeleton
	 *            integrate skeleton in general
	 */
	public VisualHand3d(AssetManager assetManager, HandOrientation orientation,
			boolean showSkeleton, ModelQuality quality,
			boolean integrateSkeleton) {

		this.quality = quality;
		String modelPath = getModelPath(quality);

		Spatial loadedAsset = assetManager.loadModel(modelPath);

		model = Utils.findNodeByName((Node) loadedAsset, "Hand-ogremesh");
		init(assetManager, orientation, showSkeleton, integrateSkeleton);
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
			modelPath = "Models/HandLow/Hand.j3o";
			break;

		case MID:
			modelPath = "Models/HandMid/Hand.j3o";
			break;
		case HIGH:
		default:
			modelPath = "Models/HandHi/Hand.j3o";
			break;
		}
		return modelPath;
	}

	/**
	 * Init method
	 * 
	 * @param assetManager
	 * @param orientation
	 * @param showSkeleton
	 * @param integrateSkeleton
	 */
	private void init(AssetManager assetManager, HandOrientation orientation,
			boolean showSkeleton, boolean integrateSkeleton) {
		this.orientation = orientation;

		if (orientation == HandOrientation.LEFT) {
			model.scale(-1, 1, 1);
		}

		this.attachChild(model);

		rootNode = new Node();
		this.attachChild(rootNode);

		for (JointType t : JointType.values()) {
			geometries.put(t, new ArrayList<Geometry>());
		}

		// initSkeletonNodeHierachie();

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

		if (integrateSkeleton) {
			// Skeleton Debug
			skeletonDebug = new SkeletonDebugger("Armature", skeleton);
			Material mat2 = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
			mat2.setColor("m_Color", SKELETON_COLOR.clone());
			mat2.getAdditionalRenderState().setDepthTest(false);
			skeletonDebug.setMaterial(mat2);
			model.attachChild(skeletonDebug);
		}
		setSkeletonVisible(showSkeleton);

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
			JointType t = entry.getKey();
			Bone b = entry.getValue();
			loadedOrientation.put(t, b.getLocalRotation());
			// Node n = jointNodes.get(t);
			// n.setLocalTranslation(b.getLocalPosition());
			// n.setLocalRotation(b.getLocalRotation());
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

	// protected void initSkeletonNodeHierachie() {
	// Node root = new Node();
	// jointNodes.put(JointType.HAND_ROOT, root);
	// this.attachChild(root);
	//
	// Node lb = new Node();
	// jointNodes.put(JointType.LITTLE_BOTTOM, lb);
	// root.attachChild(lb);
	//
	// Node lm= new Node();
	// jointNodes.put(JointType.LITTLE_MID, lm);
	// lb.attachChild(lm);
	//
	// Node lt= new Node();
	// jointNodes.put(JointType.LITTLE_TOP, lt);
	// lm.attachChild(lt);
	//
	// Node rb = new Node();
	// jointNodes.put(JointType.RING_BOTTOM, rb);
	// root.attachChild(rb);
	//
	// Node rm= new Node();
	// jointNodes.put(JointType.RING_MID, rm);
	// rb.attachChild(rm);
	//
	// Node rt= new Node();
	// jointNodes.put(JointType.RING_TOP, rt);
	// rm.attachChild(rt);
	//
	// Node mb = new Node();
	// jointNodes.put(JointType.MIDDLE_BOTTOM, mb);
	// root.attachChild(mb);
	//
	// Node mm= new Node();
	// jointNodes.put(JointType.MIDDLE_MID, mm);
	// mb.attachChild(mm);
	//
	// Node mt= new Node();
	// jointNodes.put(JointType.MIDDLE_TOP, mt);
	// mm.attachChild(mt);
	//
	// Node ib = new Node();
	// jointNodes.put(JointType.INDEX_BOTTOM, ib);
	// root.attachChild(ib);
	//
	// Node im= new Node();
	// jointNodes.put(JointType.INDEX_MID, im);
	// ib.attachChild(im);
	//
	// Node it= new Node();
	// jointNodes.put(JointType.INDEX_TOP, it);
	// im.attachChild(it);
	//
	// Node tb = new Node();
	// jointNodes.put(JointType.THUMB_BOTTOM, tb);
	// root.attachChild(tb);
	//
	// Node tm= new Node();
	// jointNodes.put(JointType.THUMB_MID, it);
	// tb.attachChild(tm);
	//
	// Node tt= new Node();
	// jointNodes.put(JointType.THUMB_TOP, tt);
	// tm.attachChild(tt);
	// }
	//
	// private void updateBoneNodeFromBone(JointType finger,Bone bone){
	// jointNodes.get(finger).setLocalRotation(bone.getLocalRotation());
	// }

	/**
	 * Root node is rotated the same like the hand root skeleton joint
	 * 
	 * @return
	 */
	public Node getRootNode() {
		return rootNode;
	}

	/**
	 * Reset all bone rotations
	 */
	public void resetRotations() {
		for (Entry<JointType, Bone> entry : bones.entrySet()) {

			setBoneRotationAbs(entry.getKey(),
					loadedOrientation.get(entry.getKey()));
		}
	}

	/**
	 * parse all geometies in model on node and subchilds
	 * 
	 * @param node
	 */
	private void findGeometries(Node node) {

		for (Spatial s : node.getChildren()) {
			if (s instanceof Node) {
				findGeometries((Node) s);
			} else if (s instanceof Geometry) {
				Geometry geom = (Geometry) s;
				// two possibilities
				JointType type;
				type = Utils.getJointTypeFromGeometryNamePostfix(geom);

				if (type != null) {
					geometries.get(type).add(geom);
				}
				geomtryList.add(geom);
			}
		}
	}

	/**
	 * Get current hand orientation
	 * 
	 * @return
	 */
	public HandOrientation getOrientation() {
		return orientation;
	}

	/**
	 * Set hand orientation
	 * 
	 * @param orientation
	 *            new orientation
	 */
	public void setOrientation(HandOrientation orientation) {

		if (this.orientation != orientation) {
			this.scale(-1, 1, 1);
		}

		this.orientation = orientation;
	}

	/**
	 * set local bone rotation
	 * 
	 * @param finger
	 * @param quad
	 */
	public void setBoneRotationRel(JointType finger, Quaternion quad) {
		Bone bone = bones.get(finger);
		setTransform(bone, bone.getLocalPosition(),
				quad.mult(bone.getLocalRotation()));
		if (finger == JointType.HAND_ROOT) {
			rootNode.setLocalRotation(bone.getLocalRotation());
		}
	}

	/**
	 * set world bone roation for finger/joint
	 * 
	 * @param finger
	 * @param quad
	 */
	public void setBoneRotationAbs(JointType finger, Quaternion quad) {
		Bone bone = bones.get(finger);
		setTransform(bone, bone.getModelSpacePosition(), quad);

		//rotate an extra node together with hand root to have a node
		//for attaching analyses
		if (finger == JointType.HAND_ROOT) {
			rootNode.setLocalRotation(quad);
		}
	}

	/**
	 * Set bone world rotation and position
	 * 
	 * @param finger
	 * @param pos
	 * @param quad
	 */
	public void setBoneRotationAndPositionAbs(JointType finger, Vector3f pos,
			Quaternion quad) {
		Bone bone = bones.get(finger);
		setTransform(bone, pos, quad);
		if (finger == JointType.HAND_ROOT) {
			rootNode.setLocalRotation(bone.getLocalRotation());
		}
	}

	/**
	 * Get local bone rotation
	 * 
	 * @param finger
	 * @return
	 */
	public Quaternion getBoneLocalRotation(JointType finger) {
		Bone bone = bones.get(finger);
		return bone.getLocalRotation();
	}

	/**
	 * Get local bone position
	 * 
	 * @param finger
	 * @return
	 */
	public Vector3f getBonePosition(JointType finger) {
		Bone bone = bones.get(finger);
		return bone.getLocalPosition();
	}

	// Node getSkeltonNode(JointType type){
	// return jointNodes.get(type);
	// }

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

	/**
	 * Get model node
	 * 
	 * @return
	 */
	public Node getModel() {
		return model;
	}

	/**
	 * Set opacity for whole model
	 * 
	 * @param opaStep
	 * @param count
	 */
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

	/**
	 * Set opacity and color of one geometry
	 * 
	 * @param colorAmbient
	 * @param colorDiffuse
	 * @param geo
	 */
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

	/**
	 * set visibilty for whole model
	 * 
	 * @param type
	 * @param visible
	 */
	public void setVisible(JointType type, boolean visible) {
		for (Geometry geo : geometries.get(type)) {
			if (geo.getName().contains(type.toString())) {
				Utils.setVisible(geo, visible);
			}
		}
	}

	/**
	 * Set skeleton visibility
	 * 
	 * @param visible
	 */
	public void setSkeletonVisible(boolean visible) {
		if (skeletonDebug != null) {
			if (visible) {
				skeletonDebug.setCullHint(CullHint.Dynamic);
			} else {
				skeletonDebug.setCullHint(CullHint.Always);
			}
		}
	}

	/**
	 * Get all joint geometries
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<Geometry> getJointGeometries(JointType type) {
		return geometries.get(type);
	}

	/**
	 * Update collision bounds of rigged model
	 */
	public void updateCollisionData() {
		for (Geometry geo : geomtryList) {
			geo.getMesh().createCollisionData();
			geo.updateModelBound();
		}
	}
}
