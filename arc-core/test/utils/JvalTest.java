package utils;

import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import org.junit.*;

import static org.junit.Assert.*;

public class JvalTest{

    @Test
    public void parseExponential(){
        Jval val = Jval.read("{a: 1e-4, b: 1e92}");
        assertEquals(1e-4f, val.getDouble("a", 0f), 0.00001f);
        assertEquals(1e92, val.getDouble("b", 0f), 0.00001f);
    }

    @Test
    public void parseUnquotedStringArray(){
        Jval val = Jval.read("{\nkey: [result, result2]\n}");
        assertEquals("result", val.get("key").asArray().get(0).asString());
        assertEquals("result2", val.get("key").asArray().get(1).asString());
    }

    @Test
    public void parseUnquotedComma(){
        //unlike the official spec, this JVal implementation discards commas in unquoted strings
        Jval val = Jval.read("[\n" +
        "O, T,\n" +
        "]");

        assertEquals("O", val.asArray().get(0).asString());
        assertEquals("T", val.asArray().get(1).asString());

        Jval comma = Jval.read("it: it,");

        assertEquals("it", comma.getString("it"));
    }

    @Test
    public void parseUnquotedString(){
        Jval val = Jval.read("name: Molten Silver\n" +
        "description: Imagine silver, but not solid at all.\n" +
        "temperature: 0.9,//comment\n" +
        "viscosity: 0.8\n" +
        "effect: melting\n" +
        "color: a4a2bd\n" +
        "empty: \"\"");

        assertEquals("Imagine silver, but not solid at all.", val.getString("description"));
        assertEquals(0.8f, val.getFloat("viscosity", 0f), 0.0001f);
        assertEquals("", val.getString("empty", "not empty"));
    }

    @Test
    public void parseWithComma(){
        Jval val = Jval.read("name: always,");

        assertEquals(Jtype.string, val.get("name").getType());
        assertEquals("always", val.getString("name"));
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
        "  text: comma, no quotes!\n" +
        "\n" +
        "  # note that for quoteless strings everything up\n" +
        "  # to the next line is part of the string!\n" +
        "\n" +
        "  # commas are optional\n" +
        "  commas:\n" +
        "  {\n" +
        "    one: 1//comment\n" +
        "    two: 2\n" +
        "  }\n" +
        "\n" +
        "  # trailing commas are allowed\n" +
        "  trailing:\n" +
        "  {\n" +
        "    one: 1,//comment\n" +
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

        String expected = "{\"rate\":1000,\"key\":\"value\",\"text\":\"comma, no quotes!\",\"commas\":{\"one\":1,\"two\":2},\"trailing\":{\"one\":1,\"two\":2},\"haiku\":\"aba1111111111111111111111111111111111basb\\nddddddddddddd1111111dyfh3289gh2goui3ho3hgohjgo3hghddddddddddddddddddddddddddddddddddddddddd\\neeeeeeeeeeeeeeesehshhhsher5hysghyr5yherd5yhg5edhy5syhedyhedyhedyhesdyhgeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeekkkkkllllbvbbbcbZzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee\",\"favNumbers\": [1,2,3,6,42]}";

        assertEquals(expected, val.toString(Jformat.plain));
    }

    @Test
    public void parseTrickyKeyNames(){
        Jval val = Jval.read(
        "\"key with spaces\": spaced key\n" +
        "\"key.with.dots\": dotted key\n" +
        "\"123numericKey\": value\n" +
        "unquoted_key: regular value"
        );

        assertEquals("spaced key", val.getString("key with spaces"));
        assertEquals("dotted key", val.getString("key.with.dots"));
        assertEquals("value", val.getString("123numericKey"));
        assertEquals("regular value", val.getString("unquoted_key"));
    }

    @Test
    public void parseCommentsInTrickyPlaces(){
        Jval val = Jval.read(
        "// Header comment\n" +
        "# Hash comment\n" +
        "/* Block comment */\n" +
        "item: value // inline JS comment\n" +
        "item2: value2 # inline Hash comment\n" +
        "array: [\n" +
        "  /* block in array */ 1,\n" +
        "  2 // inline in array\n" +
        "]"
        );

        assertEquals("value", val.getString("item"));
        assertEquals("value2", val.getString("item2"));
        assertEquals(1, val.get("array").asArray().get(0).asInt());
        assertEquals(2, val.get("array").asArray().get(1).asInt());
    }

