package arc.packer;

import arc.graphics.*;
import arc.packer.ColorBleedEffect.Mask.*;

import java.nio.*;
import java.util.*;

/**
 * @author Ruben Garat
 * @author Ariel Coppes
 * @author Nathan Sweet
 * @author Anuke
 */
public class ColorBleedEffect{
    static int TO_PROCESS = 0;
    static int IN_PROCESS = 1;
    static int REALDATA = 2;
    static int[][] offsets = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

    public Pixmap processImage(Pixmap image, int maxIterations){
        int[] pixels = new int[image.width * image.height];
        IntBuffer buffer = image.getPixels().asIntBuffer();
        buffer.position(0);
        buffer.get(pixels);

        Pixmap out = new Pixmap(image.width, image.height);
        Mask mask = new Mask(pixels);

        int iterations = 0;
        int lastPending = -1;
        while(mask.pendingSize > 0 && mask.pendingSize != lastPending && iterations < maxIterations){
            lastPending = mask.pendingSize;
            executeIteration(pixels, mask, image.width, image.height);
            iterations++;
        }

        IntBuffer outBuffer = out.getPixels().asIntBuffer();
        outBuffer.position(0);
        outBuffer.put(pixels);
        return out;
    }

    private void executeIteration(int[] rgb, Mask mask, int width, int height){
        MaskIterator iterator = mask.new MaskIterator();
        while(iterator.hasNext()){
            int pixelIndex = iterator.next();
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            int r = 0, g = 0, b = 0;
            int count = 0;

            for(int i = 0, n = offsets.length; i < n; i++){
                int[] offset = offsets[i];
                int column = x + offset[0];
                int row = y + offset[1];

                if(column < 0 || column >= width || row < 0 || row >= height) continue;

                int currentPixelIndex = getPixelIndex(width, column, row);
                if(mask.getMask(currentPixelIndex) == REALDATA){
                    int rgba = rgb[currentPixelIndex];
                    r += Color.ri(rgba);
                    g += Color.gi(rgba);
                    b += Color.bi(rgba);
                    count++;
                }
            }

            if(count != 0){
                rgb[pixelIndex] = Color.packRgba(r / count, g / count, b / count, 0);
                iterator.markAsInProgress();
            }
        }

        iterator.reset();
    }

    private int getPixelIndex(int width, int x, int y){
        return y * width + x;
    }

    static class Mask{
        int[] data, pending, changing;
        int pendingSize, changingSize;

        Mask(int[] rgb){
            data = new int[rgb.length];
            pending = new int[rgb.length];
            changing = new int[rgb.length];
            for(int i = 0; i < rgb.length; i++){
                if(Color.ai(rgb[i]) == 0){
                    data[i] = TO_PROCESS;
                    pending[pendingSize] = i;
                    pendingSize++;
                }else
                    data[i] = REALDATA;
            }
        }

        int getMask(int index){
            return data[index];
        }

        int removeIndex(int index){
            if(index >= pendingSize) throw new IndexOutOfBoundsException(String.valueOf(index));
            int value = pending[index];
            pendingSize--;
            pending[index] = pending[pendingSize];
            return value;
        }

        class MaskIterator{
            private int index;

            boolean hasNext(){
                return index < pendingSize;
            }

            int next(){
                if(index >= pendingSize) throw new NoSuchElementException(String.valueOf(index));
                return pending[index++];
            }

            void markAsInProgress(){
                index--;
                changing[changingSize] = removeIndex(index);
                changingSize++;
            }

            void reset(){
                index = 0;
                for(int i = 0; i < changingSize; i++){
                    int index = changing[i];
                    data[index] = REALDATA;
                }
                changingSize = 0;
            }
        }
    }

}
