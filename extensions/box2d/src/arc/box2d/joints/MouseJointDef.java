package arc.box2d.joints;

import arc.box2d.*;
import arc.math.geom.*;

/** Mouse joint definition. This requires a world target point, tuning parameters, and the time step. */
public class MouseJointDef extends JointDef{
    public MouseJointDef(){
        type = JointType.MouseJoint;
    }

    /** The initial world target point. This is assumed to coincide with the body anchor initially. */
    public final Vec2 target = new Vec2();

    /**
     * The maximum constraint force that can be exerted to move the candidate body. Usually you will express as some multiple of
     * the weight (multiplier * mass * gravity).
     */
    public float maxForce = 0;

    /** The response speed. */
    public float frequencyHz = 5.0f;

    /** The damping ratio. 0 = no damping, 1 = critical damping. */
    public float dampingRatio = 0.7f;
}
