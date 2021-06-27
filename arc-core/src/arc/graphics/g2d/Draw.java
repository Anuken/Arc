package arc.graphics.g2d;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;

import static arc.Core.*;

public class Draw{
    private static ScreenQuad squad;

    private static final Color[] carr = new Color[3];
    private static final float[] vertices = new float[SpriteBatch.SPRITE_SIZE];

    public static float scl = 1f;
    public static float xscl = 1f, yscl = 1f;

    private static ScreenQuad getQuad(){
        if(squad == null) squad = new ScreenQuad();
        return squad;
    }

    /** Blits an already-bound texture onto the screen with a shader.
     * This does not use a spritebatch! */
    public static void blit(Shader shader){
        shader.bind();
        shader.apply();
        getQuad().render(shader);
    }

    /** Blits a texture onto the screen with a shader.
     * This does not use a spritebatch! */
    public static void blit(Texture texture, Shader shader){
        texture.bind(0);
        shader.bind();
        shader.apply();
        getQuad().render(shader);
    }

    /** Blits a framebuffer onto the screen with a shader.
     * This does not use a spritebatch! */
    public static void blit(FrameBuffer buffer, Shader shader){
        blit(buffer.getTexture(), shader);
    }

    public static void batch(Batch nextBatch){
        flush();
        Core.batch = nextBatch;
    }

    public static void batch(Batch nextBatch, Runnable run){
        Batch prev = Core.batch;
        prev.flush();

        Core.batch = nextBatch;

        run.run();

        nextBatch.flush();
        Core.batch = prev;
    }

    public static void stencil(Runnable stencil, Runnable contents){
        beginStencil();

        stencil.run();

        beginStenciled();

        contents.run();

        endStencil();
    }

    public static void beginStencil(){
        flush();

        Gl.stencilMask(0xFF);
        Gl.colorMask(false, false, false, false);
        Gl.enable(Gl.stencilTest);
        Gl.stencilFunc(Gl.always, 1, 0xFF);
        Gl.stencilMask(0xFF);
        Gl.stencilOp(Gl.replace, Gl.replace, Gl.replace);
    }

    public static void beginStenciled(){
        flush();

        Gl.stencilOp(Gl.keep, Gl.keep, Gl.keep);
        Gl.colorMask(true, true, true, true);
        Gl.stencilFunc(Gl.equal, 1, 0xFF);
    }

    public static void endStencil(){
        flush();

        Gl.disable(Gl.stencilTest);
    }

    public static void scl(float nscl){
        scl(nscl, nscl);
    }

    public static void scl(float nxscl, float nyscl){
        xscl = nxscl;
        yscl = nyscl;
    }

    public static void scl(){
        xscl = yscl = 1f;
    }

    public static Shader getShader(){
        return Core.batch.getShader();
    }

    public static void shader(Shader shader){
        shader(shader, true);
    }

    public static void shader(Shader shader, boolean apply){
        Core.batch.setShader(shader, apply);
    }

    public static void shader(){
        Core.batch.setShader(null);
    }

    /** Note that sorting is disabled by default, even if it is supported. */
    public static void sort(boolean sort){
        batch.setSort(sort);
    }

    /** Sets sorting order to either be ascending or descending in terms of Z. Default: true. */
    public static void sortAscending(boolean ascend){
        batch.setSortAscending(ascend);
    }

    public static float z(){
        return batch.sortAscending ? batch.z : -batch.z;
    }

    /** Note that this does nothing on most Batch implementations. */
    public static void z(float z){
        Core.batch.z(z);
    }

    public static Color getColor(){
        return Core.batch.getColor();
    }

    public static Color getMixColor(){
        return Core.batch.getMixColor();
    }

    public static void mixcol(Color color, float a){
        Core.batch.setMixColor(color.r, color.g, color.b, Mathf.clamp(a));
    }

    public static void mixcol(){
        Core.batch.setPackedMixColor(Color.clearFloatBits);
    }

    public static void tint(Color a, Color b, float s){
        Tmp.c1.set(a).lerp(b, s);
        Core.batch.setColor(Tmp.c1.r, Tmp.c1.g, Tmp.c1.b, Core.batch.getColor().a);
    }

    public static void tint(Color color){
        Core.batch.setColor(color.r, color.g, color.b, Core.batch.getColor().a);
    }

    public static void colorMul(Color color, float mul){
        color(color.r * mul, color.g * mul, color.b * mul, 1f);
    }

