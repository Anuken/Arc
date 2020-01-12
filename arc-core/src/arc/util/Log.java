package arc.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Log{
    private static final Object[] empty = {};
    private static boolean useColors = true;
    private static LogLevel level = LogLevel.info;
    private static LogHandler logger = new DefaultLogHandler();

    public static void setLogger(LogHandler log){
        logger = log;
    }

    public static LogHandler getLogger(){
        return logger;
    }

    public static void setUseColors(boolean colors){
        useColors = colors;
    }

    public static void log(LogLevel level, String text, Object... args){
        if(Log.level.ordinal() > level.ordinal()) return;
        logger.log(level, format(text, args));
    }

    public static void debug(String text, Object... args){
        log(LogLevel.debug, text, args);
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
        log(LogLevel.info, "[" + tag + "] " + text);
    }

    public static void info(String text, Object... args){
        log(LogLevel.info, text, args);
    }

    public static void info(Object object){
        info(String.valueOf(object), empty);
    }

    public static void warn(String text, Object... args){
        log(LogLevel.warn, text, args);
    }

    public static void errTag(String tag, String text){
        log(LogLevel.err, "[" + tag + "] " + text);
    }

    public static void err(String text, Object... args){
        log(LogLevel.err, text, args);
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
        return formatColors(text, useColors, args);
    }

    public static String formatColors(String text, boolean useColors, Object... args){
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

    public static String removeCodes(String text){
        for(String color : ColorCodes.getColorCodes()){
            text = text.replace("&" + color, "");
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

    public interface LogHandler{
        void log(LogLevel level, String text);
    }

    public static class DefaultLogHandler implements LogHandler{
        @Override
        public void log(LogLevel level, String text){
            System.out.println(format((
                level == LogLevel.debug ? "&lc&fb" :
                level == LogLevel.info ? "&lg&fb" :
                level == LogLevel.warn ? "&ly&fb" :
                level == LogLevel.err ? "&lr&fb" :
                "") + text + "&fr"));
        }
    }

    public static class NoopLogHandler implements LogHandler{
        @Override public void log(LogLevel level, String text){}
    }

}
