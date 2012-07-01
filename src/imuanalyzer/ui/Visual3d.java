package imuanalyzer.ui;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Analyses;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.MotionAnalysis;
import imuanalyzer.signalprocessing.MovementStep;
import imuanalyzer.signalprocessing.StoredJointState;
import imuanalyzer.signalprocessing.TouchAnalysis;
import imuanalyzer.ui.VisualHand3d.HandOrientation;
import imuanalyzer.ui.swing.menu.MenuFactory;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeSystem;

public class Visual3d extends SimpleApplication {

	private static final Logger LOGGER = Logger.getLogger(Visual3d.class
			.getName());

	private static final float CAM_MOVEMENT = 2f;

	private static final float MANUAL_ANGLE_CHANGE = 0.5f;

	private static final float OPACITY_STEP = 0.005f;

	/**
	 * JPanel holding the JME3 3d view
	 */
	protected JPanel panel3d;

	/**
	 * Visual threedimensional representation of the hand
	 */
	protected VisualHand3d visualHand;

	/**
	 * Datamodel of the hand
	 */
	protected Hand hand;

	/**
	 * Used camera
	 */
	private ChaseCamera chaseCam;

	/**
	 * Shadow renderer
	 */
	private PssmShadowRenderer pssmRenderer;

	/**
	 * Current Joint for manual manipulation
	 */
	private JointType currentManipulatedJoint = JointType.HAND_ROOT;

	/**
	 * Enum representing an axis selection
	 * 
	 */
	enum Axis {
		X, Y, Z
	};

	/**
	 * the current manipulated axis
	 */
	Axis currentAxis = Axis.X;

	ArrayList<VisualHand3d> liveMovementSteps = new ArrayList<VisualHand3d>();

	ArrayList<VisualHand3d> analysesMovementSteps = new ArrayList<VisualHand3d>();

	Analyses analyses = null;

	LinkedList<MovementStep> analysesMovementPositions = new LinkedList<MovementStep>();

	int maxMotionCount = 0;

	private Visual3d myInstance;

	/**
	 * Dummy tablet device
	 */
	DeviceDummy deviceDummy = null;

	Geometry grid;

	ArrayList<Geometry> linesPoolTouch = new ArrayList<Geometry>();
	ArrayList<Geometry> linesPoolMotion = new ArrayList<Geometry>();

	Boxplot3d touchLineStatistics;

	/**
	 * State for taking screenshots of 3d window
	 */
	ScreenshotAppState state;

	private EnumMap<JointType, JointSetting> visualJointSettings = new EnumMap<JointType, JointSetting>(
			JointType.class);

	/**
	 * save device click start postion
	 */
	private Vector2f deviceClickStart;

	private MenuFactory menuFactory;

	/**
	 * Constructor, needs handmodel
	 * 
	 * @param hand
	 */
	public Visual3d(Hand hand) {
		myInstance = this;
		this.hand = hand;

		this.inputEnabled = false;
		showSettings = false;
		setDisplayStatView(false);
		setDisplayFps(false);
		setPauseOnLostFocus(false);

		AppSettings settings = new AppSettings(true);
		settings.setWidth(640);
		settings.setHeight(480);
		this.setSettings(settings);
		this.createCanvas();
		JmeCanvasContext ctx = (JmeCanvasContext) this.getContext();
		ctx.setSystemListener(this);
		ctx.getCanvas().setSize(settings.getWidth(), settings.getHeight());

		panel3d = new JPanel(new BorderLayout()); // a panel

		// add the JME canvas
		panel3d.add(ctx.getCanvas(), BorderLayout.CENTER);

		HelpManager.getInstance().enableHelpKey(panel3d, "3dview");

		this.startCanvas();

	}

