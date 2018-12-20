package java.util.regex;

import com.google.gwt.regexp.shared.RegExp;

/**
 * Emulation of the {@link Pattern} class, uses {@link RegExp} as internal implementation.
 * @author hneuer
 */
public class Pattern{
    final RegExp regExp;

    private Pattern(String regExp){
        this.regExp = RegExp.compile(regExp);
    }

    public static Pattern compile(String regExp){
        return new Pattern(regExp);
    }

    public Matcher matcher(CharSequence input){
        return new Matcher(this, input);
    }
}
