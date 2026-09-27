package utils.command;

import arc.util.command.CommandParamParseException;
import arc.util.command.CommandParamParser;
import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

    static void checkError(String message, Runnable runnable) {
        try {
            runnable.run();
            if (message != null) Assert.fail("Expected error with message: '" + message + "'");
        } catch (CommandParamParseException e) {
            Assert.assertEquals(message, e.getMessage());
        }
    }

    @Test()
    public void testUnexpectedChar() {

        checkError("Unexpected char 'd' at [11:12]\n" +
                   "[a] <b...> d[c] <d>\n" +
                   "           ^\n" +
                   "           Unexpected char 'd'", () -> {
            //noinspection ParameterOrder,VariadicParamPosition
            CommandParamParser.parse("[a] <b...> d[c] <d>");
        });
        checkError("Cannot be more than one variadic parameter! at [11:17]\n" +
                   "[a] <b...> [c...] <d>\n" +
                   "           ^^^^^^\n" +
                   "           Cannot be more than one variadic parameter!", () -> {
            //noinspection ParameterOrder,VariadicParamPosition
            CommandParamParser.parse("[a] <b...> [c...] <d>");
        });
        checkError("Malformed param '<>' at [15:17]\n" +
                   "[a] <b...> [c] <>\n" +
                   "               ^^\n" +
                   "               Malformed param '<>'", () -> {
            //noinspection ParameterOrder,VariadicParamPosition
            CommandParamParser.parse("[a] <b...> [c] <>");
        });
        checkError(null, () -> {
            //noinspection ParameterOrder,VariadicParamPosition
            CommandParamParser.parse("[a]<b...>[c]<d>");
        });
    }
}
