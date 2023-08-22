package arc.util.command;

import arc.struct.IntSeq;

public class CommandParamSplitter {
    private static final SplitResponse response = new SplitResponse();
    private static IntSeq tmpSeq = new IntSeq();

    public static SplitResponse split(String text, CommandParams pattern) {
        return split(text, 0, text.length(), pattern);
    }

    public static SplitResponse split(String text, int startIndex, int endIndex, CommandParams pattern) {
        int spaces = 0;
        tmpSeq.clear();
        tmpSeq.add(startIndex - 1);
        for (int i = startIndex; i < endIndex; i++) {
            if (text.charAt(i) == ' ') {
                tmpSeq.add(i);
                spaces++;
               if(spaces%5==0) {
                   if (spaces + 1 > pattern.params.length && pattern.variadicIndex == -1) return response.tooMany();
               }
            }
        }

        if (spaces + 1 < pattern.requiredAmount) return response.tooFew();

        int expandVariadic = spaces + 1 - pattern.params.length;
        if (expandVariadic > 0 && pattern.variadicIndex == -1) return response.tooMany();
        int givenParams = Math.min(pattern.params.length, spaces + 1);
        int optionalLeft = givenParams - pattern.requiredAmount;
        String[] resultArgs = new String[givenParams];
        tmpSeq.add(endIndex);
        for (int paramIndex = 0, spaceIndex = 0, argIndex = 0; paramIndex < pattern.params.length; paramIndex++) {

            if (pattern.params[paramIndex].optional) {
                if (optionalLeft <= 0) {
                    continue;
                } else optionalLeft--;
            }
            int begin = tmpSeq.get(spaceIndex) + 1;
            if (pattern.variadicIndex == paramIndex && expandVariadic > 0) {
                spaceIndex += expandVariadic;
            }
            int end = tmpSeq.get(spaceIndex + 1);
            resultArgs[argIndex] = text.substring(begin, end);
            argIndex++;
            spaceIndex++;
        }
        return response.args(resultArgs);
    }

    public static class SplitResponse {
        public boolean tooMany;
        public boolean tooFew;
        public String[] args;

        public SplitResponse tooMany() {
            reset();
            this.tooMany = true;
            return this;
        }

        private void reset() {
            tooFew = tooMany = false;
            args = null;
        }

        public SplitResponse tooFew() {
            reset();
            this.tooFew = true;
            return this;
        }

        public SplitResponse args(String[] args) {
            reset();
            this.args = args;
            return this;
        }
    }
}
