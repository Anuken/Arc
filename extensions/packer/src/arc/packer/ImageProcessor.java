package arc.packer;

import arc.files.*;
import arc.graphics.*;
import arc.packer.TexturePacker.*;
import arc.struct.*;
import arc.util.*;

import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;

public class ImageProcessor{
    private static final Pixmap emptyImage = new Pixmap(1, 1);

    private final Settings settings;
    private final HashMap<String, Rect> crcs = new HashMap<>();
    private final Seq<Rect> rects = new Seq<>();
    private float scale = 1;
    private boolean resampling;

    public ImageProcessor(Settings settings){
        this.settings = settings;
    }

    /**
     * @param rootPath Used to strip the root directory prefix from image file names, can be null.
     */
    public void addImage(File file, String rootPath){
        Pixmap image = new Pixmap(new Fi(file));

        String name = file.getAbsolutePath().replace('\\', '/');

        // Strip root dir off front of image path.
        if(rootPath != null){
            if(!name.startsWith(rootPath)) throw new RuntimeException("Path '" + name + "' does not start with root: " + rootPath);
            name = name.substring(rootPath.length());
        }

        // Strip extension.
        int dotIndex = name.lastIndexOf('.');
        if(dotIndex != -1) name = name.substring(0, dotIndex);

        addImage(image, name);
    }

    /**
     * The image will be kept in-memory during packing.
     * @see #addImage(File, String)
     */
    public Rect addImage(Pixmap image, String name){
        Rect rect = processImage(image, name);

        if(rect == null){
            if(!settings.silent) System.out.println("Ignoring blank input image: " + name);
            return null;
        }

        if(settings.alias){
            String crc = hash(rect.getImage(this));
            Rect existing = crcs.get(crc);
            if(existing != null){
                if(!settings.silent && settings.printAliases){
                    System.out.println(rect.name + " (alias of " + existing.name + ")");
                }
                existing.aliases.add(new Alias(rect));
                return null;
            }
            crcs.put(crc, rect);
        }

        rects.add(rect);
        return rect;
    }

    public void setScale(float scale){
        this.scale = scale;
    }

    public void setResampling(boolean resampling){
        this.resampling = resampling;
    }

    public Seq<Rect> getImages(){
        return rects;
    }

    public void clear(){
        rects.clear();
        crcs.clear();
    }

    /** Returns a rect for the image describing the texture region to be packed, or null if the image should not be packed. */
    Rect processImage(Pixmap image, String name){
        if(scale <= 0) throw new IllegalArgumentException("scale cannot be <= 0: " + scale);

        int width = image.width, height = image.height;

        boolean isPatch = name.endsWith(".9");
        int[] splits = null, pads = null;
        Rect rect;
        if(isPatch){
            // Strip ".9" from file name, read ninepatch split pixels, and strip ninepatch split pixels.
            name = name.substring(0, name.length() - 2);
            splits = getSplits(image, name);
            pads = getPads(image, name, splits);
            // Strip split pixels.
            width -= 2;
            height -= 2;
            Pixmap newImage = new Pixmap(width, height);
            newImage.draw(image, 1, 1, width + 1, height + 1, 0, 0, width, height);
            image = newImage;
        }

        // Scale image.
        if(scale != 1){
            width = Math.max(1, Math.round(width * scale));
            height = Math.max(1, Math.round(height * scale));
            Pixmap newImage = new Pixmap(width, height);
            newImage.draw(image, 0, 0, width, height, resampling);
            image = newImage;
        }

        if(isPatch){
            // Ninepatches aren't rotated or whitespace stripped.
            rect = new Rect(image, 0, 0, width, height, true);
            rect.splits = splits;
            rect.pads = pads;
            rect.canRotate = false;
        }else{
            rect = stripWhitespace(image, name);
            if(rect == null) return null;
        }

        rect.name = name;
        return rect;
    }

