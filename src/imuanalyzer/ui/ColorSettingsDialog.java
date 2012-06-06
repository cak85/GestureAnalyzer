package imuanalyzer.ui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ColorSettingsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3788222855154400648L;
	
	JointSetting setting;

	public ColorSettingsDialog(Component comp, JointSetting _setting) {
		this.setting = _setting;

		this.setTitle("Color Settings " + setting.getType());

		this.setLayout(new GridLayout(0, 2, 10, 5));

		this.add(new JLabel("Live motion"));

		this.add(new ColorChooserButton("    ", setting.getLiveMotionColor(),
				comp));

		this.add(new JLabel("Live motion max line"));

		this.add(new ColorChooserButton("    ", setting
				.getLiveMotionMaxLineColor(), comp));

		this.add(new JLabel("Live motion min line"));

		this.add(new ColorChooserButton("    ", setting
				.getLiveMotionMinLineColor(), comp));

		this.add(new JLabel("Stored motion"));

		this.add(new ColorChooserButton("    ", setting.getStoredMotionColor(),
				comp));

		this.add(new JLabel("Live touch current"));

		this.add(new ColorChooserButton("    ", setting
				.getLiveTouchCurrentColor(), comp));

		this.add(new JLabel("Live touch max"));

		this.add(new ColorChooserButton("    ", setting.getLiveTouchMaxColor(),
				comp));
		
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setting.resetColor();
				dispose();
			}
		});
		this.add(reset);

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		this.add(close);

		this.setSize(300, 300);

		this.setVisible(true);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}

}
