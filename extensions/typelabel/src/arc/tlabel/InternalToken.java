package arc.tlabel;

enum InternalToken{
    WAIT("WAIT", TokenCategory.wait),
    SPEED("SPEED", TokenCategory.speed),
    SLOWER("SLOWER", TokenCategory.speed),
    SLOW("SLOW", TokenCategory.speed),
    NORMAL("NORMAL", TokenCategory.speed),
    FAST("FAST", TokenCategory.speed),
    FASTER("FASTER", TokenCategory.speed),
    COLOR("COLOR", TokenCategory.color),
    CLEARCOLOR("CLEARCOLOR", TokenCategory.color),
    ENDCOLOR("ENDCOLOR", TokenCategory.color),
    VAR("VAR", TokenCategory.variable),
    EVENT("EVENT", TokenCategory.event),
    RESET("RESET", TokenCategory.reset),
    SKIP("SKIP", TokenCategory.skip);

    final String name;
    final TokenCategory category;

    InternalToken(String name, TokenCategory category){
        this.name = name;
        this.category = category;
    }

    @Override
    public String toString(){
        return name;
    }

    static InternalToken fromName(String name){
        if(name != null){
            for(InternalToken token : values()){
                if(name.equalsIgnoreCase(token.name)){
                    return token;
                }
            }
        }
        return null;
    }
}
