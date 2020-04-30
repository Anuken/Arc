LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-box2d
LOCAL_C_INCLUDES := 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := arc.box2d.Shape.cpp\
	arc.box2d.CircleShape.cpp\
	arc.box2d.joints.RopeJoint.cpp\
	arc.box2d.joints.GearJoint.cpp\
	arc.box2d.Manifold.cpp\
	arc.box2d.Contact.cpp\
	memcpy_wrap.c\
	arc.box2d.Body.cpp\
	arc.box2d.joints.MotorJoint.cpp\
	arc.box2d.joints.RevoluteJoint.cpp\
	arc.box2d.PolygonShape.cpp\
	arc.box2d.joints.PulleyJoint.cpp\
	arc.box2d.World.cpp\
	arc.box2d.joints.WheelJoint.cpp\
	arc.box2d.ContactImpulse.cpp\
	arc.box2d.joints.DistanceJoint.cpp\
	arc.box2d.EdgeShape.cpp\
	arc.box2d.ChainShape.cpp\
	arc.box2d.Fixture.cpp\
	arc.box2d.joints.PrismaticJoint.cpp\
	arc.box2d.Joint.cpp\
	Box2D/Dynamics/b2Body.cpp\
	Box2D/Dynamics/b2World.cpp\
	Box2D/Dynamics/b2Fixture.cpp\
	Box2D/Dynamics/Contacts/b2ChainAndPolygonContact.cpp\
	Box2D/Dynamics/Contacts/b2EdgeAndPolygonContact.cpp\
	Box2D/Dynamics/Contacts/b2ChainAndCircleContact.cpp\
	Box2D/Dynamics/Contacts/b2PolygonAndCircleContact.cpp\
	Box2D/Dynamics/Contacts/b2PolygonContact.cpp\
	Box2D/Dynamics/Contacts/b2ContactSolver.cpp\
	Box2D/Dynamics/Contacts/b2CircleContact.cpp\
	Box2D/Dynamics/Contacts/b2EdgeAndCircleContact.cpp\
	Box2D/Dynamics/Contacts/b2Contact.cpp\
	Box2D/Dynamics/b2WorldCallbacks.cpp\
	Box2D/Dynamics/Joints/b2WeldJoint.cpp\
	Box2D/Dynamics/Joints/b2MouseJoint.cpp\
	Box2D/Dynamics/Joints/b2FrictionJoint.cpp\
	Box2D/Dynamics/Joints/b2DistanceJoint.cpp\
	Box2D/Dynamics/Joints/b2WheelJoint.cpp\
	Box2D/Dynamics/Joints/b2MotorJoint.cpp\
	Box2D/Dynamics/Joints/b2RevoluteJoint.cpp\
	Box2D/Dynamics/Joints/b2GearJoint.cpp\
	Box2D/Dynamics/Joints/b2Joint.cpp\
	Box2D/Dynamics/Joints/b2PrismaticJoint.cpp\
	Box2D/Dynamics/Joints/b2PulleyJoint.cpp\
	Box2D/Dynamics/Joints/b2RopeJoint.cpp\
	Box2D/Dynamics/b2Island.cpp\
	Box2D/Dynamics/b2ContactManager.cpp\
	Box2D/Rope/b2Rope.cpp\
	Box2D/Common/b2Draw.cpp\
	Box2D/Common/b2BlockAllocator.cpp\
	Box2D/Common/b2Settings.cpp\
	Box2D/Common/b2Timer.cpp\
	Box2D/Common/b2StackAllocator.cpp\
	Box2D/Common/b2Math.cpp\
	Box2D/Collision/b2Distance.cpp\
	Box2D/Collision/b2CollideCircle.cpp\
	Box2D/Collision/b2CollidePolygon.cpp\
	Box2D/Collision/b2DynamicTree.cpp\
	Box2D/Collision/Shapes/b2ChainShape.cpp\
	Box2D/Collision/Shapes/b2EdgeShape.cpp\
	Box2D/Collision/Shapes/b2CircleShape.cpp\
	Box2D/Collision/Shapes/b2PolygonShape.cpp\
	Box2D/Collision/b2TimeOfImpact.cpp\
	Box2D/Collision/b2BroadPhase.cpp\
	Box2D/Collision/b2CollideEdge.cpp\
	Box2D/Collision/b2Collision.cpp\
	arc.box2d.joints.MouseJoint.cpp\
	arc.box2d.joints.FrictionJoint.cpp\
	arc.box2d.joints.WeldJoint.cpp
 
include $(BUILD_SHARED_LIBRARY)
