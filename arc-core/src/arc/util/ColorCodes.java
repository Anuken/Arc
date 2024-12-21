package arc.util;

import arc.struct.ObjectMap;

import org.fusesource.jansi.internal.Kernel32;

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
        lightBlack = "\u001B[90m",
        lightRed = "\u001B[91m",
        lightGreen = "\u001B[92m",
        lightYellow = "\u001B[93m",
        lightBlue = "\u001B[94m",
        lightMagenta = "\u001B[95m",
        lightCyan = "\u001B[96m",
        lightWhite = "\u001B[97m",
        white = "\u001B[37m",

        backDefault = "\u001B[49m",
        backRed = "\u001B[41m",
        backGreen = "\u001B[42m",
        backYellow = "\u001B[43m",
        backBlue = "\u001B[44m";

    public static final String[] codes, values;
    public static final boolean ansiSupported;

    static{
        boolean ansi = true;

        //try to enable color codes on windows, with powershell
        if(OS.isWindows && !OS.hasEnv("WT_SESSION")){
            try{ 
                // from https://github.com/alexarchambault/windows-ansi/blob/master/jni/src/main/java/io/github/alexarchambault/windowsansi/WindowsAnsi.java
                long console = Kernel32.GetStdHandle(Kernel32.STD_OUTPUT_HANDLE);
                int[] mode = new int[1];
                if (Kernel32.GetConsoleMode(console, mode) == 0) throw new RuntimeException("failed to get console mode");
                mode[0] |= 0x0004; // ENABLE_VIRTUAL_TERMINAL_PROCESSING
                if (Kernel32.SetConsoleMode(console, mode[0]) == 0) throw new RuntimeException("failed to set console mode");

            }catch (Throwable e){ 
                System.out.println("Failed to enable ANSI escape codes: " + e.toString());
                ansi = false;
            }
        //disable color codes on android
        }else if (OS.isAndroid){
            ansi = false;
        }

        ansiSupported = ansi;

        if(!ansiSupported){
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
