package hu.tinca.poi.xirr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
class Configuration {
    private List<Date> dates = new ArrayList<>();
    private List<Double> values = new ArrayList<>();
    private double guess;
    private double expectation;

    void add(String date, double value) throws ParseException {
        dates.add(getDate(date));
        values.add(value);
    }

    Date[] getDates() {
        return dates.toArray(new Date[]{});
    }

    double[] getValues() {
        double[] v = new double[values.size()];
        int i = 0;
        for (Double d : values) {
            v[i++] = d;
        }

        return v;
    }

    void setGuess(double guess) {
        this.guess = guess;
    }

    double getGuess() {
        return guess;
    }

    double getExpectation() {
        return expectation;
    }

    void setExpectation(double expectation) {
        this.expectation = expectation;
    }

    private Date getDate(String d) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(d);
    }


}
