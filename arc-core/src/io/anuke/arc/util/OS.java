package io.anuke.arc.util;

import io.anuke.arc.Core;
import io.anuke.arc.files.FileHandle;

public class OS{
    public static boolean isWindows = getPropertyNotNull("os.name").contains("Windows");
    public static boolean isLinux = getPropertyNotNull("os.name").contains("Linux");
    public static boolean isMac = getPropertyNotNull("os.name").contains("Mac");
    public static boolean isIos = false;
    public static boolean isAndroid = false;
    public static boolean isARM = getPropertyNotNull("os.arch").startsWith("arm");
    public static boolean is64Bit = getPropertyNotNull("os.arch").equals("amd64") || getPropertyNotNull("os.arch").equals("x86_64");

    // JDK 8 only.
    public static String abi = (getPropertyNotNull("sun.arch.abi") != null ? getPropertyNotNull("sun.arch.abi") : "");

    static{
        boolean isMOEiOS = "iOS".equals(getPropertyNotNull("moe.platform.name"));
        String vm = getPropertyNotNull("java.runtime.name");
        if(vm != null && vm.contains("Android Runtime")){
            isAndroid = true;
            isWindows = false;
            isLinux = false;
            isMac = false;
            is64Bit = false;
        }
        if(isMOEiOS || (!isAndroid && !isWindows && !isLinux && !isMac)){
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
            return getEnv("AppData") + "\\\\" + appname;
        }else if(isIos || isAndroid){
            return Core.files.getLocalStoragePath();
        }else if(OS.isLinux){
            if(System.getenv("XDG_DATA_HOME") != null){
                String dir = System.getenv("XDG_DATA_HOME");
                if(!dir.endsWith("/")) dir += "/";
                return dir + appname + "/";
            }
            return getProperty("user.home") + "/.local/share/" + appname + "/";
        }else if(OS.isMac){
            return getProperty("user.home") + "/Library/Application Support/" + appname + "/";
        }else{ //else, probably GWT
            return null;
        }
    }

    public static FileHandle getAppDataDirectory(String appname){
        return Core.files.absolute(getAppDataDirectoryString(appname));
    }

    public static String getProperty(String name){
        return System.getProperty(name);
    }

    public static String getEnv(String name){
        return System.getenv(name);
    }

    public static String getPropertyNotNull(String name){
        String s = getProperty(name);
        return s == null ? "" : s;
    }
}
