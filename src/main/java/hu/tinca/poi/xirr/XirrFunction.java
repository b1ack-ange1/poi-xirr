package hu.tinca.poi.xirr;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.functions.MultiOperandNumericFunction;
import org.apache.poi.ss.formula.functions.NumericFunction;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * POI function wrapper around Xirr, bootstraps function.
 */
class XirrFunction implements FreeRefFunction {
    private static Logger LOG = Logger.getLogger(XirrFunction.class.getName());

    private XirrFunction() {}

    static void register() {
        try {
            WorkbookEvaluator.registerFunction("Xirr", new XirrFunction());
        }
        catch(RuntimeException r) {
            LOG.warning("Already registered");
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext operationEvaluationContext) {
        if (args.length != 3) {
            return ErrorEval.VALUE_INVALID;
        }

        try {
            double[] payments = ValueCollector.collectValues(args[0]);
            Date[] days = DateValueCollector.collectValues(args[1]);
            if (LOG.isLoggable(Level.FINE)) {
                logInput(payments, days);
            }
            double res = XirrPrimitive.newtonMethod(payments, days, getNumValue(args[2]));
            NumericFunction.checkValue(res);
            return new NumberEval(res);
        } catch (EvaluationException e1) {
            return e1.getErrorEval();
        }
    }

    private void logInput(double[] payments, Date[] days) {
        LOG.fine("Input to Xirr");
        int i = 0;
        for (Date d : days) {
            LOG.log(Level.FINE, "{0}\t{1}", new Object[]{d, payments[i++]});
        }
    }

    private double getNumValue(ValueEval ve) throws EvaluationException {
        if (ve instanceof NumericValueEval) {
            return ((NumericValueEval) ve).getNumberValue();
        }

        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }

    /**
     * Copied from POI's Irr implementation visibility of which prevents its direct usage (avoided messing with
     * reflection).
     */
    private static final class ValueCollector extends MultiOperandNumericFunction {
        private static final ValueCollector INSTANCE = new ValueCollector();

        ValueCollector() {
            super(false, false);
        }

        static double[] collectValues(ValueEval... operands) throws EvaluationException {
            return INSTANCE.getNumberArray(operands);
        }

        protected double evaluate(double[] values) {
            throw new IllegalStateException("should not be called");
        }
    }

    private static final class DateValueCollector extends MultiOperandDateFunction {
        private static final DateValueCollector INSTANCE = new DateValueCollector();

        DateValueCollector() {
            super(false, false);
        }

        static Date[] collectValues(ValueEval... operands) throws EvaluationException {
            return INSTANCE.getDateArray(operands);
        }

        protected double evaluate(double[] values) {
            throw new IllegalStateException("should not be called");
        }
    }
}
