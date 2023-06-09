package arc.util;

import arc.struct.*;

/** Note that these color codes will only work on linux or mac terminals. */
public class ColorCodes{
    public static String
    flush = "\033[H\033[2J",
    reset = "\u001B[0m",
    bold = "\u001B[1m",
    italic = "\u001B[3m",
    underline = "\u001B[4m",
    black = "\u001B[30m",
    red = "\u001B[31m",
    green = "\u001B[32m",
    yellow = "\u001B[33m",
    blue = "\u001B[34m",
    purple = "\u001B[35m",
    cyan = "\u001B[36m",
    lightBlack = "\u001b[90m",
    lightRed = "\u001B[91m",
    lightGreen = "\u001B[92m",
    lightYellow = "\u001B[93m",
    lightBlue = "\u001B[94m",
    lightMagenta = "\u001B[95m",
    lightCyan = "\u001B[96m",
    lightWhite = "\u001b[97m",
    white = "\u001B[37m",

    backDefault = "\u001B[49m",
    backRed = "\u001B[41m",
    backGreen = "\u001B[42m",
    backYellow = "\u001B[43m",
    backBlue = "\u001B[44m";

    public static final String[] codes, values;

    static{
        byte exitCode = 0;
        
        if(OS.isWindows){
            /* Enable VT100, ANSI escapes codes, for windows consoles
            * 
            * Run a Window script to add a key in registry of user (no admin permissions needed) 
            * to enable the VT100 feature.
            * This is only, simple, way i found to do colored text with ANSI codes, without use 
            * an library like jna to run kernel32.GetStdHandle and kernel32.SetConsoleMode methods.
            * The problem (or not), this change is permanent and affect all windows consoles 
            * (cmd, powershell, ...) of user, and need to restart the console to take effect.
            * 
            * Exit codes:
            *   - 0: key added, need restart (so disable colors for this instance)
            *   - 1: error while adding key (probably user group strategy block this), quiet rare
            *   - 2: key already added (VT100 already enabled, so using colors)
            */ 

            try{
                String command1 = "reg query HKCU\\Console /v VirtualTerminalLevel",
                    command2 = "reg add HKCU\\Console /f /v VirtualTerminalLevel /t REG_DWORD /d 1";

                Process process = new ProcessBuilder(command1.split(" ")).redirectErrorStream(true).start();
                
                if(process.waitFor() != 0){
                    // Key don't exist, so create it
                    process = new ProcessBuilder(command2.split(" ")).redirectErrorStream(true).start();
                    exitCode = process.waitFor();
                
                }else{
                    // Key exist, so check if value is at 0x1 or more
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    bool goodValue = false;

                    for(String line : in.lines().toList()){
                        if(line.contains("VirtualTerminalLevel")){
                            goodValue = Integer.decode(line.substring(line.lastIndexOf("0x")).replace("\n", "").trim()) > 0;
                            if(goodValue) break; // Value is 1 or more
                        }
                    }

                    if(goodValue){
                        exitCode = 2;

                    }else{
                        // Value is 0 so change this at 1;
                        process = new ProcessBuilder(command2.split(" ")).redirectErrorStream(true).start();
                        exitCode = process.waitFor();
                    }
                }

            }catch(java.io.IOException | InterruptedException e){} // Error while running commands, don't enable colors
        }
        
        if(OS.isAndroid || (OS.isWindows && exitCode != 2)){
            flush = reset = bold = underline = black = red = green = yellow = blue = purple = cyan = lightWhite
            = lightBlack = lightRed = lightGreen = lightYellow = lightBlue = lightMagenta = lightCyan
            = white = backDefault = backRed = backYellow = backBlue = backGreen = italic = "";
        }

        ObjectMap<String, String> map = ObjectMap.of(
        "ff", flush,
        "fr", reset,
        "fb", bold,
        "fi", italic,
        "fu", underline,
        "k", black,
        "lk", lightBlack,
        "lw", lightWhite,
        "r", red,
        "g", green,
        "y", yellow,
        "b", blue,
        "p", purple,
        "c", cyan,
        "lr", lightRed,
        "lg", lightGreen,
        "ly", lightYellow,
        "lm", lightMagenta,
        "lb", lightBlue,
        "lc", lightCyan,
        "w", white,

        "bd", backDefault,
        "br", backRed,
        "bg", backGreen,
        "by", backYellow,
        "bb", backBlue
        );

        codes = map.keys().toSeq().toArray(String.class);
        values = map.values().toSeq().toArray(String.class);
    }
    
}
