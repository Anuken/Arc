package arc.graphics;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.zip.*;

/**
 * Writes Pixmaps to various formats.
 * @author mzechner
 * @author Nathan Sweet
 */
public class PixmapIO{

    /**
     * Arc PIXmap: Similar to to the libGDX CIM format. Writes deflation-compressed pixmap RGBA data to a file.
     * Faster and smaller than RGBA PNG.
     * */
    public static void writeApix(Fi file, Pixmap pixmap) {
        try(DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(file.write(false))); WritableByteChannel channel = Channels.newChannel(out)){
            out.writeInt(pixmap.width);
            out.writeInt(pixmap.height);

            ByteBuffer buf = pixmap.getPixels();
            buf.position(0);
            buf.limit(buf.capacity());
            channel.write(buf);
        }catch(Exception e){
            throw new ArcRuntimeException("Couldn't write Pixmap to file '" + file + "'", e);
        }
    }

    /** Reads deflation-compressed pixmap RGBA data from a file. */
    public static Pixmap readApix(Fi file) {
        try(DataInputStream in = new DataInputStream(new InflaterInputStream(new BufferedInputStream(file.read()))); ReadableByteChannel channel = Channels.newChannel(in)){
            Pixmap pixmap = new Pixmap(in.readInt(), in.readInt());

            ByteBuffer pixelBuf = pixmap.getPixels();
            pixelBuf.position(0);
            pixelBuf.limit(pixelBuf.capacity());
            channel.read(pixelBuf);
            pixelBuf.position(0);
            pixelBuf.limit(pixelBuf.capacity());
            return pixmap;
        }catch(Exception e){
            throw new ArcRuntimeException("Couldn't read Pixmap from file '" + file + "'", e);
        }
    }

    /**
     * Writes the pixmap as a PNG with compression. See {@link PngWriter} to configure the compression level, more efficiently flip the
     * pixmap vertically, and to write out multiple PNGs with minimal allocation.
     */
    public static void writePNG(Fi file, Pixmap pixmap){
        try{
            PngWriter writer = new PngWriter((int)(pixmap.width * pixmap.height * 1.5f)); // Guess at deflated size.
            try{
                writer.setFlipY(false);
                writer.write(file, pixmap);
            }finally{
                writer.dispose();
            }
        }catch(IOException ex){
            throw new ArcRuntimeException("Error writing PNG: " + file, ex);
        }
    }

    /** Reads a PNG file using a pure-Java PNG decoder. */
    public static Pixmap readPNG(Fi file){
        try{
            PngReader reader = new PngReader();
            ByteBuffer result = reader.read(new ByteArrayInputStream(file.readBytes()));
            return new Pixmap(result, reader.width, reader.height);
        }catch(Exception e){
            throw new ArcRuntimeException("Error reading PNG: " + file, e);
        }
    }

    /**
     * PNG encoder with compression. An instance can be reused to encode multiple PNGs with minimal allocation.
     * @author Matthias Mann
     * @author Nathan Sweet
     */
    public static class PngWriter implements Disposable{
        private static final byte[] SIGNATURE = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
        private static final int IHDR = 0x49484452, IDAT = 0x49444154, IEND = 0x49454E44;
        private static final byte COLOR_ARGB = 6;
        private static final byte COMPRESSION_DEFLATE = 0;
        private static final byte FILTER_NONE = 0;
        private static final byte INTERLACE_NONE = 0;
        private static final byte PAETH = 4;

        private final ChunkBuffer buffer;
        private final Deflater deflater;
        private ByteSeq lineOutBytes, curLineBytes, prevLineBytes;
        private boolean flipY = true;
        private int lastLineLen;

        public PngWriter(){
            this(128 * 128);
        }

        public PngWriter(int initialBufferSize){
            buffer = new ChunkBuffer(initialBufferSize);
            deflater = new Deflater();
        }

        /** If true, the resulting PNG is flipped vertically. Default is true. */
        public void setFlipY(boolean flipY){
            this.flipY = flipY;
        }

        /** Sets the deflate compression level. Default is {@link Deflater#DEFAULT_COMPRESSION}. */
        public void setCompression(int level){
            deflater.setLevel(level);
        }

        public void write(Fi file, Pixmap pixmap) throws IOException{
            OutputStream output = file.write(false);
            try{
                write(output, pixmap);
            }finally{
                Streams.close(output);
            }
        }

        /** Writes the pixmap to the stream without closing the stream. */
        public void write(OutputStream output, Pixmap pixmap) throws IOException{
            DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);
            DataOutputStream dataOutput = new DataOutputStream(output);
            dataOutput.write(SIGNATURE);

            buffer.writeInt(IHDR);
            buffer.writeInt(pixmap.width);
            buffer.writeInt(pixmap.height);
            buffer.writeByte(8); // 8 bits per component.
            buffer.writeByte(COLOR_ARGB);
            buffer.writeByte(COMPRESSION_DEFLATE);
            buffer.writeByte(FILTER_NONE);
            buffer.writeByte(INTERLACE_NONE);
            buffer.endChunk(dataOutput);

            buffer.writeInt(IDAT);
            deflater.reset();

            int lineLen = pixmap.width * 4;
            byte[] lineOut, curLine, prevLine;
            if(lineOutBytes == null){
                lineOut = (lineOutBytes = new ByteSeq(lineLen)).items;
                curLine = (curLineBytes = new ByteSeq(lineLen)).items;
                prevLine = (prevLineBytes = new ByteSeq(lineLen)).items;
            }else{
                lineOut = lineOutBytes.ensureCapacity(lineLen);
                curLine = curLineBytes.ensureCapacity(lineLen);
                prevLine = prevLineBytes.ensureCapacity(lineLen);
                for(int i = 0, n = lastLineLen; i < n; i++)
                    prevLine[i] = 0;
            }
            lastLineLen = lineLen;

            ByteBuffer pixels = pixmap.getPixels();
            int oldPosition = pixels.position();
            for(int y = 0, h = pixmap.height; y < h; y++){
                int py = flipY ? (h - y - 1) : y;
                pixels.position(py * lineLen);
                pixels.get(curLine, 0, lineLen);

                lineOut[0] = (byte)(curLine[0] - prevLine[0]);
                lineOut[1] = (byte)(curLine[1] - prevLine[1]);
                lineOut[2] = (byte)(curLine[2] - prevLine[2]);
                lineOut[3] = (byte)(curLine[3] - prevLine[3]);

                for(int x = 4; x < lineLen; x++){
                    int a = curLine[x - 4] & 0xff;
                    int b = prevLine[x] & 0xff;
                    int c = prevLine[x - 4] & 0xff;
                    int p = a + b - c;
                    int pa = p - a;
                    if(pa < 0) pa = -pa;
                    int pb = p - b;
                    if(pb < 0) pb = -pb;
                    int pc = p - c;
                    if(pc < 0) pc = -pc;
                    if(pa <= pb && pa <= pc)
                        c = a;
                    else if(pb <= pc) //
                        c = b;
                    lineOut[x] = (byte)(curLine[x] - c);
                }

                deflaterOutput.write(PAETH);
                deflaterOutput.write(lineOut, 0, lineLen);

                byte[] temp = curLine;
                curLine = prevLine;
                prevLine = temp;
            }
            pixels.position(oldPosition);
            deflaterOutput.finish();
            buffer.endChunk(dataOutput);

            buffer.writeInt(IEND);
            buffer.endChunk(dataOutput);

            output.flush();
        }

        @Override
        public void dispose(){
            deflater.end();
        }

        static class ChunkBuffer extends DataOutputStream{
            final ByteArrayOutputStream buffer;
            final CRC32 crc;

            ChunkBuffer(int initialSize){
                this(new ByteArrayOutputStream(initialSize), new CRC32());
            }

            private ChunkBuffer(ByteArrayOutputStream buffer, CRC32 crc){
                super(new CheckedOutputStream(buffer, crc));
                this.buffer = buffer;
                this.crc = crc;
            }

            public void endChunk(DataOutputStream target) throws IOException{
                flush();
                target.writeInt(buffer.size() - 4);
                buffer.writeTo(target);
                target.writeInt((int)crc.getValue());
                buffer.reset();
                crc.reset();
            }
        }
    }

    /** Class based on https://github.com/Mike-C/lwjPNG */
    public static class PngReader{
        /** Size fields are set after reading. */
        public int width, height;

        private int dataLen, cs;
        private byte[] imgData = null, header = new byte[5];
        private ByteBuffer buf = null;

        public ByteBuffer read(InputStream in) throws IOException{
            readChunks(new DataInputStream(in));

            if(buf != null) buf.clear();
            buf = ByteBuffer.allocateDirect(cs);
            getImage(buf);
            buf.flip();
            return buf;
        }

        private void readChunks(DataInputStream in) throws IOException{
            if(imgData == null && in.available() > 4)
                in.readLong(); // PNG signature
            else if(imgData == null){
                width = 0;
                return;
            }
            dataLen = 0;
            int chunkType;
            do{
                int chunkLen = in.readInt(); // Read the chunk length.
                if(chunkLen <= 0 || chunkLen > 99998192)
                    break;
                chunkType = in.readInt();
                if(chunkType == 0x49454e44) // IEND
                    break; // last chunk reached..
                if(chunkType != 0x49444154){ // IDAT
                    if(chunkType == 0x49484452){ // IHDR
                        width = in.readInt();
                        height = in.readInt();
                        cs = 4 * width * height;
                        imgData = new byte[in.available()]; // initialize image array
                        in.readFully(header);
                    }else{
                        byte[] chunkData = new byte[chunkLen];
                        in.readFully(chunkData);
                    }
                }else{
                    in.readFully(imgData, dataLen, chunkLen);
                    dataLen += chunkLen;
                }
                in.readInt(); // checksum skip
            }while(true);
        }

        public short getBitsPerPixel(){
            return (short)(header[0] & 0xFF);
        }

        public short getColorType(){
            return (short)(header[1] & 0xFF);
        }

        public short getCompression(){
            return (short)(header[2] & 0xFF);
        }

        public short getFilter(){
            return (short)(header[3] & 0xFF);
        }

        public short getInterlace(){
            return (short)(header[4] & 0xFF);
        }

        private void getImage(ByteBuffer bb){
            // bPx bytes per pixel, in interlace, wT total output width, v scanline width
            // oH - output offset start horizontal; oV - output offset start vertical
            // rH - repetitions horizontal (rH[p]-1) << 2; rV - repetitions vertical
            // sw - scanline width per pass; sp - scanlines/rows per pass;

            int bPx = getColorType() == 2 ? 3 : 4, in = getInterlace() == 1 ? 7 : 1, wT = width * 4, v = width * bPx;
            int[] sw = {in == 7 ? ((width & 7) != 0 ? ((width / 8) + 1) * bPx : v / 8) : v,
            (width & 7) != 0 ? (width + 3) / 8 * bPx : v / 8, (width & 3) != 0 ? ((width / 4) + 1) * bPx : v / 4,
            (width & 3) != 0 ? (width + 1) / 4 * bPx : v / 4, (width & 1) != 0 ? ((width / 2) + 1) * bPx : v / 2, width / 2 * bPx, v};
            int[] sp = {in == 7 ? ((height & 7) != 0 ? (height / 8) + 1 : height / 8) : height, (height & 7) != 0 ? (height / 8) + 1 : height / 8,
            (height & 7) != 0 ? (height + 3) / 8 : height / 8, (height & 3) != 0 ? (height / 4) + 1 : height / 4,
            (height & 3) != 0 ? (height + 1) / 4 : height / 4, (height & 1) != 0 ? (height / 2) + 1 : height / 2, height / 2};
            int[] oH = {0, 16, 0, 8, 0, 4, 0}, oV = {0, 0, 4, 0, 2, 0, 1};
            int[] rH = {in == 7 ? 28 : 0, 28, 12, 12, 4, 4, 0}, rV = {in == 7 ? 8 : 1, 8, 8, 4, 4, 2, 2};
            int oI = 0; // oI output offset/index

            Inflater inflater = new Inflater();
            inflater.setInput(imgData, 0, dataLen);

            for(int p = 0; p < in; p++){ // interlace passes..
                v = sw[p] + 1; // scanLine width
                byte[] row0 = new byte[wT + 1], row = new byte[wT + 1]; // every row contains filter byte!!!!
                oI = oH[p] + (oV[p] * wT); // start oI position
                for(int i = 1, s = 0; s < sp[p]; i = 1, s++){ // scanLine
                    try{
                        inflater.inflate(row, 0, v);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    if(row[0] != 0){ // apply filters
                        if(row[0] == 1){
                            for(i += bPx; i < v; i++){
                                row[i] += row[i - bPx];
                            }
                        }else if(row[0] == 2){
                            for(; i < v; i++){
                                row[i] += row0[i];
                            }
                        }else if(row[0] == 3){
                            for(; i < bPx + 1; i++){
                                row[i] += (row0[i] & 0xFF) >>> 1;
                            }
                            for(; i < v; i++){
                                row[i] += ((row0[i] & 0xFF) + (row[i - bPx] & 0xFF)) >>> 1;
                            }
                        }else{
                            for(; i < bPx + 1; i++){
                                row[i] += row0[i];
                            }
                            for(; i < v; i++){
                                row[i] += paethP(row[i - bPx] & 0xFF, row0[i] & 0xFF, row0[i - bPx] & 0xFF);
                            }
                        }
                    }
                    ByteBuffer wRow = ByteBuffer.wrap(row);
                    if(in == 1){ // format output, normal mode
                        if(bPx == 3){
                            for(i = 1; i < v; i += bPx){
                                bb.putInt((wRow.getInt(i) & 0xFFFFFF00) + 0xFF);
                            }
                        }else
                            bb.put(row, 1, v - 1);
                    }else{ // interlaced mode, or normal mode
                        if(bPx == 3)
                            for(i = 1; i < v; i += bPx, oI += rH[p] + 4){
                                bb.putInt(oI, (wRow.getInt(i) & 0xFFFFFF00) + 0xFF);
                            }
                        else
                            for(i = 1; i < v; i += bPx, oI += rH[p] + 4){
                                bb.putInt(oI, wRow.getInt(i));
                            }
                    }
                    byte[] swap = row0;
                    row0 = row;
                    row = swap;
                    // start oI position, increased by current scanline's iteration offset
                    oI = oH[p] + (oV[p] * wT) + ((rV[p] * wT) * (s + 1));
                } // for scanLine
            }
            bb.position(bb.capacity());
            imgData = null;
        }

        private static int ab(int a){
            int b = a >> 8;
            return (a ^ b) - b;
        }

        private static int paethP(int a, int b, int c){
            int pa = b - c, pb = a - c, pc = ab(pa + pb);
            pa = ab(pa);
            pb = ab(pb);
            return (pa <= pb && pa <= pc) ? a : (pb <= pc) ? b : c;
        }
    }
}
