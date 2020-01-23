package arc.ecs.weaver.impl.profile;

import arc.ecs.weaver.meta.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

class ProfileInitializeWeaver extends AdviceAdapter implements Opcodes{
    private ClassMetadata info;

    ProfileInitializeWeaver(MethodVisitor methodVisitor, ClassMetadata info, int access, String name, String desc){
        super(ASM4, methodVisitor, access, name, desc);
        this.info = info;
    }

    @Override
    protected void onMethodExit(int opcode){
        String systemName = info.type.getInternalName();
        String profiler = info.profilerClass.getInternalName();
        String profileDescriptor = info.profilerClass.getDescriptor();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(NEW, profiler);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, profiler, "<init>", "()V", false);
        mv.visitFieldInsn(PUTFIELD, systemName, "$profiler", profileDescriptor);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, systemName, "$profiler", profileDescriptor);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, systemName, "base", "Larc/ecs/Base;");
        mv.visitMethodInsn(INVOKEVIRTUAL, profiler, "initialize", "(Larc/ecs/BaseSystem;Larc/ecs/Base;)V", false);
    }
}
