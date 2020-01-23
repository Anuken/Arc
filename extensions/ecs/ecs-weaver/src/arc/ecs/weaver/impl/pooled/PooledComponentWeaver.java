package arc.ecs.weaver.impl.pooled;

import arc.ecs.weaver.meta.*;
import arc.ecs.weaver.impl.*;
import org.objectweb.asm.*;

public class PooledComponentWeaver extends ClassVisitor implements Opcodes{

    private ClassMetadata meta;

    public PooledComponentWeaver(ClassVisitor cv, ClassMetadata meta){
        super(ASM4, cv);
        this.meta = meta;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
        MethodVisitor method = cv.visitMethod(access, name, desc, signature, exceptions);

        if("<init>".equals(name))
            method = new ConstructorInvocationVisitor(method, meta);
        if("reset".equals(name) && "()V".equals(desc))
            method = new ResetMethodVisitor(method, meta);

        return method;
    }
}
