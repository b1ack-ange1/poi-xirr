package hu.tinca.poi.xirr;

/*
This work is licensed under the Creative Commons Attribution-ShareAlike 3.0
Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to Creative
Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
Ported from http://stackoverflow.com/a/5185144/72437 C# code.
*/

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * About Xirr: https://support.office.com/en-za/article/XIRR-function-de1242ec-6477-445b-b11b-a303ad9adc9d
 * <p/>
 * Original source is at https://github.com/yccheok/xirr/blob/master/src/org/yccheok/quant/XIRR.java
 * <p/>
 * Made to comply with Java code conventions.
 *
 *  This implementation uses BigDecimal.
 * http://stackoverflow.com/questions/3579779/how-to-do-a-fractional-power-on-bigdecimal-in-java
 */
final class Xirr {
    private static String CLZ_NAME = Xirr.class.getName();
    private static Logger LOG = Logger.getLogger(CLZ_NAME);

    private static final BigDecimal TOL = createBig(1E-8);
    private static final BigDecimal EPSILON = createBig(1E-12);
    private static final BigDecimal V365 = createBig(365);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final DateTime MICROSOFT_XIRR_EPOCH = new DateTime(1990, 1, 1, 0, 0, DateTimeZone.forTimeZone(UTC));
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    private static final int SCALE = 15;

    static double newtonMethod(double[] payments, Date[] days, double guess) {
        double[] ds = new double[days.length];
        int i = 0;
        for (Date d : days) {
            ds[i++] = getDay(d);
        }

        return newtonMethod(payments, ds, guess);
    }

    private static int getDay(Date d) {
        DateTime t = new DateTime(d.getTime(), DateTimeZone.forTimeZone(UTC));

        return Days.daysBetween(MICROSOFT_XIRR_EPOCH, t).getDays() + 1;
    }

    private static double newtonMethod(double[] payments, double[] days, double guess) {
        BigDecimal x0 = createBig(guess);

        final int maxIteration = 216;
        int iteration = 0;

        do {
            BigDecimal x1 = x0.subtract(totalFXirr(payments, days, x0)
                    .divide(totalDfXirr(payments, days, x0), SCALE, ROUNDING));
            BigDecimal err = x1.subtract(x0).abs();
            x0 = x1;

            if (lessThanOrEqualsToTolerance(err)) {
                return x0.doubleValue();
            }
        }
        while (iteration++ < maxIteration);

        return Double.NaN;
    }

    private static boolean lessThanOrEqualsToTolerance(BigDecimal v) {
        int r = v.compareTo(TOL);
        return r == -1 || r == 0;
    }


    // TODO: make more efficient
    private static Bracket findBracket(double[] payments, double[] days, double guess) {
        final int maxIteration = 108;
        final BigDecimal step = createBig(0.5);

        BigDecimal left = createBig(guess);
        BigDecimal right = createBig(guess);

        BigDecimal resl = totalFXirr(payments, days, left);

        BigDecimal resr = resl;
        int iteration = 0;

        while ((resl.multiply(resr)).doubleValue() > 0 && iteration++ < maxIteration) {
            left = left.subtract(step);
            resl = totalFXirr(payments, days, left);

            if ((resl.multiply(resr)).doubleValue() <= 0) {
                break;
            }

            right = right.add(step);
            resr = totalFXirr(payments, days, right);
        }

        if ((resl.multiply(resr)).doubleValue() <= 0) {
            return new Bracket(left, right);
        }

        return null;
    }

    private static BigDecimal composeFunctions(BigDecimal f1, BigDecimal f2) {
        return f1.add(f2);
    }

    private static BigDecimal fXirr(BigDecimal p, BigDecimal dt, BigDecimal dt0, BigDecimal x) {
        if (x.doubleValue() <= -1) {
            x = EPSILON.subtract(createBig(1)); // Very funky ... Better check what an IRR <= -100% means
        }

        double op1 = x.add(createBig(1)).doubleValue();
        double op2 = dt0.subtract(dt).divide(V365, SCALE, ROUNDING).doubleValue();
        return p.multiply(createBig(Math.pow(op1, op2)));
    }

    private static BigDecimal dfXirr(BigDecimal p, BigDecimal dt, BigDecimal dt0, BigDecimal x) {
        final double op1 = x.add(createBig(1)).doubleValue();
        final double op2 = dt0.subtract(dt).divide(V365, SCALE, ROUNDING).subtract(createBig(1)).doubleValue();
        final BigDecimal res = createBig(1)
                .divide(V365, SCALE, ROUNDING)
                .multiply(dt0.subtract(dt))
                .multiply(p)
                .multiply(createBig(Math.pow(op1, op2)));

        LOG.logp(Level.FINEST, CLZ_NAME, "dfXirr", "result: {0}, op1: {1}, op2: {2}", new Object[] {res, op1, op2});
        return res;
    }

    private static BigDecimal totalFXirr(double[] payments, double[] days, BigDecimal x) {
        BigDecimal resf = createBig(0);

        BigDecimal firstDay = createBig(days[0]);
        for (int i = 0; i < payments.length; i++) {
            resf = composeFunctions(resf, fXirr(createBig(payments[i]), createBig(days[i]), firstDay, x));
        }

        return resf;
    }

    private static BigDecimal totalDfXirr(double[] payments, double[] days, BigDecimal x) {
        BigDecimal resf = createBig(0);

        BigDecimal firstDay = createBig(days[0]);
        for (int i = 0; i < payments.length; i++) {
            resf = composeFunctions(resf, dfXirr(createBig(payments[i]), createBig(days[i]), firstDay, x));
            LOG.logp(Level.FINEST, CLZ_NAME, "totalDfXirr", "result: {0}", resf);
        }

        return resf;
    }

    static double bisectionMethod(double[] payments, Date[] days, double guess) {
        double[] ds = new double[days.length];
        int i = 0;
        for (Date d : days) {
            ds[i++] = getDay(d);
        }

        return bisectionMethod(payments, ds, guess);
    }

    private static double bisectionMethod(double[] payments, double[] days, double guess) {
        Bracket bracket = findBracket(payments, days, guess);

        if (bracket == null) {
            return Double.NaN;
        }

        BigDecimal left = bracket.left;
        BigDecimal right = bracket.right;

        final int max_iteration = 216;
        int iteration = 0;
        BigDecimal c;

        do {
            c = (left.add(right).divide(createBig(2.0), SCALE, ROUNDING));
            BigDecimal resc = totalFXirr(payments, days, c);

            BigDecimal v = right.subtract(left).divide(createBig(2.0), SCALE, ROUNDING);
            if (lessThanOrEqualToEpsilon(resc.abs()) || lessThanTolerance(v)) {
                break;
            }

            BigDecimal resl = totalFXirr(payments, days, left);

            if ((resc.multiply(resl)).doubleValue() > 0) {
                left = c;
            } else {
                right = c;
            }
        }
        while (iteration++ < max_iteration);

        return c.doubleValue();
    }

    private static boolean lessThanOrEqualToEpsilon(BigDecimal v) {
        int r = v.compareTo(EPSILON);
        return r == -1 || r == 0;
    }

    private static boolean lessThanTolerance(BigDecimal v) {
        return v.compareTo(TOL) == -1;
    }

    private static BigDecimal createBig(double v) {
        return new BigDecimal(String.valueOf(v)).setScale(SCALE, RoundingMode.HALF_EVEN);
    }

    private static class Bracket {
        final BigDecimal left;
        final BigDecimal right;

        Bracket(BigDecimal left, BigDecimal right) {
            this.left = left;
            this.right = right;
        }
    }

}
