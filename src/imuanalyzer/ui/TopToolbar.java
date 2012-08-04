package imuanalyzer.ui;

import imuanalyzer.signalprocessing.IOrientationSensors;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class TopToolbar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7403865217590908973L;

	Visual3d visual3d;

	@SuppressWarnings("serial")
	public TopToolbar(IOrientationSensors sensors, Visual3d _visual3d) {
		this.visual3d = _visual3d;

		this.setLayout(new GridLayout(0, 3));

		JPanel leftPanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.LEFT);
		leftPanel.setLayout(layout);

		ConnectionPanel connectionPanel = new ConnectionPanel(sensors);

		leftPanel.add(connectionPanel);

		this.add(leftPanel);

		JPanel rightPanel = new JPanel();
		FlowLayout layout2 = new FlowLayout();
		layout2.setAlignment(FlowLayout.LEFT);
		rightPanel.setLayout(layout2);

		JButton resetHandButton = new JButton("Reset hand");
		resetHandButton.setToolTipText("Reset to rest position");
		resetHandButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetHand();
			}
		});
		//shortcut F3
		resetHandButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "Reset_Hand");

		resetHandButton.getActionMap().put("Reset_Hand", new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				resetHand();				
			}
		});
		
		rightPanel.add(resetHandButton);

		this.add(rightPanel);
	}

	private void resetHand() {
		visual3d.resetHand();
	}
}
