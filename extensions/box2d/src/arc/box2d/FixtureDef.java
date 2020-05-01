package arc.box2d;

/**
 * A fixture definition is used to create a fixture. This class defines an abstract fixture definition. You can reuse fixture
 * definitions safely.
 * @author mzechner
 */
public class FixtureDef{
    /** The shape, this must be set. The shape will be cloned, so you can create the shape on the stack. */
    public Shape shape;

    /** The friction coefficient, usually in the range [0,1]. **/
    public float friction = 0.2f;

    /** The restitution (elasticity) usually in the range [0,1]. **/
    public float restitution = 0;

    /** The density, usually in kg/m^2. **/
    public float density = 0;

    /** A sensor shape collects contact information but never generates a collision response. */
    public boolean isSensor = false;

    /** Contact filtering data. **/
    public final Filter filter = new Filter();
}
