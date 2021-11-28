package arc.gif;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
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

    public boolean outputMp4 = true;
    public Fi exportDirectory = Core.files == null ? Fi.get("gifs") : Core.files.local("gifs");
    public float speedMultiplier = 1f;
    public int recordfps = 30;
    public float driftSpeed = 1f;
    public Rect bounds = new Rect(-defaultSize / 2, -defaultSize / 2, defaultSize, defaultSize);
    public boolean recording, open, saving;

    private float offsetx, offsety;
    private Seq<byte[]> frames = new Seq<>();
    private float frametime, saveprogress;

    /** Updates the recorder and draws the GUI */
    public void update(){
        float wx = Core.graphics.getWidth() / 2f, wy = Core.graphics.getHeight() / 2f;

        //save each frame when recording
        if(recording){
            frametime += Core.graphics.getDeltaTime() * 60.5f * speedMultiplier;
            if(frametime >= (60f / recordfps)){
                frames.add(ScreenUtils.getFrameBufferPixels(
                    (int)(bounds.x + offsetx + wx),
                    (int)(bounds.y + offsety + wy),
                    (int)bounds.width, (int)bounds.height, false
                ));
                frametime = 0;
            }
        }

        //update input
        if(Core.scene == null || !Core.scene.hasField()){
            if(Core.input.keyTap(openKey) && !saving){
                if(recording){
                    recording = false;
                    frames.clear();
                }
                open = !open;
            }

            if(open){
                if(Core.input.keyDown(resizeKey) && !recording){
                    float xs = Math.abs(wx + offsetx - Core.input.mouseX());
                    float ys = Math.abs(wy + offsety - Core.input.mouseY());
                    bounds.set(-xs, -ys, xs * 2, ys * 2);
                }

                if(Core.input.keyDown(shiftKey)){
                    offsetx = Mathf.lerpDelta(offsetx, Core.input.mouseX() - wx, driftSpeed);
                    offsety = Mathf.lerpDelta(offsety, Core.input.mouseY() - wy, driftSpeed);
                }

                if(Core.input.keyTap(recordKey) && !saving){
                    if(!recording){
                        frames.clear();
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
                                "/usr/bin/ffmpeg -r @ -s @x@ -f rawvideo -pix_fmt rgba -i - -frames:v @ -filter:v vflip,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse @/@.@",
                                recordfps, (int)bounds.width, (int)bounds.height, frames.size, exportDirectory.absolutePath(), new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date()),
                                outputMp4 ? "mp4" : "gif"
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

        //draw UI
        if(open){
            Tmp.m1.set(Draw.proj());
            Draw.proj(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

            Draw.color(
            Core.input.keyDown(resizeKey) && !recording ? Color.green :
            Core.input.keyDown(shiftKey) ? Color.orange :
            recording ? Color.red :
            Color.yellow
            );

            Lines.stroke(1f);
            Lines.rect(bounds.x + wx + offsetx, bounds.y + wy + offsety, bounds.width, bounds.height);

            if(saving){
                Draw.color(Color.black);
                float w = 200, h = 50;
                Fill.crect(wx - w / 2, wy - h / 2, w, h);
                Draw.color(Color.red, Color.green, saveprogress);
                Fill.crect(wx - w / 2, wy - h / 2, w * saveprogress, h);
            }

            Draw.color();
            Draw.flush();
            Draw.proj(Tmp.m1);
        }
    }
}