    /** Strips whitespace and returns the rect, or null if the image should be ignored. */
    private Rect stripWhitespace(Pixmap source, String name){
        int thresh = settings.alphaThreshold;

        if(Structs.contains(settings.ignoredWhitespaceStrings, name::contains)){
            return new Rect(source, 0, 0, source.width, source.height, false);
        }

        if(settings.stripWhitespaceCenter && source.width > 3 && source.height > 3){
            int crop = 0;
            int maxCrop = Math.min(source.width, source.height) / 2 - 1;
            outer:
            while(crop < maxCrop){
                //bottom and top
                for(int x = crop; x < source.width - crop; x++){
                    if(source.getA(x, crop) > thresh) break outer;
                    if(source.getA(x, source.height - 1 - crop) > thresh) break outer;
                }

                //sides
                for(int y = crop; y < source.height - crop; y++){
                    if(source.getA(crop, y) > thresh) break outer;
                    if(source.getA(source.width - 1 - crop, y) > thresh) break outer;
                }

                crop ++;
            }

            //add a pixel of padding
            int realCrop = Math.max(crop - 1, 0);

            if(realCrop > 0){
                return new Rect(source, realCrop, realCrop, source.width - realCrop * 2, source.height - realCrop * 2, false);
            }
        }

        if((!settings.stripWhitespaceX && !settings.stripWhitespaceY))
            return new Rect(source, 0, 0, source.width, source.height, false);

        int top = 0;
        int bottom = source.height;
        if(settings.stripWhitespaceY){
            outer:
            for(int y = 0; y < source.height; y++){
                for(int x = 0; x < source.width; x++){
                    int alpha = source.getA(x, y);
                    if(alpha > thresh) break outer;
                }
                top++;
            }
            outer:
            for(int y = source.height; --y >= top; ){
                for(int x = 0; x < source.width; x++){
                    int alpha = source.getA(x, y);
                    if(alpha > thresh) break outer;
                }
                bottom--;
            }
            // Leave 1px so nothing is copied into padding.
            if(settings.duplicatePadding){
                if(top > 0) top--;
                if(bottom < source.height) bottom++;
            }
        }
        int left = 0;
        int right = source.width;
        if(settings.stripWhitespaceX){
            outer:
            for(int x = 0; x < source.width; x++){
                for(int y = top; y < bottom; y++){
                    int alpha = source.getA(x, y);
                    if(alpha > thresh) break outer;
                }
                left++;
            }
            outer:
            for(int x = source.width; --x >= left; ){
                for(int y = top; y < bottom; y++){
                    int alpha = source.getA(x, y);
                    if(alpha > thresh) break outer;
                }
                right--;
            }
            // Leave 1px so nothing is copied into padding.
            if(settings.duplicatePadding){
                if(left > 0) left--;
                if(right < source.width) right++;
            }
        }
        int newWidth = right - left;
        int newHeight = bottom - top;
        if(newWidth <= 0 || newHeight <= 0){
            if(settings.ignoreBlankImages)
                return null;
            else
                return new Rect(emptyImage, 0, 0, 1, 1, false);
        }
        return new Rect(source, left, top, newWidth, newHeight, false);
    }

    /**
     * Returns the splits, or null if the image had no splits or the splits were only a single region. Splits are an int[4] that
     * has left, right, top, bottom.
     */
    private int[] getSplits(Pixmap image, String name){
        int startX = getSplitPoint(image, name, 1, 0, true, true);
        int endX = getSplitPoint(image, name, startX, 0, false, true);
        int startY = getSplitPoint(image, name, 0, 1, true, false);
        int endY = getSplitPoint(image, name, 0, startY, false, false);

        // Ensure pixels after the end are not invalid.
        getSplitPoint(image, name, endX + 1, 0, true, true);
        getSplitPoint(image, name, 0, endY + 1, true, false);

        // No splits, or all splits.
        if(startX == 0 && endX == 0 && startY == 0 && endY == 0) return null;

        // Subtraction here is because the coordinates were computed before the 1px border was stripped.
        if(startX != 0){
            startX--;
            endX = image.getWidth() - 2 - (endX - 1);
        }else{
            // If no start point was ever found, we assume full stretch.
            endX = image.getWidth() - 2;
        }
        if(startY != 0){
            startY--;
            endY = image.getHeight() - 2 - (endY - 1);
        }else{
            // If no start point was ever found, we assume full stretch.
            endY = image.getHeight() - 2;
        }

        if(scale != 1){
            startX = Math.round(startX * scale);
            endX = Math.round(endX * scale);
            startY = Math.round(startY * scale);
            endY = Math.round(endY * scale);
        }

        return new int[]{startX, endX, startY, endY};
    }

