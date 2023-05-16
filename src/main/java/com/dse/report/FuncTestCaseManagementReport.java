package com.dse.report;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.report.element.*;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.*;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import net.sourceforge.jeval.function.math.Abs;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FuncTestCaseManagementReport extends ReportView {

    private ICommonFunctionNode functionNode;

    public FuncTestCaseManagementReport(ITestcaseNode node, LocalDateTime creationDt) {
        super("Test Case Management Report");

        // set report creation date time
        setCreationDateTime(creationDt);

        // set report location path to default
        setPathDefault();

        // find selected unit
        findSelectedUnitByTestNode(node);

        generate();
    }

//    public FuncTestCaseManagementReport(String unitName, String subprogramName, LocalDateTime creationDt) {
//        super("Test Case Management Report");
//
//        // set report creation date time
//        setCreationDateTime(creationDt);
//
//        // set report location path to default
//        setPathDefault();
//
//        // find all selected unit
//        findSelectedUnitByName(unitName, subprogramName);
//
//        generate();
//    }
//
//    private void findSelectedUnitByName(String name, String subprogramName) {
//        List<INode> units = new ArrayList<>(Environment.getInstance().getUUTs());
//        units.addAll(Environment.getInstance().getSBFs());
//        units = units.stream().filter(u -> u.getName().equals(name)).collect(Collectors.toList());
//        for (INode unit : units) {
//            List<ICommonFunctionNode> subprograms = Search.searchNodes(unit, new AbstractFunctionNodeCondition());
//        }
//    }

    private void findSelectedUnitByTestNode(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode) {
            functionNode = ((TestNormalSubprogramNode) node).getFunctionNode();
            if (functionNode == null && node.getParent() instanceof TestUnitNode) {
                TestUnitNode unitNode = (TestUnitNode) node.getParent();
                String unitName = unitNode.getName();
                List<INode> allUnits = Environment.getInstance().getUUTs();
                allUnits.addAll(Environment.getInstance().getSBFs());
                INode unit = allUnits.stream().filter(u -> u.getAbsolutePath().equals(unitName))
                        .findFirst()
                        .orElse(null);
                if (unit != null) {
                    List<ICommonFunctionNode> functionNodes = Search.searchNodes(unit, new AbstractFunctionNodeCondition(), ((TestNormalSubprogramNode) node).getName());
                    if (!functionNodes.isEmpty())
                        functionNode = functionNodes.get(0);
                }
            }
        }
    }

    @Override
    protected void generate() {
        sections.add(generateTableOfContents());

        sections.add(generateConfigurationData());
        sections.add(new Section.BlankLine());

        sections.add(generateOverallResults());
        sections.add(new Section.BlankLine());

        sections.add(new TestCaseManagementForFunction(functionNode));
        sections.add(new Section.BlankLine());

        sections.add(new MetricsForFunction(functionNode));
    }

    @Override
    protected TableOfContents generateTableOfContents() {
        TableOfContents tableOfContents = new TableOfContents();

        tableOfContents.getBody().add(new TableOfContents.Item("Configuration Data", "config-data"));

        tableOfContents.getBody().add(
                new TableOfContents.Item("Overall Results", "overall-results"));

        tableOfContents.getBody().add(
                new TableOfContents.Item("Test Case Management", "tcs-manage"));

        tableOfContents.getBody().add(
                new TableOfContents.Item("Metrics", "metrics"));

        return tableOfContents;
    }

    @Override
    protected Section generateConfigurationData() {
        Section section = new Section("config-data");

        section.getTitle().add(new Section.Line("Configuration Data", COLOR.DARK));

        Table table = new Table();
        INode unit = Utils.getSourcecodeFile(functionNode);
        table.getRows().add(new Table.Row("Unit", unit.getName()));
        table.getRows().add(new Table.Row("Subprogram", functionNode.getName()));
        table.getRows().add(new Table.Row("Date of Report Creation:", getCreationDate()));
        table.getRows().add(new Table.Row("Time of Report Creation:", getCreationTime()));
        section.getBody().add(table);

        return section;
    }

    protected Section generateOverallResults() {
        Section section = new Section("overall-results");

        section.getTitle().add(new Section.Line("Overall Results", COLOR.DARK));

        Table table = new Table();
        table.getRows().add(new Table.HeaderRow("Category", "Results"));

        AssertionResult testCaseResults = new AssertionResult();
        AssertionResult expectedResults = new AssertionResult();

        List<String> testCaseNames = getAllTestCaseUnderUnit();

        for (String testCaseName : testCaseNames) {
            ITestCase testCase = TestCaseManager.getTestCaseByNameWithoutData(testCaseName);

            if (testCase != null) {
                AssertionResult result = testCase.getExecutionResult();

                // calculate result
                if (result == null) {
                    result = calculateResult(testCase);
                }

                if (result != null) {
                    if (result.isAllPass()) {
                        testCaseResults.increasePass();
                    }

                    testCaseResults.increaseTotal();
                    expectedResults.append(result);

                }
            }
        }

        table.getRows().add(new Table.Row(
                new Table.Cell<Text>("Test Cases:"),
                new Table.Cell<Text>(String.format("PASS %d/%d", testCaseResults.getPass(), testCaseResults.getTotal()),
                        getBackgroundColor(testCaseResults))
        ));

        table.getRows().add(new Table.Row(
                new Table.Cell<Text>("Expecteds:"),
                new Table.Cell<Text>(String.format("PASS %d/%d", expectedResults.getPass(), expectedResults.getTotal()),
                        getBackgroundColor(expectedResults))
        ));

        section.getBody().add(table);

        return section;
    }

    private List<String> getAllTestCaseUnderUnit() {
        List<String> testCaseNames = new ArrayList<>();

        Set<String> testcaseTmpNames = TestCaseManager.getFunctionToTestCasesMap().get(functionNode);
        if (testcaseTmpNames != null) {
            testCaseNames.addAll(testcaseTmpNames);
        }

        return testCaseNames.stream().distinct().collect(Collectors.toList());
    }

    private AssertionResult calculateResult(ITestCase testCase) {
        if (testCase instanceof TestCase) {
            new BasicExecutionResult(testCase);
        } else if (testCase instanceof CompoundTestCase) {
            new CompoundExecutionResult(testCase);
        }
        return testCase.getExecutionResult();
    }

    protected String getBackgroundColor(AssertionResult result) {
        String bgColor;

        if (result.isAllPass())
            bgColor = COLOR.GREEN;
        else if (result.getPass() == 0)
            bgColor = COLOR.RED;
        else
            bgColor = COLOR.YELLOW;

        return bgColor;
    }

    @Override
    protected void setPathDefault() {
        this.path = new WorkspaceConfig().fromJson().getReportDirectory()
                + File.separator + "test-cases-management.html";
    }
}
