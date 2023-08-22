package arc.util.command;

import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;

public class CommandParamParser{
    private static final byte SEARCH_PARAM = 0;
    private static final byte PARSING_REQUIRED = 1;
    private static final byte PARSING_OPTIONAL = 2;
    private static final Seq<TextRegion> tmpRegions = new Seq<>();
    private static final Pool<TextRegion> textRegionPool = new Pool<TextRegion>(){
        @Override
        protected TextRegion newObject(){
            return new TextRegion(-1, -1){
            };
        }
    };

    public static CommandParams parse(String text) throws CommandParamParseException{
        byte state = SEARCH_PARAM;
        int begin = -1;
        clear();
        for(int i = 0; i < text.length(); i++){
            char c = text.charAt(i);
            switch(state){
                case SEARCH_PARAM:
                    if(c != ' ' && c != '<' && c != '[')
                        throwException("Unexpected char '" + c + "'", i, text);
                    if(c == '<' || c == '['){
                        state = c == '<' ? PARSING_REQUIRED : PARSING_OPTIONAL;
                        begin = i;
                    }
                    break;

                case PARSING_REQUIRED:
                    if(c == '>'){
                        state = completeParam(text, begin, i + 1);
                    }
                    break;

                case PARSING_OPTIONAL:
                    if(c == ']'){
                        state = completeParam(text, begin, i + 1);
                    }
                    break;

            }
        }
        CommandHandler.CommandParam[] params = new CommandHandler.CommandParam[tmpRegions.size];
        boolean wasVariadic = false;
        for(int i = 0; i < tmpRegions.size; i++){
            TextRegion region = tmpRegions.get(i);
            boolean isVariadic = false;
            int nameOffset = 0;
            if(region.length() > 5){
                for(int j = 0; ; j++){
                    if(text.charAt(region.end - i - 1) != '.') break;
                    if(j == 2){
                        if(wasVariadic){

                            throwException("Cannot be more than one variadic parameter!", region, text);
                        }
                        isVariadic = wasVariadic = true;
                        nameOffset = 3;
                        break;
                    }
                }
            }
            params[i] = new CommandHandler.CommandParam(
            text.substring(region.start + 1, region.end - 1 - nameOffset),
            text.charAt(region.start) == '[',
            isVariadic
            );
        }
        clear();
        return new CommandParams(params);
    }

    private static void clear(){
        textRegionPool.freeAll(tmpRegions);
        tmpRegions.clear();
    }

    private static byte completeParam(String text, int begin, int end){
        if(end - begin <= 2){
            throwException("Malformed param '" + text.substring(begin, end) + "'",
            begin, end, text

            );
        }
        tmpRegions.add(textRegion(begin, end));
        return SEARCH_PARAM;
    }

    private static TextRegion textRegion(int begin, int end){

        return textRegionPool.obtain().set(begin, end);
    }

    static void throwException(String message, int startIndex, int endIndex, String rawText){
        throw new CommandParamParseException(message, startIndex, endIndex, rawText);
    }

    static void throwException(String message, int symbolIndex, String rawText){
        throw new CommandParamParseException(message, symbolIndex, symbolIndex + 1, rawText);
    }

    static void throwException(String message, TextRegion region, String rawText){
        throw new CommandParamParseException(message, region.start, region.end, rawText);
    }


    private static abstract class TextRegion{
        public int start;
        public int end;

        private TextRegion(int start, int end){
            this.start = start;
            this.end = end;
        }

        public TextRegion set(int start, int end){
            this.start = start;
            this.end = end;
            return this;
        }

        public int length(){
            return end - start;
        }

        public String substring(String text){
            return text.substring(start, end);
        }
    }
}
