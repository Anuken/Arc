package arc.graphics.vector;

import arc.math.*;
import arc.struct.*;

abstract class PathTriangulator implements PathConstants{
    final Array<Vertex> triangleFanVertices = new Array<>();
    final Array<Vertex> triangleStripVertices = new Array<>();
    private final float[] bevelValues = new float[4];
    Canvas canvas;
    PathMesh pathMesh;
    PathComponent component;
    int ptBevelFlag;
    int prInnerBevelFlag;

    PathTriangulator(PathComponent component, int ptBevelFlag, int prInnerBevelFlag){
        this.component = component;
        this.pathMesh = component.pathMesh;
        this.canvas = component.canvas;
        this.ptBevelFlag = ptBevelFlag;
        this.prInnerBevelFlag = prInnerBevelFlag;
    }

    GlPathComponent createGlPathComponent(){
        triangulate();
        GlPathComponent glPathComponent = GlPathComponent.obtain();
        glPathComponent.triangleFanVertices.addAll(triangleFanVertices);
        glPathComponent.triangleStripVertices.addAll(triangleStripVertices);
        triangleFanVertices.clear();
        triangleStripVertices.clear();
        return glPathComponent;
    }

    abstract void triangulate();

    protected void newTriangleFanVertex(float x, float y, float u, float v){
        triangleFanVertices.add(Vertex.obtain(x, y, u, v));
    }

    protected void newTriangleStripVertex(float x, float y, float u, float v){
        triangleStripVertices.add(Vertex.obtain(x, y, u, v));
    }

    protected void chooseBevel(boolean bevel, Point p0, Point p1, float w){
        if(bevel){
            bevelValues[0] = p1.x + p0.dy * w;
            bevelValues[1] = p1.y - p0.dx * w;
            bevelValues[2] = p1.x + p1.dy * w;
            bevelValues[3] = p1.y - p1.dx * w;
        }else{
            bevelValues[0] = p1.x + p1.dmx * w;
            bevelValues[1] = p1.y + p1.dmy * w;
            bevelValues[2] = p1.x + p1.dmx * w;
            bevelValues[3] = p1.y + p1.dmy * w;
        }
    }

    protected void bevelJoin(Array<Vertex> dst, Point p0, Point p1, float leftWidth, float rightWidth, float lu, float ru){
        float rx0 = 0, ry0 = 0, rx1 = 0, ry1 = 0;
        float lx0 = 0, ly0 = 0, lx1 = 0, ly1 = 0;
        float dlx0 = p0.dy;
        float dly0 = -p0.dx;
        float dlx1 = p1.dy;
        float dly1 = -p1.dx;

        if((p1.flags & PT_LEFT) != 0){
            chooseBevel((p1.flags & prInnerBevelFlag) != 0, p0, p1, leftWidth);
            lx0 = bevelValues[0];
            ly0 = bevelValues[1];
            lx1 = bevelValues[2];
            ly1 = bevelValues[3];

            dst.add(Vertex.obtain(lx0, ly0, lu, 1));
            dst.add(Vertex.obtain(p1.x - dlx0 * rightWidth, p1.y - dly0 * rightWidth, ru, 1));

            if((p1.flags & ptBevelFlag) != 0){
                dst.add(Vertex.obtain(lx0, ly0, lu, 1));
                dst.add(Vertex.obtain(p1.x - dlx0 * rightWidth, p1.y - dly0 * rightWidth, ru, 1));

                dst.add(Vertex.obtain(lx1, ly1, lu, 1));
                dst.add(Vertex.obtain(p1.x - dlx1 * rightWidth, p1.y - dly1 * rightWidth, ru, 1));
            }else{
                rx0 = p1.x - p1.dmx * rightWidth;
                ry0 = p1.y - p1.dmy * rightWidth;

                dst.add(Vertex.obtain(p1.x, p1.y, 0.5f, 1));
                dst.add(Vertex.obtain(p1.x - dlx0 * rightWidth, p1.y - dly0 * rightWidth, ru, 1));

                dst.add(Vertex.obtain(rx0, ry0, ru, 1));
                dst.add(Vertex.obtain(rx0, ry0, ru, 1));

                dst.add(Vertex.obtain(p1.x, p1.y, 0.5f, 1));
                dst.add(Vertex.obtain(p1.x - dlx1 * rightWidth, p1.y - dly1 * rightWidth, ru, 1));
            }

            dst.add(Vertex.obtain(lx1, ly1, lu, 1));
            dst.add(Vertex.obtain(p1.x - dlx1 * rightWidth, p1.y - dly1 * rightWidth, ru, 1));

        }else{
            chooseBevel((p1.flags & prInnerBevelFlag) != 0, p0, p1, -rightWidth);
            rx0 = bevelValues[0];
            ry0 = bevelValues[1];
            rx1 = bevelValues[2];
            ry1 = bevelValues[3];

            dst.add(Vertex.obtain(p1.x + dlx0 * leftWidth, p1.y + dly0 * leftWidth, lu, 1));
            dst.add(Vertex.obtain(rx0, ry0, ru, 1));

            if((p1.flags & ptBevelFlag) != 0){
                dst.add(Vertex.obtain(p1.x + dlx0 * leftWidth, p1.y + dly0 * leftWidth, lu, 1));
                dst.add(Vertex.obtain(rx0, ry0, ru, 1));

                dst.add(Vertex.obtain(p1.x + dlx1 * leftWidth, p1.y + dly1 * leftWidth, lu, 1));
                dst.add(Vertex.obtain(rx1, ry1, ru, 1));
            }else{
                lx0 = p1.x + p1.dmx * leftWidth;
                ly0 = p1.y + p1.dmy * leftWidth;

                dst.add(Vertex.obtain(p1.x + dlx0 * leftWidth, p1.y + dly0 * leftWidth, lu, 1));
                dst.add(Vertex.obtain(p1.x, p1.y, 0.5f, 1));

                dst.add(Vertex.obtain(lx0, ly0, lu, 1));
                dst.add(Vertex.obtain(lx0, ly0, lu, 1));

                dst.add(Vertex.obtain(p1.x + dlx1 * leftWidth, p1.y + dly1 * leftWidth, lu, 1));
                dst.add(Vertex.obtain(p1.x, p1.y, 0.5f, 1));
            }

            dst.add(Vertex.obtain(p1.x + dlx1 * leftWidth, p1.y + dly1 * leftWidth, lu, 1));
            dst.add(Vertex.obtain(rx1, ry1, ru, 1));
        }
    }

