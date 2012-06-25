package imuanalyzer.ui.swing.menu;

import java.util.ArrayList;

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
