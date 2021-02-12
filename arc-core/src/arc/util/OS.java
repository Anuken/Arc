package arc.util;

import arc.*;
import arc.files.*;

import java.io.*;

public class OS{
    public static final int cores = Runtime.getRuntime().availableProcessors();
    public static final String username = prop("user.name"), userhome = prop("user.home");

    public static boolean isWindows = propNoNull("os.name").contains("Windows");
    public static boolean isLinux = propNoNull("os.name").contains("Linux");
    public static boolean isMac = propNoNull("os.name").contains("Mac");
    public static boolean isIos = false;
    public static boolean isAndroid = false;
    public static boolean isARM = propNoNull("os.arch").startsWith("arm") || propNoNull("os.arch").startsWith("aarch64");
    public static boolean is64Bit = propNoNull("os.arch").contains("64") || propNoNull("os.arch").startsWith("armv8");

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
            Process process = Runtime.getRuntime().exec(args);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = in.readLine()) != null){
                result.append(line).append("\n");
            }

            BufferedReader inerr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while((line = inerr.readLine()) != null){
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

    public static boolean hasEnv(String name){
        return System.getenv(name) != null;
    }

    public static String env(String name){
        return System.getenv(name);
    }

    public static String propNoNull(String name){
        String s = prop(name);
        return s == null ? "" : s;
    }
}
