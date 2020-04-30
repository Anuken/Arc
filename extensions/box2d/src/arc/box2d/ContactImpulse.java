package arc.box2d;

/** Contact impulses for reporting. Impulses are used instead of forces because sub-step forces may approach infinity for rigid
 * body collisions. These match up one-to-one with the contact points in b2Manifold.
 * @author mzechner */
public class ContactImpulse {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
	final World world;
	long addr;
	float[] tmp = new float[2];
	final float[] normalImpulses = new float[2];
	final float[] tangentImpulses = new float[2];

	protected ContactImpulse (World world, long addr) {
		this.world = world;
		this.addr = addr;
	}

	public float[] getNormalImpulses () {
		jniGetNormalImpulses(addr, normalImpulses);
		return normalImpulses;
	}

	private native void jniGetNormalImpulses (long addr, float[] values); /*
		b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;	
		values[0] = contactImpulse->normalImpulses[0];
		values[1] = contactImpulse->normalImpulses[1];
	*/

	public float[] getTangentImpulses () {
		jniGetTangentImpulses(addr, tangentImpulses);
		return tangentImpulses;
	}

	private native void jniGetTangentImpulses (long addr, float[] values); /*
	  	b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;	
		values[0] = contactImpulse->tangentImpulses[0];
		values[1] = contactImpulse->tangentImpulses[1];
	*/

	public int getCount () {
		return jniGetCount(addr);
	}

	private native int jniGetCount (long addr); /*
		b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;
		return contactImpulse->count;
	*/
}
