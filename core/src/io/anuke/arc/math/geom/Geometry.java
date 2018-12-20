package io.anuke.arc.math.geom;

import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.IntArray;
import io.anuke.arc.function.PositionConsumer;
import io.anuke.arc.function.SegmentConsumer;
import io.anuke.arc.math.Mathf;

public final class Geometry{
    /** Points repesenting cardinal directions, starting at the left and going counter-clockwise. */
    public final static GridPoint2[] d4 = {
    new GridPoint2(1, 0),
    new GridPoint2(0, 1),
    new GridPoint2(-1, 0),
    new GridPoint2(0, -1)
    };
    public final static GridPoint2[] d8 = {
    new GridPoint2(1, 0),
    new GridPoint2(1, 1),
    new GridPoint2(0, 1),
    new GridPoint2(-1, 1),
    new GridPoint2(-1, 0),
    new GridPoint2(-1, -1),
    new GridPoint2(0, -1),
    new GridPoint2(1, -1),
    };
    public final static GridPoint2[] d8edge = {
    new GridPoint2(1, 1),
    new GridPoint2(-1, 1),
    new GridPoint2(-1, -1),
    new GridPoint2(1, -1)
    };
    static private final Vector2 tmp1 = new Vector2(), tmp2 = new Vector2(), tmp3 = new Vector2();


    public static GridPoint2 d4(int i){
        return d4[Mathf.mod(i, 4)];
    }

    public static GridPoint2 d8(int i){
        return d8[Mathf.mod(i, 8)];
    }

    public static GridPoint2 d8edge(int i){
        return d8edge[Mathf.mod(i, 4)];
    }

    public static <T extends Position> T findClosest(float x, float y, T[] list){
        T closest = null;
        float cdist = 0f;
        for(T t : list){
            float dst = t.dst(x, y);
            if(closest == null || dst < cdist){
                closest = t;
                cdist = dst;
            }
        }
        return closest;
    }

    public static <T extends Position> T findClosest(float x, float y, Iterable<T> list){
        T closest = null;
        float cdist = 0f;
        for(T t : list){
            float dst = t.dst(x, y);
            if(closest == null || dst < cdist){
                closest = t;
                cdist = dst;
            }
        }
        return closest;
    }

    public static <T extends Position> T findFurthest(float x, float y, Iterable<T> list){
        T closest = null;
        float cdist = 0f;
        for(T t : list){
            float dst = t.dst(x, y);
            if(closest == null || dst > cdist){
                closest = t;
                cdist = dst;
            }
        }
        return closest;
    }

    public static Vector2[] pixelCircle(float tindex){
        return pixelCircle(tindex, (index, x, y) -> Vector2.dst(x, y, index, index) < index - 0.5f);
    }

    public static Vector2[] pixelCircle(float index, SolidChecker checker){
        int size = (int)(index * 2);
        IntArray ints = new IntArray();

        //add edges (bottom left corner)
        for(int x = -1; x < size + 1; x++){
            for(int y = -1; y < size + 1; y++){
                if((checker.solid(index, x, y) || checker.solid(index, x - 1, y) || checker.solid(index, x, y - 1) || checker.solid(index, x - 1, y - 1)) &&
                !(checker.solid(index, x, y) && checker.solid(index, x - 1, y) && checker.solid(index, x, y - 1) && checker.solid(index, x - 1, y - 1))){
                    ints.add(x + y * (size + 1));
                }
            }
        }

        Array<Vector2> path = new Array<>();

        int cindex = 0;
        while(ints.size > 0){
            int x = ints.get(cindex) % (size + 1);
            int y = ints.get(cindex) / (size + 1);
            path.add(new Vector2(x - size / 2, y - size / 2));
            ints.removeIndex(cindex);

            //find nearby edge
            for(int i = 0; i < ints.size; i++){

                int x2 = ints.get(i) % (size + 1);
                int y2 = ints.get(i) / (size + 1);
                if(Math.abs(x2 - x) <= 1 && Math.abs(y2 - y) <= 1 &&
                !(Math.abs(x2 - x) == 1 && Math.abs(y2 - y) == 1)){
                    cindex = i;
                    break;
                }
            }
        }

        return path.toArray(Vector2.class);
    }

    /** returns a regular polygon with {amount} sides */
    public static float[] regPoly(int amount, float size){
        float[] v = new float[amount * 2];
        Vector2 vec = new Vector2(1, 1);
        vec.setLength(size);
        for(int i = 0; i < amount; i++){
            vec.setAngle((360f / amount) * i + 90);
            v[i * 2] = vec.x;
            v[i * 2 + 1] = vec.y;
        }
        return v;
    }

