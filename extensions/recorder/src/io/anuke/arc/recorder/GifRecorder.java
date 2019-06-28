package io.anuke.arc.recorder;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.PixmapIO.PNG;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.graphics.g2d.Fill;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.math.Matrix3;
import io.anuke.arc.math.geom.Rectangle;
import io.anuke.arc.util.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;

/** Records and saves GIFs. */
public class GifRecorder{
	private static final float defaultSize = 300;

	private KeyCode resizeKey = KeyCode.CONTROL_LEFT,
			openKey = KeyCode.E,
			recordKey = KeyCode.T,
			shiftKey = KeyCode.SHIFT_LEFT,
			fullscreenKey = KeyCode.F;

	private RecorderController controller = new DefaultController();
	
	private Matrix3 matrix = new Matrix3();
	
	private boolean skipAlpha = true;
	private int recordfps = 30;
	private float gifx, gify, gifwidth, gifheight, giftime;
	private float offsetx, offsety;
	private FileHandle exportdirectory, workdirectory;
	private boolean disableGUI;
	private float speedMultiplier = 1f;
	
	private Array<byte[]> frames = new Array<>();
	private File lastRecording;
	private float frametime;
	private boolean recording, open;
	private boolean saving;
	private float saveprogress;
	private PNG png = new PNG();

	public GifRecorder() {
		this(Core.files.local("gifexport"), Core.files.local(".gifimages"));
	}

	public GifRecorder(FileHandle exportdirectory, FileHandle workdirectory) {
		gifx = -defaultSize / 2;
		gify = -defaultSize / 2;
		gifwidth = defaultSize;
		gifheight = defaultSize;
		this.workdirectory = workdirectory;
		this.exportdirectory = exportdirectory;
		png.setFlipY(true);
	}

	protected void doInput(){
		if(controller.openKeyPressed() && !saving){
			if(recording){
				finishRecording();
				clearFrames();
			}
			open = !open;
		}

		if(open){
			if(controller.recordKeyPressed() && !saving){
				if(!recording){
					startRecording();
				}else{
					finishRecording();
					writeGIF(workdirectory, exportdirectory);
				}
			} else if (controller.fullscreenPressed()) {
				offsetx = 0;
				offsety = 0;
				gifx = Core.graphics.getWidth() * -0.5f;
				gify = Core.graphics.getHeight() * -0.5f;
				gifwidth = Core.graphics.getWidth();
				gifheight = Core.graphics.getHeight();
			}
		}
	}

	/** Updates the recorder and draws the GUI */
	public void update(){
		Draw.flush();
		
		doInput();
		float delta = Core.graphics.getDeltaTime();
		
		if(!open)
			return;
		
		matrix.set(Draw.proj());
		Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
		
		float wx = Core.graphics.getWidth() / 2;
		float wy = Core.graphics.getHeight() / 2;
		
		if(!disableGUI)
			Draw.color(Color.YELLOW);

		if(controller.resizeKeyPressed()){
			
			if(!disableGUI)
				Draw.color(Color.GREEN);
			
			float xs = Math.abs(Core.graphics.getWidth() / 2 + offsetx - Core.input.mouseX());
			float ys = Math.abs(Core.graphics.getHeight() / 2 + offsety - Core.input.mouseY());
			gifx = -xs;
			gify = -ys;
			gifwidth = xs * 2;
			gifheight = ys * 2;
		}
		
		if(controller.shiftKeyPressed()){
			if(!disableGUI)
				Draw.color(Color.ORANGE);
			
			float xs = (Core.graphics.getWidth() / 2 - Core.input.mouseX());
			float ys = (Core.graphics.getHeight() / 2 - Core.input.mouseY());
			offsetx = -xs;
			offsety = -ys;
		}

		if(!disableGUI){
			
			if(recording)
				Draw.color(Color.RED);

			Fill.crect(gifx + wx + offsetx, gify + wy + offsety, gifwidth, 1f);
			Fill.crect(gifx + wx + offsetx, gify + wy + gifheight + offsety, gifwidth, 1f);
			Fill.crect(gifx + wx + offsetx, gify + wy + offsety, 1f, gifheight);
			Fill.crect(gifx + wx + offsetx + gifwidth, gify + wy + offsety, 1f, gifheight + 1f);

			if(saving){
				if(!disableGUI)
					Draw.color(Color.BLACK);

				float w = 200, h = 50;
				Fill.crect(Core.graphics.getWidth() / 2 - w / 2, Core.graphics.getHeight() / 2 - h / 2, w, h);

				//this just blends red and green
				Color a = Color.RED;
				Color b = Color.GREEN;

				float s = saveprogress;
				float i = 1f - saveprogress;

				Draw.color(a.r * i + b.r * s, a.g * i + b.g * s, a.b * i + b.b * s, 1f);

				Fill.crect(Core.graphics.getWidth() / 2 - w / 2, Core.graphics.getHeight() / 2 - h / 2, w * saveprogress, h);
			}

			Draw.color(Color.WHITE);
		}

		if(recording){
			giftime += delta;
			frametime += delta*61f*speedMultiplier;
			if(frametime >= (60 / recordfps)){
				byte[] pix = ScreenUtils.getFrameBufferPixels((int) (gifx + offsetx) + 1 + Core.graphics.getWidth() / 2, 
						(int) (gify + offsety) + 1 + Core.graphics.getHeight() / 2, 
						(int) (gifwidth) - 2, (int) (gifheight) - 2, false);
				frames.add(pix);
				frametime = 0;
			}
		}

		Draw.color();
		Draw.flush();
		Draw.proj(matrix);
	}
	