    public static void color(Color color){
        Core.batch.setColor(color);
    }

    public static void color(Color color, float alpha){
        Core.batch.setColor(color.r, color.g, color.b, alpha);
    }

    public static void color(int color){
        Core.batch.setColor(Tmp.c1.rgba8888(color));
    }

    public static void color(float color){
        Core.batch.setPackedColor(color);
    }

    public static void color(Color a, Color b, Color c, float progress){
        carr[0] = a;
        carr[1] = b;
        carr[2] = c;
        color(Tmp.c1.lerp(carr, progress));
    }

    /** Automatically mixes colors. */
    public static void color(Color a, Color b, float s){
        Core.batch.setColor(Tmp.c1.set(a).lerp(b, s));
    }

    public static void color(){
        Core.batch.setPackedColor(Color.whiteFloatBits);
    }

    public static void color(float r, float g, float b){
        Core.batch.setColor(r, g, b, 1f);
    }

    public static void color(float r, float g, float b, float a){
        Core.batch.setColor(r, g, b, a);
    }

    /** Lightness color. */
    public static void colorl(float l){
        color(l, l, l);
    }

    /** Lightness color, alpha. */
    public static void colorl(float l, float a){
        color(l, l, l, a);
    }

    public static void blend(Blending blending){
        Core.batch.setBlending(blending);
    }

    public static void blend(){
        blend(Blending.normal);
    }

    public static void reset(){
        color();
        mixcol();
        xscl = yscl = 1f;
        Lines.stroke(1f);
    }

    public static void alpha(float alpha){
        Core.batch.setColor(Core.batch.getColor().r, Core.batch.getColor().g, Core.batch.getColor().b, alpha);
    }

    /** Draws a portion of a world-sized texture. */
    public static void fbo(FrameBuffer buffer, int worldWidth, int worldHeight, int tilesize){
        fbo(buffer.getTexture(), worldWidth, worldHeight, tilesize);
    }

    /** Draws a portion of a world-sized texture. */
    public static void fbo(Texture texture, int worldWidth, int worldHeight, int tilesize){
        float ww = worldWidth * tilesize, wh = worldHeight * tilesize;
        float x = camera.position.x + tilesize / 2f, y = camera.position.y + tilesize / 2f;
        float u = (x - camera.width / 2f) / ww,
        v = (y - camera.height / 2f) / wh,
        u2 = (x + camera.width / 2f) / ww,
        v2 = (y + camera.height / 2f) / wh;

        Tmp.tr1.set(texture);
        Tmp.tr1.set(u, v2, u2, v);

        Draw.rect(Tmp.tr1, camera.position.x, camera.position.y, camera.width, camera.height);
    }

    /** On a sorting or queued batch implementation, this treats everything inside the runnable as one unit.
     * Thus, it can be used to set shaders and do other special state. */
    public static void draw(float z, Runnable run){
        z(z);
        batch.draw(run);
    }

    /** Applies runnables for the begin and end of a specific Z value.
     * Useful for framebuffers or batched shader begin/ends. */
    public static void drawRange(float z, Runnable begin, Runnable end){
        drawRange(z, 0.001f, begin, end);
    }

    /** Applies runnables for the begin and end of a specific Z value.
     * Useful for framebuffers or batched shader begin/ends. */
    public static void drawRange(float z, float range, Runnable begin, Runnable end){
        draw(z - range, begin);
        draw(z + range, end);
    }

    /** Fill a white quad to the camera. */
    public static void rect(){
        Fill.rect(camera.position.x, camera.position.y, camera.width, camera.height);
    }

    public static void rect(String region, float x, float y, float w, float h){
        rect(Core.atlas.find(region), x, y, w, h);
    }

    public static void rect(TextureRegion region, float x, float y, float w, float h){
        Core.batch.draw(region, x - w /2f, y - h /2f, 0, 0, w, h, 0);
    }

    public static void rect(TextureRegion region, float x, float y){
        rect(region, x, y, region.width * scl * xscl, region.height * scl * yscl);
    }

    public static void rect(String region, float x, float y){
        rect(Core.atlas.find(region), x, y);
    }

    public static void rect(TextureRegion region, float x, float y, float w, float h, float originX, float originY, float rotation){
        Core.batch.draw(region, x - w /2f, y - h /2f, originX, originY, w, h, rotation);
    }

    public static void rect(String region, float x, float y, float w, float h, float originX, float originY, float rotation){
        Core.batch.draw(Core.atlas.find(region), x - w /2f, y - h /2f, originX, originY, w, h, rotation);
    }

