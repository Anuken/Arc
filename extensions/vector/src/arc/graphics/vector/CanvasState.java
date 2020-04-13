package arc.graphics.vector;

import arc.struct.*;

public class CanvasState{
    final FloatArray dashArray = new FloatArray();
    final Array<Clip> clips = new Array<>();
    float globalAlpha = 1.0f;
    float strokeWidth = 1.0f;
    float miterLimit = 10.0f;
    LineJoin lineJoin = LineJoin.miter;
    LineCap lineCap = LineCap.butt;
    DrawingStyle drawingStyle = DrawingStyle.fill;
    AffineTransform xform = AffineTransform.obtain();
    Scissor scissor = new Scissor();
    Paint fillPaint = new Paint();
    Paint strokePaint = new Paint();
    BlendMode blendMode = BlendMode.over;
    PointStyle pointStyle = PointStyle.circle;
    Winding winding = Winding.none;
    float dashOffset;
    Font font;// TODO default

    public CanvasState(){

    }

    void set(CanvasState other){
        globalAlpha = other.globalAlpha;
        strokeWidth = other.strokeWidth;
        miterLimit = other.miterLimit;
        lineJoin = other.lineJoin;
        lineCap = other.lineCap;
        drawingStyle = other.drawingStyle;
        xform.set(other.xform);
        fillPaint.set(other.fillPaint);
        strokePaint.set(other.strokePaint);
        blendMode = other.blendMode;
        pointStyle = other.pointStyle;
        winding = other.winding;
        dashOffset = other.dashOffset;
        dashArray.clear();
        dashArray.addAll(other.dashArray);
        clips.addAll(other.clips);
        font = other.font;
    }

    void mul(CanvasState other){
        globalAlpha *= other.globalAlpha;
        strokeWidth = other.strokeWidth;
        miterLimit = other.miterLimit;
        lineJoin = other.lineJoin;
        lineCap = other.lineCap;
        drawingStyle = other.drawingStyle;
        xform.mul(other.xform);
        fillPaint.set(other.fillPaint);
        strokePaint.set(other.strokePaint);
        blendMode = other.blendMode;
        pointStyle = other.pointStyle;
        winding = other.winding;
        dashOffset = other.dashOffset;
        dashArray.clear();
        dashArray.addAll(other.dashArray);
        clips.clear();
        clips.addAll(other.clips);
        font = other.font;
    }

    void mulGlobalAlpha(float alpha){
        globalAlpha *= alpha;
    }

    void addClip(Clip clip){
        //TODO do not clear all clips
        if(clip.clipOperation == ClipOperation.replace){
            clips.clear();
        }
        clips.add(clip);
    }

    public boolean isDashedStroke(){
        return dashArray.size > 0;
    }
	
	/*
	@Override
	public void reset() {
		globalAlpha = 1.0f;
		strokeWidth = 1.0f;
		miterLimit = 10.0f;
		lineJoin = LineJoin.miter;
		lineCap = LineCap.butt;
		drawingStyle = DrawingStyle.fill;
		xform.idt();
		scissor.reset();
		fillPaint.reset();
		strokePaint.reset();
		blendMode = BlendMode.over;
		pointStyle = PointStyle.circle;
		winding = Winding.none;
		dashOffset = 0;
		dashArray.clear();
		clips.clear();
		font = null;
	}*/
}
