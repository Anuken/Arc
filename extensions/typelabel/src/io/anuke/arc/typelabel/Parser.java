package io.anuke.arc.typelabel;

import io.anuke.arc.collection.Array;
import io.anuke.arc.function.IntPositionConsumer;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Colors;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.typelabel.reg.*;
import io.anuke.arc.util.reflect.*;

/** Utility class to parse tokens from a {@link TypeLabel}. */
class Parser{
    private static final Pattern PATTERN_MARKUP_STRIP = Pattern.compile("(\\[{2})|(\\[#?\\w*(\\[|\\])?)");

    private static final String[] BOOLEAN_TRUE = {"true", "yes", "t", "y", "on", "1"};
    private static final int INDEX_TOKEN = 1;
    private static final int INDEX_PARAM = 2;

    private static Pattern PATTERN_TOKEN_STRIP;
    private static String RESET_REPLACEMENT;

    /** Parses all tokens from the given {@link TypeLabel}. */
    static void parseTokens(TypeLabel label){
        // Compile patterns if necessary
        if(PATTERN_TOKEN_STRIP == null || TypingConfig.dirtyEffectMaps){
            PATTERN_TOKEN_STRIP = compileTokenPattern();
        }
        if(RESET_REPLACEMENT == null || TypingConfig.dirtyEffectMaps){
            RESET_REPLACEMENT = getResetReplacement();
        }

        // Adjust and check markup color
        if(label.forceMarkupColor) label.getBitmapFontCache().getFont().getData().markupEnabled = true;

        // Remove any previous entries
        label.tokenEntries.clear();

        //{color=red} (NOT NEEDED) or {var=value} or {endcolor} or {clearcolor} or {reset}

        // Parse all tokens with text replacements, namely color and var.
        parseReplacements(label);

        //wait / event / speed / effect start / effect end

        // Parse all regular tokens and properly register them
        parseRegularTokens(label);

        //[color] / skip

        // Parse color markups and register SKIP tokens
        parseColorMarkups(label);

        // Sort token entries
        label.tokenEntries.sort();
        label.tokenEntries.reverse();
    }

    private static void pReplacements(TypeLabel label){
        StringBuilder text = label.getText();
        StringBuilder result = new StringBuilder();
        result.ensureCapacity(text.length());

        int[] lastIndex = {0};

        parseAllTokens(label, false, (from, to) -> {
            String replacement = null;

            if(text.charAt(from + 1) == '$'){ //variable
                String varname = text.substring(from + 1, to);
                if(label.getTypingListener() != null){
                    replacement = label.getTypingListener().replaceVariable(varname);
                }

                // If replacement is null, get value from maps.
                if(replacement == null){
                    replacement = label.getVariables().get(varname);
                }

                // If replacement is still null, get value from global scope
                if(replacement == null){
                    replacement = TypingConfig.GLOBAL_VARS.get(varname);
                }
            }else if(seq(label, from, to, "/color")){ //end color
                replacement = "[#" + label.getClearColor().toString() + "]";
            }else if(seq(label, from, to, "/reset")){ //reset
                replacement = RESET_REPLACEMENT + label.getDefaultToken();
            }

            //append prev text
            result.append(text.subSequence(lastIndex[0], from - 1));

            if(replacement == null){
                //no variable or text with this name, just append everything
                result.append("{").append(text.subSequence(from, to)).append("}");
            }else{
                //otherwise append the replaced text
                result.append(replacement);
            }

            lastIndex[0] = to;
        });

        //append remaining text
        result.append(text.subSequence(lastIndex[0], text.length()));

        //update label text
        label.setText(result);
    }

    private static void pRegularTokens(TypeLabel label){
        StringBuilder text = label.getText();
        StringBuilder result = new StringBuilder();
        result.ensureCapacity(text.length());

        int[] lastIndex = {0};

        parseAllTokens(label, false, (from, to) -> {
            float floatValue = 0;
            String stringValue = null;
            String name = text.substring(from, to);
            TokenCategory category = null;
            Effect effect = null;

            if(seq(label, from, to, "wait")){
                category = TokenCategory.WAIT;
            }

            TokenEntry entry = new TokenEntry(name, category, from, floatValue, stringValue);
            entry.effect = effect;
            label.tokenEntries.add(entry);

            //TODO BROKEN INDEXING
            result.append(text.subSequence(lastIndex[0], to));
            lastIndex[0] = to;
        });

        result.append(text.subSequence(lastIndex[0], text.length()));
    }

    private static void parseAllTokens(TypeLabel label, boolean square, IntPositionConsumer handler){
        StringBuilder text = label.getText();
        char start = square ? '[' : '{';
        char end = square ? ']' : '}';

        for(int i = 0; i < text.length(); i++){
            char c = text.charAt(i);
            if(c == '\\'){
                //escaped token, skip and continue
                i ++;
                continue;
            }

            //search for an end to the token
            if(c == start){
                for(int j = i + 1; j < text.length(); j++){
                    if(text.charAt(j) == end){
                        //found token end!
                        int tokenFrom = i + 1;
                        int tokenTo = j - 1;
                        handler.accept(tokenFrom, tokenTo);
                        break;
                    }
                }
            }
        }
    }

