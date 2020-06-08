package arc.gif;

import arc.*;
import arc.files.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import javax.imageio.stream.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;

/** Records and saves GIFs. */
public class GifRecorder{
	private static final float defaultSize = 300;
	private static BufferedImage outImage;

	public KeyCode
			resizeKey = KeyCode.controlLeft,
			openKey = KeyCode.e,
			recordKey = KeyCode.t,
			shiftKey = KeyCode.shiftLeft;

    public Fi exportDirectory;
    public boolean disableGUI, autoCopy = true;
    public float speedMultiplier = 1f;
	public int recordfps = 30;
	public float gifx, gify, gifwidth, gifheight, giftime;

	private float offsetx, offsety;
	private Seq<byte[]> frames = new Seq<>();
	private float frametime;
	private boolean recording, open;
	private boolean saving;
	private float saveprogress;

	public GifRecorder(){
		this(Core.files.local("gifs"));
	}

	public GifRecorder(Fi exportDirectory){
		gifx = -defaultSize / 2;
		gify = -defaultSize / 2;
		gifwidth = defaultSize;
		gifheight = defaultSize;
		this.exportDirectory = exportDirectory;
	}

	protected void doInput(){
		if(Core.input.keyTap(openKey) && !saving){
			if(recording){
				finishRecording();
				clearFrames();
			}
			open = !open;
		}

		if(open){
			if(Core.input.keyTap(recordKey) && !saving){
				if(!recording){
					startRecording();
				}else{
					finishRecording();
					writeGIF(exportDirectory);
				}
			}
		}
	}

	/** Updates the recorder and draws the GUI */
	public void update(){
		Draw.flush();
		
		doInput();
		float delta = Core.graphics.getDeltaTime();
		
		if(!open) return;
		
		Tmp.m1.set(Draw.proj());
		Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
		
		float wx = Core.graphics.getWidth() / 2;
		float wy = Core.graphics.getHeight() / 2;
		
		if(!disableGUI) Draw.color(Color.yellow);

		if(Core.input.keyDown(resizeKey)){
			
			if(!disableGUI)
				Draw.color(Color.green);
			
			float xs = Math.abs(Core.graphics.getWidth() / 2 + offsetx - Core.input.mouseX());
			float ys = Math.abs(Core.graphics.getHeight() / 2 + offsety - Core.input.mouseY());
			gifx = -xs;
			gify = -ys;
			gifwidth = xs * 2;
			gifheight = ys * 2;
		}
		
		if(Core.input.keyDown(shiftKey)){
			if(!disableGUI) Draw.color(Color.orange);
			
			float xs = (Core.graphics.getWidth() / 2 - Core.input.mouseX());
			float ys = (Core.graphics.getHeight() / 2 - Core.input.mouseY());
			offsetx = -xs;
			offsety = -ys;
		}

		if(!disableGUI){
			
			if(recording) Draw.color(Color.red);

			Fill.crect(gifx + wx + offsetx, gify + wy + offsety, gifwidth, 1f);
			Fill.crect(gifx + wx + offsetx, gify + wy + gifheight + offsety, gifwidth, 1f);
			Fill.crect(gifx + wx + offsetx, gify + wy + offsety, 1f, gifheight);
			Fill.crect(gifx + wx + offsetx + gifwidth, gify + wy + offsety, 1f, gifheight + 1f);

			if(saving){
				if(!disableGUI) Draw.color(Color.black);

				float w = 200, h = 50;
				Fill.crect(Core.graphics.getWidth() / 2 - w / 2, Core.graphics.getHeight() / 2 - h / 2, w, h);

				//this just blends red and green
				Color a = Color.red;
				Color b = Color.green;

				float s = saveprogress;
				float i = 1f - saveprogress;

				Draw.color(a.r * i + b.r * s, a.g * i + b.g * s, a.b * i + b.b * s, 1f);

				Fill.crect(Core.graphics.getWidth() / 2 - w / 2, Core.graphics.getHeight() / 2 - h / 2, w * saveprogress, h);
			}

			Draw.color(Color.white);
		}

		if(recording){
			giftime += delta;
			frametime += delta*61f*speedMultiplier;
			if(frametime >= (60f / recordfps)){
				byte[] pix = ScreenUtils.getFrameBufferPixels((int)(gifx + offsetx) + 1 + Core.graphics.getWidth() / 2,
						(int)(gify + offsety) + 1 + Core.graphics.getHeight() / 2,
						(int)(gifwidth) - 2, (int)(gifheight) - 2, false);
				frames.add(pix);
				frametime = 0;
			}
		}

		Draw.color();
		Draw.flush();
		Draw.proj(Tmp.m1);
	}

	public boolean isSaving(){
		return saving;
	}

	public boolean isOpen(){
		return open;
	}

	public void open(){
		open = true;
	}

	public void close(){
		open = false;
	}

	public boolean isRecording(){
		return recording;
	}

	public void startRecording(){
		clearFrames();
		recording = true;
	}

	public float getTime(){
		return giftime;
	}

	public void finishRecording(){
		recording = false;
		giftime = 0;
	}

	public void clearFrames(){
		frames.clear();
		giftime = 0;
		recording = false;
	}

	/** Sets the bounds for recording, relative to the center of the screen */
	public void setBounds(float x, float y, float width, float height){
		this.gifx = x;
		this.gify = y;
		this.gifwidth = width;
		this.gifheight = height;
	}

	public void setBounds(Rect rect){
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	private void writeGIF(final Fi writedirectory){
		if(saving) return;
		saving = true;

		int width = (int)(gifwidth) - 2, height = (int)(gifheight) - 2;
		saveprogress = 0f;

		new Thread(() -> {
			File f = compileGIF(frames, width, height, writedirectory);

			//copy image to clipboard
			if(autoCopy){
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable(){
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{DataFlavor.javaFileListFlavor};
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return DataFlavor.javaFileListFlavor.equals(flavor);
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor){
                        return Collections.singletonList(f);
                    }
                }, (clipboard, contents) -> {});
            }

			saving = false;
        }).start();
	}

	private File compileGIF(Seq<byte[]> pixmaps, int width, int height, Fi directory){
		if(pixmaps.size == 0){
			throw new RuntimeException("No input files!");
		}

		try{
			String time = "" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault()).format(new Date());
			new File(directory.absolutePath()).mkdir();
			BufferedImage firstImage = toImage(pixmaps.first(), width, height);
			File file = new File(directory.absolutePath() + "/" + time + ".gif");
			ImageOutputStream output = new FileImageOutputStream(file);
			GifSequenceWriter writer = new GifSequenceWriter(output, firstImage.getType(), (int)(1f / recordfps * 1000f), true);

			writer.writeToSequence(firstImage);

			for(int i = 1; i < pixmaps.size; i++){
				BufferedImage after = toImage(pixmaps.get(i), width, height);
				saveprogress += (1f / frames.size);
				writer.writeToSequence(after);
			}
			writer.close();
			output.close();
			return file;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private BufferedImage toImage(byte[] frames, int width, int height){
		if(outImage == null || outImage.getWidth() != width || outImage.getHeight() != height){
			outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}
		BufferedImage image = outImage;

		for(int i = 0; i < width*height*4; i += 4){
			int r = frames[i];
			int g = frames[i + 1];
			int b = frames[i + 2];
			if(r < 0) r += 256;
			if(g < 0) g += 256;
			if(b < 0) b += 256;

			int result = Color.argb8888(1f, r / 255f, g / 255f, b / 255f);
			int index = i / 4;
			image.setRGB(index % width, height - 1 - index / width, result);
		}
		return image;
	}
}
