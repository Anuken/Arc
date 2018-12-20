package io.anuke.arc.backends.gwt.soundmanager2;

import io.anuke.arc.backends.gwt.soundmanager2.SMSound.SMSoundCallback;

public class SMSoundOptions{
    public int volume = 100;
    public int pan = 0;
    public int loops = 1;
    public int from = 0;
    public SMSoundCallback callback = null;
    public SMSoundOptions(){
    }
}