    @Test
    public void parseEdgeCasesAndLiteralTypes(){
        Jval val = Jval.read(
        "boolTrue: true\n" +
        "boolFalse: false\n" +
        "nullVal: null\n" +
        "stringTrue: \"true\"\n" +
        "stringNumber: \"12345\"\n" +
        "negativeNum: -987.654\n" +
        "zeroNum: 0"
        );

        assertTrue(val.get("boolTrue").asBool());
        assertFalse(val.get("boolFalse").asBool());
        assertTrue(val.get("nullVal").isNull());
        assertEquals(Jtype.string, val.get("stringTrue").getType());
        assertEquals(Jtype.string, val.get("stringNumber").getType());
        assertEquals(-987.654f, val.getFloat("negativeNum", 0f), 0.001f);
        assertEquals(0, val.getInt("zeroNum", -1));
    }

    @Test
    public void parseEmptyContainersAndWhitespace(){
        Jval val = Jval.read(
        "emptyObj: {}\n" +
        "emptyArr: []\n" +
        "commentObj: {\n" +
        "  // empty\n" +
        "}\n" +
        "commentArr: [\n" +
        "  # empty\n" +
        "]"
        );

        assertEquals(0, val.get("emptyObj").asObject().size);
        assertEquals(0, val.get("emptyArr").asArray().size);
        assertEquals(0, val.get("commentObj").asObject().size);
        assertEquals(0, val.get("commentArr").asArray().size);
    }

    @Test
    public void parseUnicodeEscapes(){
        Jval val = Jval.read(
        "smiley: \"\\u263A\"\n" +
        "combined: \"caf\\u00e9\"\n" +
        "emoji: \"\\ud83d\\ude00\"\n"
        );

        assertEquals("\u263A", val.getString("smiley"));
        assertEquals("café", val.getString("combined"));
        assertEquals("😀", val.getString("emoji"));
    }

    @Test
    public void parseExponentialAndLargeNumbers(){
        Jval val = Jval.read(
        "sci1: 1.5e10\n" +
        "sci2: 2E-5\n" +
        "sci3: 1e+3\n" +
        "maxLong: 9223372036854775807\n" +
        "minLong: -9223372036854775808\n" +
        "overflow: 99999999999999999999\n" +
        "tiny: 0.0000001\n"
        );

        assertEquals(1.5e10, val.getDouble("sci1", 0), 0.001);
        assertEquals(2E-5, val.getDouble("sci2", 0), 1e-10);
        assertEquals(1000.0, val.getDouble("sci3", 0), 0.001);
        assertEquals(9223372036854775807L, val.get("maxLong").asLong());
        assertEquals(Long.MIN_VALUE, val.get("minLong").asLong());
        assertEquals(1.0E20, val.getDouble("overflow", 0), 1e15); // too big for long, falls back to double
        assertEquals(0.0000001, val.getDouble("tiny", 0), 1e-12);
    }

    @Test
    public void parseNumberFormatsThatLookTricky(){
        Jval val = Jval.read(
        "leadingZero: 0123\n" +
        "justDot: .5\n" +
        "trailingDot: 5.\n" +
        "plusSign: +5\n" +
        "notANumber: 5-5\n" +
        "versionString: 1.2.3\n" +
        "negZero: -0\n" +
        "onlyMinus: -\n"
        );

        assertEquals(Jtype.string, val.get("leadingZero").getType());
        assertEquals("0123", val.getString("leadingZero"));
        assertEquals(".5", val.getString("justDot"));
        assertEquals("5.", val.getString("trailingDot"));
        assertEquals("+5", val.getString("plusSign"));
        assertEquals("5-5", val.getString("notANumber"));
        assertEquals("1.2.3", val.getString("versionString"));
        assertEquals(0, val.get("negZero").asInt());
        assertEquals("-", val.getString("onlyMinus"));
    }

