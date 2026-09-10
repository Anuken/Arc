package arc.util;

public class Log{
    private static final Object[] empty = {};

    public static boolean useColors = true;
    public static LogLevel level = LogLevel.info;
    public static LogHandler logger = new DefaultLogHandler();
    public static LogFormatter formatter = new DefaultLogFormatter();

    public static void log(LogLevel level, String text, Object... args){
        logger.log(level, "", text, args);
    }

    public static void logTag(LogLevel level, String tag, String text, Object... args){
        logger.log(level, tag, text, args);
    }

    public static void debug(String text, Object... args){
        logger.log(LogLevel.debug, "", text, args);
    }
    
    public static void debug(Object object){
        logger.log(LogLevel.debug, "", String.valueOf(object), empty);
    }

    public static void infoList(Object... args){
        logger.logList(LogLevel.info, "", args);
    }

    public static void infoTag(String tag, String text){
        logger.log(LogLevel.info, tag, text, empty);
    }

    public static void info(String text, Object... args){
        logger.log(LogLevel.info, "", text, args);
    }

    public static void info(Object object){
        logger.log(LogLevel.info, "", String.valueOf(object), empty);
    }

    public static void warn(String text, Object... args){
        logger.log(LogLevel.warn, "", text, args);
    }

    public static void errTag(String tag, String text){
        logger.log(LogLevel.err, tag, text, empty);
    }

    public static void err(String text, Object... args){
        logger.log(LogLevel.err, "", text, args);
    }

    public static void err(Throwable th){
        logger.logException(LogLevel.err, "", "", th);
    }

    public static void err(String text, Throwable th){
        logger.logException(LogLevel.err, "", text, th);
    }

    public static String format(String text, Object... args){
        return formatColors(text, useColors, args);
    }

    public static String formatColors(String text, boolean useColors, Object... args){
        return formatter.format(text, useColors, args);
    }

    public static String removeColors(String text){
        for(String color : ColorCodes.codes){
            text = text.replace("&" + color, "");
        }
        return text;
    }

    public static String addColors(String text){
        for(int i = 0; i < ColorCodes.codes.length; i++){
            text = text.replace("&" + ColorCodes.codes[i], ColorCodes.values[i]);
        }
        return text;
    }

    public enum LogLevel{
        debug,
        info,
        warn,
        err,
        none
    }

    public interface LogFormatter{
        String format(String text, boolean useColors, Object... args);
    }

    public static class DefaultLogFormatter implements LogFormatter{
        @Override
        public String format(String text, boolean useColors, Object... args){
            text = Strings.format(text, args);
            return useColors ? addColors(text) : removeColors(text);
        }
    }

    public interface LogHandler{
        void log(LogLevel level, String text);

        default void log(LogLevel level, String tag, String text, Object... args){
            if(Log.level.ordinal() > level.ordinal()) return;
            this.log(level, format((tag.isEmpty() ? "" : "[" + tag + "] ") + text, args));
        }

        default void logList(LogLevel level, String tag, Object... args){
            if(Log.level.ordinal() > level.ordinal()) return;
            StringBuilder build = new StringBuilder();
            build.append(tag.isEmpty() ? "" : "[" + tag + "] ");
            for(int i = 0; i < args.length; i++){
                build.append(args[i]);
                if(i + 1 < args.length) build.append(" ");
            }
            this.log(level, format(build.toString(), empty));
        }

        default void logException(LogLevel level, String tag, String text, Throwable th){
            if(Log.level.ordinal() > level.ordinal()) return;
            text += (text.isEmpty() ? "" : ": ") + Strings.getStackTrace(th);
            this.log(level, format((tag.isEmpty() ? "" : "[" + tag + "] ") + text, empty));
        }
    }

    public static class DefaultLogHandler implements LogHandler{
        @Override
        public void log(LogLevel level, String text){
            System.out.println(format((
                level == LogLevel.debug ? "&lc&fb" :
                level == LogLevel.info ? "&fb" :
                level == LogLevel.warn ? "&ly&fb" :
                level == LogLevel.err ? "&lr&fb" :
                "") + text + "&fr"));
        }
    }

    public static class NoopLogHandler implements LogHandler{
        @Override public void log(LogLevel level, String text){}
        @Override public void log(LogLevel level, String tag, String text, Object... args){}
        @Override public void logList(LogLevel level, String tag, Object... args) {}
        @Override public void logException(LogLevel level, String tag, String text, Throwable th){}
    }
}
