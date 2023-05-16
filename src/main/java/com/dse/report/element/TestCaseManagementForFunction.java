package com.dse.report.element;

import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestCaseManagementForFunction extends AbstractTestCaseManagement {

    private static final AkaLogger logger = AkaLogger.get(TestCaseManagementForFunction.class);

    private final ICommonFunctionNode functionNode;

    public TestCaseManagementForFunction(ICommonFunctionNode functionNode) {
        super();
        this.functionNode = functionNode;
        generate();
    }

    @Override
    protected void generate() {
        title.add(new Line("Test Case Management", COLOR.DARK));

        Table table = new Table(false);
        table.getRows().add(new Table.HeaderRow("Test Cases", "Execution Date and Time", "Pass/Fail"));
        List<Table.Row> row = generateBasicTable(functionNode);
        table.getRows().addAll(row);
        body.add(table);
    }

    private List<Table.Row> generateBasicTable(ICommonFunctionNode subprogram) {
        List<Table.Row> rows = new ArrayList<>();

        AssertionResult summaryResult = new AssertionResult();
        int testCaseLength = 0;

        logger.debug(String.format("Calculate assertion in Subprogram %s", subprogram.getName()));
        if (TestCaseManager.getFunctionToTestCasesMap().get(subprogram) != null) {
            List<String> testCaseNames = new ArrayList<>(TestCaseManager.getFunctionToTestCasesMap().get(subprogram));
            testCaseNames = testCaseNames.stream().distinct().collect(Collectors.toList());

            for (String name : testCaseNames) {
                TestCase testCase = TestCaseManager.getBasicTestCaseByNameWithoutData(name);

                if (testCase != null) {
                    Table.Row row = generateRow(testCase);
                    rows.add(row);

                    AssertionResult result = testCase.getExecutionResult();
                    if (result != null) {
                        if (result.isAllPass())
                            summaryResult.increasePass();
                        summaryResult.increaseTotal();
                    }

                    testCaseLength++;
                }
            }
        }

        Table.Row endRow = generateEndRow(String.valueOf(testCaseLength), summaryResult);
        rows.add(endRow);

        return rows;
    }

    private Table.Row generateEndRow(String testCases, AssertionResult summaryResult) {
        Table.Row endRow = new Table.Row(
                new Text("TOTAL", TEXT_STYLE.BOLD),
                new Text(testCases, TEXT_STYLE.BOLD)
        );

        Table.Cell<Text> statusCell = generateStatusCell(summaryResult);
        statusCell.getContent().setStyle(TEXT_STYLE.BOLD);
        endRow.getCells().add(statusCell);

        return endRow;
    }

    private Table.Row generateRow(ITestCase testCase) {
        String testCaseCol = testCase.getName();

        String execDateTimeCol = SpecialCharacter.EMPTY;
        if (testCase.getExecutionDateTime() != null)
            execDateTimeCol = testCase.getExecutionDate() + " " + testCase.getExecutionTime();

        Table.Row row = new Table.Row(testCaseCol, execDateTimeCol);

        AssertionResult result = testCase.getExecutionResult();
        row.getCells().add(generateStatusCell(result));

        return row;
    }
}
