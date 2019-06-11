package com.badlogic.gdx.backends.iosrobovm.objectal;

import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSObjectProtocol;
import org.robovm.objc.annotation.Method;

/**
 * @author Niklas Therning
 */
public interface AVAudioPlayerDelegate extends NSObjectProtocol{

    @Method(selector = "audioPlayerDidFinishPlaying:successfully:")
    void didFinishPlaying(NSObject player, boolean success);
}
