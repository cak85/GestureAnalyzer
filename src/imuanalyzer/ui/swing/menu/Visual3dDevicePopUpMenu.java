package imuanalyzer.ui.swing.menu;

import imuanalyzer.ui.jmonkey.DeviceDummy;
import imuanalyzer.ui.jmonkey.Visual3d;
import imuanalyzer.ui.swing.help.HelpManager;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Popup for context sensitive tablet device configuration
 * allows moving an rotation of device
 * @author Christopher-Eyk Hrabia
 *
 */
class Visual3dDevicePopUpMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4904278609301818369L;

	Visual3d visual3d;
	DeviceDummy device;
	
	FinishListenerHandler finishHandler = new FinishListenerHandler();

	public Visual3dDevicePopUpMenu(Visual3d visual3d, final DeviceDummy device) {
		this.visual3d = visual3d;
		this.device = device;
		
		HelpManager.getInstance().enableHelpKey(this, "device");

		JMenuItem anItem;

		// move
		if (device.isMoving()) {
			anItem = new JMenuItem("Finish Moving");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					device.setMoving(false);
					finishHandler.notifyFinished();
				}
			});
			add(anItem);
		} else {
			anItem = new JMenuItem("Move");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					device.setMoving(true);
					finishHandler.notifyFinished();
				}
			});
			add(anItem);
		}

		if (device.isRotating()) {
			anItem = new JMenuItem("Finish rotating");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					device.setRotating(false);
					finishHandler.notifyFinished();
				}
			});
			add(anItem);
		} else {
			anItem = new JMenuItem("Rotate");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					device.setRotating(true);
					finishHandler.notifyFinished();
				}
			});
			add(anItem);
		}

		if (device.isVisible()) {
			// visibility
			anItem = new JMenuItem("Hide");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					device.setVisible(false);
					finishHandler.notifyFinished();
				}
			});
			add(anItem);
		}

	}

	public FinishListenerHandler getFinishHandler() {
		return finishHandler;
	}

}