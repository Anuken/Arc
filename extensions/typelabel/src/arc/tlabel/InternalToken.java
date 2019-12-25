package arc.tlabel;

enum InternalToken{
    WAIT("WAIT", TokenCategory.WAIT),
    SPEED("SPEED", TokenCategory.SPEED),
    SLOWER("SLOWER", TokenCategory.SPEED),
    SLOW("SLOW", TokenCategory.SPEED),
    NORMAL("NORMAL", TokenCategory.SPEED),
    FAST("FAST", TokenCategory.SPEED),
    FASTER("FASTER", TokenCategory.SPEED),
    COLOR("COLOR", TokenCategory.COLOR),
    CLEARCOLOR("CLEARCOLOR", TokenCategory.COLOR),
    ENDCOLOR("ENDCOLOR", TokenCategory.COLOR),
    VAR("VAR", TokenCategory.VARIABLE),
    EVENT("EVENT", TokenCategory.EVENT),
    RESET("RESET", TokenCategory.RESET),
    SKIP("SKIP", TokenCategory.SKIP);

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
