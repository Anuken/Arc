package arc.audio;

import arc.*;

public class Speech extends Sound{

    public Speech(){
        if(Core.audio != null && Core.audio.initialized()){
            handle = Soloud.speechNew();
        }
    }

    public void setText(String text){
        if(handle != 0){
            Soloud.speechText(handle, text);
        }
    }

    public void setParams(int freq, float speed, float declination, int waveform){
        if(handle != 0){
            Soloud.speechParams(handle, freq, speed, declination, waveform);
        }
    }
}