    public static float iterateLine(float start, float x1, float y1, float x2, float y2, float segment, PositionConsumer pos){
        float len = Vector2.dst(x1, y1, x2, y2);
        int steps = (int)(len / segment);
        float step = 1f / steps;

        float offset = len;
        tmp2.set(x2, y2);
        for(int i = 0; i < steps; i++){
            float s = step * i;
            tmp1.set(x1, y1);
            tmp1.lerp(tmp2, s);
            pos.accept(tmp1.x, tmp1.y);
            offset -= step;
        }

        return offset;
    }

    public static void iteratePolySegments(float[] vertices, SegmentConsumer it){
        for(int i = 0; i < vertices.length; i += 2){
            float x = vertices[i];
            float y = vertices[i + 1];
            float x2, y2;
            if(i == vertices.length - 2){
                x2 = vertices[0];
                y2 = vertices[1];
            }else{
                x2 = vertices[i + 2];
                y2 = vertices[i + 3];
            }

            it.accept(x, y, x2, y2);
        }
    }

    public static void iteratePolygon(PositionConsumer path, float[] vertices){
        for(int i = 0; i < vertices.length; i += 2){
            float x = vertices[i];
            float y = vertices[i + 1];
            path.accept(x, y);
        }
    }

    public static GridPoint2[] getD4Points(){
        return d4;
    }

    public static GridPoint2[] getD8Points(){
        return d8;
    }

    public static GridPoint2[] getD8EdgePoints(){
        return d8edge;
    }

    /**
     * Checks for collisions between two rectangles, and returns the correct delta vector of A.
     * Note: The same vector instance is returned each time!
     */
    public static Vector2 overlap(Rectangle a, Rectangle b, boolean x){
        float penetration = 0f;

        float ax = a.x + a.width / 2, bx = b.x + b.width / 2;
        float ay = a.y + a.height / 2, by = b.y + b.height / 2;

        // Vector from A to B
        float nx = ax - bx,
        ny = ay - by;

        // Calculate half extends along x axis
        float aex = a.width / 2,
        bex = b.width / 2;

        // Overlap on x axis
        float xoverlap = aex + bex - Math.abs(nx);
        if(Math.abs(xoverlap) > 0){

            // Calculate half extends along y axis
            float aey = a.height / 2,
            bey = b.height / 2;

            // Overlap on x axis
            float yoverlap = aey + bey - Math.abs(ny);
            if(Math.abs(yoverlap) > 0){

                // Find out which axis is the axis of least penetration
                if(Math.abs(xoverlap) < Math.abs(yoverlap)){
                    // Point towards B knowing that n points from A to B
                    tmp1.x = nx < 0 ? 1 : -1;
                    tmp1.y = 0;
                    penetration = xoverlap;
                }else{
                    // Point towards B knowing that n points from A to B
                    tmp1.x = 0;
                    tmp1.y = ny < 0 ? 1 : -1;
                    penetration = yoverlap;
                }

            }
        }

        float percent = 1f,
        slop = 0.0f,
        m = Math.max(penetration - slop, 0.0f);

        // Apply correctional impulse
        float cx = m * tmp1.x * percent,
        cy = m * tmp1.y * percent;

        tmp1.x = -cx;
        tmp1.y = -cy;

        return tmp1;
    }

    /**
     * Computes the barycentric coordinates v,w for the specified point in the triangle.
     * <p>
     * If barycentric.x >= 0 && barycentric.y >= 0 && barycentric.x + barycentric.y <= 1 then the point is inside the triangle.
     * <p>
     * If vertices a,b,c have values aa,bb,cc then to get an interpolated value at point p:
     *
     * <pre>
     * Geometry.barycentric(p, a, b, c, barycentric);
     * float u = 1.f - barycentric.x - barycentric.y;
     * float x = u * aa.x + barycentric.x * bb.x + barycentric.y * cc.x;
     * float y = u * aa.y + barycentric.x * bb.y + barycentric.y * cc.y;
     * </pre>
     * @return barycentricOut
     */
    static public Vector2 toBarycoord(Vector2 p, Vector2 a, Vector2 b, Vector2 c, Vector2 barycentricOut){
        Vector2 v0 = tmp1.set(b).sub(a);
        Vector2 v1 = tmp2.set(c).sub(a);
        Vector2 v2 = tmp3.set(p).sub(a);
        float d00 = v0.dot(v0);
        float d01 = v0.dot(v1);
        float d11 = v1.dot(v1);
        float d20 = v2.dot(v0);
        float d21 = v2.dot(v1);
        float denom = d00 * d11 - d01 * d01;
        barycentricOut.x = (d11 * d20 - d01 * d21) / denom;
        barycentricOut.y = (d00 * d21 - d01 * d20) / denom;
        return barycentricOut;
    }

    /** Returns true if the barycentric coordinates are inside the triangle. */
    static public boolean barycoordInsideTriangle(Vector2 barycentric){
        return barycentric.x >= 0 && barycentric.y >= 0 && barycentric.x + barycentric.y <= 1;
    }

