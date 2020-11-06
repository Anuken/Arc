package arc.audio;

public class SoloudAudio{

    /*JNI
    #include "soloud.h"
    #include "soloud_wav.h"
    #include "soloud_speech.h"
    #include "soloud_thread.h"
    #include <stdio.h>
     */

    public static native void init(String soundFile); /*
        SoLoud::Soloud soloud;  // SoLoud engine core
          SoLoud::Speech speech;  // A sound source (speech, in this case)

          // Configure sound source
          speech.setText("1 2 3   1 2 3   Hello world. Welcome to So-Loud.");

          // initialize SoLoud.
          soloud.init();

          // Play the sound source (we could do this several times if we wanted)
          soloud.play(speech);

          // Wait until sounds have finished
          while (soloud.getActiveVoiceCount() > 0)
          {
            // Still going, sleep for a bit
            SoLoud::Thread::sleep(100);
          }

          // Clean up SoLoud
          soloud.deinit();
    */
}
