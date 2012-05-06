package imuanalyzer.ui;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.MovementStep;
import imuanalyzer.signalprocessing.StoredJointState;
import imuanalyzer.ui.VisualHand3d.HandOrientation;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

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
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

public class Visual3d extends SimpleApplication {

	private static final float CAM_MOVEMENT = 2f;

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
	ChaseCamera chaseCam;

	/**
	 * not used in the moment
	 */
	// BasicShadowRenderer bsr;

	/**
	 * Current Joint for manual manipulation
	 */
	JointType currentManipulatedJoint = JointType.HR;

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

	Dimension dim = new Dimension(640, 480);

	ArrayList<VisualHand3d> movementSteps = new ArrayList<VisualHand3d>();

	private Visual3d myInstance;

	/**
	 * Constructor, needs handmodel
	 * 
	 * @param hand
	 */
	public Visual3d(Hand hand) {
		myInstance = this;
		this.hand = hand;

		showSettings = false;
		setDisplayStatView(false);
		setDisplayFps(false);
		setPauseOnLostFocus(false);

		AppSettings settings = new AppSettings(true);
		settings.setWidth(640);
		settings.setHeight(480);
		this.setShowSettings(false);
		this.setSettings(settings);
		this.createCanvas();
		JmeCanvasContext ctx = (JmeCanvasContext) this.getContext();
		ctx.setSystemListener(this);
		ctx.getCanvas().setPreferredSize(dim);

		panel3d = new JPanel(new FlowLayout()); // a panel

		// add the JME canvas
		panel3d.add(ctx.getCanvas());

		this.startCanvas();

	}

	public JPanel get3dPanel() {
		return panel3d;
	}

	@Override
	public void simpleInitApp() {

		viewPort.setBackgroundColor(ColorRGBA.LightGray);

		// bsr = new BasicShadowRenderer(assetManager, 256);
		// bsr.setDirection(new Vector3f(0, 6, 6).normalizeLocal()); // light
		// // direction
		// viewPort.addProcessor(bsr);

		attachLight();

		visualHand = new VisualHand3d(assetManager, HandOrientation.LEFT, true);

		rootNode.attachChild(visualHand);

		adjustBoneJointMapping();

		hand.setSaveMovement(true); // TODO remove

		configureCam();

		attachCoordinateAxes(new Vector3f(-5, 0, 0));

		initKeys();

		attachGrid(new Vector3f(0, -10, 0), 50, ColorRGBA.Black);

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

		// Add the names to the action listener.
		inputManager.addListener(actionListener, new String[] { "Switch", "X",
				"Y", "Z", "Increase", "Decrease", "Quit" });

		inputManager.addListener(analogListener, new String[] { "CAM_UP",
				"CAM_DOWN", "CAM_LEFT", "CAM_RIGHT" });

		inputManager.addListener(mousePicker, new String[] { "pick target" });

	}

	public void adjustBoneJointMapping() {
		// init hand
		for (Entry<JointType, Joint> entry : hand.getJointSet()) {

			Quaternion quat = Utils.getSensorQuad(visualHand
					.getBoneRotation(entry.getKey()));

			entry.getValue().setInitialOrientation(quat);

			Vector3f pos = visualHand.getBonePosition(entry.getKey());
			entry.getValue().setInitialPosition(
					new Quaternion(0, pos.x, pos.y, pos.z));
		}
	}

	private void attachLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		PointLight lamp_light = new PointLight();
		lamp_light.setColor(ColorRGBA.White);
		lamp_light.setRadius(12f);
		Vector3f lamp_pos_1 = new Vector3f(0, 6, -6);
		lamp_light.setPosition(lamp_pos_1);
		rootNode.addLight(lamp_light);

		PointLight lamp_light2 = new PointLight();
		lamp_light2.setColor(ColorRGBA.White);
		lamp_light2.setRadius(10f);
		Vector3f lamp_pos_2 = new Vector3f(0, 6, 6);
		lamp_light2.setPosition(lamp_pos_2);

		rootNode.addLight(lamp_light2);

		// createTestSphere(lamp_pos_1);
		// createTestSphere(lamp_pos_2);

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

		double[] angles = quat.getAnglesRadFromQuaternion();

		int i = axis.ordinal();
		angles[i] += value;

		Quaternion newQuat = new Quaternion(angles[0], angles[1], angles[2]);

		// System.out.printf("R%.3f: P%.3f: Y%.3f\n", angles[0], angles[1],
		// angles[2]);
		//
		// newQuat.print(3);

