package arc.box2d.joints;

import arc.box2d.*;
import arc.math.geom.*;

/** Prismatic joint definition. This requires defining a line of motion using an axis and an anchor point. The definition uses
 * local anchor points and a local axis so that the initial configuration can violate the constraint slightly. The joint
 * translation is zero when the local anchor points coincide in world space. Using local anchors and a local axis helps when
 * saving and loading a game.
 * @warning at least one body should by dynamic with a non-fixed rotation. */
public class PrismaticJointDef extends JointDef {
	public PrismaticJointDef () {
		type = JointType.PrismaticJoint;
	}

	/** Initialize the bodies, anchors, axis, and reference angle using the world anchor and world axis. */
	public void initialize (Body bodyA, Body bodyB, Vec2 anchor, Vec2 axis) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		localAnchorA.set(bodyA.getLocalPoint(anchor));
		localAnchorB.set(bodyB.getLocalPoint(anchor));
		localAxisA.set(bodyA.getLocalVector(axis));
		referenceAngle = bodyB.getAngle() - bodyA.getAngle();

	}

	/** The local anchor point relative to body1's origin. */
	public final Vec2 localAnchorA = new Vec2();

	/** The local anchor point relative to body2's origin. */
	public final Vec2 localAnchorB = new Vec2();

	/** The local translation axis in body1. */
	public final Vec2 localAxisA = new Vec2(1, 0);

	/** The constrained angle between the bodies: body2_angle - body1_angle. */
	public float referenceAngle = 0;

	/** Enable/disable the joint limit. */
	public boolean enableLimit = false;

	/** The lower translation limit, usually in meters. */
	public float lowerTranslation = 0;

	/** The upper translation limit, usually in meters. */
	public float upperTranslation = 0;

	/** Enable/disable the joint motor. */
	public boolean enableMotor = false;

	/** The maximum motor torque, usually in N-m. */
	public float maxMotorForce = 0;

	/** The desired motor speed in radians per second. */
	public float motorSpeed = 0;
}
