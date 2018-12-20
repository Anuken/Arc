package io.anuke.arc.utils;

public class Strings{

    public static String dir(int i){
        return i < 0 ? "left" : "right";
    }

    public static String parseException(Throwable e, boolean stacktrace){
        java.lang.StringBuilder build = new java.lang.StringBuilder();

        while(e.getCause() != null){
            e = e.getCause();
        }

        String name = e.getClass().toString().substring("class ".length()).replace("Exception", "");
        if(name.indexOf('.') != -1){
            name = name.substring(name.lastIndexOf('.') + 1);
        }

        build.append(name);
        if(e.getMessage() != null){
            build.append(": ");
            build.append(e.getMessage());
        }

        if(stacktrace){
            for(StackTraceElement s : e.getStackTrace()){
                build.append("\n");
                build.append(s.toString());
            }
        }
        return build.toString();
    }

    public static String formatArgs(String text, Object... args){

        for(int i = 0; i < args.length; i++){
            text = text.replace("{" + i + "}", String.valueOf(args[i]));
        }

        return text;
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

    public static String capitalize(String s){
        String out = s.substring(0, 1).toUpperCase() + s.substring(1);
        int idx = out.indexOf('_');
        if(idx != -1 && idx < s.length() - 2){
            out = out.substring(0, idx) + " " + Character.toUpperCase(out.charAt(idx + 1)) + out.substring(idx + 2);
        }
        return out.replace("_", " ");
    }

    public static String kebabToCamel(String s){
        String out = s;
        int idx = out.indexOf('-');
        int lastidx = 0;
        while(idx != -1){
            out = out.substring(lastidx, idx) + Character.toUpperCase(out.charAt(idx + 1)) + out.substring(idx + 2);
            lastidx = idx;
            idx = out.indexOf(idx + 1, '-');
        }
        return out;
    }

    public static boolean canParseInt(String s){
        return parseInt(s) != Integer.MIN_VALUE;
    }

    public static boolean canParsePostiveInt(String s){
        int p = parseInt(s);
        return p > 0;
    }

    /** Returns Integer.MIN_VALUE if parsing failed. */
    public static int parseInt(String s){
        try{
            return Integer.parseInt(s);
        }catch(Exception e){
            return Integer.MIN_VALUE;
        }
    }

    /** Returns Integer.MIN_VALUE if parsing failed. */
    public static int parsePositiveInt(String s){
        if(!canParsePostiveInt(s)) return Integer.MIN_VALUE;
        try{
            return Integer.parseInt(s);
        }catch(Exception e){
            return Integer.MIN_VALUE;
        }
    }

    /** Returns Float.NEGATIVE_INFINITY if parsing failed. */
    public static float parseFloat(String s){
        try{
            return Float.parseFloat(s);
        }catch(Exception e){
            return Float.NEGATIVE_INFINITY;
        }
    }

    public static String toFixed(double d, int decimalPlaces){
        if(decimalPlaces < 0 || decimalPlaces > 8){
            throw new IllegalArgumentException("Unsupported number of "
            + "decimal places: " + decimalPlaces);
        }
        String s = "" + Math.round(d * Math.pow(10, decimalPlaces));
        int len = s.length();
        int decimalPosition = len - decimalPlaces;
        java.lang.StringBuilder result = new java.lang.StringBuilder();
        if(decimalPlaces == 0){
            return s;
        }else if(decimalPosition > 0){
            // Insert a dot in the right place
            result.append(s.substring(0, decimalPosition));
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

    static public String formatMillis(long val){
        java.lang.StringBuilder buf = new java.lang.StringBuilder(20);
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

    static private void append(java.lang.StringBuilder tgt, String pfx, int dgt, long val){
        tgt.append(pfx);
        if(dgt > 1){
            int pad = (dgt - 1);
            for(long xa = val; xa > 9 && pad > 0; xa /= 10) pad--;
            for(int xa = 0; xa < pad; xa++) tgt.append('0');
        }
        tgt.append(val);
    }
}
