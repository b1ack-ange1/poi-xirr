package hu.tinca.poi.xirr;

import org.testng.annotations.Test;

import java.text.ParseException;

/**
 * When in doubt if for a given data set this Java implementation fails create new test case with the same input params
 * as in Excel and compare calculated value with that of Excel.
 */
public class XirrPrimitiveTest {

    @Test(dataProvider = "create", dataProviderClass = XirrDataProvider.class)
    public void testNewtonMethod(Configuration c) throws ParseException {
        double res = XirrPrimitive.newtonMethod(c.getValues(), c.getDates(), c.getGuess());

        assertValue(c.getExpectation(), res);
    }

//    @Test(dataProvider = "create", dataProviderClass = XirrDataProvider.class)
//    public void testBisectionMethod(Configuration c) throws ParseException {
//        double res = XirrPrimitive.bisectionMethod(c.getValues(), c.getDates(), c.getGuess());
//
//         assertValue(c.getExpectation(), res);
//    }

    private void assertValue(double tester, double testee) {
        double diff = 100 * Math.abs(tester - testee);
        assert  diff < 0.005 : String.format("Expected/got/diff: %s, %s, %s", tester, testee, diff);
    }


}
