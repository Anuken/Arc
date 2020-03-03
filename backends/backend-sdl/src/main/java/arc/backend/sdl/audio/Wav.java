package arc.backend.sdl.audio;

import arc.files.*;
import arc.util.*;
import arc.util.io.*;

import java.io.*;

public class Wav{
    public static class Music extends ALMusic{
        private WavInputStream input;

        public Music(ALAudio audio, Fi file){
            super(audio, file);
            input = new WavInputStream(file);
            if(audio.noDevice) return;
            setup(input.channels, input.sampleRate);
        }

        public int read(byte[] buffer){
            if(input == null){
                input = new WavInputStream(file);
                setup(input.channels, input.sampleRate);
            }
            try{
                return input.read(buffer);
            }catch(IOException ex){
                throw new ArcRuntimeException("Error reading WAV file: " + file, ex);
            }
        }

        public void reset(){
            Streams.close(input);
            input = null;
        }
    }

    public static class Sound extends ALSound{
        public Sound(ALAudio audio, Fi file){
            super(audio);
            if(audio.noDevice) return;

            WavInputStream input = null;
            try{
                input = new WavInputStream(file);
                setup(Streams.copyBytes(input, input.dataRemaining), input.channels, input.sampleRate);
            }catch(IOException ex){
                throw new ArcRuntimeException("Error reading WAV file: " + file, ex);
            }finally{
                Streams.close(input);
            }
        }
    }

    /** @author Nathan Sweet */
    static private class WavInputStream extends FilterInputStream{
        int channels, sampleRate, dataRemaining;

        WavInputStream(Fi file){
            super(file.read());
            try{
                if(read() != 'R' || read() != 'I' || read() != 'F' || read() != 'F')
                    throw new ArcRuntimeException("RIFF header not found: " + file);

                skipFully(4);

                if(read() != 'W' || read() != 'A' || read() != 'V' || read() != 'E')
                    throw new ArcRuntimeException("Invalid wave file header: " + file);

                int fmtChunkLength = seekToChunk('f', 'm', 't', ' ');

                int type = read() & 0xff | (read() & 0xff) << 8;
                if(type != 1) throw new ArcRuntimeException("WAV files must be PCM: " + type);

                channels = read() & 0xff | (read() & 0xff) << 8;
                if(channels != 1 && channels != 2)
                    throw new ArcRuntimeException("WAV files must have 1 or 2 channels: " + channels);

                sampleRate = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;

                skipFully(6);

                int bitsPerSample = read() & 0xff | (read() & 0xff) << 8;
                if(bitsPerSample != 16)
                    throw new ArcRuntimeException("WAV files must have 16 bits per sample: " + bitsPerSample);

                skipFully(fmtChunkLength - 16);

                dataRemaining = seekToChunk('d', 'a', 't', 'a');
            }catch(Throwable ex){
                Streams.close(this);
                throw new ArcRuntimeException("Error reading WAV file: " + file, ex);
            }
        }

        private int seekToChunk(char c1, char c2, char c3, char c4) throws IOException{
            while(true){
                boolean found = read() == c1;
                found &= read() == c2;
                found &= read() == c3;
                found &= read() == c4;
                int chunkLength = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;
                if(chunkLength == -1) throw new IOException("Chunk not found: " + c1 + c2 + c3 + c4);
                if(found) return chunkLength;
                skipFully(chunkLength);
            }
        }

        private void skipFully(int count) throws IOException{
            while(count > 0){
                long skipped = in.skip(count);
                if(skipped <= 0) throw new EOFException("Unable to skip.");
                count -= skipped;
            }
        }

        public int read(byte[] buffer) throws IOException{
            if(dataRemaining == 0) return -1;
            int offset = 0;
            do{
                int length = Math.min(super.read(buffer, offset, buffer.length - offset), dataRemaining);
                if(length == -1){
                    if(offset > 0) return offset;
                    return -1;
                }
                offset += length;
                dataRemaining -= length;
            }while(offset < buffer.length);
            return offset;
        }
    }
}