    private static boolean seq(TypeLabel label, int from, int to, String dest){
        if(dest.length() != to - from) return false;
        for(int i = from; i < to; i++){
            if(label.getText().charAt(i) != dest.charAt(i - from)){
                return false;
            }
        }
        return true;
    }

    /** Parse tokens that only replace text, such as colors and variables. */
    private static void parseReplacements(TypeLabel label){
        // Get text
        CharSequence text = label.getText();
        boolean hasMarkup = label.getBitmapFontCache().getFont().getData().markupEnabled;

        // Create string builder
        Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
        int matcherIndexOffset = 0;

        // Iterate through matches
        while(true){
            // Reset StringBuilder and matcher
            m.setTarget(text);
            m.setPosition(matcherIndexOffset);

            // Make sure there's at least one regex match
            if(!m.find()) break;

            // Get token and parameter
            final InternalToken internalToken = InternalToken.fromName(m.group(INDEX_TOKEN));
            final String param = m.group(INDEX_PARAM);

            // If token couldn't be parsed, move one index forward to continue the search
            if(internalToken == null){
                matcherIndexOffset++;
                continue;
            }

            // Process tokens and handle replacement
            String replacement = "";
            switch(internalToken){
                case COLOR:
                    if(hasMarkup) replacement = stringToColorMarkup(param);
                    break;
                case ENDCOLOR:
                case CLEARCOLOR:
                    if(hasMarkup) replacement = "[#" + label.getClearColor().toString() + "]";
                    break;
                case VAR:
                    replacement = null;

                    // Try to replace variable through listener.
                    if(label.getTypingListener() != null){
                        replacement = label.getTypingListener().replaceVariable(param);
                    }

                    // If replacement is null, get value from maps.
                    if(replacement == null){
                        replacement = label.getVariables().get(param.toUpperCase());
                    }

                    // If replacement is still null, get value from global scope
                    if(replacement == null){
                        replacement = TypingConfig.GLOBAL_VARS.get(param.toUpperCase());
                    }

                    // Make sure we're not inserting "null" to the text.
                    if(replacement == null) replacement = param.toUpperCase();
                    break;
                case RESET:
                    replacement = RESET_REPLACEMENT + label.getDefaultToken();
                    break;
                default:
                    // We don't want to process this token now. Move one index forward to continue the search
                    matcherIndexOffset++;
                    continue;
            }

            // Update text with replacement
            m.setPosition(m.start());
            text = m.replaceFirst(replacement);
        }

        // Set new text
        label.setText(text, false);
    }

    /** Parses regular tokens that don't need replacement and register their indexes in the {@link TypeLabel}. */
    private static void parseRegularTokens(TypeLabel label){
        // Get text
        CharSequence text = label.getText();

        // Create matcher and StringBuilder
        Matcher m = PATTERN_TOKEN_STRIP.matcher(text);
        int matcherIndexOffset = 0;

        // Iterate through matches
        while(true){
            // Reset matcher and StringBuilder
            m.setTarget(text);
            m.setPosition(matcherIndexOffset);

            // Make sure there's at least one regex match
            if(!m.find()) break;

            // Get token name and category
            String tokenName = m.group(INDEX_TOKEN).toUpperCase();
            TokenCategory tokenCategory = null;
            InternalToken tmpToken = InternalToken.fromName(tokenName);
            if(tmpToken == null){
                if(TypingConfig.EFFECT_START_TOKENS.containsKey(tokenName)){
                    tokenCategory = TokenCategory.EFFECT_START;
                }else if(TypingConfig.EFFECT_END_TOKENS.containsKey(tokenName)){
                    tokenCategory = TokenCategory.EFFECT_END;
                }
            }else{
                tokenCategory = tmpToken.category;
            }

            // Get token, param and index of where the token begins
            int groupCount = m.groupCount();
            final String paramsString = groupCount == INDEX_PARAM ? m.group(INDEX_PARAM) : null;
            final String[] params = paramsString == null ? new String[0] : paramsString.split(";");
            final String firstParam = params.length > 0 ? params[0] : null;
            final int index = m.start(0);
            int indexOffset = 0;

            // If token couldn't be parsed, move one index forward to continue the search
            if(tokenCategory == null){
                matcherIndexOffset++;
                continue;
            }

            // Process tokens
            float floatValue = 0;
            String stringValue = null;
            Effect effect = null;

            switch(tokenCategory){
                case WAIT:{
                    floatValue = stringToFloat(firstParam, TypingConfig.DEFAULT_WAIT_VALUE);
                    break;
                }
                case EVENT:{
                    stringValue = paramsString;
                    indexOffset = -1;
                    break;
                }
                case SPEED:{
                    switch(tokenName){
                        case "SPEED":
                            float minModifier = TypingConfig.MIN_SPEED_MODIFIER;
                            float maxModifier = TypingConfig.MAX_SPEED_MODIFIER;
                            float modifier = Mathf.clamp(stringToFloat(firstParam, 1), minModifier, maxModifier);
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / modifier;
                            break;
                        case "SLOWER":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.500f;
                            break;
                        case "SLOW":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.667f;
                            break;
                        case "NORMAL":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR;
                            break;
                        case "FAST":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 2.000f;
                            break;
                        case "FASTER":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 4.000f;
                            break;
                    }
                    break;
                }
                case EFFECT_START:{
                    Class<? extends Effect> clazz = TypingConfig.EFFECT_START_TOKENS.get(tokenName.toUpperCase());
                    try{
                        if(clazz != null){
                            Constructor constructor = ClassReflection.getConstructors(clazz)[0];
                            int constructorParamCount = constructor.getParameterTypes().length;
                            if(constructorParamCount >= 2){
                                effect = (Effect) constructor.newInstance(label, params);
                            }else{
                                effect = (Effect) constructor.newInstance(label);
                            }
                        }
                    }catch(ReflectionException e){
                        String message = "Failed to initialize " + tokenName + " effect token. Make sure the associated class (" + clazz + ") has only one constructor with TypeLabel as first parameter and optionally String[] as second.";
                        throw new IllegalStateException(message, e);
                    }
                    break;
                }
                case EFFECT_END:{
                    break;
                }
            }

            // Register token
            TokenEntry entry = new TokenEntry(tokenName, tokenCategory, index + indexOffset, floatValue, stringValue);
            entry.effect = effect;
            label.tokenEntries.add(entry);

            // Set new text without tokens
            m.setPosition(0);
            text = m.replaceFirst("");
        }

        // Update label text
        label.setText(text, false);
    }

