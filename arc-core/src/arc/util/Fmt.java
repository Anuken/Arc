package arc.util;

import arc.*;

public enum Fmt{
    $;

    public String prefixBind(String s){
        return Core.bundle.get(s);
    }
}
