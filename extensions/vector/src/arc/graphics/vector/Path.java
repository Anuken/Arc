package arc.graphics.vector;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

import static arc.graphics.vector.Canvas.distanceTolerance;

public class Path implements PathConstants, Poolable{
    public static final UnmodifiablePath emptyPath = new UnmodifiablePath();

    // Length  proportional to radius of a cubic bezier handle for 90deg arcs.
    private static final float kappa90 = 0.5522847493f;
    private static final float oneMinusKappa90 = 1 - kappa90;
    private static final int initCommandsSize = 256;

    private final FloatArray commands = new FloatArray(initCommandsSize);
    private final FloatArray tempCommands = new FloatArray(initCommandsSize);
    private final Vec2 tempPoint = new Vec2();
    private final AffineTransform tempMatrix = AffineTransform.obtain();
    private final BoundingBox tempBounds = new BoundingBox();
    private float lastX;
    private float lastY;
    private float lastStartX;
    private float lastStartY;
    private float lastKnotX;
    private float lastKnotY;

    public static Path obtain(){
        return Pools.obtain(Path.class, Path::new);
    }

    private static float normalize(Vec2 point){
        float d = (float)Math.sqrt(((point.x) * (point.x) + (point.y) * (point.y)));
        if(d > 1e-6f){
            float id = 1.0f / d;
            point.x *= id;
            point.y *= id;
        }

        return d;
    }

    private static boolean canConvertArcToLine(float startX, float startY, float endX, float endY, float radius, float x0, float y0){
        return radius < distanceTolerance
        || Point.pointEquals(x0, y0, startX, startY, distanceTolerance)
        || Point.pointEquals(startX, startY, endX, endY, distanceTolerance)
        || distPtSeg(startX, startY, x0, y0, endX, endY) < distanceTolerance * distanceTolerance;
    }

    private static float distPtSeg(float x, float y, float px, float py, float qx, float qy){
        float pqx, pqy, dx, dy, d, t;
        pqx = qx - px;
        pqy = qy - py;
        dx = x - px;
        dy = y - py;
        d = pqx * pqx + pqy * pqy;
        t = pqx * dx + pqy * dy;

        if(d > 0){
            t /= d;
        }

        if(t < 0){
            t = 0;
        }else if(t > 1){
            t = 1;
        }

        dx = px + t * pqx - x;
        dy = py + t * pqy - y;
        return dx * dx + dy * dy;
    }

    private static float clampAngles(float angle0, float angle1, Direction direction){
        float deltaAngle = angle1 - angle0;

        if(direction == Direction.clockwise){
            if(Math.abs(deltaAngle) >= Mathf.PI * 2){
                deltaAngle = Mathf.PI * 2;
            }else{
                while(deltaAngle < 0.0f){
                    deltaAngle += Mathf.PI * 2;
                }
            }
        }else{
            if(Math.abs(deltaAngle) >= Mathf.PI * 2){
                deltaAngle = -Mathf.PI * 2;
            }else{
                while(deltaAngle > 0.0f){
                    deltaAngle -= Mathf.PI * 2;
                }
            }
        }

        return deltaAngle;
    }

    //https://github.com/uxebu/bonsai/blob/master/src/runner/path/curved_path.js
    //TODO http://bazaar.launchpad.net/~inkscape.dev/inkscape/trunk/view/head:/src/helper/geom.cpp cubic_bbox
    private static void extCubicBounds(float start, float control1, float control2, float end, float otherVal, boolean xVal, BoundingBox out){
        if(containsRange(control1, control2, otherVal, xVal, out)){
            return;
        }

        float b = 6 * start - 12 * control1 + 6 * control2;
        float a = -3 * start + 9 * control1 - 9 * control2 + 3 * end;
        float c = 3 * control1 - 3 * start;

        if(Math.abs(a) < 1e-12){// Numerical robustness
            if(Math.abs(b) < 1e-12){
                return;
            }

            float t = -c / b;
            extCubicBounds(start, control1, control2, end, t, otherVal, xVal, out);
            return;
        }

        float b2ac = b * b - 4 * c * a;
        if(b2ac < 0){
            return;
        }

        float sqrtb2ac = (float)Math.sqrt(b2ac);
        float a2 = 2 * a;
        float t1 = (-b + sqrtb2ac) / a2;
        extCubicBounds(start, control1, control2, end, t1, otherVal, xVal, out);

        float t2 = (-b - sqrtb2ac) / a2;
        extCubicBounds(start, control1, control2, end, t2, otherVal, xVal, out);
    }

    private static boolean containsRange(float control1, float control2, float otherVal, boolean xVal, BoundingBox out){
        if(xVal){
            return contains(control1, otherVal, out) && contains(control2, otherVal, out);
        }else{
            return contains(otherVal, control1, out) && contains(otherVal, control2, out);
        }
    }

    private static boolean contains(float x, float y, BoundingBox out){
        return out.min.x <= x && out.max.x >= x && out.min.y <= y && out.max.y >= y;
    }

    private static void extCubicBounds(float start, float control1, float control2, float end, float t, float otherVal, boolean xVal, BoundingBox out){
        if(0 >= t || t >= 1){
            return;
        }

        float tInv = 1 - t;
        float val = (tInv * tInv * tInv * start) + (3 * tInv * tInv * t * control1) + (3 * tInv * t * t * control2) + (t * t * t * end);

        if(xVal){
            out.ext(val, otherVal, 0);
        }else{
            out.ext(otherVal, val, 0);
        }
    }

