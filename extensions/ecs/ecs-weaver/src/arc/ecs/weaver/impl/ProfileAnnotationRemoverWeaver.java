package arc.ecs.weaver.impl;


import arc.ecs.weaver.*;
import org.objectweb.asm.*;

class ProfileAnnotationRemoverWeaver extends ClassVisitor implements Opcodes{

    public ProfileAnnotationRemoverWeaver(ClassVisitor cv){
        super(ASM4, cv);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible){
        return (Weaver.PROFILER_ANNOTATION.equals(desc))
        ? null
        : super.visitAnnotation(desc, visible);
    }
}
