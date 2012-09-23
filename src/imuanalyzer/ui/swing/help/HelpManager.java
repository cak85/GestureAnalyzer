package imuanalyzer.ui.swing.help;

import java.awt.Component;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

/**
 * Manage included online help includes some helper functions implemented as
 * singleton
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class HelpManager {
	public javax.help.HelpSet helpSet = null;
	public javax.help.HelpBroker helpBroker = null;

	private static HelpManager instance = null;

	private static final Logger LOGGER = Logger.getLogger(HelpManager.class
			.getName());

	public static HelpManager getInstance() {
		if (instance == null) {
			instance = new HelpManager();
		}
		return instance;
	}

	/**
	 * Constructor for initializing help
	 */
	private HelpManager() {

		try {
			java.net.URL hsURL = HelpSet.findHelpSet(null, "jhelpset.hs");
			helpSet = new HelpSet(null, hsURL);
			helpBroker = helpSet.createHelpBroker();
		} catch (HelpSetException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Enable help via F1 key on component
	 * 
	 * @param componet
	 * @param id
	 */
	public void enableHelpKey(Component componet, String id) {
		try {
			helpBroker.enableHelpKey(componet, id, helpSet);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Did not find help key: " + id);
		}
	}

	/**
	 * Start help on menu item to id
	 * 
	 * @param item
	 * @param id
	 */
	public void enableHelpOnButton(JMenuItem item, String id) {
		try {
			helpBroker.enableHelpOnButton(item, id, helpSet);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Did not find help key: " + id);
		}
	}

	/**
	 * Starts help from button to id
	 * 
	 * @param item
	 * @param id
	 */
	public void enableHelpOnButton(JButton item, String id) {
		try {
			helpBroker.enableHelpOnButton(item, id, helpSet);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Did not find help key: " + id);
		}
	}
}
