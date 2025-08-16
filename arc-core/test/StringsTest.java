import arc.struct.*;
import arc.util.*;
import org.junit.*;

import static org.junit.Assert.*;

public class StringsTest{

    @Test
    public void testFixed(){
        Object[] values = {
        3, 1.327f,  "1.327",
        2, 1.327f,  "1.32",
        1, 1.327f,  "1.3",
        0, 1.327f,  "1",
        3, 1.3005f, "1.3",
        2, 0.0001f, "0",
        4, 0.0001f, "0.0001",
        3, 0.0001f, "0",
        2, 2.435f, "2.43",
        2, 2.99f, "2.99",
        2, 2.99999f, "3",
        2, 0f, "0"
        };

        for(int i = 0; i < values.length; i += 3){
            assertEquals(values[i + 2], Strings.autoFixed((Float)values[i + 1], (Integer)values[i]));
        }
    }

    @Test
    public void testLongParse(){
        Seq.with("0", "+0", "-0", "235235", "99424", "1234", "1", "-24242", "170589", "-289157", "4246", "19284", "-672396", "-42412042040945", "1592835012852095")
        .each(StringsTest::checkLong);
    }

    @Test
    public void testDoubleParse(){
        Seq.with("0", "0.0", "123.456", "123f", "145.6", "1e10", "-512515", "-535.646", "999.9344", "0.24324", ".325235", "3424324.", "+.31245", "-.51354", ".0", "-.0", "+.0", "0.000002", "200000.2000", "2000.00004", "-0.5")
        .each(StringsTest::checkDouble);
    }

    @Test
    public void testSanitizeFilename(){
        assertEquals(Strings.sanitizeFilename("test"), "test");
        assertEquals(Strings.sanitizeFilename("test.txt"), "test.txt");
        assertEquals(Strings.sanitizeFilename("test/test"), "test_test");
        assertEquals(Strings.sanitizeFilename("CON"), "_CON");
        assertEquals(Strings.sanitizeFilename("CON.a/test"), "_CON.a_test");
        assertEquals(Strings.sanitizeFilename("."), "_");
        assertEquals(Strings.sanitizeFilename(".."), "__");
        assertEquals(Strings.sanitizeFilename("..txt"), "..txt");
    }

    static void checkDouble(String value){
        assertEquals(Double.parseDouble(value), Strings.parseDouble(value, 999999), 0.00001);
    }

    static void checkLong(String value){
        assertEquals(Long.parseLong(value), Strings.parseLong(value, 999999));
    }

}
