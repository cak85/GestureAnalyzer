package imuanalyzer.ui;

import java.awt.Component;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.JButton;
import javax.swing.JMenuItem;

public class HelpManager {
	public javax.help.HelpSet helpSet = null;
	public javax.help.HelpBroker helpBroker = null;

	private static HelpManager instance = null;

	public static HelpManager getInstance() {
		if (instance == null) {
			instance = new HelpManager();
		}
		return instance;
	}

	private HelpManager() {

		try {
			java.net.URL hsURL = HelpSet.findHelpSet(null, "jhelpset.hs");
			helpSet = new HelpSet(null, hsURL);
			helpBroker = helpSet.createHelpBroker();
		} catch (HelpSetException e) {
			e.printStackTrace();
		}

	}

	public void enableHelpKey(Component componet, String id) {
		helpBroker.enableHelpKey(componet, id, helpSet);
	}

	public void enableHelpOnButton(JMenuItem item, String id) {
		helpBroker.enableHelpOnButton(item, id, helpSet);
	}
	
	public void enableHelpOnButton(JButton item, String id) {
		helpBroker.enableHelpOnButton(item, id, helpSet);
	}
}
