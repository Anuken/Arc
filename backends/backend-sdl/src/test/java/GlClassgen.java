import arc.graphics.*;
import arc.struct.*;
import arc.util.*;

import java.lang.reflect.*;

public class GlClassgen{

    public static void main(String[] args){
        Class ctype = GL30.class;

        StringBuilder natives = new StringBuilder();
        StringBuilder iface = new StringBuilder();

        String prefix = "SDLGL";

        for(Method meth : ctype.getDeclaredMethods()){
            boolean returns = meth.getReturnType() != void.class;

            natives.append("    public static native ");
            natives.append(meth.getReturnType().getSimpleName()).append(" ");
            natives.append(meth.getName()).append("(");
            natives.append(Seq.with(meth.getParameters()).map(p -> p.getType().getSimpleName() + " " + p.getName()).toString(", "));
            natives.append("); /*").append("\n        ");
            natives.append(returns ? "return " : "").append(meth.getName()).append("(").append(Seq.with(meth.getParameters()).map(Parameter::getName).toString(", ")).append(");\n");
            natives.append("    */\n\n");

            iface.append("    @Override public ");
            iface.append(meth.getReturnType().getSimpleName()).append(" ");
            iface.append(meth.getName()).append("(");
            iface.append(Seq.with(meth.getParameters()).map(p -> p.getType().getSimpleName() + " " + p.getName()).toString(", "));
            iface.append("){ ");
            iface.append(returns ? "return " : "").append(prefix).append(".").append(meth.getName()).append("(").append(Seq.with(meth.getParameters()).map(Parameter::getName).toString(", ")).append(");");
            iface.append(" }\n");
        }

        Log.info(natives);
        Log.info("\n\n-IFACE-\n\n");
        Log.info(iface);
    }
}
