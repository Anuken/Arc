package arc.box2d;


import arc.math.geom.*;

/** A line segment (edge) shape. These can be connected in chains or loops to other edge shapes. The connectivity information is
 * used to ensure correct contact normals. */
public class EdgeShape extends Shape {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
	public EdgeShape () {
		addr = newEdgeShape();
	}

	private native long newEdgeShape (); /*
		return (jlong)(new b2EdgeShape());
	*/

	EdgeShape (long addr) {
		this.addr = addr;
	}

	/** Set this as an isolated edge. */
	public void set (Vec2 v1, Vec2 v2) {
		set(v1.x, v1.y, v2.x, v2.y);
	}

	/** Set this as an isolated edge. */
	public void set (float v1X, float v1Y, float v2X, float v2Y) {
		jniSet(addr, v1X, v1Y, v2X, v2Y);
	}

	private native void jniSet (long addr, float v1x, float v1y, float v2x, float v2y); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->Set(b2Vec2(v1x, v1y), b2Vec2(v2x, v2y));
	*/

	static final float[] vertex = new float[2];

	public void getVertex1 (Vec2 vec) {
		jniGetVertex1(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex1 (long addr, float[] vertex); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr; 
		vertex[0] = edge->m_vertex1.x;
		vertex[1] = edge->m_vertex1.y;
	*/

	public void getVertex2 (Vec2 vec) {
		jniGetVertex2(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex2 (long addr, float[] vertex); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex2.x;
		vertex[1] = edge->m_vertex2.y;
	*/

	public void getVertex0 (Vec2 vec) {
		jniGetVertex0(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex0 (long addr, float[] vertex); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex0.x;
		vertex[1] = edge->m_vertex0.y;
	*/

	public void setVertex0 (Vec2 vec) {
		jniSetVertex0(addr, vec.x, vec.y);
	}

	public void setVertex0 (float x, float y) {
		jniSetVertex0(addr, x, y);
	}

	private native void jniSetVertex0 (long addr, float x, float y); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_vertex0.x = x;
		edge->m_vertex0.y = y;
	*/

	public void getVertex3 (Vec2 vec) {
		jniGetVertex3(addr, vertex);
		vec.x = vertex[0];
		vec.y = vertex[1];
	}

	private native void jniGetVertex3 (long addr, float[] vertex); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex3.x;
		vertex[1] = edge->m_vertex3.y;
	*/

	public void setVertex3 (Vec2 vec) {
		jniSetVertex3(addr, vec.x, vec.y);
	}

	public void setVertex3 (float x, float y) {
		jniSetVertex3(addr, x, y);
	}

	private native void jniSetVertex3 (long addr, float x, float y); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_vertex3.x = x;
		edge->m_vertex3.y = y;
	*/

	public boolean hasVertex0() {
		return jniHasVertex0(addr);
	}

	private native boolean jniHasVertex0 (long addr); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		return edge->m_hasVertex0;
	*/

	public void setHasVertex0 (boolean hasVertex0) {
		jniSetHasVertex0(addr, hasVertex0);
	}

	private native void jniSetHasVertex0 (long addr, boolean hasVertex0); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_hasVertex0 = hasVertex0;
	*/

	public boolean hasVertex3 () {
		return jniHasVertex3(addr);
	}

	private native boolean jniHasVertex3 (long addr); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		return edge->m_hasVertex3;
	*/

	public void setHasVertex3 (boolean hasVertex3) {
		jniSetHasVertex3(addr, hasVertex3);
	}

	private native void jniSetHasVertex3 (long addr, boolean hasVertex3); /*
		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_hasVertex3 = hasVertex3;
	*/

	@Override
	public Type getType () {
		return Type.Edge;
	}

}