package imuanalyzer.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

class AboutDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2851522481401543266L;

	public AboutDialog() {
		setTitle("About");
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		add(Box.createRigidArea(new Dimension(0, 10)));

		JLabel name = new JLabel(
				"<html><body><center>Chistopher-Eyk Hrabia<br>TU-Berlin 2012</center></body></html>");
		add(name);

		add(Box.createRigidArea(new Dimension(0, 100)));

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});

		add(close);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(300, 200);
	}
}