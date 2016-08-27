package hu.tinca.poi.xirr;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * TODO: test excel file
 */
public class XirrFunctionTest {
    private HSSFSheet calcSheet;
    private FormulaEvaluator evaluator;

    @Test
    public void xirrCalledByPOI() throws IOException {
        XirrFunction.register();

        File xls = null;
        try (FileInputStream in = new FileInputStream(xls)) {
            POIFSFileSystem fs = new POIFSFileSystem(in);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            calcSheet = wb.getSheet("XXXXXX");
            evaluator = wb.getCreationHelper().createFormulaEvaluator();
        }

        Cell calcSpec = Util.getCell(calcSheet, 156, "AW");
        evaluator.evaluateFormulaCell(calcSpec);
    }
}
