package arc.tlabel;

/** Container representing a token, parsed parameters and its position in text. */
class TokenEntry implements Comparable<TokenEntry>{
    String token;
    TokenCategory category;
    int index;
    float floatValue;
    String stringValue;
    Effect effect;

    TokenEntry(String token, TokenCategory category, int index, float floatValue, String stringValue){
        this.token = token;
        this.category = category;
        this.index = index;
        this.floatValue = floatValue;
        this.stringValue = stringValue;
    }

    @Override
    public int compareTo(TokenEntry o){
        return Integer.compare(index, o.index);
    }

}