    private static boolean checkTesselationTolerance(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
        float dx = x4 - x1;
        float dy = y4 - y1;
        float d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
        float d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));

        return ((d2 + d3) * (d2 + d3) < 0.25f * (dx * dx + dy * dy));
    }

    public Path moveToRel(float x, float y){
        return moveTo(x + lastX, y + lastY);
    }

    public Path moveTo(float x, float y){
        commands.addAll(moveTo, x, y);
        lastX = x;
        lastY = y;
        lastStartX = x;
        lastStartY = y;
        lastKnotX = x;
        lastKnotY = y;
        return this;
    }

    public Path verticalMoveTo(float y){
        return moveTo(lastX, y);
    }

    public Path verticalMoveToRel(float y){
        return moveTo(lastX, y + lastY);
    }

    public Path horizontalMoveTo(float x){
        return moveTo(x, lastY);
    }

    public Path horizontalMoveToRel(float x){
        return moveTo(x + lastX, lastY);
    }

    public Path lineToRel(float x, float y){
        return lineTo(x + lastX, y + lastY);
    }

    public Path lineTo(float x, float y){
        commands.addAll(lineTo, x, y);
        lastX = x;
        lastY = y;
        lastKnotX = x;
        lastKnotY = y;
        return this;
    }

    public Path verticalLineTo(float y){
        return lineTo(lastX, y);
    }

    public Path verticalLineToRel(float y){
        return lineTo(lastX, y + lastY);
    }

    public Path horizontalLineTo(float x){
        return lineTo(x, lastY);
    }

    public Path horizontalLineToRel(float x){
        return lineTo(x + lastX, lastY);
    }

    public Path cubicToRel(float controlX1, float controlY1, float controlX2, float controlY2, float x, float y){
        return cubicTo(controlX1 + lastX, controlY1 + lastY, controlX2 + lastX, controlY2 + lastY, x + lastX, y + lastY);
    }

    public Path cubicTo(float controlX1, float controlY1, float controlX2, float controlY2, float x, float y){
        //TODO broken when filling.
        commands.addAll(cubicTo, controlX1, controlY1, controlX2, controlY2, x, y);
        lastX = x;
        lastY = y;
        lastKnotX = controlX2;
        lastKnotY = controlY2;
        return this;
    }

    public Path cubicSmoothTo(float controlX2, float controlY2, float x, float y){
        float controlX1 = lastX * 2f - lastKnotX;
        float controlY1 = lastY * 2f - lastKnotY;
        return cubicTo(controlX1, controlY1, controlX2, controlY2, x, y);
    }

    public Path cubicSmoothToRel(float controlX2, float controlY2, float x, float y){
        float controlX1 = lastX * 2f - lastKnotX;
        float controlY1 = lastY * 2f - lastKnotY;
        return cubicTo(controlX1, controlY1, controlX2 + lastX, controlY2 + lastY, x + lastX, y + lastY);
    }

    public Path quadToRel(float controlX, float controlY, float x, float y){
        return quadTo(controlX + lastX, controlY + lastY, x + lastX, y + lastY);
    }

    public Path quadTo(float controlX, float controlY, float x, float y){
        commands.addAll(cubicTo,
        lastX + 2.0f / 3.0f * (controlX - lastX),
        lastY + 2.0f / 3.0f * (controlY - lastY),
        x + 2.0f / 3.0f * (controlX - x),
        y + 2.0f / 3.0f * (controlY - y),
        x,
        y);
        lastX = x;
        lastY = y;
        lastKnotX = controlX;
        lastKnotY = controlY;
        return this;
    }

    public Path quadSmoothTo(float x, float y){
        float controlX = lastX * 2f - lastKnotX;
        float controlY = lastY * 2f - lastKnotY;
        return quadTo(controlX, controlY, x, y);
    }

    public Path quadSmoothToRel(float x, float y){
        float controlX = lastX * 2f - lastKnotX;
        float controlY = lastY * 2f - lastKnotY;
        return quadTo(controlX, controlY, x + lastX, y + lastY);
    }

    public Path arcToRad(float radiusX, float radiusY, float angleRadians, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        return arcTo(radiusX, radiusY, Mathf.radiansToDegrees * angleRadians, largeArcFlag, sweepFlag, x, y);
    }

    public Path arcToRadRel(float radiusX, float radiusY, float angleRadians, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        return arcTo(radiusX, radiusY, Mathf.radiansToDegrees * angleRadians, largeArcFlag, sweepFlag, x + lastX, y + lastY);
    }

    public Path arcToRel(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        return arcTo(radiusX, radiusY, angleDegrees, largeArcFlag, sweepFlag, x + lastX, y + lastY);
    }

    //TODO copied from batik
    public Path arcTo(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag, boolean sweepFlag, float x, float y){
        if(lastX == x && lastY == y){
            return this;
        }

        // Handle degenerate case
        if(radiusX == 0 || radiusY == 0){
            lineTo(x, y);
            return this;
        }

        // Sign of the radii is ignored
        radiusX = Math.abs(radiusX);
        radiusY = Math.abs(radiusY);

        // Convert angle from degrees to radians
        float angleRad = Mathf.degreesToRadians * (angleDegrees % 360.0f);
        float cosAngle = Mathf.cos(angleRad);
        float sinAngle = Mathf.sin(angleRad);

        // We simplify the calculations by transforming the arc so that the origin is at the
        // midpoint calculated above followed by a rotation to line up the coordinate axes
        // with the axes of the ellipse.

        // Compute the midpoint of the line between the current and the end point
        float dx2 = (lastX - x) / 2.0f;
        float dy2 = (lastY - y) / 2.0f;

        // Step 1 : Compute (x1', y1') - the transformed start point
        float x1 = (cosAngle * dx2 + sinAngle * dy2);
        float y1 = (-sinAngle * dx2 + cosAngle * dy2);

        float rx_sq = radiusX * radiusX;
        float ry_sq = radiusY * radiusY;
        float x1_sq = x1 * x1;
        float y1_sq = y1 * y1;

        // Check that radii are large enough. If they are not, scale them up so they are.
        float radiiCheck = x1_sq / rx_sq + y1_sq / ry_sq;
        if(radiiCheck > 1){
            radiusX = (float)Math.sqrt(radiiCheck) * radiusX;
            radiusY = (float)Math.sqrt(radiiCheck) * radiusY;
            rx_sq = radiusX * radiusX;
            ry_sq = radiusY * radiusY;
        }

        // Step 2 : Compute (cx1, cy1) - the transformed centre point
        float sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        float sq = ((rx_sq * ry_sq) - (rx_sq * y1_sq) - (ry_sq * x1_sq)) / ((rx_sq * y1_sq) + (ry_sq * x1_sq));
        sq = (sq < 0) ? 0 : sq;
        float coef = (sign * (float)Math.sqrt(sq));
        float cx1 = coef * ((radiusX * y1) / radiusY);
        float cy1 = coef * -((radiusY * x1) / radiusX);

        // Step 3 : Compute (cx, cy) from (cx1, cy1)
        float sx2 = (lastX + x) / 2.0f;
        float sy2 = (lastY + y) / 2.0f;
        float cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        float cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        float ux = (x1 - cx1) / radiusX;
        float uy = (y1 - cy1) / radiusY;
        float vx = (-x1 - cx1) / radiusX;
        float vy = (-y1 - cy1) / radiusY;

        // Compute the angle start
        float n = (float)Math.sqrt((ux * ux) + (uy * uy));
        float p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1.0f : 1.0f;
        float angleStart = Mathf.radiansToDegrees * (sign * (float)Math.acos(p / n));

        // Compute the angle extent
        n = (float)Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1.0f : 1.0f;
        float angleExtent = Mathf.radiansToDegrees * (sign * (float)Math.acos(p / n));
        if(!sweepFlag && angleExtent > 0){
            angleExtent -= 360f;
        }else if(sweepFlag && angleExtent < 0){
            angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;

        // Calculate a transformation matrix that will move and scale these  bezier points to the correct location.
        tempMatrix.setToTranslation(cx, cy).preRotate(angleDegrees).preScale(radiusX, radiusY);

        int numSegments = (int)Math.ceil(Math.abs(angleExtent) / 90.0);

        angleStart = Mathf.degreesToRadians * angleStart;
        angleExtent = Mathf.degreesToRadians * angleExtent;
        float angleIncrement = (angleExtent / numSegments);

        // The length of each control point vector is given by the following formula.
        float controlLength = 4.0f / 3.0f * Mathf.sin(angleIncrement / 2.0f) / (1.0f + Mathf.cos(angleIncrement / 2.0f));

        float control1x, control1y, control2x, control2y;
        for(int i = 0; i < numSegments; i++){
            float angle = angleStart + i * angleIncrement;
            // Calculate the control vector at this angle
            float dx = Mathf.cos(angle);
            float dy = Mathf.sin(angle);

            // First control point
            tempPoint.set((dx - controlLength * dy), (dy + controlLength * dx));
            tempMatrix.transform(tempPoint);
            control1x = tempPoint.x;
            control1y = tempPoint.y;

            // Second control point
            angle += angleIncrement;
            dx = Mathf.cos(angle);
            dy = Mathf.sin(angle);
            tempPoint.set((dx + controlLength * dy), (dy - controlLength * dx));
            tempMatrix.transform(tempPoint);
            control2x = tempPoint.x;
            control2y = tempPoint.y;

            // Endpoint of bezier
            // The last point in the bezier set should match exactly the last coord
            // pair in the arc (ie: x,y). But
            // considering all the mathematical manipulation we have been doing, it
            // is bound to be off by a tiny
            // fraction. Experiments show that it can be up to around 0.00002. So
            // why don't we just set it to
            // exactly what it ought to be.
            if(i < numSegments - 1){
                tempPoint.set(dx, dy);
                tempMatrix.transform(tempPoint);
                cubicTo(control1x, control1y, control2x, control2y, tempPoint.x, tempPoint.y);
            }else{
                cubicTo(control1x, control1y, control2x, control2y, x, y);
            }
        }

        return this;
    }

    public Path arcToRel(float startX, float startY, float endX, float endY, float radius){
        return arcTo(startX + lastX, startY + lastY, endX + lastX, endY + lastY, radius);
    }

    public Path arcTo(float startX, float startY, float endX, float endY, float radius){
        float x0 = lastX;
        float y0 = lastY;
        float dx0, dy0, dx1, dy1, a, d, cx, cy, a0, a1;

        // Handle degenerate cases. TODO remove
        if(canConvertArcToLine(startX, startY, endX, endY, radius, x0, y0)){
            return lineTo(startX, startY);
        }

        // Calculate tangential circle to lines (x0,y0)-(x1,y1) and (x1,y1)-(x2,y2).
        dx0 = x0 - startX;
        dy0 = y0 - startY;
        dx1 = endX - startX;
        dy1 = endY - startY;
        normalize(tempPoint.set(dx0, dy0));
        dx0 = tempPoint.x;
        dy0 = tempPoint.y;
        normalize(tempPoint.set(dx1, dy1));
        dx1 = tempPoint.x;
        dy1 = tempPoint.y;
        a = CanvasUtils.acosf(dx0 * dx1 + dy0 * dy1);
        d = radius / CanvasUtils.tanf(a / 2.0f);


        if(d > 10000.0f){
            return lineTo(startX, startY);
        }

        if(CanvasUtils.cross(dx0, dy0, dx1, dy1) > 0.0f){
            cx = startX + dx0 * d + dy0 * radius;
            cy = startY + dy0 * d + -dx0 * radius;
            a0 = Mathf.atan2(dx0, -dy0);
            a1 = Mathf.atan2(-dx1, dy1);
            return arc(cx, cy, radius, a0, a1, Direction.clockwise);
        }else{
            cx = startX + dx0 * d + -dy0 * radius;
            cy = startY + dy0 * d + dx0 * radius;
            a0 = Mathf.atan2(-dx0, dy0);
            a1 = Mathf.atan2(dx1, -dy1);
            return arc(cx, cy, radius, a0, a1, Direction.counterClockwise);
        }
    }

    public Path arcRel(float centerX, float centerY, float radius, float startAngle, float endAngle, Direction direction){
        return arc(centerX + lastX, centerY + lastY, radius, radius, startAngle, endAngle, direction);
    }

    public Path arc(float centerX, float centerY, float radius, float startAngle, float endAngle, Direction direction){
        return arc(centerX, centerY, radius, radius, startAngle, endAngle, direction);
    }

    public Path arcRel(float centerX, float centerY, float radiusX, float radiusY, float startAngle, float endAngle, Direction direction){
        return arc(centerX + lastX, centerY + lastY, radiusX, radiusY, startAngle, endAngle, direction);
    }

    public Path arc(float centerX, float centerY, float radiusX, float radiusY, float startAngle, float endAngle, Direction direction){
        float a = 0;
        float dx = 0, dy = 0, x = 0, y = 0, tanx = 0, tany = 0;
        float px = 0, py = 0, ptanx = 0, ptany = 0;

        float da = clampAngles(startAngle, endAngle, direction);

        // Split arc into max 90 degree segments.
        int ndivs = Math.max(1, Math.min((int)(Math.abs(da) / (Mathf.PI * 0.5f) + 0.5f), 5));
        float hda = (da / ndivs) / 2.0f;
        float kappa = Math.abs(4.0f / 3.0f * (1.0f - Mathf.cos(hda)) / Mathf.sin(hda));

        if(direction == Direction.counterClockwise){
            kappa = -kappa;
        }

        for(int i = 0; i <= ndivs; i++){
            a = startAngle + da * (i / (float)ndivs);
            dx = Mathf.cos(a);
            dy = Mathf.sin(a);
            x = centerX + dx * radiusX;
            y = centerY + dy * radiusY;
            tanx = -dy * radiusX * kappa;
            tany = dx * radiusY * kappa;

            if(i == 0){
                if(commands.size > 0){
                    lineTo(x, y);
                }else{
                    moveTo(x, y);
                }
            }else{
                cubicTo(px + ptanx, py + ptany, x - tanx, y - tany, x, y);
            }

            px = x;
            py = y;
            ptanx = tanx;
            ptany = tany;
        }

        return this;
    }

    public Path close(){
        commands.add(close);
        lastX = lastStartX;
        lastY = lastStartY;
        lastKnotX = lastStartX;
        lastKnotY = lastStartY;
        return this;
    }

    public Path winding(Winding wnd){
        commands.addAll(winding, wnd == null ? Winding.counterClockwise.ordinal() : wnd.ordinal());
        return this;
    }

    public Path rect(float left, float top, float width, float height){
        commands.addAll(
        moveTo, left, top,
        lineTo, left, top + height,
        lineTo, left + width, top + height,
        lineTo, left + width, top,
        close);

        lastX = 0;
        lastY = 0;
        lastKnotX = 0;
        lastKnotY = 0;
        return this;
    }

    public Path roundedRect(float left, float top, float width, float height, float radius){
        return roundedRect(left, top, width, height, radius, radius);
    }

    public Path roundedRect(float left, float top, float width, float height, float radiusX, float radiusY){
        if(radiusX < 0.1f && radiusY < 0.1f){
            return rect(left, top, width, height);
        }else{
            float radiusX1 = Math.min(radiusX, Math.abs(width) * 0.5f) * CanvasUtils.sign(width);
            float radiusY1 = Math.min(radiusY, Math.abs(height) * 0.5f) * CanvasUtils.sign(height);

            commands.addAll(
            moveTo, left, top + radiusY1,
            lineTo, left, top + height - radiusY1,
            cubicTo, left, top + height - radiusY1 * oneMinusKappa90, left + radiusX1 * oneMinusKappa90, top + height, left + radiusX1, top + height,
            lineTo, left + width - radiusX1, top + height,
            cubicTo, left + width - radiusX1 * oneMinusKappa90, top + height, left + width, top + height - radiusY1 * oneMinusKappa90, left + width, top + height - radiusY1,
            lineTo, left + width, top + radiusY1,
            cubicTo, left + width, top + radiusY1 * oneMinusKappa90, left + width - radiusX1 * oneMinusKappa90, top, left + width - radiusX1, top,
            lineTo, left + radiusX1, top,
            cubicTo, left + radiusX1 * oneMinusKappa90, top, left, top + radiusY1 * oneMinusKappa90, left, top + radiusY1,
            close);

            lastX = 0;
            lastY = 0;
            lastKnotX = 0;
            lastKnotY = 0;
            return this;
        }
    }

    public Path circle(float centerX, float centerY, float radius){
        return ellipse(centerX, centerY, radius, radius);
    }

    public Path ellipse(float centerX, float centerY, float radiusX, float radiusY){
        commands.addAll(
        moveTo, centerX - radiusX, centerY,
        cubicTo, centerX - radiusX, centerY + radiusY * kappa90, centerX - radiusX * kappa90, centerY + radiusY, centerX, centerY + radiusY,
        cubicTo, centerX + radiusX * kappa90, centerY + radiusY, centerX + radiusX, centerY + radiusY * kappa90, centerX + radiusX, centerY,
        cubicTo, centerX + radiusX, centerY - radiusY * kappa90, centerX + radiusX * kappa90, centerY - radiusY, centerX, centerY - radiusY,
        cubicTo, centerX - radiusX * kappa90, centerY - radiusY, centerX - radiusX, centerY - radiusY * kappa90, centerX - radiusX, centerY,
        close);

        lastX = 0;
        lastY = 0;
        lastKnotX = 0;
        lastKnotY = 0;
        return this;
    }

    public Path line(float startX, float startY, float stopX, float stopY){
        moveTo(startX, startY).lineTo(stopX, stopY);
        return this;
    }

    public Path lines(float... points){
        return lines(points, 0, points.length / 4);
    }

    public Path lines(float[] points, int offset, int count){
        int i = offset;
        while(i <= offset + (count * 4) - 1){
            moveTo(points[i++], points[i++]).lineTo(points[i++], points[i++]);
        }
        return this;
    }

    public Path lines(FloatArray points){
        return lines(points, 0, points.size / 4);
    }

    public Path lines(FloatArray points, int offset, int count){
        int i = offset;
        while(i <= offset + (count * 4) - 1){
            moveTo(points.get(i++), points.get(i++)).lineTo(points.get(i++), points.get(i++));
        }
        return this;
    }

    public Path lines(Vec2... points){
        return lines(points, 0, points.length / 2);
    }

    public Path lines(Vec2[] points, int offset, int count){
        int i = offset;
        while(i <= offset + (count * 2) - 1){
            Vec2 start = points[i++];
            Vec2 end = points[i++];
            moveTo(start.x, start.y).lineTo(end.x, end.y);
        }
        return this;
    }

    public Path polyline(float... points){
        if(points.length < 4){
            return this;
        }

        moveTo(points[0], points[1]);
        for(int i = 2; i < points.length; i += 2){
            lineTo(points[i], points[i + 1]);
        }
        return this;
    }

    public Path polyline(FloatArray points){
        if(points.size < 4){
            return this;
        }

        moveTo(points.get(0), points.get(1));
        for(int i = 2; i < points.size; i += 2){
            lineTo(points.get(i), points.get(i + 1));
        }
        return this;
    }

    public Path polyline(Vec2... points){
        if(points.length < 2){
            return this;
        }

        moveTo(points[0].x, points[0].y);
        for(int i = 1; i < points.length; i++){
            lineTo(points[i].x, points[i].y);
        }
        return this;
    }

    public Path polygon(float... points){
        if(points.length < 4){
            return this;
        }

        int i = 0;
        moveTo(points[i++], points[i++]);
        while(i < points.length){
            lineTo(points[i++], points[i++]);
        }
        close();
        return this;
    }

    public Path polygon(FloatArray points){
        if(points.size < 4){
            return this;
        }

        int i = 0;
        moveTo(points.get(i++), points.get(i++));
        while(i < points.size){
            lineTo(points.get(i++), points.get(i++));
        }
        close();
        return this;
    }

    public Path polygon(Vec2... points){
        if(points.length < 2){
            return this;
        }

        int i = 0;
        moveTo(points[i].x, points[i].y);
        i++;

        while(i < points.length){
            lineTo(points[i].x, points[i].y);
            i++;
        }
        close();
        return this;
    }

    public Path set(FloatArray commands){
        this.commands.clear();
        this.commands.addAll(commands);
        return this;
    }

    public Path set(Path other){
        commands.clear();
        commands.addAll(other.commands);
        return this;
    }

    public Path set(Path other, AffineTransform xform){
        commands.clear();
        other.getTansformedCommands(commands, xform);
        return this;
    }

    public Path append(Path other){
        commands.addAll(other.commands);
        return this;
    }

    public Path append(Path other, AffineTransform xform){
        other.getTansformedCommands(commands, xform);
        return this;
    }

    public FloatArray getCommands(FloatArray out){
        out.addAll(commands);
        return out;
    }

    public Path tansform(AffineTransform xform){
        tempCommands.clear();
        getTansformedCommands(tempCommands, xform);
        commands.clear();
        commands.addAll(tempCommands);
        return this;
    }

    public Path reverse(){
        normalize();
        tempCommands.clear();

        int segmentStart = 0;
        int segmentCommandsCount = 0;
        FloatArray segmentCommands = new FloatArray();

        int i = 0;
        while(i < commands.size){
            int cmd = (int)commands.get(i);

            switch(cmd){
                case moveTo:
                    if(segmentCommandsCount > 0){
                        reverseSegment(segmentStart, segmentCommands);
                        segmentStart = i;
                        segmentCommands.clear();
                        segmentCommandsCount = 0;

                    }
                    segmentCommands.add(moveTo);
                    tempCommands.add(commands.get(i + 1));
                    tempCommands.add(commands.get(i + 2));
                    i += 3;
                    break;
                case lineTo:
                    segmentCommands.add(lineTo);
                    tempCommands.add(commands.get(i + 1));
                    tempCommands.add(commands.get(i + 2));
                    segmentCommandsCount++;
                    i += 3;
                    break;
                case cubicTo:
                    segmentCommands.add(cubicTo);
                    tempCommands.add(commands.get(i + 1));
                    tempCommands.add(commands.get(i + 2));
                    tempCommands.add(commands.get(i + 3));
                    tempCommands.add(commands.get(i + 4));
                    tempCommands.add(commands.get(i + 5));
                    tempCommands.add(commands.get(i + 6));
                    segmentCommandsCount++;
                    i += 7;
                    break;
                case close:
                    segmentCommands.add(close);
                    i++;
                    break;
                case winding:
                    segmentCommands.add(winding);
                    tempCommands.add(commands.get(i + 1));
                    i += 2;
                    break;
                default:
                    i++;
            }
        }
        reverseSegment(segmentStart, segmentCommands);
        return this;
    }

    private void normalize(){
        tempCommands.clear();

        int segmentStart = 0;
        float lastStartX = 0;
        float lastStartY = 0;
        int segmentCommandsCount = 0;
        int segmentWinding = Winding.none.ordinal();
        boolean segmentClosed = false;

        int i = 0;
        while(i < commands.size){
            int cmd = (int)commands.get(i);
            switch(cmd){
                case moveTo:
                    if(segmentCommandsCount != 0){
                        tempCommands.insert(segmentStart, moveTo);
                        tempCommands.insert(segmentStart + 1, lastStartX);
                        tempCommands.insert(segmentStart + 2, lastStartY);

                        if(segmentWinding != Winding.none.ordinal()){
                            tempCommands.add(winding);
                            tempCommands.add(segmentWinding);
                        }

                        if(segmentClosed){
                            tempCommands.add(close);
                        }

                        segmentStart = tempCommands.size;
                        segmentCommandsCount = 0;
                        segmentWinding = Winding.none.ordinal();
                        segmentClosed = false;
                    }

                    lastStartX = commands.get(i + 1);
                    lastStartY = commands.get(i + 2);
                    i += 3;
                    break;
                case lineTo:
                    tempCommands.addAll(commands, i, 3);
                    segmentCommandsCount++;
                    i += 3;
                    break;
                case cubicTo:
                    tempCommands.addAll(commands, i, 7);
                    segmentCommandsCount++;
                    i += 7;
                    break;
                case close:
                    segmentClosed = true;
                    i++;
                    break;
                case winding:
                    segmentWinding = (int)commands.get(i + 1);
                    i += 2;
                    break;
                default:
                    i++;
            }
        }

        tempCommands.insert(segmentStart, moveTo);
        tempCommands.insert(segmentStart + 1, lastStartX);
        tempCommands.insert(segmentStart + 2, lastStartY);

        commands.clear();
        commands.addAll(tempCommands);
    }

    private void reverseSegment(int segmentStart, FloatArray segmentCommands){
        int segment = segmentStart;
        int last = segmentCommands.size - 1;
        boolean segmentClosed = false;
        boolean segmentWinding = false;
        int lastWinding = Winding.none.ordinal();

        if(segmentCommands.get(last) == close){
            last--;
            segmentClosed = true;
        }

        if(segmentCommands.get(last) == winding){
            last--;
            segmentWinding = true;
            lastWinding = (int)tempCommands.removeIndex(tempCommands.size - 1);
        }

        tempCommands.reverse();
        int i = 0;
        commands.set(segment++, moveTo);
        commands.set(segment++, tempCommands.get(i + 1));
        commands.set(segment++, tempCommands.get(i));
        i += 2;

        while(last > 0){
            int cmd = (int)segmentCommands.get(last--);

            switch(cmd){
                case lineTo:
                    commands.set(segment++, lineTo);
                    commands.set(segment++, tempCommands.get(i + 1));
                    commands.set(segment++, tempCommands.get(i));
                    i += 2;
                    break;
                case cubicTo:
                    commands.set(segment++, cubicTo);
                    commands.set(segment++, tempCommands.get(i + 1));
                    commands.set(segment++, tempCommands.get(i));
                    commands.set(segment++, tempCommands.get(i + 3));
                    commands.set(segment++, tempCommands.get(i + 2));
                    commands.set(segment++, tempCommands.get(i + 5));
                    commands.set(segment++, tempCommands.get(i + 4));
                    i += 6;
                    break;
                default:
                    i++;
            }
        }

        if(segmentWinding){
            commands.set(segment++, winding);
            commands.set(segment++, lastWinding);
        }

        if(segmentClosed){
            commands.set(segment++, close);
        }
    }

    FloatArray getCommands(){
        return commands;
    }

    //TODO make private when canvas xform is processed by vertex shader
    FloatArray getTansformedCommands(AffineTransform xform){
        tempCommands.clear();
        return getTansformedCommands(tempCommands, xform);
    }

    public FloatArray getTansformedCommands(FloatArray out, AffineTransform xform){
        if(xform.isIdentity()){
            out.addAll(commands);
            return out;
        }

        int i = 0;
        while(i < commands.size){
            int cmd = (int)commands.get(i);
            out.add(cmd);

            switch(cmd){
                case moveTo:
                    appendTransformedPoint(i + 1, out, xform);
                    i += 3;
                    break;
                case lineTo:
                    appendTransformedPoint(i + 1, out, xform);
                    i += 3;
                    break;
                case cubicTo:
                    appendTransformedPoint(i + 1, out, xform);
                    appendTransformedPoint(i + 3, out, xform);
                    appendTransformedPoint(i + 5, out, xform);
                    i += 7;
                    break;
                case close:
                    i++;
                    break;
                case winding:
                    out.add(commands.get(i + 1));
                    i += 2;
                    break;
                default:
                    i++;
            }
        }

        return out;
    }

    private void appendTransformedPoint(int pointIndex, FloatArray transformed, AffineTransform xform){
        float sx = commands.get(pointIndex);
        float sy = commands.get(pointIndex + 1);
        float[] xformValues = xform.values;
        transformed.add(sx * xformValues[0] + sy * xformValues[2] + xformValues[4]);
        transformed.add(sx * xformValues[1] + sy * xformValues[3] + xformValues[5]);
    }

    public BoundingBox getControlBounds(BoundingBox out){
        return getControlBounds(out, commands);
    }

    public Rect getControlBounds(Rect out){
        getControlBounds(tempBounds);
        return boundsToRectangle(out);
    }

    private Rect boundsToRectangle(Rect out){
        float x = tempBounds.min.x;
        float y = tempBounds.min.y;
        return out.set(x, y, tempBounds.max.x - x, tempBounds.max.y = y);
    }

    public BoundingBox getControlBounds(BoundingBox out, AffineTransform transform){
        return getControlBounds(out, getTansformedCommands(transform));
    }

    public Rect getControlBounds(Rect out, AffineTransform transform){
        getControlBounds(tempBounds, transform);
        return boundsToRectangle(out);
    }

    public BoundingBox getControlBounds(BoundingBox out, FloatArray commands){
        int i = 0;
        float lastX = 0;
        float lastY = 0;
        float lastStartX = 0;
        float lastStartY = 0;
        boolean lastUsed = false;

        while(i < commands.size){
            int cmd = (int)commands.get(i);

            switch(cmd){
                case moveTo:
                    lastStartX = lastX = commands.get(i + 1);
                    lastStartY = lastY = commands.get(i + 2);
                    lastUsed = false;
                    i += 3;
                    break;
                case lineTo:
                    if(!lastUsed){
                        out.ext(lastX, lastY, 0);
                    }

                    lastX = commands.get(i + 1);
                    lastY = commands.get(i + 2);
                    out.ext(lastX, lastY, 0);
                    lastUsed = true;
                    i += 3;
                    break;
                case cubicTo:
                    if(!lastUsed){
                        out.ext(lastX, lastY, 0);
                    }

                    out.ext(commands.get(i + 1), commands.get(i + 2), 0);
                    out.ext(commands.get(i + 3), commands.get(i + 4), 0);
                    lastX = commands.get(i + 5);
                    lastY = commands.get(i + 6);
                    out.ext(lastX, lastY, 0);
                    lastUsed = true;
                    i += 7;
                    break;
                case close:
                    lastX = lastStartX;
                    lastY = lastStartY;
                    i++;
                    break;
                case winding:
                    i += 2;
                    break;
                default:
                    i++;
            }
        }

        return out;
    }

    public BoundingBox getBounds(BoundingBox out){
        return getBounds(out, commands);
    }

    public Rect getBounds(Rect out){
        getBounds(tempBounds);
        return boundsToRectangle(out);
    }

    public BoundingBox getBounds(BoundingBox out, AffineTransform transform){
        return getBounds(out, getTansformedCommands(transform));
    }

    public Rect getBounds(Rect out, AffineTransform transform){
        getBounds(tempBounds, transform);
        return boundsToRectangle(out);
    }

    public BoundingBox getBounds(BoundingBox out, FloatArray commands){
        int i = 0;
        float lastX = 0;
        float lastY = 0;
        float lastStartX = 0;
        float lastStartY = 0;
        boolean lastUsed = false;

        while(i < commands.size){
            int cmd = (int)commands.get(i);

            switch(cmd){
                case moveTo:
                    lastStartX = lastX = commands.get(i + 1);
                    lastStartY = lastY = commands.get(i + 2);
                    lastUsed = false;
                    i += 3;
                    break;
                case lineTo:
                    if(!lastUsed){
                        out.ext(lastX, lastY, 0);
                    }

                    lastX = commands.get(i + 1);
                    lastY = commands.get(i + 2);
                    out.ext(lastX, lastY, 0);
                    lastUsed = true;
                    i += 3;
                    break;
                case cubicTo:
                    if(!lastUsed){
                        out.ext(lastX, lastY, 0);
                    }

                    float endX = commands.get(i + 5);
                    extCubicBounds(lastX, commands.get(i + 1), commands.get(i + 3), endX, lastY, true, out);

                    float endY = commands.get(i + 6);
                    extCubicBounds(lastY, commands.get(i + 2), commands.get(i + 4), endY, lastX, false, out);

                    out.ext(endX, endY, 0);

                    lastX = endX;
                    lastY = endY;
                    lastUsed = true;
                    i += 7;
                    break;
                case close:
                    lastX = lastStartX;
                    lastY = lastStartY;
                    i++;
                    break;
                case winding:
                    i += 2;
                    break;
                default:
                    i++;
            }
        }

        return out;
    }

    public float getArea(){
        tempCommands.clear();
        float area = 0;
        int segmentStart = 0;
        float lastStartX = 0;
        float lastStartY = 0;
        float lastX = 0;
        float lastY = 0;
        int segmentCommandsCount = 0;

        int i = 0;
        while(i < commands.size){
            int cmd = (int)commands.get(i);
            switch(cmd){
                case moveTo:
                    if(segmentCommandsCount != 0){
                        tempCommands.insert(segmentStart, lastStartX);
                        tempCommands.insert(segmentStart + 1, lastStartY);
                        tempCommands.add(lastStartX);
                        tempCommands.add(lastStartY);
                        area += getSegmentArea();

                        tempCommands.clear();
                        segmentStart = tempCommands.size;
                        segmentCommandsCount = 0;
                    }

                    lastStartX = commands.get(i + 1);
                    lastStartY = commands.get(i + 2);

                    lastX = lastStartX;
                    lastY = lastStartY;

                    i += 3;
                    break;
                case lineTo:
                    lastX = commands.get(i + 1);
                    lastY = commands.get(i + 2);

                    tempCommands.add(lastX);
                    tempCommands.add(lastY);
                    segmentCommandsCount++;
                    i += 3;
                    break;
                case cubicTo:
                    float x1 = lastX;
                    float y1 = lastY;
                    float x2 = commands.get(i + 1);
                    float y2 = commands.get(i + 2);
                    float x3 = commands.get(i + 3);
                    float y3 = commands.get(i + 4);
                    float x4 = commands.get(i + 5);
                    float y4 = commands.get(i + 6);
                    tesselateBezier(x1, y1, x2, y2, x3, y3, x4, y4, 0);
                    segmentCommandsCount++;
                    i += 7;
                    break;
                case close:
                    i++;
                    break;
                case winding:
                    i += 2;
                    break;
                default:
                    i++;
            }
        }

        tempCommands.insert(segmentStart, lastStartX);
        tempCommands.insert(segmentStart + 1, lastStartY);
        tempCommands.add(lastStartX);
        tempCommands.add(lastStartY);
        area += getSegmentArea();

        return area;
    }

    private void tesselateBezier(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int level){
        if(level > 8){
            return;
        }else if(level == 8 || checkTesselationTolerance(x1, y1, x2, y2, x3, y3, x4, y4)){
            tempCommands.add(x4);
            tempCommands.add(y4);
            return;
        }

        float x12 = (x1 + x2) * 0.5f;
        float y12 = (y1 + y2) * 0.5f;
        float x23 = (x2 + x3) * 0.5f;
        float y23 = (y2 + y3) * 0.5f;
        float x34 = (x3 + x4) * 0.5f;
        float y34 = (y3 + y4) * 0.5f;
        float x123 = (x12 + x23) * 0.5f;
        float y123 = (y12 + y23) * 0.5f;
        float x234 = (x23 + x34) * 0.5f;
        float y234 = (y23 + y34) * 0.5f;
        float x1234 = (x123 + x234) * 0.5f;
        float y1234 = (y123 + y234) * 0.5f;

        tesselateBezier(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1);
        tesselateBezier(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1);
    }

    private float getSegmentArea(){
        float area = 0;
        int size = tempCommands.size;
        for(int i = 0; i < size; i += 2){
            int y1 = i + 1;
            int x2 = (i + 2) % size;
            int y2 = (i + 3) % size;
            area += tempCommands.get(i) * tempCommands.get(y2);
            area -= tempCommands.get(x2) * tempCommands.get(y1);
        }
        area *= 0.5f;
        return area;
    }

    public Path unmodifiable(){
        return new UnmodifiablePath(commands);
    }

    public boolean isEmpty(){
        return commands.size == 0;
    }

    @Override
    public void reset(){
        commands.clear();
        tempCommands.clear();
        lastX = 0;
        lastY = 0;
        lastStartX = 0;
        lastStartY = 0;
        lastKnotX = 0;
        lastKnotY = 0;
    }

    public void free(){
        Pools.free(this);
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        Path other = (Path)obj;
        return commands.equals(other.commands);
    }

    public boolean equals(Path other){
        if(this == other){
            return true;
        }
        if(other == null){
            return false;
        }
        return commands.equals(other.commands);
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        for(int i = 0, n = commands.size; i < n; i++){
            result = prime * result + Float.floatToIntBits(commands.get(i));
        }
        return result;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("Commands:\n");

        int i = 0;
        int size = commands.size;
        while(i < size){
            int cmd = (int)commands.get(i);

            switch(cmd){
                case moveTo:
                    builder.append("moveTo: ");
                    builder.append(commands.get(i + 1));
                    builder.append(", ");
                    builder.append(commands.get(i + 2));
                    builder.append("\n");
                    i += 3;
                    break;
                case lineTo:
                    builder.append("lineTo: ");
                    builder.append(commands.get(i + 1));
                    builder.append(", ");
                    builder.append(commands.get(i + 2));
                    builder.append("\n");
                    i += 3;
                    break;
                case cubicTo:
                    builder.append("cubicTo: ");
                    builder.append(commands.get(i + 1));
                    builder.append(", ");
                    builder.append(commands.get(i + 2));
                    builder.append(",");
                    builder.append(commands.get(i + 3));
                    builder.append(", ");
                    builder.append(commands.get(i + 4));
                    builder.append(", ");
                    builder.append(commands.get(i + 5));
                    builder.append(", ");
                    builder.append(commands.get(i + 6));
                    builder.append("\n");
                    i += 7;
                    break;
                case close:
                    builder.append("close\n");
                    i++;
                    break;
                case winding:
                    builder.append("winding: ");
                    builder.append(commands.get(i + 1));
                    builder.append("\n");
                    i += 2;
                    break;
                default:
                    i++;
            }
        }

        return builder.toString();
    }

    //TODO
    public interface PathVisitor{
        void moveTo(float x, float y);

        void lineTo(float x, float y);

        void cubicTo(float controlX1, float controlY1, float controlX2, float controlY2, float x, float y);

        void close();
    }

    public static class UnmodifiablePath extends Path{
        public UnmodifiablePath(){
        }

        public UnmodifiablePath(FloatArray commands){
            super.set(commands);
        }

        @Override
        public Path moveToRel(float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path moveTo(float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path verticalMoveTo(float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path verticalMoveToRel(float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path horizontalMoveTo(float x){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path horizontalMoveToRel(float x){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path lineToRel(float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path lineTo(float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path verticalLineTo(float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path verticalLineToRel(float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path horizontalLineTo(float x){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path horizontalLineToRel(float x){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path cubicToRel(float controlX1, float controlY1, float controlX2, float controlY2, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path cubicTo(float controlX1, float controlY1, float controlX2, float controlY2, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path cubicSmoothTo(float controlX2, float controlY2, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path cubicSmoothToRel(float controlX2, float controlY2, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path quadToRel(float controlX, float controlY, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path quadTo(float controlX, float controlY, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path quadSmoothTo(float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path quadSmoothToRel(float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arcToRad(float radiusX, float radiusY, float angleRadians, boolean largeArcFlag, boolean sweepFlag, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arcToRadRel(float radiusX, float radiusY, float angleRadians, boolean largeArcFlag, boolean sweepFlag, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arcToRel(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag, boolean sweepFlag, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arcTo(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag, boolean sweepFlag, float x, float y){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arcToRel(float startX, float startY, float endX, float endY, float radius){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arcTo(float startX, float startY, float endX, float endY, float radius){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arcRel(float centerX, float centerY, float radius, float startAngle, float endAngle, Direction direction){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arc(float centerX, float centerY, float radius, float startAngle, float endAngle, Direction direction){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arcRel(float centerX, float centerY, float radiusX, float radiusY, float startAngle, float endAngle, Direction direction){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path arc(float centerX, float centerY, float radiusX, float radiusY, float startAngle, float endAngle, Direction direction){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path close(){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path winding(Winding wnd){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path rect(float left, float top, float width, float height){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path roundedRect(float left, float top, float width, float height, float radius){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path roundedRect(float left, float top, float width, float height, float radiusX, float radiusY){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path circle(float centerX, float centerY, float radius){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path ellipse(float centerX, float centerY, float radiusX, float radiusY){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path set(FloatArray commands){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path set(Path other){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path set(Path other, AffineTransform xform){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path append(Path other){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path append(Path other, AffineTransform xform){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path tansform(AffineTransform xform){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path reverse(){
            throw new UnsupportedOperationException();
        }

        @Override
        public void reset(){
            throw new UnsupportedOperationException();
        }

        @Override
        public void free(){
            throw new UnsupportedOperationException();
        }

        @Override
        public Path unmodifiable(){
            return this;
        }

        @Override
        public int hashCode(){
            return super.hashCode() + 31;
        }
    }
}
