package imuanalyzer.ui.swing.menu;

import imuanalyzer.ui.DeviceDummy;
import imuanalyzer.ui.Visual3d;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class Visual3dDevicePopUpMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4904278609301818369L;

	Visual3d visual3d;
	DeviceDummy device;

	public Visual3dDevicePopUpMenu(Visual3d visual3d, final DeviceDummy device) {
		this.visual3d = visual3d;
		this.device = device;

		JMenuItem anItem;

		// move
		if (device.isMoving()) {
			anItem = new JMenuItem("Finish Moving");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					device.setMoving(false);
				}
			});
			add(anItem);
		} else {
			anItem = new JMenuItem("Move");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					device.setMoving(true);
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
				}
			});
			add(anItem);
		} else {
			anItem = new JMenuItem("Rotate");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					device.setRotating(true);
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
				}
			});
			add(anItem);
		}

	}

}