    @Test
    public void parseBooleanAndNullLookalikes(){
        Jval val = Jval.read(
        "truthy: trueish\n" +
        "falsey: falsely\n" +
        "nully: nullable\n" +
        "exactTrue: true\n" +
        "exactFalse: false\n" +
        "exactNull: null\n"
        );

        assertEquals(Jtype.string, val.get("truthy").getType());
        assertEquals("trueish", val.getString("truthy"));
        assertEquals("falsely", val.getString("falsey"));
        assertEquals("nullable", val.getString("nully"));
        assertTrue(val.get("exactTrue").asBool());
        assertFalse(val.get("exactFalse").asBool());
        assertTrue(val.get("exactNull").isNull());
    }

    @Test
    public void parseEscapedCharactersInStrings(){
        Jval val = Jval.read(
        "escaped: \"line1\\nline2\\ttabbed\"\n" +
        "backslash: \"C:\\\\Users\\\\test\"\n" +
        "quotes: \"It said \\\"hi\\\"\"\n"
        );

        assertEquals("line1\nline2\ttabbed", val.getString("escaped"));
        assertEquals("C:\\Users\\test", val.getString("backslash"));
        assertEquals("It said \"hi\"", val.getString("quotes"));
    }

    @Test
    public void parseSingleLineTripleQuoteString(){
        Jval val = Jval.read(
        "note: '''single line note'''\n"
        );

        assertEquals("single line note", val.getString("note"));
    }

    @Test
    public void parseArraysWithMixedQuotelessAndQuoted(){
        Jval val = Jval.read(
        "items: [\n" +
        "  bare word,\n" +
        "  \"quoted string\",\n" +
        "  42,\n" +
        "  true,\n" +
        "  null\n" +
        "]\n"
        );

        assertEquals("bare word", val.get("items").asArray().get(0).asString());
        assertEquals("quoted string", val.get("items").asArray().get(1).asString());
        assertEquals(42, val.get("items").asArray().get(2).asInt());
        assertTrue(val.get("items").asArray().get(3).asBool());
        assertTrue(val.get("items").asArray().get(4).isNull());
    }

    @Test
    public void parseNestedObjectsAndArrays(){
        Jval val = Jval.read(
        "outer: {\n" +
        "  inner: {\n" +
        "    deep: value\n" +
        "  }\n" +
        "  list: [1, 2, [3, 4]]\n" +
        "}\n"
        );

        assertEquals("value", val.get("outer").get("inner").getString("deep"));
        assertEquals(4, val.get("outer").get("list").asArray().get(2).asArray().get(1).asInt());
    }

    @Test
    public void parseEmptyContainersAndStrings(){
        Jval val = Jval.read(
        "emptyObj: {}\n" +
        "emptyArr: []\n" +
        "emptyString: \"\"\n"
        );

        assertEquals(Jtype.object, val.get("emptyObj").getType());
        assertEquals(Jtype.array, val.get("emptyArr").getType());
        assertEquals(0, val.get("emptyArr").asArray().size);
        assertEquals("", val.getString("emptyString"));
    }

    @Test
    public void parseKeyNamesEdgeCases(){
        Jval val = Jval.read(
        "\"\": emptyKeyName\n" +
        "_underscore_key: value1\n" +
        "key-with-dashes: value2\n" +
        "key123: value3\n" +
        "café: value4\n" +
        "日本語: japanese value\n"
        );

        assertEquals("emptyKeyName", val.getString(""));
        assertEquals("value1", val.getString("_underscore_key"));
        assertEquals("value2", val.getString("key-with-dashes"));
        assertEquals("value3", val.getString("key123"));
        assertEquals("value4", val.getString("café"));
        assertEquals("japanese value", val.getString("日本語"));
    }

    @Test
    public void parseTrailingCommasAndNoTrailingNewline(){
        Jval val = Jval.read(
        "a: 1,\n" +
        "b: two,\n" +
        "noNewlineAtEnd: last value without newline"
        );

        assertEquals(1, val.getInt("a", -1));
        assertEquals("two", val.getString("b"));
        assertEquals("last value without newline", val.getString("noNewlineAtEnd"));
    }

    @Test
    public void parseQuotelessStringsWithInternalWhitespace(){
        Jval val = Jval.read(
        "spaced:   value   with   spaces   \n" +
        "single: word\n"
        );

        assertEquals("value   with   spaces", val.getString("spaced"));
        assertEquals("word", val.getString("single"));
    }
}
