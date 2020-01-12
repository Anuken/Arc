package arc.backend.sdl;

import arc.*;
import arc.util.*;

public class SdlNet extends Net{

    @Override
    public boolean openURI(String url){
        try{
            if(OS.isMac){
                Class.forName("com.apple.eio.FileManager").getMethod("openURL", String.class).invoke(null, url);
                return true;
            }else if(OS.isLinux){
                return OS.execSafe("xdg-open " + url);
            }else if(OS.isWindows){
                return OS.execSafe("rundll32 url.dll,FileProtocolHandler " + url);
            }
            return false;
        }catch(Throwable e){
            e.printStackTrace();
            return false;
        }
    }
}
