package hu.tinca.poi.xirr;

import org.testng.annotations.DataProvider;

import java.text.ParseException;

/**
 *
 */
public class XirrDataProvider {

    @DataProvider
    public static Object[][] create() throws ParseException {
        return new Object[][] {
                case1()
        };


    }

    private static Object[] case1() throws ParseException {
        Configuration c = new Configuration();
        c.setGuess(0);
        
        c.add("2009-10-27", -27225);
        c.add("2009-12-25", 0);
        c.add("2010-01-25", 0);
        c.add("2010-02-25", 0);
        c.add("2010-03-25", 5885);
        c.add("2010-04-25", 4434);
        c.add("2010-05-25", 4434);
        c.add("2010-06-25", 4434);
        c.add("2010-07-25", 4434);
        c.add("2010-08-25", 4434);
        c.add("2010-09-25", 4434);
        c.add("2010-10-25", 4434);
        c.add("2010-11-25", 4434);
        c.add("2010-12-25", 4427);

        c.setExpectation(0.994862);
        return new Object[] {c};
    }


    private static Object[] case2() throws ParseException {
        Configuration c = new Configuration();
        c.setGuess(0);

        c.add("2001-01-01", -10000.3);
        c.add("2001-01-02", 2000.234);
        c.add("2001-03-15", 2500.456);
        c.add("2001-05-12", 5000.34);
        c.add("2001-08-10", 1000.56);
        c.setExpectation(0.195324907);

        return new Object[] {c};
    }

}
