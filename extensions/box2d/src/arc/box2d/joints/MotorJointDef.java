package arc.box2d.joints;

import arc.box2d.*;
import arc.math.geom.*;

public class MotorJointDef extends JointDef {

	public MotorJointDef () {
		type = JointType.MotorJoint;
	}

	/** Initialize the bodies and offsets using the current transforms. */
	public void initialize (Body body1, Body body2) {
		this.bodyA = body1;
		this.bodyB = body2;
		this.linearOffset.set(bodyA.getLocalPoint(bodyB.getPosition()));
		this.angularOffset = bodyB.getAngle() - bodyA.getAngle();
	}

	/** Position of bodyB minus the position of bodyA, in bodyA's frame, in meters. */
	public final Vec2 linearOffset = new Vec2();
	
	/** The bodyB angle minus bodyA angle in radians. */
	public float angularOffset = 0.0f;
	
	/** The maximum motor force in N. */
	public float maxForce = 1.0f;
	
	/** The maximum motor torque in N-m. */
	public float maxTorque = 1.0f;
	
	/** Position correction factor in the range [0,1]. */
	public float correctionFactor = 0.3f;

}
