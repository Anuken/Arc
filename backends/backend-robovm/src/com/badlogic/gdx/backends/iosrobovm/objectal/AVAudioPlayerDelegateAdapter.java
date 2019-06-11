package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.annotation.NotImplemented;

/**
 * @author Niklas Therning
 */
public class AVAudioPlayerDelegateAdapter extends NSObject implements AVAudioPlayerDelegate{
    @NotImplemented("audioPlayerDidFinishPlaying:successfully:")
    public void didFinishPlaying(NSObject player, boolean success){
        throw new UnsupportedOperationException();
    }
}