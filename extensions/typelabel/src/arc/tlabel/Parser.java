package arc.tlabel;

import arc.struct.Array;
import arc.func.*;

/** Utility class to parse tokens from a {@link TypeLabel}. */
class Parser{
    private static String RESET_REPLACEMENT;

    /** Parses all tokens from the given {@link TypeLabel}. */
    static void parseTokens(TypeLabel label){
        // Compile patterns if necessary
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

        //remove everything

        // Parse color markups and register SKIP tokens
        stripTokens(label);

        // Sort token entries
        label.tokenEntries.sort();
        //Log.info(new Json().prettyPrint(label.tokenEntries));
        label.tokenEntries.reverse();
    }

    private static void baseParse(TypeLabel label, TokenHandler replacer){
        StringBuilder text = label.getText();
        StringBuilder result = new StringBuilder();
        result.ensureCapacity(text.length());

        int[] lastIndex = {0};
        int[] afterIndex = {0};

        parseAllTokens(label, false, (from, to) -> {
            String replacement = text.charAt(from - 1) == '{' ? replacer.handle(text.substring(from, to), from + afterIndex[0]) : "[" + text.substring(from, to) + "]";
            afterIndex[0] -= (to - from + 2);

            //append prev text
            result.append(text.subSequence(lastIndex[0], from - 1));

            if(replacement == null){
                //no variable or text with this name, just append everything
                result.append("{").append(text.subSequence(from, to)).append("}");
            }else{
                //otherwise append the replaced text
                result.append(replacement);
            }

            lastIndex[0] = to + 1;
        });

        //append remaining text
        result.append(text.subSequence(lastIndex[0], text.length()));

        //update label text
        label.setText(result);
    }

    private static void parseReplacements(TypeLabel label){

        baseParse(label, (text, index) -> {
            String replacement = null;

            if(!text.isEmpty() && text.charAt(1) == '$'){ //variable
                String varname = text.substring(1);
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
            }else if(text.equals("/color")){ //end color
                replacement = "[#" + label.getClearColor().toString() + "]";
            }else if(text.equals("reset")){ //reset
                replacement = RESET_REPLACEMENT + label.getDefaultToken();
            }

            return replacement;
        });
    }

    private static void parseRegularTokens(TypeLabel label){
        baseParse(label, (text, index) -> {
            float floatValue = 0;
            String stringValue = null;
            Effect effect = null;
            int indexOffset = 0;

            TokenCategory tokenCategory = TokenCategory.EVENT;
            InternalToken tmpToken = InternalToken.fromName(text);
            if(tmpToken == null){
                if(TypingConfig.EFFECTS.containsKey(text)){
                    tokenCategory = TokenCategory.EFFECT_START;
                }else if(!text.isEmpty() && TypingConfig.EFFECTS.containsKey(text.substring(1))){
                    tokenCategory = TokenCategory.EFFECT_END;
                }
            }else{
                tokenCategory = tmpToken.category;
            }

            switch(tokenCategory){
                case WAIT:{
                    floatValue = TypingConfig.DEFAULT_WAIT_VALUE;
                    break;
                }
                case EVENT:{
                    stringValue = text;
                    indexOffset = -1;
                    break;
                }
                case SPEED:{
                    switch(text){
                        case "slower":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.500f;
                            break;
                        case "slow":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 0.667f;
                            break;
                        case "normal":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR;
                            break;
                        case "fast":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 2.000f;
                            break;
                        case "faster":
                            floatValue = TypingConfig.DEFAULT_SPEED_PER_CHAR / 4.000f;
                            break;
                    }
                    break;
                }
                case EFFECT_START:{
                    effect = TypingConfig.EFFECTS.get(text).get(label);
                    effect.endToken = "/" + text;
                    break;
                }
                case EFFECT_END:{
                    break;
                }
            }

            TokenEntry entry = new TokenEntry(text, tokenCategory, index + indexOffset - 1, floatValue, stringValue);
            entry.effect = effect;
            label.tokenEntries.add(entry);

            return "{" + text + "}";
        });
    }

    private static void parseAllTokens(TypeLabel label, boolean square, Intc2 handler){
        StringBuilder text = label.getText();

        for(int i = 0; i < text.length(); i++){
            char c = text.charAt(i);
            if(c == '\\'){
                //escaped token, skip and continue
                i ++;
                continue;
            }

            //search for an end to the token
            if(c == '['){
                for(int j = i + 1; j < text.length(); j++){
                    if(text.charAt(j) == ']'){
                        //found token end!
                        int tokenFrom = i + 1;
                        int tokenTo = j;
                        handler.get(tokenFrom, tokenTo);
                        break;
                    }
                }
            }else if(c == '{'){
                for(int j = i + 1; j < text.length(); j++){
                    if(text.charAt(j) == '}'){
                        //found token end!
                        int tokenFrom = i + 1;
                        int tokenTo = j;
                        handler.get(tokenFrom, tokenTo);
                        break;
                    }
                }
            }
        }
    }

    private static void stripTokens(TypeLabel label){
        baseParse(label, (text, index) -> "");

        int[] offset = {0};
        //label.tokenEntries.add(new TokenEntry("SKIP", TokenCategory.SKIP, 4, 0, new String(new char[8])));
        //label.tokenEntries.add(new TokenEntry("SKIP", TokenCategory.SKIP, 16, 0, new String(new char[2])));

        //must be a square token
        parseAllTokens(label, true, (from, to) -> {
            //Log.info(label.getText().substring(from - 1, to + 1));
            //label.tokenEntries.add(new TokenEntry("SKIP", TokenCategory.SKIP, from - 2, 0, label.getText().substring(from - 1, to + 1)));
            //Log.info("index = " + (from - 1 + offset[0]) + " from = "+ from);
            offset[0] -= 2;
        });
    }

    /** Returns the replacement string intended to be used on {RESET} tokens. */
    private static String getResetReplacement(){
        Array<String> tokens = new Array<>();
        TypingConfig.EFFECTS.keys().toArray(tokens);
        tokens.replace(m -> "/" + m);
        tokens.add("clear");
        tokens.add("normal");

        StringBuilder sb = new StringBuilder();
        for(String token : tokens){
            sb.append("{").append(token).append('}');
        }
        return sb.toString();
    }

    private interface TokenHandler{
        String handle(String string, int position);
    }

}
