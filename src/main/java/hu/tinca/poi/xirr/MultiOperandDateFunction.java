package hu.tinca.poi.xirr;

import org.apache.poi.ss.formula.ThreeDEval;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.usermodel.DateUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * A quick hack (?) that copies POI's MultiOperandNumericFunction. Not sure if it is really needed.
 */
abstract class MultiOperandDateFunction implements Function {
    private final boolean _isReferenceBoolCounted;
    private final boolean _isBlankCounted;
    static final Date[] EMPTY_DOUBLE_ARRAY = new Date[0];

    MultiOperandDateFunction(boolean isReferenceBoolCounted, boolean isBlankCounted) {
        this._isReferenceBoolCounted = isReferenceBoolCounted;
        this._isBlankCounted = isBlankCounted;
    }

    public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
        double d;
        try {
            Date[] e = this.getDateArray(args);
            d = this.evaluate(null);
        } catch (EvaluationException var7) {
            return var7.getErrorEval();
        }

        return (!Double.isNaN(d) && !Double.isInfinite(d) ? new NumberEval(d) : ErrorEval.NUM_ERROR);
    }

    protected abstract double evaluate(double[] var1) throws EvaluationException;

    private int getMaxNumOperands() {
        return 30;
    }

    final Date[] getDateArray(ValueEval[] operands) throws EvaluationException {
        if (operands.length > this.getMaxNumOperands()) {
            throw EvaluationException.invalidValue();
        } else {
            DateList retval = new DateList();
            int i = 0;

            for (int iSize = operands.length; i < iSize; ++i) {
                this.collectValues(operands[i], retval);
            }

            return retval.toArray();
        }
    }

    private boolean isSubtotalCounted() {
        return true;
    }

    private void collectValues(ValueEval operand, DateList temp) throws EvaluationException {
        int sIx;
        int height;
        int rrIx;
        int rcIx;
        if (operand instanceof ThreeDEval) {
            ThreeDEval var11 = (ThreeDEval) operand;

            for (sIx = var11.getFirstSheetIndex(); sIx <= var11.getLastSheetIndex(); ++sIx) {
                height = var11.getWidth();
                rrIx = var11.getHeight();

                for (rcIx = 0; rcIx < rrIx; ++rcIx) {
                    for (int var12 = 0; var12 < height; ++var12) {
                        ValueEval ve1 = var11.getValue(sIx, rcIx, var12);
                        if (this.isSubtotalCounted() || !var11.isSubTotal(rcIx, var12)) {
                            this.collectValue(ve1, true, temp);
                        }
                    }
                }
            }

        } else if (operand instanceof TwoDEval) {
            TwoDEval var10 = (TwoDEval) operand;
            sIx = var10.getWidth();
            height = var10.getHeight();

            for (rrIx = 0; rrIx < height; ++rrIx) {
                for (rcIx = 0; rcIx < sIx; ++rcIx) {
                    ValueEval ve = var10.getValue(rrIx, rcIx);
                    if (this.isSubtotalCounted() || !var10.isSubTotal(rrIx, rcIx)) {
                        this.collectValue(ve, true, temp);
                    }
                }
            }

        } else if (!(operand instanceof RefEval)) {
            this.collectValue(operand, false, temp);
        } else {
            RefEval re = (RefEval) operand;

            for (sIx = re.getFirstSheetIndex(); sIx <= re.getLastSheetIndex(); ++sIx) {
                this.collectValue(re.getInnerValueEval(sIx), true, temp);
            }

        }
    }

    private void collectValue(ValueEval ve, boolean isViaReference, DateList temp) throws EvaluationException {
        temp.add(getValue(ve));
    }

    private Date getValue(ValueEval arg) throws EvaluationException {
        if (arg instanceof NumberEval) {
            return getDate(((NumberEval) arg).getNumberValue());
        } else if(arg instanceof BlankEval) {
            return getDate(0.0D);
        } else {
            if(arg instanceof RefEval) {
                RefEval refEval = (RefEval)arg;
                if(refEval.getNumberOfSheets() > 1) {
                    throw new EvaluationException(ErrorEval.VALUE_INVALID);
                }

                ValueEval innerValueEval = refEval.getInnerValueEval(refEval.getFirstSheetIndex());
                if (innerValueEval instanceof NumberEval) {
                    return getDate(((NumberEval) innerValueEval).getNumberValue());
                }

                if (innerValueEval instanceof BlankEval) {
                    return getDate(0.0D);
                }
            }

            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }
    }

    private static Date getDate(double d) throws EvaluationException {
        Date startDate = DateUtil.getJavaDate(d);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        return calendar.getTime();
    }

    private static class DateList {
        private Date[] _array = new Date[8];
        private int _count = 0;

        DateList() {
        }

        Date[] toArray() {
            if (this._count < 1) {
                return MultiOperandDateFunction.EMPTY_DOUBLE_ARRAY;
            } else {
                Date[] result = new Date[this._count];
                System.arraycopy(this._array, 0, result, 0, this._count);
                return result;
            }
        }

        private void ensureCapacity(int reqSize) {
            if (reqSize > this._array.length) {
                int newSize = reqSize * 3 / 2;
                Date[] newArr = new Date[newSize];
                System.arraycopy(this._array, 0, newArr, 0, this._count);
                this._array = newArr;
            }

        }

        void add(Date value) {
            this.ensureCapacity(this._count + 1);
            this._array[this._count] = value;
            ++this._count;
        }
    }
}
