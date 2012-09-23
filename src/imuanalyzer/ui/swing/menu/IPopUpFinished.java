package imuanalyzer.ui.swing.menu;

/**
 * Interface for getting a notifiaction if a popup is finished/closed
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IPopUpFinished {
	/**
	 * Will be called after popup is closed/selection is done
	 */
	void notifyFinished();
}