    /** Parse color markup tags and register SKIP tokens. */
    private static void parseColorMarkups(TypeLabel label){
        // Get text
        final CharSequence text = label.getText();

        // Iterate through matches and register skip tokens
        Matcher m = PATTERN_MARKUP_STRIP.matcher(text);
        while(m.find()){
            final String tag = m.group(0);
            final int index = m.start(0);
            label.tokenEntries.add(new TokenEntry("SKIP", TokenCategory.SKIP, index, 0, tag));
        }
    }

    /** Returns a float value parsed from the given String, or the default value if the string couldn't be parsed. */
    static float stringToFloat(String str, float defaultValue){
        if(str != null){
            try{
                return Float.parseFloat(str);
            }catch(Exception ignored){
            }
        }
        return defaultValue;
    }

    /** Returns a boolean value parsed from the given String, or the default value if the string couldn't be parsed. */
    static boolean stringToBoolean(String str){
        if(str != null){
            for(String booleanTrue : BOOLEAN_TRUE){
                if(booleanTrue.equalsIgnoreCase(str)){
                    return true;
                }
            }
        }
        return false;
    }

    /** Parses a color from the given string. Returns null if the color couldn't be parsed. */
    static Color stringToColor(String str){
        if(str != null){

            // Try to parse named color
            Color namedColor = Colors.get(str);
            if(namedColor != null){
                return new Color(namedColor);
            }

            // Try to parse hex
            if(str.length() >= 6){
                return Color.valueOf(str);
            }
        }

        return null;
    }

    /** Encloses the given string in brackets to work as a regular color markup tag. */
    private static String stringToColorMarkup(String str){
        if(str != null) str = str.toUpperCase();
        return "[" + str + "]";
    }

    /**
     * Returns a compiled {@link Pattern} that groups the token name in the first group and the params in an optional
     * second one. Case insensitive.
     */
    private static Pattern compileTokenPattern(){
        StringBuilder sb = new StringBuilder();
        sb.append("\\{(");
        Array<String> tokens = new Array<>();
        TypingConfig.EFFECT_START_TOKENS.keys().toArray(tokens);
        TypingConfig.EFFECT_END_TOKENS.keys().toArray(tokens);
        for(InternalToken token : InternalToken.values()){
            tokens.add(token.name);
        }
        for(int i = 0; i < tokens.size; i++){
            sb.append(tokens.get(i));
            if((i + 1) < tokens.size) sb.append('|');
        }
        sb.append(")(?:=([;#-_ \\.\\w]+))?\\}");
        return Pattern.compile(sb.toString(), REFlags.IGNORE_CASE);
    }

    /** Returns the replacement string intended to be used on {RESET} tokens. */
    private static String getResetReplacement(){
        Array<String> tokens = new Array<>();
        TypingConfig.EFFECT_END_TOKENS.keys().toArray(tokens);
        tokens.add("CLEARCOLOR");
        tokens.add("NORMAL");

        StringBuilder sb = new StringBuilder();
        for(String token : tokens){
            sb.append('{').append(token).append('}');
        }
        return sb.toString();
    }

}
