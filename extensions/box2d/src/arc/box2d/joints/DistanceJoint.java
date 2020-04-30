package arc.box2d.joints;

import arc.box2d.*;
import arc.math.geom.*;

/** A distance joint constrains two points on two bodies to remain at a fixed distance from each other. You can view this as a
 * massless, rigid rod. */
public class DistanceJoint extends Joint {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
	private final float[] tmp = new float[2];
	private final Vec2 localAnchorA = new Vec2();
	private final Vec2 localAnchorB = new Vec2();

	public DistanceJoint (World world, long addr) {
		super(world, addr);
	}

	public Vec2 getLocalAnchorA () {
		jniGetLocalAnchorA(addr, tmp);
		localAnchorA.set(tmp[0], tmp[1]);
		return localAnchorA;
	}

	private native void jniGetLocalAnchorA (long addr, float[] anchor); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	*/

	public Vec2 getLocalAnchorB () {
		jniGetLocalAnchorB(addr, tmp);
		localAnchorB.set(tmp[0], tmp[1]);
		return localAnchorB;
	}

	private native void jniGetLocalAnchorB (long addr, float[] anchor); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	*/

	/** Set/get the natural length. Manipulating the length can lead to non-physical behavior when the frequency is zero. */
	public void setLength (float length) {
		jniSetLength(addr, length);
	}

	private native void jniSetLength (long addr, float length); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetLength( length );
	*/

	/** Set/get the natural length. Manipulating the length can lead to non-physical behavior when the frequency is zero. */
	public float getLength () {
		return jniGetLength(addr);
	}

	private native float jniGetLength (long addr); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetLength();
	*/

	/** Set/get frequency in Hz. */
	public void setFrequency (float hz) {
		jniSetFrequency(addr, hz);
	}

	private native void jniSetFrequency (long addr, float hz); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetFrequency( hz );
	*/

	/** Set/get frequency in Hz. */
	public float getFrequency () {
		return jniGetFrequency(addr);
	}

	private native float jniGetFrequency (long addr); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetFrequency();
	*/

	/** Set/get damping ratio. */
	public void setDampingRatio (float ratio) {
		jniSetDampingRatio(addr, ratio);
	}

	private native void jniSetDampingRatio (long addr, float ratio); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetDampingRatio( ratio );
	*/

	/** Set/get damping ratio. */
	public float getDampingRatio () {
		return jniGetDampingRatio(addr);
	}

	private native float jniGetDampingRatio (long addr); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetDampingRatio();
	*/
}
