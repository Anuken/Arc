package arc.box2d.joints;

import arc.box2d.*;
import arc.math.geom.*;

/** Rope joint definition. This requires two body anchor points and a maximum lengths. Note: by default the connected objects will
 * not collide. see collideConnected in b2JointDef.
 * @author mzechner */
public class RopeJointDef extends JointDef {
	public RopeJointDef () {
		type = JointType.RopeJoint;
	}

	/** The local anchor point relative to bodyA's origin. **/
	public final Vec2 localAnchorA = new Vec2(-1, 0);

	/** The local anchor point relative to bodyB's origin. **/
	public final Vec2 localAnchorB = new Vec2(1, 0);

	/** The maximum length of the rope. Warning: this must be larger than b2_linearSlop or the joint will have no effect. */
	public float maxLength = 0;
}
