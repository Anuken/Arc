package jnibuild;

import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;
import io.anuke.arc.input.*;
import io.anuke.arc.input.KeyCode.*;
import io.anuke.arc.util.*;

class Scripts{
    static String[] mappings = {"ANY_KEY", "NUM_0", "NUM_1", "NUM_2", "NUM_3", "NUM_4", "NUM_5", "NUM_6", "NUM_7", "NUM_8", "NUM_9", "A", "ALT_LEFT", "ALT_RIGHT", "APOSTROPHE", "AT", "B", "BACK", "BACKSLASH", "C", "CALL", "CAMERA", "CLEAR", "COMMA", "D", "DEL", "BACKSPACE", "FORWARD_DEL", "DPAD_CENTER", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "DPAD_UP", "CENTER", "DOWN", "LEFT", "RIGHT", "UP", "E", "ENDCALL", "ENTER", "ENVELOPE", "EQUALS", "EXPLORER", "F", "FOCUS", "G", "GRAVE", "H", "HEADSETHOOK", "HOME", "I", "J", "K", "L", "LEFT_BRACKET", "M", "MEDIA_FAST_FORWARD", "MEDIA_NEXT", "MEDIA_PLAY_PAUSE", "MEDIA_PREVIOUS", "MEDIA_REWIND", "MEDIA_STOP", "MENU", "MINUS", "MUTE", "N", "NOTIFICATION", "NUM", "O", "P", "PERIOD", "PLUS", "POUND", "POWER", "Q", "R", "RIGHT_BRACKET", "S", "SEARCH", "SEMICOLON", "SHIFT_LEFT", "SHIFT_RIGHT", "SLASH", "SOFT_LEFT", "SOFT_RIGHT", "SPACE", "STAR", "SYM", "T", "TAB", "U", "UNKNOWN", "V", "VOLUME_DOWN", "VOLUME_UP", "W", "X", "Y", "Z", "META_ALT_LEFT_ON", "META_ALT_ON", "META_ALT_RIGHT_ON", "META_SHIFT_LEFT_ON", "META_SHIFT_ON", "META_SHIFT_RIGHT_ON", "META_SYM_ON", "CONTROL_LEFT", "CONTROL_RIGHT", "ESCAPE", "END", "INSERT", "PAGE_UP", "PAGE_DOWN", "PICTSYMBOLS", "SWITCH_CHARSET", "BUTTON_CIRCLE", "BUTTON_A", "BUTTON_B", "BUTTON_C", "BUTTON_X", "BUTTON_Y", "BUTTON_Z", "BUTTON_L1", "BUTTON_R1", "BUTTON_L2", "BUTTON_R2", "BUTTON_THUMBL", "BUTTON_THUMBR", "BUTTON_START", "BUTTON_SELECT", "BUTTON_MODE", "NUMPAD_0", "NUMPAD_1", "NUMPAD_2", "NUMPAD_3", "NUMPAD_4", "NUMPAD_5", "NUMPAD_6", "NUMPAD_7", "NUMPAD_8", "NUMPAD_9", "COLON", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "UNSET"};
    static Array<String> mmap = Array.with(mappings);

    public static void main(String[] args){
        /*
        FileHandle file = new FileHandle("/home/anuke/gltemplate");
        new FileHandle("/home/anuke/glout").writeString(Array.with(file.readString().replace("    ", "").replace("\n", "").split(";")).map(s -> {
            String sign = "public static native " + s + ";";

            Array<String> params = Array.with(s.substring(s.indexOf("(") + 1, s.indexOf(")")).split(", "));
            Log.info(params);
            String methodName = s.substring(s.indexOf(" ") + 1, s.indexOf("("));
            return "@Override\npublic " + s + "{\n    " + (s.contains("void") ? "" : "return ") + "SDLGL." + methodName + "(" + (params.isEmpty() ? "" : params.select(f -> !f.trim().isEmpty()).map(f -> f.split(" ")[1]).toString(", ")) + ");\n}\n";
        }).toString("\n"));*/

        FileHandle file = new FileHandle("/home/anuke/keycodes");
        Array<String> groups = new Array<>();
        String[] ssplit = file.readString().split("\n\n");
        for(int i = 0; i < ssplit.length; i+= 3){
            groups.add(ssplit[i] + "|||" + ssplit[i + 1] + "|||" + ssplit[i + 2]);
        }
;
        //KeyCode
        Log.info(Array.select(KeyCode.values(), s -> s.type == KeyType.key).toString(", ", s -> "\"" + s.name() + "\""));

        new FileHandle("/home/anuke/keyout").writeString(groups.map(s -> {
            String[] split = s.split("\\|\\|\\|");
            String dec = split[0];
            String sdlname = split[2].substring("SDLK_".length());
            if(Strings.canParseInt(sdlname)){
                sdlname = "NUM_" + sdlname;
            }

            String fsdl = sdlname;
            String found = mmap.find(f -> f.equalsIgnoreCase(fsdl));
            if(found == null){
                Log.info("Not found: " + sdlname);
                return null;
            }
            return "case " + dec + ": return KeyCode." + found + ";";
        }).select(s -> s != null).toString("\n"));
    }

}
