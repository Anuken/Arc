package io.anuke.arc.recorder;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.PixmapIO;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.graphics.g2d.Fill;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.math.Matrix3;
import io.anuke.arc.math.geom.Rectangle;
import io.anuke.arc.util.BufferUtils;
import io.anuke.arc.util.ScreenUtils;
import io.anuke.arc.util.Time;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;

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
	
	private boolean skipAlpha = false;
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
			float ys = Math.abs(Core.graphics.getHeight() / 2 + offsety - (Core.graphics.getHeight() - Core.input.mouseY()));
			gifx = -xs;
			gify = -ys;
			gifwidth = xs * 2;
			gifheight = ys * 2;
		}
		
		if(controller.shiftKeyPressed()){
			if(!disableGUI)
				Draw.color(Color.ORANGE);
			
			float xs = (Core.graphics.getWidth() / 2 - Core.input.mouseX());
			float ys = (Core.graphics.getHeight() / 2 - (Core.graphics.getHeight() - Core.input.mouseY()));
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
						(int) (gifwidth) - 2, (int) (gifheight) - 2, true);
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
	
	/**Takes a full-screen screenshot and saves it to a file.*/
	public FileHandle takeScreenshot(){
		return takeScreenshot(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
	}
	
	/**Takes a full-screen screenshot of the specified region saves it to a file.*/
	public FileHandle takeScreenshot(int x, int y, int width, int height){
		byte[] pix = ScreenUtils.getFrameBufferPixels(x, y, width, height, true);

		Pixmap pixmap = createPixmap(pix, width, height);

		FileHandle file = exportdirectory.child("screenshot-" + Time.millis() + ".png");
		PixmapIO.writePNG(file, pixmap);
		pixmap.dispose();
		return file;
	}
	
	public void writeGIF(){
		writeGIF(workdirectory, exportdirectory);
	}

	private void writeGIF(final FileHandle directory, final FileHandle writedirectory){
		if(saving)
			return;
		saving = true;
		final Array<String> strings = new Array<>();
		final Array<Pixmap> pixmaps = new Array<>();

		for(byte[] bytes : frames){
			Pixmap pixmap = createPixmap(bytes);
			pixmaps.add(pixmap);
		}

		new Thread(() -> {

            saveprogress = 0;
            int i = 0;
            for(Pixmap pixmap : pixmaps){
                PixmapIO.writePNG(Core.files.absolute(directory.file().getAbsolutePath() + "/frame" + i + ".png"), pixmap);
                strings.add("frame" + i + ".png");
                saveprogress += (0.5f / pixmaps.size);
                i++;
            }

            lastRecording = compileGIF(strings, directory, writedirectory);
            directory.deleteDirectory();
            for(Pixmap pixmap : pixmaps){
                pixmap.dispose();
            }
            saving = false;
        }).start();
	}

	private File compileGIF(Array<String> strings, FileHandle inputdirectory, FileHandle directory){
		if(strings.size == 0){
			throw new RuntimeException("No strings!");
		}

		try{
			String time = "" + (int) (System.currentTimeMillis() / 1000);
			String dirstring = inputdirectory.file().getAbsolutePath();
			new File(directory.file().getAbsolutePath()).mkdir();
			BufferedImage firstImage = ImageIO.read(new File(dirstring + "/" + strings.get(0)));
			File file = new File(directory.file().getAbsolutePath() + "/recording" + time + ".gif");
			ImageOutputStream output = new FileImageOutputStream(file);
			io.anuke.gif.GifSequenceWriter writer = new io.anuke.gif.GifSequenceWriter(output, firstImage.getType(), (int) (1f / recordfps * 1000f), true);

			writer.writeToSequence(firstImage);

			for(int i = 1; i < strings.size; i++){
				BufferedImage after = ImageIO.read(new File(dirstring + "/" + strings.get(i)));
				saveprogress += (0.5f / frames.size);
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

	private Pixmap createPixmap(byte[] pixels, int width, int height){
		Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);

		Color color = new Color();

		if(!skipAlpha)
			for(int x = 0; x < pixmap.getWidth(); x++){
				for(int y = 0; y < pixmap.getHeight(); y++){
					color.set(pixmap.getPixel(x, y));
					if(color.a <= 0.999f){
						color.a = 1f;
						pixmap.setColor(color);
						pixmap.drawPixel(x, y);
					}
				}
			}

		return pixmap;
	}

	private Pixmap createPixmap(byte[] pixels){
		return createPixmap(pixels, (int) (gifwidth) - 2, (int) (gifheight) - 2);
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