	/**Sets the speed multiplier. Higher numbers make the gif go slower, lower numbers make it go faster*/
	public void setSpeedMultiplier(float m){
		this.speedMultiplier = m;
	}
	
	/**Set to true to disable drawing the UI.*/
	public void setGUIDisabled(boolean disabled){
		this.disableGUI = true;
	}
	
	/**Sets the controller (or class that controls input)*/
	public void setController(RecorderController controller){
		this.controller = controller;
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

	public void setExportDirectory(FileHandle handle){
		exportdirectory = handle;
	}

	public void setWorkingDirectory(FileHandle handle){
		workdirectory = handle;
	}

	public void setResizeKey(KeyCode key){
		this.resizeKey = key;
	}

	public void setOpenKey(KeyCode key){
		this.openKey = key;
	}

	public void setRecordKey(KeyCode key){
		this.recordKey = key;
	}

	public void setFPS(int fps){
		recordfps = fps;
	}

	public File getLastRecording(){
		return lastRecording;
	}

	public void setSkipAlpha(boolean skipAlpha){
		this.skipAlpha = skipAlpha;
	}

	/** Sets the bounds for recording, relative to the center of the screen */
	public void setBounds(float x, float y, float width, float height){
		this.gifx = x;
		this.gify = y;
		this.gifwidth = width;
		this.gifheight = height;
	}

	public void setBounds(Rectangle rect){
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}
	
	public void writeGIF(){
		writeGIF(workdirectory, exportdirectory);
	}

	private void writeGIF(final FileHandle directory, final FileHandle writedirectory){
		if(saving) return;
		saving = true;

		int width = (int) (gifwidth) - 2, height = (int) (gifheight) - 2;
		saveprogress = 0f;

		new Thread(() -> {
			lastRecording = compileGIF(frames, width, height, writedirectory);
			directory.deleteDirectory();
			saving = false;
        }).start();
	}

	private File compileGIF(Array<byte[]> pixmaps, int width, int height, FileHandle directory){
		if(pixmaps.size == 0){
			throw new RuntimeException("No input files!");
		}

		try{
			String time = "" + (int) (System.currentTimeMillis() / 1000);
			new File(directory.file().getAbsolutePath()).mkdir();
			BufferedImage firstImage = toImage(pixmaps.first(), width, height);
			File file = new File(directory.file().getAbsolutePath() + "/recording" + time + ".gif");
			ImageOutputStream output = new FileImageOutputStream(file);
			GifSequenceWriter writer = new GifSequenceWriter(output, firstImage.getType(), (int) (1f / recordfps * 1000f), true);

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
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

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

	private BufferedImage toImage(Pixmap pixmap){
		Color color = new Color();
		BufferedImage image = new BufferedImage(pixmap.getWidth(), pixmap.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int x = 0; x < image.getWidth(); x++){
			for(int y = 0; y < image.getHeight(); y++){
				color.set(pixmap.getPixel(x, y));
				color.a = 1f;
				image.setRGB(x, y, Color.argb8888(color));
			}
		}
		return image;
	}

	/** Default controller implementation, uses the provided keys */
	class DefaultController implements RecorderController{
		
		public boolean openKeyPressed(){
			return Core.input.keyTap(openKey);
		}

		public boolean recordKeyPressed(){
			return Core.input.keyTap(recordKey);
		}

		public boolean resizeKeyPressed(){
			return Core.input.keyDown(KeyCode.MOUSE_LEFT) && Core.input.keyDown(resizeKey);
		}
		
		public boolean shiftKeyPressed(){
			return Core.input.keyDown(KeyCode.MOUSE_LEFT) && Core.input.keyDown(shiftKey);
		}

		@Override
		public boolean fullscreenPressed() {
			return Core.input.keyTap(fullscreenKey);
		}
	}

	/**
	 * Provide an implementation and call recorder.setController() for custom
	 * input
	 */
	public interface RecorderController{
		boolean openKeyPressed();

		boolean recordKeyPressed();

		boolean resizeKeyPressed();
		
		boolean shiftKeyPressed();

		boolean fullscreenPressed();
	}
}
