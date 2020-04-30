package arc.box2d.joints;

import arc.box2d.*;
import arc.math.geom.*;

/** Wheel joint definition. This requires defining a line of motion using an axis and an anchor point. The definition uses local
 * anchor points and a local axis so that the initial configuration can violate the constraint slightly. The joint translation is
 * zero when the local anchor points coincide in world space. Using local anchors and a local axis helps when saving and loading a
 * game. */
public class WheelJointDef extends JointDef {
	public WheelJointDef () {
		type = JointType.WheelJoint;
	}

	public void initialize (Body bodyA, Body bodyB, Vec2 anchor, Vec2 axis) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		localAnchorA.set(bodyA.getLocalPoint(anchor));
		localAnchorB.set(bodyB.getLocalPoint(anchor));
		localAxisA.set(bodyA.getLocalVector(axis));
	}

	/** The local anchor point relative to body1's origin. **/
	public final Vec2 localAnchorA = new Vec2();

	/** The local anchor point relative to body2's origin. **/
	public final Vec2 localAnchorB = new Vec2();

	/** The local translation axis in body1. **/
	public final Vec2 localAxisA = new Vec2(1, 0);

	/** Enable/disable the joint motor. **/
	public boolean enableMotor = false;

	/** The maximum motor torque, usually in N-m. */
	public float maxMotorTorque = 0;

	/** The desired motor speed in radians per second. */
	public float motorSpeed = 0;

	/** Suspension frequency, zero indicates no suspension */
	public float frequencyHz = 2;

	/** Suspension damping ratio, one indicates critical damping */
	public float dampingRatio = 0.7f;
}
