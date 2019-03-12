package io.anuke.arc.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Log{
    private static final Object[] empty = {};
    private static boolean useColors = true;
    private static LogLevel level = LogLevel.info;
    private static LogHandler logger = new LogHandler();

    public static void setLogger(LogHandler log){
        logger = log;
    }

    public static void setUseColors(boolean colors){
        useColors = colors;
    }

    public static void debug(String text, Object... args){
        if(level.ordinal() > LogLevel.debug.ordinal()) return;
        logger.debug(text, args);
    }

    public static void infoList(Object... args){
        if(level.ordinal() > LogLevel.info.ordinal()) return;
        StringBuilder build = new StringBuilder();
        for(Object o : args){
            build.append(o);
            build.append(" ");
        }
        info(build.toString());
    }

    public static void infoTag(String tag, String text){
        if(level.ordinal() > LogLevel.info.ordinal()) return;
        logger.info("[" + tag + "] " + text);
    }

    public static void info(String text, Object... args){
        if(level.ordinal() > LogLevel.info.ordinal()) return;
        logger.info(text, args);
    }

    public static void info(Object object){
        info(String.valueOf(object), empty);
    }

    public static void warn(String text, Object... args){
        if(level.ordinal() > LogLevel.warn.ordinal()) return;
        logger.warn(text, args);
    }

    public static void errTag(String tag, String text){
        if(level.ordinal() > LogLevel.err.ordinal()) return;
        logger.err("[" + tag + "] " + text);
    }

    public static void err(String text, Object... args){
        if(level.ordinal() > LogLevel.err.ordinal()) return;
        logger.err(text, args);
    }

    public static void err(Throwable th){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        err(sw.toString());
    }

    public static void err(String text, Throwable th){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        err(text + ": " + sw.toString());
    }

    public static String format(String text, Object... args){
        return format(text, useColors, args);
    }

    public static String format(String text, boolean useColors, Object... args){
        text = Strings.format(text, args);

        if(useColors){
            for(String color : ColorCodes.getColorCodes()){
                text = text.replace("&" + color, ColorCodes.getColorText(color));
            }
        }else{
            for(String color : ColorCodes.getColorCodes()){
                text = text.replace("&" + color, "");
            }
        }
        return text;
    }

    public static void setLogLevel(LogLevel level){
        Log.level = level;
    }

    public enum LogLevel{
        debug,
        info,
        warn,
        err,
        none
    }

    public static class LogHandler{
        public void debug(String text, Object... args){
            print("&lc&fb" + format(text, args));
        }

        public void info(String text, Object... args){
            print("&lg&fb" + format(text, args));
        }

        public void warn(String text, Object... args){
            print("&ly&fb" + format(text, args));
        }

        public void err(String text, Object... args){
            print("&lr&fb" + format(text, args));
        }

        public void print(String text, Object... args){
            System.out.println(format(text + "&fr", args));
        }
    }

    public static class NoopLogHandler extends LogHandler{
        @Override
        public void print(String text, Object... args){
        }
    }

}
