package arc.box2d;


import arc.math.geom.*;

/** This is used to compute the current state of a contact manifold. */
public class WorldManifold {
	protected final Vec2 normal = new Vec2();
	protected final Vec2[] points = {new Vec2(), new Vec2()};
	protected final float[] separations = new float[2];
	protected int numContactPoints;

	protected WorldManifold () {
	}

	/** Returns the normal of this manifold */
	public Vec2 getNormal () {
		return normal;
	}

	/** Returns the contact points of this manifold. Use getNumberOfContactPoints to determine how many contact points there are
	 * (0,1 or 2) */
	public Vec2[] getPoints () {
		return points;
	}

	/** Returns the separations of this manifold, a negative value indicates overlap, in meters. Use getNumberOfContactPoints to
	 * determine how many separations there are (0,1 or 2) */
	public float[] getSeparations () {
		return separations;
	}

	/** @return the number of contact points */
	public int getNumberOfContactPoints () {
		return numContactPoints;
	}
}
