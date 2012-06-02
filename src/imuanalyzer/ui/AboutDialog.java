package imuanalyzer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

class AboutDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2851522481401543266L;

	public AboutDialog() {
		setTitle("About");
		setLayout(new GridLayout(0, 1));

		JLabel name = new JLabel(
				"<html><body>Chistopher-Eyk Hrabia</body></html>");
		name.setHorizontalAlignment(SwingConstants.CENTER);
		add(name);

		JLabel institue = new JLabel("<html><body>TU-Berlin 2012</body></html>");
		institue.setHorizontalAlignment(SwingConstants.CENTER);
		add(institue);

		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new BorderLayout());
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		buttonPanel.add(close, BorderLayout.EAST);

		add(buttonPanel);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(300, 200);
	}
}