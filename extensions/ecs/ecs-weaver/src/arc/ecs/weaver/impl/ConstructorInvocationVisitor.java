package arc.ecs.weaver.impl;

import arc.ecs.weaver.meta.*;
import org.objectweb.asm.*;

public class ConstructorInvocationVisitor extends MethodVisitor implements Opcodes{

    private final ClassMetadata meta;

    private boolean hasCalledSuper = false;

    public ConstructorInvocationVisitor(MethodVisitor mv, ClassMetadata meta){
        super(ASM4, mv);
        this.meta = meta;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf){
        if(!hasCalledSuper && INVOKESPECIAL == opcode && "<init>".equals(name)){
            mv.visitMethodInsn(opcode, owner(meta, owner), name, desc, itf);
            hasCalledSuper = true;
        }else{
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    private static String owner(ClassMetadata meta, String owner){
        if(owner.equals(meta.type.getInternalName()))
            return owner;

        switch(meta.annotation){
            case POOLED:
                return "arc/ecs/PooledComponent";
            default:
                return "FailedTransformingSuperConstructorInvocation";
        }
    }
}
