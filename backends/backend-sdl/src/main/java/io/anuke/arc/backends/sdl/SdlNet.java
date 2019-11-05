package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.func.*;
import io.anuke.arc.util.*;

import java.io.*;

/**
 * LWJGL implementation of the {@link Net} API, it could be reused in other Desktop backends since it doesn't depend on LWJGL.
 * @author acoppes
 */
public class SdlNet implements Net{
    NetJavaImpl impl = new NetJavaImpl();

    @Override
    public void http(HttpRequest httpRequest, Cons<HttpResponse> success, Cons<Throwable> failure){
        impl.http(httpRequest, success, failure);
    }

    @Override
    public boolean openURI(String url){
        try{
            if(OS.isMac){
                Class.forName("com.apple.eio.FileManager").getMethod("openURL", String.class).invoke(null, url);
                return true;
            }else if(OS.isLinux){
                exec("xdg-open " + url);
                return true;
            }else if(OS.isWindows){
                exec("rundll32 url.dll,FileProtocolHandler " + url);
                return true;
            }
            return false;
        }catch(Throwable e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean openFolder(String file){
        try{
            if(OS.isWindows){
                exec("explorer.exe /select," + file.replace("/", "\\"));
                return true;
            }else if(OS.isLinux){
                exec("xdg-open " + file);
                return true;
            }else if(OS.isMac){
                exec("open " + file);
            }
            return false;
        }catch(Throwable e){
            e.printStackTrace();
            return false;
        }
    }

    private void exec(String command) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
    }

}
