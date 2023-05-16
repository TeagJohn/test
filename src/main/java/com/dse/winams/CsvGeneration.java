package com.dse.winams;

import com.dse.guifx_v3.helps.UIController;
import com.dse.testcase_manager.TestCase;
import com.dse.util.Utils;
import com.dse.winams.converter.SimpleTreeBuilder;
import com.dse.winams.retriever.TestCaseRecord;
import com.dse.winams.retriever.TestSuiteRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CsvGeneration {
    private ICsvInfo info;
    private TestSuiteRecord testSuiteRecord;
    final String COMMA_DELIMITER = ",";
    final String NEW_LINE_SEPARATOR = "\n";
    final String MODULE = "mod";
    final String C_TYPE = "C";
    final String CPP_TYPE = "CPP";
    final String COMMENT = "#COMMENT";
    final String ATDEDITOR_USING = "0";
    final String TEST_DATA_ANALYSIS = "1";

    public CsvGeneration(ICsvInfo csvInfo, TestSuiteRecord testSuiteRecord) {
        this.info = csvInfo;
        this.testSuiteRecord = testSuiteRecord;
    }

    public void generateCsv() {
        File file = new File(info.getPath() + File.separator + info.getName() + ".csv");
        if (file.exists()) file.delete();
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Note: header line format: "mod, function name, , #inputs, #outputs, , , , C"
        String headerLine = MODULE
                + COMMA_DELIMITER + info.getFunction().getSingleSimpleName()
                + COMMA_DELIMITER
                + COMMA_DELIMITER + info.getInputs().size()
                + COMMA_DELIMITER + info.getOutputs().size()
                + COMMA_DELIMITER + COMMA_DELIMITER + COMMA_DELIMITER
                + COMMA_DELIMITER + C_TYPE
                + COMMA_DELIMITER + COMMA_DELIMITER + COMMA_DELIMITER
                + COMMA_DELIMITER + ATDEDITOR_USING
                + NEW_LINE_SEPARATOR;

        String commentLine = COMMENT + COMMA_DELIMITER;
        for (int i = 0; i < info.getInputs().size(); i++) {
            commentLine += info.getInputs().get(i).getAlias() + COMMA_DELIMITER;
        }
        for (int i = 0; i < info.getOutputs().size(); i++) {
            commentLine += info.getOutputs().get(i).getAlias() + COMMA_DELIMITER;
        }
        commentLine += NEW_LINE_SEPARATOR;

        List<String> testCaseLines = new ArrayList<>();

        for (TestCaseRecord testCaseRecord : testSuiteRecord) {
            String lineContent = COMMA_DELIMITER;
            for (int i = 0; i < testCaseRecord.size(); i++) {
                lineContent += testCaseRecord.get(i) + COMMA_DELIMITER;
            }
            lineContent += NEW_LINE_SEPARATOR;
            testCaseLines.add(lineContent);
        }

        String csvContent = headerLine + commentLine + String.join("", testCaseLines);
        Utils.writeContentToFile(csvContent, file.getPath());

        UIController.showSuccessDialog("A new csv file export at " + file.getPath(), "Export CSV", "Export CSV successfully!");
    }
}