		hand.setLocalJointOrientation(joint, newQuat);

	}

	private ActionListener mousePicker = new ActionListener() {

		@Override
		public void onAction(String name, boolean isPressed, float tpf) {
			if (name.equals("pick target") && !isPressed) {
				// Reset results list.
				CollisionResults results = new CollisionResults();
				// Convert screen click to 3d position
				Vector2f click2d = inputManager.getCursorPosition();
				Vector3f click3d = cam.getWorldCoordinates(
						new Vector2f(click2d.x, click2d.y), 0f).clone();
				Vector3f dir = cam
						.getWorldCoordinates(
								new Vector2f(click2d.x, click2d.y), 1f)
						.subtractLocal(click3d).normalizeLocal();
				// Aim the ray from the clicked spot forwards.
				Ray ray = new Ray(click3d, dir);
				// Collect intersections between ray and all nodes in results
				// list.
				visualHand.collideWith(ray, results);

				// Use the results
				if (results.size() > 0) { // TODO
					// The closest result is the target that the player picked:
					Geometry target = results.getClosestCollision()
							.getGeometry();

					System.out.println("Name: " + target.getName());
					System.out.println("Mouse " + click2d);

					Visual3dPopUpMenu menu = new Visual3dPopUpMenu(myInstance, hand,
							Utils.getJointTypeFromGeometry(target));

					menu.show(panel3d, (int) click2d.x + 10, dim.height
							- (int) click2d.y);
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
			} else if (name.equals("Increase")) {
				manualAddToQuat(currentManipulatedJoint, currentAxis, 0.05);
			} else if (name.equals("Decrease")) {
				manualAddToQuat(currentManipulatedJoint, currentAxis, -0.05);
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
		flyCam.setMoveSpeed(8f);

		flyCam.setDragToRotate(true);

		flyCam.setEnabled(false);

		chaseCam = new ChaseCamera(cam, visualHand, inputManager);
		chaseCam.setInvertHorizontalAxis(true);
		chaseCam.setInvertVerticalAxis(true);
		chaseCam.setMinDistance(4f);
		chaseCam.setMaxDistance(30f);
		chaseCam.setMinVerticalRotation(0);
		chaseCam.setMaxVerticalRotation((float) Math.PI * 2);
		chaseCam.setRotationSensitivity(8);
		chaseCam.setDefaultHorizontalRotation((float) Math.PI / 4);
		chaseCam.setLookAtOffset(new Vector3f(0, 3, 0));
	}

	public void createTestSphere(Vector3f pos) {
		Sphere testSphere = new Sphere(20, 20, 1);
		Geometry ball_geo = new Geometry("cannon ball", testSphere);
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", ColorRGBA.Yellow);
		ball_geo.setMaterial(mat);
		ball_geo.setLocalTranslation(pos);
		rootNode.attachChild(ball_geo);
	}

	@Override
	public void simpleUpdate(float tpf) {
		try {
			Set<Entry<JointType, Joint>> handset = hand.getJointSet();

			LinkedList<MovementStep> storedMovementPositions = hand
					.getSavedMovementFlow();
			// don't draw the last state because it is current one
			int numberOfSteps = storedMovementPositions.size() - 1;

			// add additional hand geometries for movement if necessary
			while (numberOfSteps > movementSteps.size()) {
				VisualHand3d newHand = new VisualHand3d(assetManager,
						HandOrientation.LEFT, false);
				newHand.setOpacity(0.35f);
				movementSteps.add(newHand);
				rootNode.attachChild(newHand);
			}

			for (Entry<JointType, Joint> entry : handset) {

				// update actual hand
				Joint joint = entry.getValue();
				JointType type = entry.getKey();
				Quaternion quat = joint.getWorldOrientation();
				visualHand.setBoneRotationAbs(type, Utils.getJMEQuad(quat));
				visualHand.setVisible(type, joint.isVisible());

				for (int i = 0; i < numberOfSteps; i++) {
					VisualHand3d hand = movementSteps.get(i);

					// TODO not effecient to calculate the set every time
					EnumMap<JointType, StoredJointState> storedSet = storedMovementPositions
							.get(i).getMove().getAll();

					// update movement flow with joint from actual hand if not
					// available in movement.
					if (storedSet.containsKey(type)) {
						hand.setBoneRotationAbs(type, Utils
								.getJMEQuad(storedSet.get(type)
										.getWorldOrientation()));

						hand.setVisible(type, joint.isVisible());
					} else {
						hand.setBoneRotationAbs(type, Utils.getJMEQuad(quat));
						hand.setVisible(type, false);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void attachCoordinateAxes(Vector3f pos) {
		Arrow arrow = new Arrow(Vector3f.UNIT_X.mult(3));
		arrow.setLineWidth(4); // make arrow thicker
		putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

		arrow = new Arrow(Vector3f.UNIT_Y.mult(3));
		arrow.setLineWidth(4); // make arrow thicker
		putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

		arrow = new Arrow(Vector3f.UNIT_Z.mult(3));
		arrow.setLineWidth(4); // make arrow thicker
		putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
	}

	private void attachGrid(Vector3f pos, int size, ColorRGBA color) {
		Geometry g = new Geometry("wireframe grid", new Grid(size, size, 2f));
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		g.center().move(pos);
		rootNode.attachChild(g);
	}

	private Geometry putShape(Mesh shape, ColorRGBA color) {
		Geometry g = new Geometry("coordinate axis", shape);
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		rootNode.attachChild(g);
		return g;
	}

	public JointType getCurrentManipulatedJoint() {
		return currentManipulatedJoint;
	}

	public void setCurrentManipulatedJoint(JointType currentManipulatedJoint) {
		this.currentManipulatedJoint = currentManipulatedJoint;
	}

}
