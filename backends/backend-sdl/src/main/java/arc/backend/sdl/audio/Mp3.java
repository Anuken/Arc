package arc.backend.sdl.audio;

import arc.files.*;
import arc.util.*;
import javazoom.jl.decoder.*;

import java.io.*;

/** @author Nathan Sweet */
public class Mp3{
    public static class Music extends ALMusic{
        // Note: This uses a slightly modified version of JLayer.

        private Bitstream bitstream;
        private OutputBuffer outputBuffer;
        private MP3Decoder decoder;

        public Music(ALAudio audio, Fi file){
            super(audio, file);
            if(audio.noDevice) return;
            bitstream = new Bitstream(file.read());
            decoder = new MP3Decoder();
            bufferOverhead = 4096;
            try{
                Header header = bitstream.readFrame();
                if(header == null) throw new ArcRuntimeException("Empty MP3");
                int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
                outputBuffer = new OutputBuffer(channels, false);
                decoder.setOutputBuffer(outputBuffer);
                setup(channels, header.getSampleRate());
            }catch(BitstreamException e){
                throw new ArcRuntimeException("error while preloading mp3", e);
            }
        }

        @Override
        public int read(byte[] buffer){
            try{
                boolean setup = bitstream == null;
                if(setup){
                    bitstream = new Bitstream(file.read());
                    decoder = new MP3Decoder();
                }

                int totalLength = 0;
                int minRequiredLength = buffer.length - OutputBuffer.BUFFERSIZE * 2;
                while(totalLength <= minRequiredLength){
                    Header header = bitstream.readFrame();
                    if(header == null) break;
                    if(setup){
                        int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
                        outputBuffer = new OutputBuffer(channels, false);
                        decoder.setOutputBuffer(outputBuffer);
                        setup(channels, header.getSampleRate());
                        setup = false;
                    }
                    try{
                        decoder.decodeFrame(header, bitstream);
                    }catch(Exception ignored){
                        // JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
                    }
                    bitstream.closeFrame();

                    int length = outputBuffer.reset();
                    System.arraycopy(outputBuffer.getBuffer(), 0, buffer, totalLength, length);
                    totalLength += length;
                }
                return totalLength;
            }catch(Throwable ex){
                reset();
                throw new ArcRuntimeException("Error reading audio data.", ex);
            }
        }

        @Override
        public void reset(){
            if(bitstream == null) return;
            try{
                bitstream.close();
            }catch(BitstreamException ignored){
            }
            bitstream = null;
        }
    }

    public static class Sound extends ALSound{
        // Note: This uses a slightly modified version of JLayer.

        public Sound(ALAudio audio, Fi file){
            super(audio);
            if(audio.noDevice) return;
            ByteArrayOutputStream output = new ByteArrayOutputStream(4096);

            Bitstream bitstream = new Bitstream(file.read());
            MP3Decoder decoder = new MP3Decoder();

            try{
                OutputBuffer outputBuffer = null;
                int sampleRate = -1, channels = -1;
                while(true){
                    Header header = bitstream.readFrame();
                    if(header == null) break;
                    if(outputBuffer == null){
                        channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
                        outputBuffer = new OutputBuffer(channels, false);
                        decoder.setOutputBuffer(outputBuffer);
                        sampleRate = header.getSampleRate();
                    }
                    try{
                        decoder.decodeFrame(header, bitstream);
                    }catch(Exception ignored){
                        // JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
                    }
                    bitstream.closeFrame();
                    output.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
                }
                bitstream.close();
                setup(output.toByteArray(), channels, sampleRate);
            }catch(Throwable ex){
                throw new ArcRuntimeException("Error reading audio data.", ex);
            }
        }
    }
}
