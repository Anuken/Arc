package io.anuke.mnet.broadcast;


public interface BroadcastReceiver{

    /**
     * Called when someone responded for discovery message
     */
    void receive(BroadcastResponse response);

    /**
     * Called when Discovery is finished.
     * @param interrupted whether or not current discovery was interrupted by calling {@link BroadcastSocket#interrupt()}
     */
    void finished(boolean interrupted);

}
