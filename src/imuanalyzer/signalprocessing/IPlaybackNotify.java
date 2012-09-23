package imuanalyzer.signalprocessing;

/**
 * Interface fpr notifing about playback events
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IPlaybackNotify {
	
	/**
	 * Playback has stopped/ is finised
	 */
	void playbackStopped();

	/**
	 * If playback is looping will be notified about every finished cycle
	 */
	void loopCyclePassed();
}