	@Override
	public void simpleInitApp() {

		// optional
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

		// anitalasing
		// FXAAFilter fxaaFilter = new FXAAFilter();
		// fpp.addFilter(fxaaFilter);

		// fpp.setNumSamples(4);
		// SSAOFilter ssaoFilter = new SSAOFilter(0.92f, 2.2f, 0.46f, 0.2f);
		// final WaterFilter water=new WaterFilter(rootNode,new
		// Vector3f(-0.4790551f, -0.39247334f, -0.7851566f));
		// water.setWaterHeight(-6);
		//
		// final BloomFilter bloom = new BloomFilter();
		// final ColorOverlayFilter overlay = new
		// ColorOverlayFilter(ColorRGBA.LightGray);
		// fpp.addFilter(bloom) ;
		// fpp.addFilter(overlay) ;
		// fpp.addFilter(water) ;
		// only add the ssao filter
		// fpp.addFilter(ssaoFilter);
		// FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		// BloomFilter bloom = new BloomFilter(GlowMode.Objects);
		// fpp.addFilter(bloom);

		// fpp.addFilter(ssaoFilter);
		viewPort.addProcessor(fpp);

		for (JointType t : JointType.values()) {
			visualJointSettings.put(t, new JointSetting(t));
		}

		// background
		viewPort.setBackgroundColor(ColorRGBA.DarkGray.clone());

		attachLight();

		visualHand = new VisualHand3d(assetManager, HandOrientation.LEFT, true);

		visualHand.getModel().setShadowMode(ShadowMode.CastAndReceive);

		rootNode.attachChild(visualHand);

		adjustBoneJointMapping();

		configureCam();

		attachCoordinateAxes(new Vector3f(-5, 0, 0));

		initKeys();

		grid = attachGrid(new Vector3f(0, -10, 0), 50, ColorRGBA.Black.clone());

		touchLineStatistics = new Boxplot3d(assetManager);

		visualHand.attachChild(touchLineStatistics);

		deviceDummy = new DeviceDummy(assetManager);

		state = new ScreenshotAppState();
		this.stateManager.attach(state);
	}

