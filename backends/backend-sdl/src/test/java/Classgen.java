import arc.struct.*;
import arc.files.*;
import arc.util.*;

public class Classgen{

    public static void main(String[] args){
        String str = new Fi("/home/anuke/work/alc.h").readString();
        StringMap map = StringMap.of(
        "ALuint", "int",
        "ALint", "int",
        "ALsizei", "int",
        "ALuint*", "IntBuffer",
        "ALenum", "int",
        "ALfloat", "float",
        "ALdouble", "double",
        "ALfloat*", "FloatBuffer",
        "ALint*", "IntBuffer",
        "ALvoid*", "ByteBuffer",
        "void", "void",
        "ALboolean", "boolean",
        "ALchar*", "String",
        "ALCint*", "IntBuffer",
        "ALCchar*", "String",
        "ALCenum", "int",
        "ALCdevice*", "long",
        "ALCvoid*", "ByteBuffer",
        "ALCsizei", "int",
        "ALCcontext*", "long",
        "ALCboolean", "boolean"
        );

        StringBuilder result = new StringBuilder("public class AL{\n");

        str = str.replace("AL_API ", "").replace("AL_APIENTRY ", "").replace("ALC_API ", "").replace("ALC_APIENTRY ", "");
        Array.with(str.split("\n")).select(s -> !s.isEmpty() && !s.startsWith("typedef") && s.trim().length() > 1 && !s.startsWith(" ") && !s.startsWith("typedef") && !(s.startsWith("#")&& !s.startsWith("#define"))).each(s -> {

            if(s.startsWith("#define")){
                s = s.substring("#define ".length()).replaceAll(" +", " ");
                String[] split = s.split(" ");
                Log.err(s);
                if(split.length != 2) return;
                String key = split[0];
                int val = split[1].startsWith("0x") ? Integer.parseInt(split[1].substring(2), 16) : Strings.parseInt(split[1]);

                result.append("    public static final int " + key + " = " + val + ";\n");
            }else{
                Log.info(s);
                s = s.replaceAll(" +", " ").replace("const ", "").replace(" *", "* ").replace("(void)", "()");
                String[] res = {s};

                //map.each((key, val) -> res[0] = res[0].replace(key, val));

                s = res[0];

                String[] split = s.split(" ");
                String ret = split[0];
                String name = split[1].substring(0, split[1].indexOf("("));
                result.append("    public static native " + map.get(ret, ret) + " " + name + "(");
                String[] params = s.substring(s.indexOf("(") + 1, s.lastIndexOf(")")).split(", ");
                Array<String> pnames = new Array<>();
                Array<String> ptypes = new Array<>();

                if(!s.substring(s.indexOf("(") + 1, s.lastIndexOf(")")).isEmpty()){
                    int i = 0;
                    for(String p : params){
                        String[] ps = p.split(" ");
                        pnames.add(ps[1]);
                        ptypes.add(ps[0]);
                        result.append(map.get(ps[0], ps[0])).append(" ").append(ps[1]).append(i++ == params.length - 1 ? "" : ", ");
                    }
                }

                result.append(");");

                result.append(" /* ");

                if(!ret.equals("void")){
                    result.append("return (j" + map.get(ret, ret) + ")");
                }

                int[] i = {0};
                result.append(name + "(" + pnames.toString(", ", f -> "(" + ptypes.get(i[0]++) + ")" + f) + ")" + ";");

                result.append(" */\n");
            }

        });

        result.append("}");

        Log.info(result);

    }
}
