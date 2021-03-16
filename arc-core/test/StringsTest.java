import arc.struct.*;
import arc.util.*;
import org.junit.*;

import static org.junit.Assert.*;

public class StringsTest{

    @Test
    public void testLongParse(){
        Seq.with("0", "+0", "-0", "235235", "99424", "1234", "1", "-24242", "170589", "-289157", "4246", "19284", "-672396", "-42412042040945", "1592835012852095")
        .each(StringsTest::checkLong);
    }

    @Test
    public void testDoubleParse(){
        Seq.with("0", "0.0", "123.456", "123f", "145.6", "1e10", "-512515", "-535.646", "999.9344", "0.24324", ".325235", "3424324.", "+.31245", "-.51354", ".0", "-.0", "+.0", "0.000002", "200000.2000", "2000.00004")
        .each(StringsTest::checkDouble);
    }

    static void checkDouble(String value){
        assertEquals(Double.parseDouble(value), Strings.parseDouble(value, 999999), 0.00001);
    }

    static void checkLong(String value){
        assertEquals(Long.parseLong(value), Strings.parseLong(value, 999999));
    }

}
