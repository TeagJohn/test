package com.dse.report.element;

import com.dse.coverage.CoverageDataObject;
import com.dse.coverage.CoverageManager;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_manager.AbstractTestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MetricsForFunction extends Section {

    private static final AkaLogger logger = AkaLogger.get(MetricsForFunction.class);

    private final ICommonFunctionNode functionNode;

    public MetricsForFunction(ICommonFunctionNode functionNode) {
        super("metrics");
        this.functionNode = functionNode;
        try {
            generate();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generate() throws InterruptedException {
        title.add(new Line("Metrics", COLOR.DARK));
        title.add(new Line(Environment.getInstance().getTypeofCoverage(), COLOR.MEDIUM));

        Table table = new Table(false);
        Table.HeaderRow headerRow = new Table.HeaderRow("Test Cases");
        String[] types = Environment.getInstance().getTypeofCoverage().split("\\+");
        Arrays.stream(types)
                .forEach(text -> {
                    char[] title = text.toLowerCase().toCharArray();
                    title[0] = Character.toUpperCase(title[0]);
                    Table.Cell<Text> cell = new Table.Cell<Text>(new String(title) +  " (File coverage)", COLOR.MEDIUM);

                    Table.Cell<Text> funcCell = new Table.Cell<Text>(new String(title) + " (Function coverage)", COLOR.MEDIUM);
                    headerRow.getCells().add(cell);
                    headerRow.getCells().add(funcCell);
                });

        table.getRows().add(headerRow);
        table.getRows().addAll(generateUnitRow(functionNode));
        table.getRows().add(new Table.Row(new Table.SpanCell<Text>(SpecialCharacter.EMPTY, headerRow.getCells().size())));

        body.add(table);
    }

    private List<Table.Row> generateUnitRow(ICommonFunctionNode subprogram) {
        List<Table.Row> rows = new ArrayList<>();

        String typeOfCoverage = Environment.getInstance().getTypeofCoverage();
        String[] typeItems = typeOfCoverage.split("\\+");

        logger.debug(String.format("Calculate metrics in Subprogram %s", subprogram.getName()));

        List<TestCase> testCases = getAllTestCaseOf(subprogram);
        List<TestCase> allTestCases = new ArrayList<>(testCases);

        for (TestCase testCase : testCases) {
            Table.Row row = new Table.Row(testCase.getName());
            List<Table.Cell<Text>> cells = computeCoverage(typeOfCoverage, testCase);
            row.getCells().addAll(cells);
            rows.add(row);
        }

        Table.Row totalRow = generateEndRow(allTestCases, typeOfCoverage);
        rows.add(totalRow);

        return rows;
    }

    private List<Table.Cell<Text>> computeCoverage(String typeOfCoverage, TestCase... testCases) {
        List<Table.Cell<Text>> cells = new ArrayList<>();

        switch (typeOfCoverage) {
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.MCDC:
            case EnviroCoverageTypeNode.BASIS_PATH: {
                Table.Cell<Text> coverageCell, functionCoverageCell;

                if (testCases.length > 0) {
                    CoverageDataObject srcCoverageData = CoverageManager
                            .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(Arrays.asList(testCases), typeOfCoverage);

                    if (srcCoverageData != null) {
                        int visited = srcCoverageData.getVisited();
                        int total = srcCoverageData.getTotal();
                        coverageCell = generateCoverageCell(visited, total);
                    } else {
                        coverageCell = generateCoverageCell(0, 0);
                    }

                    CoverageDataObject funcCoverageData = CoverageManager
                            .getCoverageOfMultiTestCaseAtFunctionLevel(Arrays.asList(testCases), typeOfCoverage);
                    if (funcCoverageData != null) {
                        functionCoverageCell = generateCoverageCell(funcCoverageData.getVisited(), funcCoverageData.getTotal());
                    } else {
                        functionCoverageCell = generateCoverageCell(0, 0);
                    }

                } else {
                    coverageCell = new Table.Cell<>(SpecialCharacter.EMPTY);
                    functionCoverageCell = new Table.Cell<>(SpecialCharacter.EMPTY);
                }

                cells.add(coverageCell);
                cells.add(functionCoverageCell);

                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:
            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                Table.Cell<Text>[] coverageCells = new Table.Cell[4];

                String[] typeItems = typeOfCoverage.split("\\+");

                if (testCases.length > 0) {
                    for (int i = 0; i < typeItems.length; i++) {
                        String coverageType = typeItems[i];

                        int srcTotal = 0, srcVisited = 0, funcVisited = 0, funcTotal = 0;

                        CoverageDataObject srcCoverageData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(Arrays.asList(testCases), coverageType);
                        if (srcCoverageData != null) {
                            srcVisited = srcCoverageData.getVisited();
                            srcTotal = srcCoverageData.getTotal();
                        }

                        coverageCells[i*2] = generateCoverageCell(srcVisited, srcTotal);

                        CoverageDataObject funcCoverageData = CoverageManager
                                .getCoverageOfMultiTestCaseAtFunctionLevel(Arrays.asList(testCases), coverageType);
                        if (funcCoverageData != null) {
                            funcVisited = funcCoverageData.getVisited();
                            funcTotal = funcCoverageData.getTotal();
                        }
                        coverageCells[i*2+1] = generateCoverageCell(funcVisited, funcTotal);
                    }

                } else {
                    for (int i = 0; i < coverageCells.length; i++)
                        coverageCells[i] = new Table.Cell<>(SpecialCharacter.EMPTY);
                }

                cells.addAll(Arrays.asList(coverageCells));

                break;
            }
        }

        return cells;
    }

    private String getBackgroundColor(int visited, int total) {
        String bgColor;
        if (visited == total && visited != 0)
            bgColor = COLOR.GREEN;
        else if (visited != 0)
            bgColor = COLOR.YELLOW;
        else
            bgColor = COLOR.RED;

        return bgColor;
    }

    private Table.Row generateEndRow(List<TestCase> testCases, String coverageType) {
        Table.Row endRow = new Table.Row(new Text("TOTAL", TEXT_STYLE.BOLD));
        endRow.getCells().addAll(computeCoverage(coverageType, testCases.toArray(new TestCase[0])));
        return endRow;
    }

    private Table.Cell<Text> generateCoverageCell(int visited, int total) {
        String coverage = String.format("%.2f%% (%d/%d)", (double) visited * 100 / (double) total, visited, total);
        String bgColor = getBackgroundColor(visited, total);

        return new Table.Cell<>(coverage, bgColor);
    }

    private List<TestCase> getAllTestCaseOf(ICommonFunctionNode function) {
        Set<String> testCaseNames = TestCaseManager.getFunctionToTestCasesMap().get(function);
        List<TestCase> testCases = new ArrayList<>();

        if (testCaseNames != null)
            for (String name : testCaseNames) {
                TestCase testCase = TestCaseManager.getBasicTestCaseByNameWithoutData(name);
                if (testCase != null && new File(testCase.getPath()).exists())
                    testCases.add(testCase);
            }

        return testCases;
    }
}
