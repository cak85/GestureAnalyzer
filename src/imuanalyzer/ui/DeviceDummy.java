package imuanalyzer.ui;

import java.util.ArrayList;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class DeviceDummy extends Node {

	boolean isRotating = false;

	boolean isMoving = false;

	protected Node model;

	ArrayList<Geometry> subgeometries = new ArrayList<Geometry>();

	public DeviceDummy(AssetManager assetManager) {
		model = (Node) assetManager.loadModel("Models/ogre/myIpad.mesh.xml");

		model.setShadowMode(ShadowMode.CastAndReceive);

		findGeometries(model);

		setOpacity(0.7f);

		this.attachChild(model);
	}

	public void setVisible(boolean isVisible) {
		if (isVisible) {
			model.setCullHint(CullHint.Dynamic);
		} else {
			model.setCullHint(CullHint.Always);
		}
	}

	public boolean isVisible() {
		if (model.getCullHint().equals(CullHint.Dynamic)) {
			return true;
		} else {
			return false;
		}
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

	public void setOpacity(float opacity) {

		ColorRGBA colorDiffuse = ColorRGBA.White.clone();

		colorDiffuse.a = opacity;

		// LOGGER.debug("Count " + count + " Div " + div + " Opacity "
		// +colorDiffuse.a);

		for (Geometry geo : subgeometries) {
			Material mat = geo.getMaterial();
			mat.setColor("Diffuse", colorDiffuse);

			mat.setBoolean("UseMaterialColors", true);
			mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			mat.setFloat("Shininess", 0.5f);
			geo.setMaterial(mat);
			geo.setQueueBucket(Bucket.Transparent);

		}
	}

	public boolean isRotating() {
		return isRotating;
	}

	public void setRotating(boolean isRotating) {
		this.isRotating = isRotating;
	}

	public boolean isMoving() {
		return isMoving;
	}

	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

}
