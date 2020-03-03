package arc.backend.sdl.audio;

import arc.files.*;
import arc.util.io.*;

import java.io.*;

/** @author Nathan Sweet */
public class Ogg{
    public static class Music extends ALMusic{
        private OggInputStream input;
        private OggInputStream previousInput;

        public Music(ALAudio audio, Fi file){
            super(audio, file);
            if(audio.noDevice) return;
            input = new OggInputStream(file.read());
            setup(input.getChannels(), input.getSampleRate());
        }

        @Override
        public int read(byte[] buffer){
            if(input == null){
                input = new OggInputStream(file.read(), previousInput);
                setup(input.getChannels(), input.getSampleRate());
                previousInput = null; // release this reference
            }
            return input.read(buffer);
        }

        @Override
        public void reset(){
            Streams.close(input);
            previousInput = null;
            input = null;
        }

        @Override
        protected void loop(){
            Streams.close(input);
            previousInput = input;
            input = null;
        }
    }

    public static class Sound extends ALSound{
        public Sound(ALAudio audio, Fi file){
            super(audio);
            if(audio.noDevice) return;
            OggInputStream input = null;
            try{
                input = new OggInputStream(file.read());
                ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
                byte[] buffer = new byte[2048];
                while(!input.atEnd()){
                    int length = input.read(buffer);
                    if(length == -1) break;
                    output.write(buffer, 0, length);
                }
                setup(output.toByteArray(), input.getChannels(), input.getSampleRate());
            }finally{
                Streams.close(input);
            }
        }
    }
}
