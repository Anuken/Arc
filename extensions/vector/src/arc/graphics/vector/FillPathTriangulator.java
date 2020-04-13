package arc.graphics.vector;

import arc.struct.*;

class FillPathTriangulator extends PathTriangulator{
    FillPathTriangulator(PathComponent component){
        super(component, PT_FILL_BEVEL, PT_FILL_INNERBEVEL);
    }

    @Override
    void triangulate(){
        float width = component.getFillWidth();
        boolean antialiased = width > 0.0f;
        if(antialiased){
            triangulateAntialiasedFill(width);
        }else{
            triangulateFill();
        }
    }

    private void triangulateAntialiasedFill(float width){
        float woff = 0.5f * canvas.fringeWidth;
        Array<Point> pts = component.points;

        float rightWidth = width - woff;
        float leftWidth = width + woff;
        //float leftWidth = width - (woff * 0.5f);
        int ptsSize = pts.size;
        int ptsSizeMinusOne = ptsSize - 1;

        Point p0 = pts.get(ptsSizeMinusOne);
        Point p1 = pts.get(0);

        for(int i = 0; i < ptsSize; ++i){
            expandFillWithFringe(p0, p1, woff);
            triangulateFringe(p0, p1, rightWidth, leftWidth);
            p0 = p1;
            p1 = i < ptsSizeMinusOne ? pts.get(i + 1) : pts.get(0);
        }

        loopFringe();
    }

    private void expandFillWithFringe(Point p0, Point p1, float woff){
        if((p1.flags & ptBevelFlag) != 0){
            float dlx0 = p0.dy;
            float dly0 = -p0.dx;
            float dlx1 = p1.dy;
            float dly1 = -p1.dx;
            if((p1.flags & PT_LEFT) != 0){
                float lx = p1.x + p1.dmx * woff;
                float ly = p1.y + p1.dmy * woff;
                newTriangleFanVertex(lx, ly, 0.5f, 1);
            }else{
                float lx0 = p1.x + dlx0 * woff;
                float ly0 = p1.y + dly0 * woff;
                float lx1 = p1.x + dlx1 * woff;
                float ly1 = p1.y + dly1 * woff;
                newTriangleFanVertex(lx0, ly0, 0.5f, 1);
                newTriangleFanVertex(lx1, ly1, 0.5f, 1);
            }
        }else{
            newTriangleFanVertex(p1.x + (p1.dmx * woff), p1.y + (p1.dmy * woff), 0.5f, 1);
        }
    }

    private void triangulateFringe(Point p0, Point p1, float rightWidth, float leftWidth){
        if((p1.flags & (ptBevelFlag | prInnerBevelFlag)) != 0){
            bevelJoin(triangleStripVertices, p0, p1, leftWidth, rightWidth, 0, 1);
        }else{
            newTriangleStripVertex(p1.x + (p1.dmx * leftWidth), p1.y + (p1.dmy * leftWidth), 0, 1);
            newTriangleStripVertex(p1.x - (p1.dmx * rightWidth), p1.y - (p1.dmy * rightWidth), 1, 1);
        }
    }

    private void loopFringe(){
        Vertex dst0 = triangleStripVertices.get(0);
        Vertex dst1 = triangleStripVertices.get(1);
        newTriangleStripVertex(dst0.x, dst0.y, 0, 1);
        newTriangleStripVertex(dst1.x, dst1.y, 1, 1);
    }

    private void triangulateFill(){
        Array<Point> points = component.points;
        for(int j = 0; j < points.size; ++j){
            Point p = points.get(j);
            newTriangleFanVertex(p.x, p.y, 0.5f, 1);
        }
    }
}
