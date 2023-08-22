package utils.command;

import arc.util.command.CommandParamParser;
import arc.util.command.CommandParamSplitter;
import arc.util.command.CommandParams;
import org.junit.Assert;
import org.junit.Test;

public class SplitterTest {
    static <T> T[] array(T... array) {
        return array;
    }

    @Test
    public void test() {
        //noinspection ParameterOrder,VariadicParamPosition
        CommandParams params = CommandParamParser.parse("[a] <b...> [c] <d>");
        Assert.assertTrue(CommandParamSplitter.split("1", params).tooFew);

        //noinspection ParameterOrder,VariadicParamPosition
        Assert.assertTrue(CommandParamSplitter.split("1 2 3 4", CommandParamParser.parse("<a> <b> <c>")).tooMany);
        Assert.assertArrayEquals(
                array("1", "2"),
                CommandParamSplitter.split("1 2", params).args
        );
        Assert.assertArrayEquals(
                array("1", "2", "3"),
                CommandParamSplitter.split("1 2 3", params).args
        );
        Assert.assertArrayEquals(
                array("1", "2", "3", "4"),
                CommandParamSplitter.split("1 2 3 4", params).args
        );
        Assert.assertArrayEquals(
                array("1", "2 3", "4", "5"),
                CommandParamSplitter.split("1 2 3 4 5", params).args
        );
    }
}