    /**
     * Returns the pads, or null if the image had no pads or the pads match the splits. Pads are an int[4] that has left, right,
     * top, bottom.
     */
    private int[] getPads(Pixmap image, String name, int[] splits){
        int bottom = image.height - 1;
        int right = image.width - 1;

        int startX = getSplitPoint(image, name, 1, bottom, true, true);
        int startY = getSplitPoint(image, name, right, 1, true, false);

        // No need to hunt for the end if a start was never found.
        int endX = 0;
        int endY = 0;
        if(startX != 0) endX = getSplitPoint(image, name, startX + 1, bottom, false, true);
        if(startY != 0) endY = getSplitPoint(image, name, right, startY + 1, false, false);

        // Ensure pixels after the end are not invalid.
        getSplitPoint(image, name, endX + 1, bottom, true, true);
        getSplitPoint(image, name, right, endY + 1, true, false);

        // No pads.
        if(startX == 0 && endX == 0 && startY == 0 && endY == 0){
            return null;
        }

        // -2 here is because the coordinates were computed before the 1px border was stripped.
        if(startX == 0 && endX == 0){
            startX = -1;
            endX = -1;
        }else{
            if(startX > 0){
                startX--;
                endX = image.getWidth() - 2 - (endX - 1);
            }else{
                // If no start point was ever found, we assume full stretch.
                endX = image.getWidth() - 2;
            }
        }
        if(startY == 0 && endY == 0){
            startY = -1;
            endY = -1;
        }else{
            if(startY > 0){
                startY--;
                endY = image.getHeight() - 2 - (endY - 1);
            }else{
                // If no start point was ever found, we assume full stretch.
                endY = image.getHeight() - 2;
            }
        }

        if(scale != 1){
            startX = Math.round(startX * scale);
            endX = Math.round(endX * scale);
            startY = Math.round(startY * scale);
            endY = Math.round(endY * scale);
        }

        int[] pads = new int[]{startX, endX, startY, endY};

        if(splits != null && Arrays.equals(pads, splits)){
            return null;
        }

        return pads;
    }

    /**
     * Hunts for the start or end of a sequence of split pixels. Begins searching at (startX, startY) then follows along the x or
     * y axis (depending on value of xAxis) for the first non-transparent pixel if startPoint is true, or the first transparent
     * pixel if startPoint is false. Returns 0 if none found, as 0 is considered an invalid split point being in the outer border
     * which will be stripped.
     */
    private static int getSplitPoint(Pixmap image, String name, int startX, int startY, boolean startPoint, boolean xAxis){
        int next = xAxis ? startX : startY;
        int end = xAxis ? image.width : image.height;
        int breakA = startPoint ? 255 : 0;

        int x = startX;
        int y = startY;
        while(next != end){
            if(xAxis)
                x = next;
            else
                y = next;

            int rgba = image.get(x, y), r = Color.ri(rgba), g = Color.gi(rgba), b = Color.bi(rgba), a = Color.ai(rgba);
            if(a == breakA) return next;

            if(!startPoint && (r != 0 || g != 0 || b != 0 || a != 255)){
                throw new RuntimeException("Invalid " + name + " ninepatch split pixel at " + x + ", " + y + ", rgba: " + r + ", " + g + ", " + b +  ", " + a);
            }

            next++;
        }

        return 0;
    }

    private static String hash(Pixmap image){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA1");

            int width = image.width;
            int height = image.height;

            byte[] bytes = new byte[image.pixels.capacity()];
            image.pixels.position(0);
            image.pixels.get(bytes);
            digest.update(bytes);
            image.pixels.position(0);

            hash(digest, width);
            hash(digest, height);

            return new BigInteger(1, digest.digest()).toString(16);
        }catch(NoSuchAlgorithmException ex){
            throw new RuntimeException(ex);
        }
    }

    private static void hash(MessageDigest digest, int value){
        digest.update((byte)(value >> 24));
        digest.update((byte)(value >> 16));
        digest.update((byte)(value >> 8));
        digest.update((byte)value);
    }
}
