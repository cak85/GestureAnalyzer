package imuanalyzer.ui.swing;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.ui.jmonkey.Visual3d;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

/**
 * vertical toolbar panel
 * includes feelings ui and infobox
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class VerticalToolbarPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7650659036965941580L;

	protected Hand hand;

	protected IOrientationSensors sensors;

	InfoBox infoBox;

	public VerticalToolbarPanel(Hand _hand, IOrientationSensors _sensors,
			MainFrame mainFrame, Visual3d visual3d) {
		this.hand = _hand;
		this.sensors = _sensors;

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		infoBox = new InfoBox();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0; // reset to default
		c.weightx = 1;
		c.weighty = 1; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(0, 0, 0, 0);

		this.add(infoBox, c);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0; // reset to default
		c.weighty = 1; // request any extra vertical space
		c.weightx = 1;
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 1;
		c.gridheight = 3;
		c.insets = new Insets(20, 0, 0, 0);

		JPanel subjectivePanel = new FeelingSliders(hand);

		this.add(subjectivePanel, c);
	}

	public InfoBox getInfoBox() {
		return infoBox;
	}
}
