package arc.box2d.joints;

import arc.box2d.*;
import arc.math.geom.*;

/** Pulley joint definition. This requires two ground anchors, two dynamic body anchor points, max lengths for each side, and a
 * pulley ratio. */
public class PulleyJointDef extends JointDef {
	private final static float minPulleyLength = 2.0f;

	public PulleyJointDef () {
		type = JointType.PulleyJoint;
		collideConnected = true;
	}

	/** Initialize the bodies, anchors, lengths, max lengths, and ratio using the world anchors. */
	public void initialize (Body bodyA, Body bodyB, Vec2 groundAnchorA, Vec2 groundAnchorB, Vec2 anchorA,
                            Vec2 anchorB, float ratio) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		this.groundAnchorA.set(groundAnchorA);
		this.groundAnchorB.set(groundAnchorB);
		this.localAnchorA.set(bodyA.getLocalPoint(anchorA));
		this.localAnchorB.set(bodyB.getLocalPoint(anchorB));
		lengthA = anchorA.dst(groundAnchorA);
		lengthB = anchorB.dst(groundAnchorB);
		this.ratio = ratio;
		float C = lengthA + ratio * lengthB;
	}

	/** The first ground anchor in world coordinates. This point never moves. */
	public final Vec2 groundAnchorA = new Vec2(-1, 1);

	/** The second ground anchor in world coordinates. This point never moves. */
	public final Vec2 groundAnchorB = new Vec2(1, 1);

	/** The local anchor point relative to bodyA's origin. */
	public final Vec2 localAnchorA = new Vec2(-1, 0);

	/** The local anchor point relative to bodyB's origin. */
	public final Vec2 localAnchorB = new Vec2(1, 0);

	/** The a reference length for the segment attached to bodyA. */
	public float lengthA = 0;

	/** The a reference length for the segment attached to bodyB. */
	public float lengthB = 0;

	/** The pulley ratio, used to simulate a block-and-tackle. */
	public float ratio = 1;
}
