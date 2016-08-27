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
 * This implementation uses primitive types.
 */
final class XirrPrimitive {
    private static String CLZ_NAME = Xirr.class.getName();
    private static Logger LOG = Logger.getLogger(CLZ_NAME);

    private static final double tol = 1E-8;
    private static final double epsilon = 1E-12;
    private static TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static DateTime MICROSOFT_XIRR_EPOCH = new DateTime(1990, 1, 1, 0, 0, DateTimeZone.forTimeZone(UTC));

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
        double x0 = guess;

        final int maxIteration = 216;
        int iteration = 0;

        do {
            double x1 = x0 - totalFXirr(payments, days, x0) / totalDfXirr(payments, days, x0);
            double err = Math.abs(x1 - x0);
            x0 = x1;

            if (err <= tol) {
                return x0;
            }
        }
        while (iteration++ < maxIteration);

        return Double.NaN;
    }

    private static Bracket findBracket(double[] payments, double[] days, double guess) {
        final int maxIteration = 108;
        final double step = 0.5;

        double left = guess;
        double right = guess;

        double resl = totalFXirr(payments, days, left);

        double resr = resl;
        int iteration = 0;

        while ((resl * resr) > 0 && iteration++ < maxIteration) {
            left = left - step;
            resl = totalFXirr(payments, days, left);

            if ((resl * resr) <= 0) {
                break;
            }

            right = right + step;
            resr = totalFXirr(payments, days, right);
        }

        if ((resl * resr) <= 0) {
            return new Bracket(left, right);
        }

        return null;
    }

    private static double composeFunctions(double f1, double f2) {
        return f1 + f2;
    }

    private static double fXirr(double p, double dt, double dt0, double x) {
        if (x <= -1) {
            x = -1 + epsilon; // Very funky ... Better check what an IRR <= -100% means
        }

        return p * Math.pow((1.0 + x), ((dt0 - dt) / 365.0));
    }

    private static double dfXirr(double p, double dt, double dt0, double x) {
        double op1 = x + 1.0;
        double op2 = (((dt0 - dt) / 365.0) - 1.0);
        double res = (1.0 / 365.0) * (dt0 - dt) * p * Math.pow(op1, op2);
        LOG.logp(Level.FINEST, CLZ_NAME, "dfXirr", "result: {0}, op1: {1}, op2: {2}", new Object[] {res, op1, op2});

        return res;
    }

    private static double totalFXirr(double[] payments, double[] days, double x) {
        double resf = 0.0;

        for (int i = 0; i < payments.length; i++) {
            resf = composeFunctions(resf, fXirr(payments[i], days[i], days[0], x));
        }

        return resf;
    }

    private static double totalDfXirr(double[] payments, double[] days, double x) {
        double resf = 0.0;

        for (int i = 0; i < payments.length; i++) {
            resf = composeFunctions(resf, dfXirr(payments[i], days[i], days[0], x));
            LOG.logp(Level.FINEST, CLZ_NAME, "totalDfXirr", "result: {0}", resf);
        }

        System.out.println();
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

        double left = bracket.left;
        double right = bracket.right;

        final int max_iteration = 216;
        int iteration = 0;
        double c = 0;

        do {
            c = (left + right) / 2;
            double resc = totalFXirr(payments, days, c);

            if (Math.abs(resc) <= epsilon || ((right - left) / 2.0) < tol) {
                break;
            }

            double resl = totalFXirr(payments, days, left);

            if ((resc * resl) > 0) {
                left = c;
            } else {
                right = c;
            }
        }
        while (iteration++ < max_iteration);

        return c;
    }

    private static class Bracket {
        final double left;
        final double right;

        Bracket(double left, double right) {
            this.left = left;
            this.right = right;
        }
    }

}
