package hu.tinca.poi.xirr;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Utility methods for Excel model handling.
 */
public class Util {
    private Util() {}

    static Cell getCell(Sheet calcSheet, int row, int col) {
        Row r = calcSheet.getRow(row - 1);
        return r.getCell(col - 1);
    }

    static Cell getCell(Sheet calcSheet, int row, char col) {
        Row r = calcSheet.getRow(row - 1);
        return r.getCell(getCol(col) - 1);
    }

    static Cell getCell(Sheet calcSheet, int row, String col) {
        Row r = calcSheet.getRow(row - 1);
        return r.getCell(getCol(col) - 1);
    }

    static void setCellValue(Sheet calcSheet, int row, char col, double v) {
        getCell(calcSheet, row, col).setCellValue(v);
    }

    static void setCellValue(Sheet calcSheet, int row, char col, BigDecimal v) {
        getCell(calcSheet, row, col).setCellValue(v.doubleValue());
    }

    static void setCellValue(Sheet calcSheet, int row, char col, String v) {
        getCell(calcSheet, row, col).setCellValue(v);
    }

    static void setCellValue(Sheet calcSheet, int row, char col, Date v) {
        getCell(calcSheet, row, col).setCellValue(v);
    }

    private static int getCol(char c) {
        char ch = Character.toLowerCase(c);
        return Character.getNumericValue(ch) - Character.getNumericValue('a') + 1;
    }

    private static int getCol(String s) {
        if (s.length() > 2 || s.length() < 1) {
            throw new IllegalArgumentException("Only 1 or 2 letter string is allowed");
        }

        if (s.length() == 2) {
            return 26 + getCol(s.charAt(1));
        }
        else {
            return getCol(s.charAt(0));
        }
    }

    static void log(Sheet calcSheet, int row, char col, Object v) {
        System.out.println(String.format("%s%s: old - new: %s - %s", col, row, getCell(calcSheet, row, col), v));
    }

    static void log(Sheet calcSheet, int row, char col) {
        System.out.println(String.format("%s%s: %s", col, row, getCell(calcSheet, row, col).getNumericCellValue()));
    }

    static void logAsString(Sheet calcSheet, int row, char col) {
        System.out.println(String.format("%s%s: %s", col, row, getCell(calcSheet, row, col)));
    }

    static void log(Sheet calcSheet, int row, String col) {
        System.out.println(String.format("%s%s: %s", col, row, getCell(calcSheet, row, col).getNumericCellValue()));
    }


    private static void logValue(int row, String col, Object exp, Object got) {
        System.out.println(String.format("%s%s: old - new: %s - %s", col, row, exp, got));
    }

    private static void logCell(Sheet sheet, int row, String col, char err) {
        //double d = evaluator.evaluate(Util.getCell(calcSheet, row, col)).getNumberValue();
        double d = Util.getCell(sheet, row, col).getNumericCellValue();
        System.out.println(String.format("%s%s%s: %s", err, col, row, d));
    }

    private static void logCell(Sheet sheet, int row, char col, char err) {
        //double d = evaluator.evaluate(Util.getCell(calcSheet, row, col)).getNumberValue();
        double d = Util.getCell(sheet, row, col).getNumericCellValue();
        System.out.println(String.format("%s%s%s: %s", err, col, row, d));
    }

}