    protected void roundJoin(Array<Vertex> dst, Point p0, Point p1, float lw, float rw, float lu, float ru, int ncap){
        float dlx0 = p0.dy;
        float dly0 = -p0.dx;
        float dlx1 = p1.dy;
        float dly1 = -p1.dx;

        if((p1.flags & PT_LEFT) != 0){
            float lx0 = 0, ly0 = 0, lx1 = 0, ly1 = 0, a0, a1;

            chooseBevel((p1.flags & prInnerBevelFlag) != 0, p0, p1, lw);
            lx0 = bevelValues[0];
            ly0 = bevelValues[1];
            lx1 = bevelValues[2];
            ly1 = bevelValues[3];

            a0 = Mathf.atan2(-dly0, -dlx0);
            a1 = Mathf.atan2(-dly1, -dlx1);

            if(a1 > a0){
                a1 -= Mathf.PI * 2;
            }

            dst.add(Vertex.obtain(lx0, ly0, lu, 1));
            dst.add(Vertex.obtain(p1.x - dlx0 * rw, p1.y - dly0 * rw, ru, 1));

            int n = Mathf.clamp((int)Math.ceil(((a0 - a1) / Mathf.PI) * ncap), 2, ncap);
            for(int i = 0; i < n; i++){
                float u = i / (float)(n - 1);
                float a = a0 + u * (a1 - a0);
                float rx = p1.x + Mathf.cos(a) * rw;
                float ry = p1.y + Mathf.sin(a) * rw;

                dst.add(Vertex.obtain(p1.x, p1.y, 0.5f, 1));
                dst.add(Vertex.obtain(rx, ry, ru, 1));
            }

            dst.add(Vertex.obtain(lx1, ly1, lu, 1));
            dst.add(Vertex.obtain(p1.x - dlx1 * rw, p1.y - dly1 * rw, ru, 1));

        }else{
            float rx0 = 0, ry0 = 0, rx1 = 0, ry1 = 0, a0, a1;
            chooseBevel((p1.flags & prInnerBevelFlag) != 0, p0, p1, -rw);
            rx0 = bevelValues[0];
            ry0 = bevelValues[1];
            rx1 = bevelValues[2];
            ry1 = bevelValues[3];

            a0 = Mathf.atan2(dly0, dlx0);
            a1 = Mathf.atan2(dly1, dlx1);

            if(a1 < a0){
                a1 += Mathf.PI * 2;
            }

            dst.add(Vertex.obtain(p1.x + dlx0 * rw, p1.y + dly0 * rw, lu, 1));
            dst.add(Vertex.obtain(rx0, ry0, ru, 1));

            int n = Mathf.clamp((int)Math.ceil(((a1 - a0) / Mathf.PI) * ncap), 2, ncap);
            for(int i = 0; i < n; i++){
                float u = i / (float)(n - 1);
                float a = a0 + u * (a1 - a0);
                float lx = p1.x + Mathf.cos(a) * lw;
                float ly = p1.y + Mathf.sin(a) * lw;

                dst.add(Vertex.obtain(lx, ly, lu, 1));
                dst.add(Vertex.obtain(p1.x, p1.y, 0.5f, 1));
            }

            dst.add(Vertex.obtain(p1.x + dlx1 * rw, p1.y + dly1 * rw, lu, 1));
            dst.add(Vertex.obtain(rx1, ry1, ru, 1));
        }
    }
}
