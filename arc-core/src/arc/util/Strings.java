package arc.util;

import arc.graphics.*;
import arc.struct.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;

public class Strings{
    public static final Charset utf8 = Charset.forName("UTF-8");

    public static int count(CharSequence s, char c){
        int total = 0;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == c) total ++;
        }
        return total;
    }

    public static Seq<Throwable> getCauses(Throwable e){
        Seq<Throwable> arr = new Seq<>();
        while(e != null){
            arr.add(e);
            e = e.getCause();
        }
        return arr;
    }

    public static String getSimpleMessage(Throwable e){
        Throwable fcause = getFinalCause(e);
        return fcause.getMessage() == null ? fcause.getClass().getSimpleName() : fcause.getClass().getSimpleName() + ": " + fcause.getMessage();
    }

    public static String getFinalMessage(Throwable e){
        String message = e.getMessage();
        while(e.getCause() != null){
            e = e.getCause();
            if(e.getMessage() != null){
                message = e.getMessage();
            }
        }
        return message;
    }

    public static Throwable getFinalCause(Throwable e){
        while(e.getCause() != null){
            e = e.getCause();
        }
        return e;
    }

    public static String getStackTrace(Throwable e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /** @return a neat error message of a throwable, with stack trace. */
    public static String neatError(Throwable e){
        return neatError(e, true);
    }

    /** @return a neat error message of a throwable, with stack trace. */
    public static String neatError(Throwable e, boolean stacktrace){
        StringBuilder build = new StringBuilder();

        while(e != null){
            String name = e.getClass().toString().substring("class ".length()).replace("Exception", "");
            if(name.indexOf('.') != -1){
                name = name.substring(name.lastIndexOf('.') + 1);
            }

            build.append("> ").append(name);
            if(e.getMessage() != null){
                build.append(": ");
                build.append("'").append(e.getMessage()).append("'");
            }

            if(stacktrace){
                for(StackTraceElement s : e.getStackTrace()){
                    if(s.getClassName().contains("MethodAccessor") || s.getClassName().substring(s.getClassName().lastIndexOf(".") + 1).equals("Method")) continue;
                    build.append("\n");

                    String className = s.getClassName();
                    build.append(className.substring(className.lastIndexOf(".") + 1)).append(".").append(s.getMethodName()).append(": ").append(s.getLineNumber());
                }
            }

            build.append("\n");

            e = e.getCause();
        }


        return build.toString();
    }

    public static String stripColors(CharSequence str){
        StringBuilder out = new StringBuilder(str.length());

        int i = 0;
        while(i < str.length()){
            char c = str.charAt(i);

            // Possible color tag.
            if(c == '['){
                int length = parseColorMarkup(str, i + 1, str.length());
                if(length >= 0){
                    i += length + 2;
                }else{
                    out.append(c);
                    //escaped string
                    i++;
                }
            }else{
                out.append(c);
                i++;
            }
        }

        return out.toString();
    }

    public static String stripGlyphs(CharSequence str){
        StringBuilder out = new StringBuilder(str.length());

        for(int i = 0; i < str.length(); i++){
            int c = str.charAt(i);
            if(c >= 0xE000 && c <= 0xF8FF) continue;
            out.append(c);
        }

        return out.toString();
    }

    private static int parseColorMarkup(CharSequence str, int start, int end){
        if(start >= end) return -1; // String ended with "[".
        switch(str.charAt(start)){
            case '#':
                // Parse hex color RRGGBBAA where AA is optional and defaults to 0xFF if less than 6 chars are used.
                int colorInt = 0;
                for(int i = start + 1; i < end; i++){
                    char ch = str.charAt(i);
                    if(ch == ']'){
                        if(i < start + 2 || i > start + 9) break; // Illegal number of hex digits.
                        if(i - start <= 7){ // RRGGBB or fewer chars.
                            for(int ii = 0, nn = 9 - (i - start); ii < nn; ii++)
                                colorInt = colorInt << 4;
                            colorInt |= 0xff;
                        }
                        //colorInt is the result value, do something with it if you want
                        return i - start;
                    }
                    if(ch >= '0' && ch <= '9')
                        colorInt = colorInt * 16 + (ch - '0');
                    else if(ch >= 'a' && ch <= 'f')
                        colorInt = colorInt * 16 + (ch - ('a' - 10));
                    else if(ch >= 'A' && ch <= 'F')
                        colorInt = colorInt * 16 + (ch - ('A' - 10));
                    else
                        break; // Unexpected character in hex color.
                }
                return -1;
            case '[': // "[[" is an escaped left square bracket.
                return -2;
            case ']': // "[]" is a "pop" color tag.
                //pop the color stack here if needed
                return 0;
        }
        // Parse named color.
        for(int i = start + 1; i < end; i++){
            char ch = str.charAt(i);
            if(ch != ']') continue;
            Color namedColor = Colors.get(str.subSequence(start, i).toString());
            if(namedColor == null) return -1; // Unknown color name.
            //namedColor is the result color here
            return i - start;
        }
        return -1; // Unclosed color tag.
    }

    public static int count(String str, String substring){
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = str.indexOf(substring, lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += substring.length();
            }
        }
        return count;
    }

    public static String encode(String str){
        try{
            return URLEncoder.encode(str, "UTF-8");
        }catch(UnsupportedEncodingException why){
            //why the HECK does this even throw an exception
            throw new RuntimeException(why);
        }
    }

    public static String format(String text, Object... args){
        if(args.length > 0){
            StringBuilder out = new StringBuilder(text.length() + args.length*2);
            int argi = 0;
            for(int i = 0; i < text.length(); i++){
                char c = text.charAt(i);
                if(c == '@' &&  argi < args.length){
                    out.append(args[argi++]);
                }else{
                    out.append(c);
                }
            }

            return out.toString();
        }

        return text;
    }

    public static String join(String separator, String... strings){
        StringBuilder builder = new StringBuilder();
        for(String s : strings){
            builder.append(s);
            builder.append(separator);
        }
        builder.setLength(builder.length() - separator.length());
        return builder.toString();
    }

    public static String join(String separator, Iterable<String> strings){
        StringBuilder builder = new StringBuilder();
        for(String s : strings){
            builder.append(s);
            builder.append(separator);
        }
        builder.setLength(builder.length() - separator.length());
        return builder.toString();
    }

    /** Returns the levenshtein distance between two strings. */
    public static int levenshtein(String x, String y){
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for(int i = 0; i <= x.length(); i++){
            for(int j = 0; j <= y.length(); j++){
                if(i == 0){
                    dp[i][j] = j;
                }else if(j == 0){
                    dp[i][j] = i;
                }else{
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1]
                    + (x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1),
                    dp[i - 1][j] + 1),
                    dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    public static String animated(float time, int length, float scale, String replacement){
        return new String(new char[Math.abs((int)(time / scale) % length)]).replace("\0", replacement);
    }

    public static String kebabToCamel(String s){
        StringBuilder result = new StringBuilder(s.length());

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(c != '_' && c != '-'){
                if(i != 0 && (s.charAt(i - 1) == '_' || s.charAt(i - 1) == '-')){
                    result.append(Character.toUpperCase(c));
                }else{
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    public static String camelToKebab(String s){
        StringBuilder result = new StringBuilder(s.length() + 1);

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(i > 0 && Character.isUpperCase(s.charAt(i))){
                result.append('-');
            }

            result.append(Character.toLowerCase(c));

        }

        return result.toString();
    }

    /**Converts a snake_case or kebab-case string to Upper Case.
     * For example: "test_string" -> "Test String"*/
    public static String capitalize(String s){
        StringBuilder result = new StringBuilder(s.length());

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(c == '_' || c == '-'){
                result.append(" ");
            }else if(i == 0 || s.charAt(i - 1) == '_' || s.charAt(i - 1) == '-'){
                result.append(Character.toUpperCase(c));
            }else{
                result.append(c);
            }
        }

        return result.toString();
    }

    /** Adds spaces to a camel/pascal case string. */
    public static String insertSpaces(String s){
        StringBuilder result = new StringBuilder(s.length());

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(i > 0 && Character.isUpperCase(c)){
                result.append(' ');
            }

            result.append(c);
        }

        return result.toString();
    }

    /**Converts a Space Separated string to camelCase.
     * For example: "Camel Case" -> "camelCase"*/
    public static String camelize(String s){
        StringBuilder result = new StringBuilder(s.length());

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(i == 0){
                result.append(Character.toLowerCase(c));
            }else if(c != ' '){
                result.append(c);
            }

        }

        return result.toString();
    }

    public static boolean canParseInt(String s){
        return parseInt(s) != Integer.MIN_VALUE;
    }

    public static boolean canParsePositiveInt(String s){
        int p = parseInt(s);
        return p >= 0;
    }

    public static long parseLong(String s, long defaultValue){
        try{
            return Long.parseLong(s);
        }catch(Exception e){
            return defaultValue;
        }
    }

    public static int parseInt(String s, int defaultValue){
        try{
            return Integer.parseInt(s);
        }catch(Exception e){
            return defaultValue;
        }
    }

    /** Returns Integer.MIN_VALUE if parsing failed. */
    public static int parseInt(String s){
        return parseInt(s, Integer.MIN_VALUE);
    }

    public static boolean canParseFloat(String s){
        try{
            Float.parseFloat(s);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static boolean canParsePositiveFloat(String s){
        try{
            return Float.parseFloat(s) > 0;
        }catch(Exception e){
            return false;
        }
    }

    /** Returns Integer.MIN_VALUE if parsing failed. */
    public static int parsePositiveInt(String s){
        if(!canParsePositiveInt(s)) return Integer.MIN_VALUE;
        try{
            return Integer.parseInt(s);
        }catch(Exception e){
            return Integer.MIN_VALUE;
        }
    }

    public static double parseDouble(String s, double defaultValue){
        try{
            return Double.parseDouble(s);
        }catch(Exception e){
            return defaultValue;
        }
    }

    /** Returns Float.NEGATIVE_INFINITY if parsing failed. */
    public static float parseFloat(String s){
        return parseFloat(s, Float.MIN_VALUE);
    }

    public static float parseFloat(String s, float defaultValue){
        try{
            return Float.parseFloat(s);
        }catch(Exception e){
            return defaultValue;
        }
    }

    public static String autoFixed(float value, int max){
        int precision = Math.abs((int)value - value) <= 0.0001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.0001f ? 1 : 2;
        return fixed(value, Math.min(precision, max));
    }

    public static String fixed(float d, int decimalPlaces){
        if(decimalPlaces < 0 || decimalPlaces > 8){
            throw new IllegalArgumentException("Unsupported number of " + "decimal places: " + decimalPlaces);
        }
        String s = "" + (int)(d * Math.pow(10, decimalPlaces) + 0.000001f);
        int len = s.length();
        int decimalPosition = len - decimalPlaces;
        StringBuilder result = new StringBuilder();
        if(decimalPlaces == 0){
            return s;
        }else if(decimalPosition > 0){
            // Insert a dot in the right place
            result.append(s, 0, decimalPosition);
            result.append(".");
            result.append(s.substring(decimalPosition));
        }else{
            result.append("0.");
            // Insert leading zeroes into the decimal part
            while(decimalPosition++ < 0){
                result.append("0");
            }
            result.append(s);
        }
        return result.toString();
    }

    public static String formatMillis(long val){
        StringBuilder buf = new StringBuilder(20);
        String sgn = "";

        if(val < 0) sgn = "-";
        val = Math.abs(val);

        append(buf, sgn, 0, (val / 3600000));
        val %= 3600000;
        append(buf, ":", 2, (val / 60000));
        val %= 60000;
        append(buf, ":", 2, (val / 1000));
        return buf.toString();
    }

    private static void append(StringBuilder tgt, String pfx, int dgt, long val){
        tgt.append(pfx);
        if(dgt > 1){
            int pad = (dgt - 1);
            for(long xa = val; xa > 9 && pad > 0; xa /= 10) pad--;
            for(int xa = 0; xa < pad; xa++) tgt.append('0');
        }
        tgt.append(val);
    }

    /** Replaces all instances of {@code find} with {@code replace}. */
    public static StringBuilder replace(StringBuilder builder, String find, String replace){
        int findLength = find.length(), replaceLength = replace.length();
        int index = 0;
        while(true){
            index = builder.indexOf(find, index);
            if (index == -1) break;
            builder.replace(index, index + findLength, replace);
            index += replaceLength;
        }
        return builder;
    }

    /** Replaces all instances of {@code find} with {@code replace}. */
    public static StringBuilder replace(StringBuilder builder, char find, String replace) {
        int replaceLength = replace.length();
        int index = 0;
        while(true){
            while(true){
                if (index == builder.length()) return builder;
                if (builder.charAt(index) == find) break;
                index++;
            }
            builder.replace(index, index + 1, replace);
            index += replaceLength;
        }
    }
}