    /**
     * Returns interpolated values given the barycentric coordinates of a point in a triangle and the values at each vertex.
     * @return interpolatedOut
     */
    static public Vector2 fromBarycoord(Vector2 barycentric, Vector2 a, Vector2 b, Vector2 c, Vector2 interpolatedOut){
        float u = 1 - barycentric.x - barycentric.y;
        interpolatedOut.x = u * a.x + barycentric.x * b.x + barycentric.y * c.x;
        interpolatedOut.y = u * a.y + barycentric.x * b.y + barycentric.y * c.y;
        return interpolatedOut;
    }

    /**
     * Returns an interpolated value given the barycentric coordinates of a point in a triangle and the values at each vertex.
     * @return interpolatedOut
     */
    static public float fromBarycoord(Vector2 barycentric, float a, float b, float c){
        float u = 1 - barycentric.x - barycentric.y;
        return u * a + barycentric.x * b + barycentric.y * c;
    }

    /**
     * Returns the lowest positive root of the quadric equation given by a* x * x + b * x + c = 0. If no solution is given
     * Float.Nan is returned.
     * @param a the first coefficient of the quadric equation
     * @param b the second coefficient of the quadric equation
     * @param c the third coefficient of the quadric equation
     * @return the lowest positive root or Float.Nan
     */
    static public float lowestPositiveRoot(float a, float b, float c){
        float det = b * b - 4 * a * c;
        if(det < 0) return Float.NaN;

        float sqrtD = (float)Math.sqrt(det);
        float invA = 1 / (2 * a);
        float r1 = (-b - sqrtD) * invA;
        float r2 = (-b + sqrtD) * invA;

        if(r1 > r2){
            float tmp = r2;
            r2 = r1;
            r1 = tmp;
        }

        if(r1 > 0) return r1;
        if(r2 > 0) return r2;
        return Float.NaN;
    }

    static public boolean colinear(float x1, float y1, float x2, float y2, float x3, float y3){
        float dx21 = x2 - x1, dy21 = y2 - y1;
        float dx32 = x3 - x2, dy32 = y3 - y2;
        float det = dx32 * dy21 - dx21 * dy32;
        return Math.abs(det) < Mathf.FLOAT_ROUNDING_ERROR;
    }

    static public Vector2 triangleCentroid(float x1, float y1, float x2, float y2, float x3, float y3, Vector2 centroid){
        centroid.x = (x1 + x2 + x3) / 3;
        centroid.y = (y1 + y2 + y3) / 3;
        return centroid;
    }

    /** Returns the circumcenter of the triangle. The input points must not be colinear. */
    static public Vector2 triangleCircumcenter(float x1, float y1, float x2, float y2, float x3, float y3, Vector2 circumcenter){
        float dx21 = x2 - x1, dy21 = y2 - y1;
        float dx32 = x3 - x2, dy32 = y3 - y2;
        float dx13 = x1 - x3, dy13 = y1 - y3;
        float det = dx32 * dy21 - dx21 * dy32;
        if(Math.abs(det) < Mathf.FLOAT_ROUNDING_ERROR)
            throw new IllegalArgumentException("Triangle points must not be colinear.");
        det *= 2;
        float sqr1 = x1 * x1 + y1 * y1, sqr2 = x2 * x2 + y2 * y2, sqr3 = x3 * x3 + y3 * y3;
        circumcenter.set((sqr1 * dy32 + sqr2 * dy13 + sqr3 * dy21) / det, -(sqr1 * dx32 + sqr2 * dx13 + sqr3 * dx21) / det);
        return circumcenter;
    }

