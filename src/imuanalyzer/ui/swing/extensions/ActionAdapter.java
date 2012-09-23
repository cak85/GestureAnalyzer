package imuanalyzer.ui.swing.extensions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import org.apache.log4j.Logger;

/**
 * Adapter for actions
 * shortens code for abstract implementations of actions
 * 
 * @author Christopher-Eyk Hrabia
 *
 */
public class ActionAdapter implements Action {

	private static final Logger LOGGER = Logger.getLogger(ActionAdapter.class
			.getName());

	@Override
	public void actionPerformed(ActionEvent e) {
		LOGGER.debug("actionPerformed");
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		LOGGER.debug("addPropertyChangeListener");
	}

	@Override
	public Object getValue(String key) {
		LOGGER.debug("getValue");
		return null;
	}

	@Override
	public boolean isEnabled() {
		LOGGER.debug("isEnabled");
		return false;
	}

	@Override
	public void putValue(String key, Object value) {
		LOGGER.debug("putValue");
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		LOGGER.debug("removePropertyChangeListener");
	}

	@Override
	public void setEnabled(boolean b) {
		LOGGER.debug("setEnabled");
	}

}
