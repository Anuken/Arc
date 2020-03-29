package arc.util;

import arc.*;
import arc.files.*;

import java.io.*;

public class OS{
    static public boolean isWindows = propNoNull("os.name").contains("Windows");
    static public boolean isLinux = propNoNull("os.name").contains("Linux");
    static public boolean isMac = propNoNull("os.name").contains("Mac");
    static public boolean isIos = false;
    static public boolean isAndroid = false;
    static public boolean isARM = propNoNull("os.arch").startsWith("arm") || propNoNull("os.arch").startsWith("aarch64");
    static public boolean is64Bit = propNoNull("os.arch").contains("64") || propNoNull("os.arch").startsWith("armv8");

    static{
        if(propNoNull("java.runtime.name").contains("Android Runtime") || propNoNull("java.vm.vendor").contains("The Android Project") || propNoNull("java.vendor").contains("The Android Project")){
            isAndroid = true;
            isWindows = false;
            isLinux = false;
            isMac = false;
            is64Bit = false;
        }
        if(propNoNull("moe.platform.name").equals("iOS") || (!isAndroid && !isWindows && !isLinux && !isMac)){
            isIos = true;
            isAndroid = false;
            isWindows = false;
            isLinux = false;
            isMac = false;
            is64Bit = false;
        }
    }

    public static String getAppDataDirectoryString(String appname){
        if(OS.isWindows){
            return env("AppData") + "\\\\" + appname;
        }else if(isIos || isAndroid){
            return Core.files.getLocalStoragePath();
        }else if(OS.isLinux){
            if(System.getenv("XDG_DATA_HOME") != null){
                String dir = System.getenv("XDG_DATA_HOME");
                if(!dir.endsWith("/")) dir += "/";
                return dir + appname + "/";
            }
            return prop("user.home") + "/.local/share/" + appname + "/";
        }else if(OS.isMac){
            return prop("user.home") + "/Library/Application Support/" + appname + "/";
        }else{ //else, probably web
            return null;
        }
    }

    public static String exec(String... args){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(args).getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = in.readLine()) != null){
                result.append(line).append("\n");
            }
            return result.toString();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static boolean execSafe(String command){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));
            String line;
            while((line = in.readLine()) != null){
                System.out.println(line);
            }
            return true;
        }catch(Throwable t){
            return false;
        }
    }

    public static Fi getAppDataDirectory(String appname){
        return Core.files.absolute(getAppDataDirectoryString(appname));
    }

    public static boolean hasProp(String name){
        return System.getProperty(name) != null;
    }

    public static String prop(String name){
        return System.getProperty(name);
    }

    public static String env(String name){
        return System.getenv(name);
    }

    public static String propNoNull(String name){
        String s = prop(name);
        return s == null ? "" : s;
    }
}