    static public float triangleCircumradius(float x1, float y1, float x2, float y2, float x3, float y3){
        float m1, m2, mx1, mx2, my1, my2, x, y;
        if(Math.abs(y2 - y1) < Mathf.FLOAT_ROUNDING_ERROR){
            m2 = -(x3 - x2) / (y3 - y2);
            mx2 = (x2 + x3) / 2;
            my2 = (y2 + y3) / 2;
            x = (x2 + x1) / 2;
            y = m2 * (x - mx2) + my2;
        }else if(Math.abs(y3 - y2) < Mathf.FLOAT_ROUNDING_ERROR){
            m1 = -(x2 - x1) / (y2 - y1);
            mx1 = (x1 + x2) / 2;
            my1 = (y1 + y2) / 2;
            x = (x3 + x2) / 2;
            y = m1 * (x - mx1) + my1;
        }else{
            m1 = -(x2 - x1) / (y2 - y1);
            m2 = -(x3 - x2) / (y3 - y2);
            mx1 = (x1 + x2) / 2;
            mx2 = (x2 + x3) / 2;
            my1 = (y1 + y2) / 2;
            my2 = (y2 + y3) / 2;
            x = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
            y = m1 * (x - mx1) + my1;
        }
        float dx = x1 - x, dy = y1 - y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Ratio of circumradius to shortest edge as a measure of triangle quality.
     * <p>
     * Gary L. Miller, Dafna Talmor, Shang-Hua Teng, and Noel Walkington. A Delaunay Based Numerical Method for Three Dimensions:
     * Generation, Formulation, and Partition.
     */
    static public float triangleQuality(float x1, float y1, float x2, float y2, float x3, float y3){
        float length1 = (float)Math.sqrt(x1 * x1 + y1 * y1);
        float length2 = (float)Math.sqrt(x2 * x2 + y2 * y2);
        float length3 = (float)Math.sqrt(x3 * x3 + y3 * y3);
        return Math.min(length1, Math.min(length2, length3)) / triangleCircumradius(x1, y1, x2, y2, x3, y3);
    }

    static public float triangleArea(float x1, float y1, float x2, float y2, float x3, float y3){
        return Math.abs((x1 - x3) * (y2 - y1) - (x1 - x2) * (y3 - y1)) * 0.5f;
    }

    static public Vector2 quadrilateralCentroid(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                                                Vector2 centroid){
        float avgX1 = (x1 + x2 + x3) / 3;
        float avgY1 = (y1 + y2 + y3) / 3;
        float avgX2 = (x1 + x4 + x3) / 3;
        float avgY2 = (y1 + y4 + y3) / 3;
        centroid.x = avgX1 - (avgX1 - avgX2) / 2;
        centroid.y = avgY1 - (avgY1 - avgY2) / 2;
        return centroid;
    }

    /** Returns the centroid for the specified non-self-intersecting polygon. */
    static public Vector2 polygonCentroid(float[] polygon, int offset, int count, Vector2 centroid){
        if(count < 6) throw new IllegalArgumentException("A polygon must have 3 or more coordinate pairs.");
        float x = 0, y = 0;

        float signedArea = 0;
        int i = offset;
        for(int n = offset + count - 2; i < n; i += 2){
            float x0 = polygon[i];
            float y0 = polygon[i + 1];
            float x1 = polygon[i + 2];
            float y1 = polygon[i + 3];
            float a = x0 * y1 - x1 * y0;
            signedArea += a;
            x += (x0 + x1) * a;
            y += (y0 + y1) * a;
        }

        float x0 = polygon[i];
        float y0 = polygon[i + 1];
        float x1 = polygon[offset];
        float y1 = polygon[offset + 1];
        float a = x0 * y1 - x1 * y0;
        signedArea += a;
        x += (x0 + x1) * a;
        y += (y0 + y1) * a;

        if(signedArea == 0){
            centroid.x = 0;
            centroid.y = 0;
        }else{
            signedArea *= 0.5f;
            centroid.x = x / (6 * signedArea);
            centroid.y = y / (6 * signedArea);
        }
        return centroid;
    }

    /** Computes the area for a convex polygon. */
    static public float polygonArea(float[] polygon, int offset, int count){
        float area = 0;
        for(int i = offset, n = offset + count; i < n; i += 2){
            int y1 = i + 1;
            int x2 = (i + 2) % n;
            if(x2 < offset) x2 += offset;
            int y2 = (i + 3) % n;
            if(y2 < offset) y2 += offset;
            area += polygon[i] * polygon[y2];
            area -= polygon[x2] * polygon[y1];
        }
        area *= 0.5f;
        return area;
    }

    static public void ensureCCW(float[] polygon){
        ensureCCW(polygon, 0, polygon.length);
    }

    static public void ensureCCW(float[] polygon, int offset, int count){
        if(!isClockwise(polygon, offset, count)) return;
        int lastX = offset + count - 2;
        for(int i = offset, n = offset + count / 2; i < n; i += 2){
            int other = lastX - i;
            float x = polygon[i];
            float y = polygon[i + 1];
            polygon[i] = polygon[other];
            polygon[i + 1] = polygon[other + 1];
            polygon[other] = x;
            polygon[other + 1] = y;
        }
    }

    static public boolean isClockwise(float[] polygon, int offset, int count){
        if(count <= 2) return false;
        float area = 0, p1x, p1y, p2x, p2y;
        for(int i = offset, n = offset + count - 3; i < n; i += 2){
            p1x = polygon[i];
            p1y = polygon[i + 1];
            p2x = polygon[i + 2];
            p2y = polygon[i + 3];
            area += p1x * p2y - p2x * p1y;
        }
        p1x = polygon[offset + count - 2];
        p1y = polygon[offset + count - 1];
        p2x = polygon[offset];
        p2y = polygon[offset + 1];
        return area + p1x * p2y - p2x * p1y < 0;
    }

    public interface SolidChecker{
        boolean solid(float index, int x, int y);
    }
}
