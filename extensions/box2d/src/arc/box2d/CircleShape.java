package arc.box2d;


import arc.math.geom.*;

/**
 * A circle shape.
 * @author mzechner
 */
public class CircleShape extends Shape{
    // @off
	/*JNI
    #include <Box2D/Box2D.h>
	 */

    public CircleShape(){
        addr = newCircleShape();
    }

    public CircleShape(float radius){
        this();

        setRadius(radius);
    }

    private native long newCircleShape(); /*
		return (jlong)(new b2CircleShape( ));
	*/

    protected CircleShape(long addr){
        this.addr = addr;
    }

    /** {@inheritDoc} */
    @Override
    public Type getType(){
        return Type.Circle;
    }

    /** Returns the position of the shape */
    private final float[] tmp = new float[2];
    private final Vec2 position = new Vec2();

    public Vec2 getPosition(){
        jniGetPosition(addr, tmp);
        position.x = tmp[0];
        position.y = tmp[1];
        return position;
    }

    private native void jniGetPosition(long addr, float[] position); /*
		b2CircleShape* circle = (b2CircleShape*)addr;
		position[0] = circle->m_p.x;
		position[1] = circle->m_p.y;
	*/

    /** Sets the position of the shape */
    public void setPosition(Vec2 position){
        jniSetPosition(addr, position.x, position.y);
    }

    private native void jniSetPosition(long addr, float positionX, float positionY); /*
		b2CircleShape* circle = (b2CircleShape*)addr;
		circle->m_p.x = positionX;
		circle->m_p.y = positionY;
	*/
}
