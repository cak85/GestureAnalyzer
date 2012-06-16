package imuanalyzer.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class TopToolbar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7403865217590908973L;
	
	Visual3d visual3d;

	public TopToolbar(Visual3d _visual3d) {
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.LEFT);
		this.setLayout(layout);
		this.visual3d = _visual3d;

		JButton resetHandButton = new JButton("Reset hand");
		resetHandButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				visual3d.resetHand();
			}
		});
		this.add(resetHandButton);
	}
}
