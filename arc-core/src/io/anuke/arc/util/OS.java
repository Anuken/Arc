package io.anuke.arc.util;

import io.anuke.arc.Core;
import io.anuke.arc.files.FileHandle;

public class OS{
    static public boolean isWindows = System.getProperty("os.name").contains("Windows");
    static public boolean isLinux = System.getProperty("os.name").contains("Linux");
    static public boolean isMac = System.getProperty("os.name").contains("Mac");
    static public boolean isIos = false;
    static public boolean isAndroid = false;
    static public boolean isARM = System.getProperty("os.arch").startsWith("arm") || System.getProperty("os.arch").startsWith("aarch64");
    static public boolean is64Bit = System.getProperty("os.arch").contains("64") || System.getProperty("os.arch").startsWith("armv8");
    static public boolean isChromeOS = System.getProperty("os.name").contains("Chrome");

    static {
        boolean isMOEiOS = "iOS".equals(System.getProperty("moe.platform.name"));
        String vm = System.getProperty("java.runtime.name");
        if (vm != null && vm.contains("Android Runtime")) { //Can be both isAndroid and isChromeOS
            isAndroid = true;
            isWindows = false;
            isLinux = false;
            isMac = false;
            is64Bit = false;
        }
        if (isMOEiOS || (!isAndroid && !isWindows && !isLinux && !isMac)) {
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
        }else if(isIos || isAndroid || isChromeOS){
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
