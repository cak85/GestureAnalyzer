package imuanalyzer.ui.swing.menu;

import java.util.ArrayList;

/**
 * Handler for managing the hand of an menu selection
 * @author Christopher-Eyk Hrabia
 *
 */
public class FinishListenerHandler {

	ArrayList<IPopUpFinished> finishListeners = new ArrayList<IPopUpFinished>(); 
	
	public void addFinishListener(IPopUpFinished listener){
		if(!finishListeners.contains(listener)){
			finishListeners.add(listener);
		}
	}
	
	public void removeFinishListener(IPopUpFinished listener){
		finishListeners.remove(listener);
	}
	
	public void notifyFinished(){
		for ( IPopUpFinished listener: finishListeners){
			listener.notifyFinished();
		}
	}
}
