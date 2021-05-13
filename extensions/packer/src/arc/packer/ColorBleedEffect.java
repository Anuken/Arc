package arc.packer;

import arc.graphics.*;

import java.nio.*;

/**
 * @author Ruben Garat
 * @author Ariel Coppes
 * @author Nathan Sweet
 * @author Anuke
 */
public class ColorBleedEffect{
    private static final int toProcess = 0, realData = 2;
    private static final int[][] offsets = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

    int[] data, pending, changing;
    int pendingSize, changingSize;

    public Pixmap processImage(Pixmap image, int maxIterations){
        int[] pixels = new int[image.width * image.height];
        IntBuffer buffer = image.getPixels().asIntBuffer();
        buffer.position(0);
        buffer.get(pixels);

        Pixmap out = new Pixmap(image.width, image.height);

        data = new int[pixels.length];
        pending = new int[pixels.length];
        changing = new int[pixels.length];
        for(int i = 0; i < pixels.length; i++){
            if(Color.ai(pixels[i]) == 0){
                data[i] = toProcess;
                pending[pendingSize] = i;
                pendingSize++;
            }else{
                data[i] = realData;
            }
        }

        int iterations = 0;
        int lastPending = -1;
        while(pendingSize > 0 && pendingSize != lastPending && iterations < maxIterations){
            lastPending = pendingSize;
            executeIteration(pixels, image.width, image.height);
            iterations++;
        }

        IntBuffer outBuffer = out.getPixels().asIntBuffer();
        outBuffer.position(0);
        outBuffer.put(pixels);
        return out;
    }

    private void executeIteration(int[] rgb, int width, int height){
        int index = 0;

        while(index < pendingSize){
            int pixelIndex = pending[index++];
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            int r = 0, g = 0, b = 0;
            int count = 0;

            for(int[] offset : offsets){
                int nx = x + offset[0];
                int ny = y + offset[1];

                if(nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

                int currentPixelIndex = ny * width + nx;
                if(data[currentPixelIndex] == realData){
                    int rgba = rgb[currentPixelIndex];
                    r += Color.ri(rgba);
                    g += Color.gi(rgba);
                    b += Color.bi(rgba);
                    count++;
                }
            }

            if(count != 0){
                rgb[pixelIndex] = Color.packRgba(r / count, g / count, b / count, 0);

                index--;
                int value = pending[index];
                pendingSize--;
                pending[index] = pending[pendingSize];
                changing[changingSize] = value;
                changingSize++;
            }
        }

        for(int i = 0; i < changingSize; i++){
            data[changing[i]] = realData;
        }
        changingSize = 0;
    }

}