    public static void rect(TextureRegion region, float x, float y, float w, float h, float rotation){
        rect(region, x, y, w, h, w/2f, h/2f, rotation);
    }

    public static void rect(String region, float x, float y, float w, float h, float rotation){
        rect(Core.atlas.find(region), x, y, w, h, w/2f, h/2f, rotation);
    }

    public static void rect(TextureRegion region, Position pos, float w, float h){
        rect(region, pos.getX(), pos.getY(), w, h);
    }

    public static void rect(TextureRegion region, Position pos, float w, float h, float rotation){
        rect(region, pos.getX(), pos.getY(), w, h, rotation);
    }

    public static void rect(TextureRegion region, Position pos, float rotation){
        rect(region, pos.getX(), pos.getY(), rotation);
    }

    public static void rect(TextureRegion region, float x, float y, float rotation){
        rect(region, x, y, region.width * scl * xscl, region.height * scl * yscl, rotation);
    }

    public static void rect(String region, float x, float y, float rotation){
        rect(Core.atlas.find(region), x, y, rotation);
    }

    public static void vert(Texture texture, float[] vertices, int offset, int length){
        Core.batch.draw(texture, vertices, offset, length);
    }

    public static void flush(){
        Core.batch.flush();
    }

    public static void proj(float x, float y, float w, float h){
        Draw.flush();
        batch.getProjection().setOrtho(x, y, w, h);
    }

    public static void proj(Camera proj){
        proj(proj.mat);
    }

    public static void proj(Mat proj){
        Core.batch.setProjection(proj);
    }

    public static Mat proj(){
        return Core.batch.getProjection();
    }

    public static void trans(Mat trans){
        Core.batch.setTransform(trans);
    }

    public static Mat trans(){
        return Core.batch.getTransform();
    }

    public static TextureRegion wrap(Texture texture){
        Tmp.tr2.set(texture);
        return Tmp.tr2;
    }

    public static void rectv(TextureRegion region, float x, float y, float width, float height, Cons<Vec2> tweaker){
        rectv(region, x, y, width, height, 0, tweaker);
    }

    public static void rectv(TextureRegion region, float x, float y, float width, float height, float rotation, Cons<Vec2> tweaker){
        rectv(region, x, y, width, height, width/2, height/2, rotation, tweaker);
    }

    public static void rectv(TextureRegion region, float x, float y, float width, float height, float originX, float originY, float rotation, Cons<Vec2> tweaker){
        x -= width/2f;
        y -= height/2f;

        //bottom left and top right corner points relative to origin
        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // rotate
        float cos = Mathf.cosDeg(rotation);
        float sin = Mathf.sinDeg(rotation);

        float x1 = cos * fx - sin * fy + worldOriginX;
        float y1 = sin * fx + cos * fy + worldOriginY;
        float x2 = cos * fx - sin * fy2 + worldOriginX;
        float y2 = sin * fx + cos * fy2 + worldOriginY;
        float x3 = cos * fx2 - sin * fy2 + worldOriginX;
        float y3 = sin * fx2 + cos * fy2 + worldOriginY;
        float x4 = x1 + (x3 - x2);
        float y4 = y3 - (y2 - y1);

        tweaker.get(Tmp.v1.set(x1, y1));
        x1 = Tmp.v1.x;
        y1 = Tmp.v1.y;

        tweaker.get(Tmp.v1.set(x2, y2));
        x2 = Tmp.v1.x;
        y2 = Tmp.v1.y;

        tweaker.get(Tmp.v1.set(x3, y3));
        x3 = Tmp.v1.x;
        y3 = Tmp.v1.y;

        tweaker.get(Tmp.v1.set(x4, y4));
        x4 = Tmp.v1.x;
        y4 = Tmp.v1.y;

        final float u = region.u;
        final float v = region.v2;
        final float u2 = region.u2;
        final float v2 = region.v;

        final float color = batch.getPackedColor();
        final float mixColor = batch.getPackedMixColor();
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;
        vertices[5] = mixColor;

        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = color;
        vertices[9] = u;
        vertices[10] = v2;
        vertices[11] = mixColor;

        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = color;
        vertices[15] = u2;
        vertices[16] = v2;
        vertices[17] = mixColor;

        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = color;
        vertices[21] = u2;
        vertices[22] = v;
        vertices[23] = mixColor;

        Draw.vert(region.texture, vertices, 0, vertices.length);
    }
}
