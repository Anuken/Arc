package arc.gif;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.async.*;

import java.io.*;
import java.text.*;
import java.util.*;

/** Records and saves GIFs. */
public class GifRecorder{
    private static final float defaultSize = 300;

    public KeyCode
        resizeKey = KeyCode.controlLeft,
        openKey = KeyCode.e,
        recordKey = KeyCode.t,
        shiftKey = KeyCode.shiftLeft;

    public Fi exportDirectory = Core.files == null ? Fi.get("gifs") : Core.files.local("gifs");
    public boolean disableGUI;
    public float speedMultiplier = 1f;
    public int recordfps = 30;
    public float driftSpeed = 1f;
    public float gifx = -defaultSize / 2, gify = -defaultSize / 2, gifwidth = defaultSize, gifheight = defaultSize;
    public boolean recording, open, saving;

    private float offsetx, offsety;
    private Seq<byte[]> frames = new Seq<>();
    private float frametime;
    private float saveprogress;

    protected void doInput(){
        if(Core.input.keyTap(openKey) && !saving){
            if(recording){
                recording = false;
                frames.clear();
                recording = false;
            }
            open = !open;
        }

        if(open){
            if(Core.input.keyTap(recordKey) && !saving){
                if(!recording){
                    frames.clear();
                    recording = false;
                    recording = true;
                }else{
                    recording = false;

                    saving = true;
                    saveprogress = 0f;

                    Threads.daemon(() -> {
                        if(frames.size == 0){
                            saving = false;
                            return;
                        }

                        try{
                            exportDirectory.mkdirs();

                            //linux-only
                            String args = Strings.format(
                            "/usr/bin/ffmpeg -r @ -s @x@ -f rawvideo -pix_fmt rgba -i - -frames:v @ -filter:v vflip,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse @/@.gif",
                            recordfps, (int)gifwidth, (int)gifheight, frames.size, exportDirectory.absolutePath(), new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date())
                            );

                            ProcessBuilder builder = new ProcessBuilder(args.split(" ")).redirectErrorStream(true);
                            Process process = builder.start();
                            OutputStream out = process.getOutputStream();

                            for(byte[] frame : frames){
                                out.write(frame);
                                out.flush();
                                saveprogress += (1f / frames.size);
                            }

                            out.close();
                            process.waitFor();
                            frames.clear();
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                        saving = false;
                    });
                }
            }
        }
    }

    /** Updates the recorder and draws the GUI */
    public void update(){
        if(Core.scene == null || !Core.scene.hasField()){
            doInput();
        }

        if(!open) return;

        Tmp.m1.set(Draw.proj());
        Draw.proj(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        float wx = Core.graphics.getWidth() / 2f, wy = Core.graphics.getHeight() / 2f;

        if(!disableGUI) Draw.color(Color.yellow);

        if(Core.input.keyDown(resizeKey) && !recording){
            if(!disableGUI) Draw.color(Color.green);

            float xs = Math.abs(wx + offsetx - Core.input.mouseX());
            float ys = Math.abs(wy + offsety - Core.input.mouseY());
            gifx = -xs;
            gify = -ys;
            gifwidth = xs * 2;
            gifheight = ys * 2;
        }

        if(Core.input.keyDown(shiftKey)){
            if(!disableGUI) Draw.color(Color.orange);

            offsetx = Mathf.lerpDelta(offsetx, -(wx - Core.input.mouseX()), driftSpeed);
            offsety = Mathf.lerpDelta(offsety, -(wy - Core.input.mouseY()), driftSpeed);
        }

        if(recording){
            frametime += Core.graphics.getDeltaTime() * 60.5f * speedMultiplier;
            if(frametime >= (60f / recordfps)){
                byte[] pix = ScreenUtils.getFrameBufferPixels(
                (int)(gifx + offsetx + wx),
                (int)(gify + offsety + wy),
                (int)(gifwidth), (int)(gifheight), false);
                frames.add(pix);
                frametime = 0;
            }
        }

        if(!disableGUI){
            if(recording) Draw.color(Color.red);

            Lines.stroke(1f);
            Lines.rect(gifx + wx + offsetx, gify + wy + offsety, gifwidth, gifheight);

            if(saving){
                if(!disableGUI) Draw.color(Color.black);

                float w = 200, h = 50;
                Fill.crect(wx - w / 2, wy - h / 2, w, h);
                Draw.color(Color.red, Color.green, saveprogress);
                Fill.crect(wx - w / 2, wy - h / 2, w * saveprogress, h);
            }
        }

        Draw.color();
        Draw.flush();
        Draw.proj(Tmp.m1);
    }
}
