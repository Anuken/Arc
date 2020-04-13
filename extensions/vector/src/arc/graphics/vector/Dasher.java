package arc.graphics.vector;

import arc.struct.*;
import arc.util.pooling.Pool.*;

class Dasher implements Poolable{
    private final Array<Point> points = new Array<>();
    private final Array<PathComponent> dashedComponents = new Array<>();
    private final Array<Point> unusedPoints = new Array<>();
    int pointIndex;
    Point p0;
    Point p1;
    DashElement activeElement;
    private float length;
    private DashElement first = new DashElement();
    private Array<DashElement> dashElements = new Array<>();
    private PathMesh pathMesh;

    void init(FloatArray dashes, float offset){
        initCommonElements(dashes);
        initFirstElement(offset);
    }

    private void initCommonElements(FloatArray dashes){
        boolean visible = true;
        for(int i = 0; i < dashes.size; i++){
            float dashLength = dashes.get(i);
            DashElement element = new DashElement();
            element.visible = visible;
            element.start = length;
            length += dashLength;
            element.end = length;
            element.index = i;
            dashElements.add(element);
            visible = !visible;
        }

        if(dashes.size % 2 != 0){
            for(int i = 0; i < dashes.size; i++){
                float dashLength = dashes.get(i);
                DashElement element = new DashElement();
                element.visible = visible;
                element.start = length;
                length += dashLength;
                element.end = length;
                element.index = dashes.size + i;
                dashElements.add(element);
                visible = !visible;
            }
        }
    }

    private void initFirstElement(float offset){
        float adjustedOffset = adjustOffset(offset);
        DashElement offsetedDashElement = getElement(adjustedOffset);
        first.index = offsetedDashElement.index;
        first.visible = offsetedDashElement.visible;
        first.start = adjustedOffset;
        first.end = offsetedDashElement.end;
    }

    private float adjustOffset(float offset){
        if(offset < 0){
            return length + offset % length;
        }else{
            return offset % length;
        }
    }

    private DashElement getElement(float offset){
        for(int i = 0; i < dashElements.size; i++){
            DashElement element = dashElements.get(i);
            if(element.start <= offset && element.end > offset){
                return element;
            }
        }

        throw new IllegalStateException();
    }

    private DashElement nextElement(){
        return dashElements.get((activeElement.index + 1) % dashElements.size);
    }

    void appendDashedStrokeComponents(Array<PathComponent> out, PathComponent pathComponent){
        if(pathComponent.points.size < 2){
            return;
        }

        this.pathMesh = pathComponent.pathMesh;
        clonePoints(pathComponent);

        activeElement = first;
        p0 = points.get(pointIndex++);
        p1 = points.get(pointIndex++);

        while(activeElement != null){
            if(activeElement.visible){
                dash();
            }else{
                gap();
            }
        }

        out.addAll(dashedComponents);
        clear();
    }

    private void clonePoints(PathComponent pathComponent){
        Array<Point> componentPoints = pathComponent.points;
        for(int i = 0; i < componentPoints.size; i++){
            appendPoint(componentPoints.get(i));
        }

        if(pathComponent.closed){
            appendPoint(componentPoints.get(0));
        }
    }

    private void appendPoint(Point point){
        Point cloned = Point.obtain(point);
        points.add(cloned);//TODO ???? points.add(point);
        unusedPoints.add(cloned);
    }

    void dash(){
        PathComponent component = PathComponent.obtain(pathMesh);
        dashedComponents.add(component);
        component.points.add(p0);
        unusedPoints.remove(p0, true);

        float dashLength = activeElement.getLength();
        float currentLength = 0;

        while(true){
            if(currentLength + p1.length <= dashLength){
                component.points.add(p1);
                unusedPoints.remove(p1, true);
                currentLength += p1.length;
                if(pointIndex < points.size){
                    p0 = p1;
                    p1 = points.get(pointIndex++);
                }else{
                    component.refine();//TODO should update path bounds like Path.refineLastComponent();
                    activeElement = null;
                    return;
                }
            }else{
                p0 = createMidPoint(p0, p1, dashLength - currentLength);
                component.points.add(p0);
                unusedPoints.remove(p0, true);
                activeElement = nextElement();
                component.refine();
                return;
            }
        }
    }

    void gap(){
        float dashLength = activeElement.getLength();
        float currentLength = 0;

        while(true){
            float length = currentLength + p1.length;
            if(length <= dashLength){
                currentLength = length;
                if(pointIndex < points.size){
                    p0 = p1;
                    p1 = points.get(pointIndex++);
                }else{
                    activeElement = null;
                    return;
                }
            }else{
                p0 = createMidPoint(p0, p1, dashLength - currentLength);
                activeElement = nextElement();
                return;
            }
        }
    }

    private Point createMidPoint(Point p0, Point p1, float newLength){
        Point point = Point.obtain();
        point.length = newLength;
        float reminder = p1.length -= newLength;
        point.x = (newLength * p1.x + reminder * p0.x) / (newLength + reminder);
        point.y = (newLength * p1.y + reminder * p0.y) / (newLength + reminder);
        unusedPoints.add(point);
        return point;
    }

    private void clear(){
        pathMesh = null;
        points.clear();
        dashedComponents.clear();

        pointIndex = 0;
        p0 = null;
        p1 = null;

        CanvasUtils.resetArray(unusedPoints);
    }

    @Override
    public void reset(){
        length = 0;
        CanvasUtils.resetArray(dashElements);
        clear();
    }

    private static class DashElement{
        boolean visible;
        float start;
        float end;
        int index;

        float getLength(){
            return end - start;
        }
    }
}