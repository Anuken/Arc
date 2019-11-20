package utils;

import io.anuke.arc.Files.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;
import io.anuke.arc.util.serialization.*;
import io.anuke.arc.util.serialization.Jval.*;
import org.junit.*;

public class JvalTest{

    @Test
    public void parseJson(){
        //taken from hjson site
        Jval val = Jval.read("\n" +
        "\n" +
        "  // Live demo.\n" +
        "  // Type either into the Hjson or JSON input\n" +
        "  // to convert to the other format.\n" +
        "  // Reload to reset.\n" +
        "\n" +
        "  # comments are useful\n" +
        "  # specify rate in requests/second\n" +
        "  \"rate\": 1000\n" +
        "\n" +
        "  // maybe you prefer js style comments\n" +
        "  /* or if you feel old fashioned */\n" +
        "\n" +
        "  # key names do not need to be placed in quotes\n" +
        "  key: \"value\"\n" +
        "\n" +
        "  # you don't need quotes for strings\n" +
        "  text: look ma, no quotes!\n" +
        "\n" +
        "  # note that for quoteless strings everything up\n" +
        "  # to the next line is part of the string!\n" +
        "\n" +
        "  # commas are optional\n" +
        "  commas:\n" +
        "  {\n" +
        "    one: 1\n" +
        "    two: 2\n" +
        "  }\n" +
        "\n" +
        "  # trailing commas are allowed\n" +
        "  trailing:\n" +
        "  {\n" +
        "    one: 1,\n" +
        "    two: 2,\n" +
        "  }\n" +
        "\n" +
        "  # multiline string\n" +
        "  haiku:\n" +
        "    '''\n" +
        "    aba1111111111111111111111111111111111basb\n" +
        "    ddddddddddddd1111111dyfh3289gh2goui3ho3hgohjgo3hghddddddddddddddddddddddddddddddddddddddddd\n" +
        "    eeeeeeeeeeeeeeesehshhhsher5hysghyr5yherd5yhg5edhy5syhedyhedyhedyhesdyhgeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeekkkkkllllbvbbbcbZzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee\n" +
        "    '''\n" +
        "\n" +
        "  # Obviously you can always use standard JSON syntax as well:\n" +
        "  favNumbers: [ 1, 2, 3, 6, 42 ]\n" +
        "\n" +
        "  ");

        Log.info(val.toString(Jformat.formatted));
    }

    @Test
    public void benchmarkJson(){
        FileHandle file = new FileHandle("generated.json", FileType.Classpath);
        String text = file.readString();

        Runnable hjson = () -> {
            Jval val = Jval.read(text);
        };

        Runnable json = () -> {
            JsonValue value = new JsonReader().parse(text);
        };

        //warmup
        int iterations = 10000;
        for(int i = 0; i < iterations; i++){
            hjson.run();
            json.run();
        }

        Time.mark();
        for(int i = 0; i < iterations; i++){
            json.run();
        }
        Log.info("Time taken to parse json: {0}ms", Time.elapsed());

        Time.mark();
        for(int i = 0; i < iterations; i++){
            hjson.run();
        }
        Log.info("Time taken to parse H-json: {0}ms", Time.elapsed());
    }
}
