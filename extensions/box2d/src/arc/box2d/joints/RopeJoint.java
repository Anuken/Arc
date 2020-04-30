package arc.box2d.joints;

import arc.box2d.*;
import arc.math.geom.*;

/** A rope joint enforces a maximum distance between two points on two bodies. It has no other effect. Warning: if you attempt to
 * change the maximum length during the simulation you will get some non-physical behavior. A model that would allow you to
 * dynamically modify the length would have some sponginess, so I chose not to implement it that way. See b2DistanceJoint if you
 * want to dynamically control length. */
public class RopeJoint extends Joint {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */

	private final float[] tmp = new float[2];
	private final Vec2 localAnchorA = new Vec2();
	private final Vec2 localAnchorB = new Vec2();

	public RopeJoint (World world, long addr) {
		super(world, addr);
	}

	public Vec2 getLocalAnchorA () {
		jniGetLocalAnchorA(addr, tmp);
		localAnchorA.set(tmp[0], tmp[1]);
		return localAnchorA;
	}

	private native void jniGetLocalAnchorA (long addr, float[] anchor); /*
		b2RopeJoint* joint = (b2RopeJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	*/

	public Vec2 getLocalAnchorB () {
		jniGetLocalAnchorB(addr, tmp);
		localAnchorB.set(tmp[0], tmp[1]);
		return localAnchorB;
	}

	private native void jniGetLocalAnchorB (long addr, float[] anchor); /*
		b2RopeJoint* joint = (b2RopeJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	*/

	/** Get the maximum length of the rope. */
	public float getMaxLength () {
		return jniGetMaxLength(addr);
	}

	private native float jniGetMaxLength (long addr); /*
		b2RopeJoint* rope = (b2RopeJoint*)addr;
		return rope->GetMaxLength();
	*/

	/** Set the maximum length of the rope. */
	public void setMaxLength (float length) {
		jniSetMaxLength(addr, length);
	}

	private native float jniSetMaxLength (long addr, float length); /*
		b2RopeJoint* rope = (b2RopeJoint*)addr;
		rope->SetMaxLength(length);
	*/
}
