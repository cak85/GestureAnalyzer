package imuanalyzer.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

public class ActionAdapter implements Action{

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	@Override
	public Object getValue(String key) {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void putValue(String key, Object value) {
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

	@Override
	public void setEnabled(boolean b) {
	}

}
