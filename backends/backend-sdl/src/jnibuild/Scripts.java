package jnibuild;

import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;

class Scripts{

    public static void main(String[] args){
        FileHandle file = new FileHandle("/home/anuke/gltemplate");
        new FileHandle("/home/anuke/glout").writeString(Array.with(file.readString().replace("    ", "").replace("\n", "").split(";")).map(s -> {
            String sign = "public static native " + s + ";";

            Array<String> params = Array.with(s.substring(s.indexOf("(") + 1, s.indexOf(")")).split(", "));
            Log.info(params);
            String methodName = s.substring(s.indexOf(" ") + 1, s.indexOf("("));
            return "@Override\npublic " + s + "{\n    " + (s.contains("void") ? "" : "return ") + "SDLGL." + methodName + "(" + (params.isEmpty() ? "" : params.select(f -> !f.trim().isEmpty()).map(f -> f.split(" ")[1]).toString(", ")) + ");\n}\n";
        }).toString("\n"));
    }

}