	/** Custom Keybinding: Map named actions to inputs. */
	private void initKeys() {
		// You can map one or several inputs to one named action
		inputManager.addMapping("Switch", new KeyTrigger(KeyInput.KEY_TAB));
		inputManager.addMapping("X", new KeyTrigger(KeyInput.KEY_X));
		inputManager.addMapping("Y", new KeyTrigger(KeyInput.KEY_Y));
		inputManager.addMapping("Z", new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("CAM_UP", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("CAM_DOWN", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("CAM_LEFT", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("CAM_RIGHT", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Increase", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("Decrease", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("Quit", new KeyTrigger(KeyInput.KEY_ESCAPE));
		inputManager.addMapping("pick target", new MouseButtonTrigger(
				MouseInput.BUTTON_RIGHT));
		inputManager.addMapping("DeviceMove", new MouseButtonTrigger(
				MouseInput.BUTTON_LEFT));
		inputManager.addMapping("DeviceMoveTrigger", new MouseButtonTrigger(
				MouseInput.BUTTON_LEFT));

		// Add the names to the action listener.
		inputManager.addListener(actionListener, new String[] { "Switch", "X",
				"Y", "Z", "Quit" });

		inputManager.addListener(analogListener, new String[] { "CAM_UP",
				"CAM_DOWN", "CAM_LEFT", "CAM_RIGHT", "Increase", "Decrease", });

		inputManager.addListener(mousePicker, new String[] { "pick target" });

		inputManager.addListener(deviceAnalogListener,
				new String[] { "DeviceMove" });

		inputManager.addListener(deviceClickListener,
				new String[] { "DeviceMoveTrigger" });

	}

	public void resetHand() {
		visualHand.resetRotations();
		adjustBoneJointMapping();
	}

	public void adjustBoneJointMapping() {
		// init hand
		for (Entry<JointType, Joint> entry : hand.getJointSet()) {

			JointType type = entry.getKey();

			Quaternion quat = Utils.getSensorQuad(visualHand
					.getBoneRotation(type));

			entry.getValue().setLocalOrientation(quat);

			Vector3f pos = visualHand.getBonePosition(type);
			entry.getValue().setLocalPosition(
					new Quaternion(0, pos.x, pos.y, pos.z));
		}
	}

	private void attachLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.clone().mult(1.3f));
		rootNode.addLight(al);

		PointLight lamp_light2 = new PointLight();
		lamp_light2.setColor(ColorRGBA.White.clone());
		lamp_light2.setRadius(10f);
		Vector3f lamp_pos_2 = new Vector3f(0, 8, 3);
		lamp_light2.setPosition(lamp_pos_2);

		rootNode.addLight(lamp_light2);

		pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 7);
		pssmRenderer.setDirection(new Vector3f(0, -8, -3).normalizeLocal()); // light
																				// direction
		pssmRenderer.setShadowIntensity(0.15f);

		viewPort.addProcessor(pssmRenderer);

	}

	private void switchJoint() {
		if (currentManipulatedJoint.ordinal() < JointType.values().length - 1) {
			currentManipulatedJoint = JointType.values()[currentManipulatedJoint
					.ordinal() + 1];
		} else {
			currentManipulatedJoint = JointType.values()[0];
		}
		System.out.println("Current Joint: "
				+ currentManipulatedJoint.toString());
	}

	private void manualAddToQuat(JointType joint, Axis axis, double value) {
		Quaternion quat = hand.getLocalJointOrientation(joint);

		double[] angles = quat.getAnglesRad();

		int i = axis.ordinal();
		angles[i] += value;

		Quaternion newQuat = new Quaternion(angles[0], angles[1], angles[2]);

		// System.out.printf("R%.3f: P%.3f: Y%.3f\n", angles[0], angles[1],
		// angles[2]);
		//
		// newQuat.print(3);

		hand.setLocalJointOrientation(joint, newQuat);

	}

	private Geometry mousePick() {
		// Reset results list.
		CollisionResults results = new CollisionResults();
		// Convert screen click to 3d position
		Vector2f click2d = inputManager.getCursorPosition();
		Vector3f click3d = cam.getWorldCoordinates(
				new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = cam
				.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f)
				.subtractLocal(click3d).normalizeLocal();
		// Aim the ray from the clicked spot forwards.
		Ray ray = new Ray(click3d, dir);
		// Collect intersections between ray and all nodes in results
		// list.
		visualHand.updateCollisionData();
		visualHand.getModel().collideWith(ray, results);

		// Use the results
		if (results.size() > 0) {
			return results.getClosestCollision().getGeometry();
		} else {
			return null;
		}
	}

	private ActionListener mousePicker = new ActionListener() {

		@Override
		public void onAction(String name, boolean isPressed, float tpf) {
			if (name.equals("pick target") && !isPressed) {

				// The closest result is the target that the player picked:
				Geometry target = mousePick();

				if (target != null) {

					String targetName = target.getName();

					LOGGER.debug("Name: " + targetName);

					JPopupMenu menu = null;
					if (targetName.contains("myIpad")) {
						// create popUp with further options
						menu = menuFactory.getDevicePopUpMenu(myInstance,
								deviceDummy);
					} else {
						// create popUp with further options
						menu = menuFactory.getHandPopUpMenu(myInstance,
								Utils.getJointTypeFromGeometry(target));

					}
					if (menu != null) {
						Vector2f click2d = inputManager.getCursorPosition();
						menu.show(panel3d, (int) click2d.x + 10,
								settings.getHeight() - (int) click2d.y);
					}
				}
			}
		}
	};

	private AnalogListener deviceAnalogListener = new AnalogListener() {

		@Override
		public void onAnalog(String name, float value, float tpf) {

			if (deviceClickStart != null) {
				Vector2f currentPos = inputManager.getCursorPosition().clone();

				Vector3f start3d = cam.getWorldCoordinates(
						new Vector2f(deviceClickStart.x, deviceClickStart.y),
						0f).clone();

				Vector3f end3d = cam.getWorldCoordinates(
						new Vector2f(currentPos.x, currentPos.y), 0f).clone();

				Vector3f diff = end3d.subtract(start3d);

				if (deviceDummy.isMoving) {
					Geometry geom = mousePick();
					if (geom != null && geom.getName().contains("myIpad")) {
						deviceDummy.move(diff);
					}
				}
				if (deviceDummy.isRotating) {

					Vector2f refHorizontal = new Vector2f(1, 0);

					Vector2f startToCurrent = currentPos
							.subtract(deviceClickStart);

					float angle = refHorizontal.angleBetween(startToCurrent);

					deviceDummy.setLocalRotation(deviceDummy.getLocalRotation()
							.fromAngleAxis(angle, cam.getDirection()));

				}
			}

		}

	};

	private ActionListener deviceClickListener = new ActionListener() {
		public void onAction(String name, boolean keyPressed, float tpf) {
			if (deviceDummy.isMoving() || deviceDummy.isRotating) {
				if (name.equals("DeviceMoveTrigger") && keyPressed) {
					if (deviceClickStart == null) {
						deviceClickStart = inputManager.getCursorPosition()
								.clone();
						LOGGER.debug("Startpos:" + deviceClickStart);
					}
				} else {
					deviceClickStart = null;
				}
			}
		}
	};

	private AnalogListener analogListener = new AnalogListener() {
		public void onAnalog(String name, float value, float tpf) {
			if (name.equals("CAM_UP")) {
				moveCam(new Vector3f(0, CAM_MOVEMENT * value, 0));
			} else if (name.equals("CAM_DOWN")) {
				moveCam(new Vector3f(0, -CAM_MOVEMENT * value, 0));
			} else if (name.equals("CAM_LEFT")) {
				moveCam(new Vector3f(0, 0, CAM_MOVEMENT * value));
			} else if (name.equals("CAM_RIGHT")) {
				moveCam(new Vector3f(0, 0, -CAM_MOVEMENT * value));
			} else if (name.equals("Increase")) {
				manualAddToQuat(currentManipulatedJoint, currentAxis,
						MANUAL_ANGLE_CHANGE * value);
			} else if (name.equals("Decrease")) {
				manualAddToQuat(currentManipulatedJoint, currentAxis, -1
						* MANUAL_ANGLE_CHANGE * value);
			}
		}
	};

	private ActionListener actionListener = new ActionListener() {
		public void onAction(String name, boolean keyPressed, float tpf) {

			if (name.equals("Switch") && !keyPressed) {
				switchJoint();
			} else if ((name.equals("X") || name.equals("Y") || name
					.equals("Z")) && !keyPressed) {
				currentAxis = Axis.valueOf(name);
			} else if (name.equals("Quit")) {
				System.exit(0);
			}
		}
	};

	private void moveCam(Vector3f change) {
		Vector3f vec = chaseCam.getLookAtOffset();
		chaseCam.setLookAtOffset(vec.add(change));
	}

	private void configureCam() {
		stateManager.detach(stateManager.getState(FlyCamAppState.class));

		chaseCam = new ChaseCamera(cam, visualHand, inputManager);
		chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(
				MouseInput.BUTTON_MIDDLE));
		chaseCam.setInvertHorizontalAxis(true);
		chaseCam.setInvertVerticalAxis(true);
		chaseCam.setMinDistance(0f);
		chaseCam.setMaxDistance(30f);
		chaseCam.setZoomSensitivity(2);
		chaseCam.setMinVerticalRotation(0);
		chaseCam.setMaxVerticalRotation((float) Math.PI * 2);
		chaseCam.setRotationSensitivity(8);
		chaseCam.setDefaultHorizontalRotation((float) Math.PI / 4);
		chaseCam.setLookAtOffset(new Vector3f(0, 3, 0));
	}

	@Override
	public void simpleUpdate(float tpf) {

		try {
			Set<Entry<JointType, Joint>> handset = hand.getJointSet();

			// don't draw the last state because it is current one
			int numberOfLiveSteps = hand.getNumberOfSavedMotionSteps();

			// add additional hand geometries for movement if necessary
			while (numberOfLiveSteps > liveMovementSteps.size()) {
				VisualHand3d newHand = new VisualHand3d(assetManager,
						HandOrientation.LEFT, false);
				newHand.setOpacity(OPACITY_STEP, 0);
				liveMovementSteps.add(newHand);
				visualHand.attachChild(newHand);
			}

			// handle stored analyses movement
			// don't draw the last state because it is current one
			int numberOfStoredSteps = analysesMovementPositions.size() - 1;

			// add additional hand geometries for movement if necessary
			while (numberOfStoredSteps > analysesMovementSteps.size()) {
				VisualHand3d newHand = new VisualHand3d(assetManager,
						HandOrientation.LEFT, false);
				newHand.setOpacity(OPACITY_STEP, 0);
				analysesMovementSteps.add(newHand);
				visualHand.attachChild(newHand);
			}

			for (Entry<JointType, Joint> entry : handset) {

				// update actual hand << non transparent one
				Joint currentJoint = entry.getValue();
				JointType currentJointType = entry.getKey();
				Quaternion currentJointOrientation = currentJoint
						.getWorldOrientation();
				visualHand.setBoneRotationAbs(currentJointType,
						Utils.getJMEQuad(currentJointOrientation));

				JointSetting currentSetting = visualJointSettings
						.get(currentJointType);
				visualHand.setVisible(currentJointType,
						currentSetting.isVisible());

				// update live movement

				updateLiveMovement(currentJoint, currentJointType,
						currentJointOrientation, currentSetting,
						numberOfLiveSteps);

				// update stored analyses movement
				for (int i = 0; i < numberOfStoredSteps; i++) {
					VisualHand3d hand3d = analysesMovementSteps.get(i);

					MovementStep moveStep = analysesMovementPositions.get(i);

					StoredJointState currentStep = moveStep.getMove();
					currentStep.updateWorldOrientation();

					float opacity;
					if (maxMotionCount == 0) {
						opacity = 0;
					} else {
						opacity = moveStep.getCount() / (float) maxMotionCount;
					}

					hand3d.setOpacity(currentJointType,
							currentSetting.getStoredMotionColor(), opacity);

					EnumMap<JointType, StoredJointState> storedSet = analysesMovementPositions
							.get(i).getJointSet();

					// update movement flow with joint from actual hand if
					// not available in movement.
					if (storedSet.containsKey(currentJointType)) {

						hand3d.setBoneRotationAbs(currentJointType, Utils
								.getJMEQuad(storedSet.get(currentJointType)
										.getWorldOrientation()));

						hand3d.setVisible(currentJointType,
								currentSetting.isVisible());
					} else {
						hand3d.setBoneRotationAbs(currentJointType,
								Utils.getJMEQuad(currentJointOrientation));
						hand3d.setVisible(currentJointType, false);
					}
				}

			}

			updateLines();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateLines() {
		// update touch lines
		ArrayList<TouchAnalysis> touchAnalysises = hand
				.getRunningTouchAnalysis();
		for (int i = 0; i < touchAnalysises.size(); i++) {
			TouchAnalysis t = touchAnalysises.get(i);

			JointSetting setting = visualJointSettings.get(t.getObservedJoint()
					.getType());

			if (linesPoolTouch.size() <= i * 2) {
				Geometry geom = Utils.CreateLine(assetManager, t.getMaxLine(),
						setting.getLiveTouchMaxColor(), false, 4);
				linesPoolTouch.add(geom);
				visualHand.attachChild(geom);
				Geometry geom2 = Utils.CreateLine(assetManager,
						t.getCurrentLine(), setting.getLiveTouchCurrentColor(),
						false, 4);
				linesPoolTouch.add(geom2);
				visualHand.attachChild(geom2);
			} else {
				Utils.updateLine(linesPoolTouch.get(i * 2), t.getMaxLine(),
						false, setting.getLiveTouchMaxColor());
				Utils.updateLine(linesPoolTouch.get(i * 2 + 1),
						t.getCurrentLine(), false,
						setting.getLiveTouchCurrentColor());
			}

		}
		// update motion lines
		ArrayList<MotionAnalysis> motionAnalysises = hand
				.getRunningMotionAnalysis();
		for (int i = 0; i < motionAnalysises.size(); i++) {
			MotionAnalysis m = motionAnalysises.get(i);

			JointSetting setting = visualJointSettings.get(m.getObservedJoint()
					.getType());

			if (linesPoolMotion.size() <= i * 2) {
				Geometry geom = Utils.CreateLinesVec(assetManager,
						m.getMaxLine(), setting.getLiveTouchMaxColor(), false,
						4);
				linesPoolMotion.add(geom);
				visualHand.attachChild(geom);
				Geometry geom2 = Utils.CreateLinesVec(assetManager,
						m.getMinLine(), setting.getLiveTouchCurrentColor(),
						false, 4);
				linesPoolMotion.add(geom2);
				visualHand.attachChild(geom2);
			} else {
				Utils.updateLinesVec(linesPoolMotion.get(i * 2),
						m.getMaxLine(), setting.getLiveMotionMaxLineColor());
				Utils.updateLinesVec(linesPoolMotion.get(i * 2 + 1),
						m.getMinLine(), setting.getLiveMotionMinLineColor());
			}

		}
	}

	private void updateLiveMovement(Joint currentJoint,
			JointType currentJointType, Quaternion currentJointOrientation,
			JointSetting currentSetting, int numberOfLiveSteps) {
		int id_live_hand_modell = 0;

		for (MotionAnalysis motionAnalysis : hand.getRunningMotionAnalysis()) {
			LinkedList<MovementStep> storedMovementPositions = motionAnalysis
					.getSavedMovementFlow();
			for (int i = 0; i < numberOfLiveSteps; i++) {
				VisualHand3d hand3d = liveMovementSteps
						.get(id_live_hand_modell);
				MovementStep moveStep = storedMovementPositions.get(i);

				int count = moveStep.getCount();

				int maxCount = motionAnalysis.getMaxCount();

				float opacity;
				if (maxCount == 0) {
					opacity = 0;
				} else {
					opacity = count / (float) maxCount;
				}

				hand3d.setOpacity(currentJointType,
						currentSetting.getLiveMotionColor(), opacity);

				EnumMap<JointType, StoredJointState> storedSet = moveStep
						.getJointSet();

				// update movement flow with joint from actual hand
				// if not available in movement.
				if (storedSet.containsKey(currentJointType)) {
					hand3d.setBoneRotationAbs(currentJointType, Utils
							.getJMEQuad(storedSet.get(currentJointType)
									.getWorldOrientation()));

					hand3d.setVisible(currentJointType,
							currentSetting.isVisible());
				} else {
					hand3d.setBoneRotationAbs(currentJointType,
							Utils.getJMEQuad(currentJointOrientation));
					hand3d.setVisible(currentJointType, false);
				}

				id_live_hand_modell++;
			}
		}
	}

	Node coordinateAxes = new Node();

	private void attachCoordinateAxes(Vector3f pos) {
		Arrow arrow = new Arrow(Vector3f.UNIT_X.mult(3));
		arrow.setLineWidth(4); // make arrow thicker
		putShape(coordinateAxes, arrow, ColorRGBA.Red.clone())
				.setLocalTranslation(pos);

		arrow = new Arrow(Vector3f.UNIT_Y.mult(3));
		arrow.setLineWidth(4); // make arrow thicker
		putShape(coordinateAxes, arrow, ColorRGBA.Green.clone())
				.setLocalTranslation(pos);

		arrow = new Arrow(Vector3f.UNIT_Z.mult(3));
		arrow.setLineWidth(4); // make arrow thicker
		// violett
		putShape(coordinateAxes, arrow, ColorRGBA.Blue)
				.setLocalTranslation(pos);
		rootNode.attachChild(coordinateAxes);
	}

	private Geometry attachGrid(Vector3f pos, int size, ColorRGBA color) {
		Geometry g = new Geometry("wireframe grid", new Grid(size, size, 2f));
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		g.center().move(pos);
		rootNode.attachChild(g);
		return g;
	}

	private Geometry putShape(Node node, Mesh shape, ColorRGBA color) {
		Geometry g = new Geometry("coordinate axis", shape);
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("GlowColor", color);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		node.attachChild(g);
		return g;
	}

	public JointType getCurrentManipulatedJoint() {
		return currentManipulatedJoint;
	}

	public void setCurrentManipulatedJoint(JointType newCurrentManipulatedJoint) {
		if (newCurrentManipulatedJoint != this.currentManipulatedJoint) {
			this.currentManipulatedJoint = newCurrentManipulatedJoint;
		}
	}

	public Analyses getAnalyses() {
		return analyses;
	}

	public void setAnalyses(Analyses analyses) {
		this.analyses = analyses;
		// threadsafe update
		enqueue(new Callable<Object>() {
			public Object call() {
				updateAnalysesData();
				return null;
			}
		});
	}

	private void updateAnalysesData() {
		if (analyses != null) {
			LinkedList<MovementStep> analysesMovementPositions = analyses
					.getMoveResult();

			// update joint parent if not root node of object
			// with the goal of moving the analyzed hand in the same frame

			for (MovementStep m : analysesMovementPositions) {
				StoredJointState observedRoot = m.getMove();
				JointType currentObservedType = observedRoot.getType();
				observedRoot.setParent(hand.getJoint(currentObservedType)
						.getParent());
			}
			this.analysesMovementPositions = analysesMovementPositions;

			maxMotionCount = analyses.getMaxMotionCount();

			touchLineStatistics.setStatistics(analyses.getStatistics());

			touchLineStatistics.setCullHint(CullHint.Never);

		} else {
			for (VisualHand3d hand : analysesMovementSteps) {
				hand.removeFromParent();
			}
			analysesMovementPositions.clear();
			analysesMovementSteps.clear();

			touchLineStatistics.setCullHint(CullHint.Always);
		}
	}

	public void clearLiveMovement() {
		// threadsafe update
		enqueue(new Callable<Object>() {
			public Object call() {
				for (VisualHand3d hand3d : liveMovementSteps) {
					hand3d.removeFromParent();
				}
				liveMovementSteps.clear();
				for (Geometry geom : linesPoolMotion) {
					geom.removeFromParent();
				}
				linesPoolMotion.clear();
				return null;
			}
		});
	}

	public void clearTouchLines() {
		// threadsafe update
		enqueue(new Callable<Object>() {
			public Object call() {
				for (Geometry geom : linesPoolTouch) {
					geom.removeFromParent();
				}
				linesPoolTouch.clear();
				return null;
			}
		});
	}

	public void setShowFPS(boolean isShown) {
		setDisplayFps(isShown);
	}

	public void setShowStatistics(boolean isShown) {
		setDisplayStatView(isShown);
	}

	public JPanel get3dPanel() {
		return panel3d;
	}

	public void setDeviceVisible(final boolean isVisible) {
		// threadsafe update
		enqueue(new Callable<Object>() {
			public Object call() {
				if (isVisible && deviceDummy.getParent() == null) {
					visualHand.attachChild(deviceDummy);
				} else {
					visualHand.detachChild(deviceDummy);
				}
				return null;
			}
		});
	}

	public void takeScreenshot(String path) {
		state.takeScreenShot(path);
	}

	public JointSetting getJointSetting(JointType type) {
		return visualJointSettings.get(type);
	}

	public void setGridVisibility(boolean visible) {
		if (visible) {
			grid.setCullHint(CullHint.Dynamic);
		} else {
			grid.setCullHint(CullHint.Always);
		}
	}

	public void setCoordinateAxisVisibile(boolean visible) {
		if (visible) {
			coordinateAxes.setCullHint(CullHint.Dynamic);
		} else {
			coordinateAxes.setCullHint(CullHint.Always);
		}
	}

	public void setRightHand(final boolean isRight) {
		enqueue(new Callable<Object>() {
			public Object call() {
				HandOrientation orientation;
				if (isRight) {
					orientation = HandOrientation.RIGHT;
				} else {
					orientation = HandOrientation.LEFT;
				}

				visualHand.setOrientation(orientation);

				return null;
			}
		});
	}

	public void setSkeletonVisible(final boolean visible) {
		enqueue(new Callable<Object>() {
			public Object call() {
				visualHand.setSkeletonVisible(visible);
				return null;
			}
		});
	}

	public void setLiveHandVisible(final boolean visible) {
		enqueue(new Callable<Object>() {
			public Object call() {
				if (visible) {
					visualHand.setCullHint(CullHint.Dynamic);
				} else {
					visualHand.setCullHint(CullHint.Always);
				}
				visualHand.setSkeletonVisible(visible);
				return null;
			}
		});
	}

	public MenuFactory getMenuFactory() {
		return menuFactory;
	}

	public void setMenuFactory(MenuFactory menuFactory) {
		this.menuFactory = menuFactory;
	}

	public void showSettings() {
		enqueue(new Callable<Object>() {
			public Object call() {
				JmeSystem.showSettingsDialog(settings, false);
				setSettings(settings);
				restart();
				return null;
			}
		});
	}

}
