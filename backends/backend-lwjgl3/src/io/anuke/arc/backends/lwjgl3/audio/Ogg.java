package io.anuke.arc.backends.lwjgl3.audio;

import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.io.Streams;

import java.io.ByteArrayOutputStream;

/** @author Nathan Sweet */
public class Ogg{
    public static class Music extends OpenALMusic{
        private OggInputStream input;
        private OggInputStream previousInput;

        public Music(OpenALAudio audio, FileHandle file){
            super(audio, file);
            if(audio.noDevice) return;
            input = new OggInputStream(file.read());
            setup(input.getChannels(), input.getSampleRate());
        }

        public int read(byte[] buffer){
            if(input == null){
                input = new OggInputStream(file.read(), previousInput);
                setup(input.getChannels(), input.getSampleRate());
                previousInput = null; // release this reference
            }
            return input.read(buffer);
        }

        public void reset(){
            Streams.closeQuietly(input);
            previousInput = null;
            input = null;
        }

        @Override
        protected void loop(){
            Streams.closeQuietly(input);
            previousInput = input;
            input = null;
        }
    }

    public static class Sound extends OpenALSound{
        public Sound(OpenALAudio audio, FileHandle file){
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
                Streams.closeQuietly(input);
            }
        }
    }
}
