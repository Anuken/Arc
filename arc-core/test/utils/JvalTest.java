package utils;

import arc.Files.*;
import arc.files.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import org.junit.*;
import static org.junit.Assert.*;

public class JvalTest{

    @Test
    public void parseUnquotedStringArray(){
        Jval val = Jval.read("{\nkey: [result, result2]\n}");
        assertEquals("result", val.get("key").asArray().get(0).asString());
        assertEquals("result2", val.get("key").asArray().get(1).asString());
    }

    @Test
    public void parseUnquotedString(){
        Jval val = Jval.read("name: Molten Silver\n" +
        "description: Imagine silver, but not solid at all.\n" +
        "temperature: 0.9\n" +
        "viscosity: 0.8\n" +
        "effect: melting\n" +
        "color: a4a2bd");

        assertEquals("Imagine silver, but not solid at all.", val.getString("description"));
    }

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
        //not an actual benchmark, ignore
        Fi file = new Fi("generated.json", FileType.classpath);
        String text = file.readString();

        Runnable hjson = () -> {
            Jval val = Jval.read(text);
        };

        Runnable json = () -> {
            JsonValue value = new JsonReader().parse(text);
        };

        //warmup
        int iterations = 10;
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
