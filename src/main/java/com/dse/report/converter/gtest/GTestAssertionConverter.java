package com.dse.report.converter.gtest;

import com.dse.parser.object.EnumNode;
import com.dse.parser.object.IFunctionPointerTypeNode;
import com.dse.parser.object.INode;
import com.dse.report.converter.Converter;
import com.dse.report.element.IElement;
import com.dse.report.element.Table;
import com.dse.search.Search2;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.testcase_execution.result_trace.gtest2.Failure;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.comparable.gtest.*;
import com.dse.testdata.object.*;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.util.Arrays;
import java.util.List;

public abstract class GTestAssertionConverter extends Converter {
    /**
     * List of failures from gtest report
     */
    protected List<IResultTrace> failures;

    /**
     * Function call tag: index of function call expresstion
     */
    protected int fcalls;

    /**
     * Result PASS/ALL
     */
    protected AssertionResult results = new AssertionResult();

    public GTestAssertionConverter(List<IResultTrace> failures, int fcalls) {
        this.failures = failures;
        this.fcalls = fcalls;
    }

    public Table execute(SubprogramNode root) {
        Table table = generateHeaderRows(root);

        for (IDataNode child : root.getChildren())
            table = recursiveConvert(table, child, 2);

        return table;
    }

    protected Table generateHeaderRows(SubprogramNode root) {
        Table table = new Table(false);

        table.getRows().add(new Table.Row("Parameter", "Type", "Expected Value", "Actual Value", "Message"));

        INode sourceNode = Utils.getSourcecodeFile(root.getFunctionNode());
        String unitName = sourceNode.getName();
        Table.Row uutRow = new Table.Row("Calling UUT: " + unitName, "", "", "", "");
        table.getRows().add(uutRow);
        Table.Row sutRow = new Table.Row(generateTab(1) + "Subprogram: " + root.getName(), "", "", "", "");
        table.getRows().add(sutRow);

        return table;
    }

    @Override
    public boolean isShowInReport(IDataNode node) {
        if (node instanceof ValueDataNode) {
            ValueDataNode valueNode = (ValueDataNode) node;
            IDataNode parent = valueNode.getParent();

            if (parent instanceof SubprogramNode)
                return true;

            if (parent instanceof UnionDataNode) {
                String field = ((UnionDataNode) parent).getSelectedField();
                return node.getName().equals(field);
            }

            if (parent instanceof ArrayDataNode || parent instanceof PointerDataNode) {
                if (!((ValueDataNode) parent).haveValue()) {
                    return false;
                }

                if (!valueNode.haveValue()) {
                    return false;
                }
            }

            if (isShowInReport(node.getParent()))
                return true;

            return valueNode.haveValue();
        }

        return false;
    }

    @Override
    public Table.Row convertSingleNode(IDataNode node, int level) {
        String key = generateTab(level) + node.getDisplayNameInParameterTree();

        String type = "";
        if (node instanceof ValueDataNode && !(node instanceof SubprogramNode))
            type = ((ValueDataNode) node).getRawType();

        Table.Row row = new Table.Row(key, type);

        Table.Cell[] valueCells = new Table.Cell[]{new Table.Cell<>(""), new Table.Cell<>(""), new Table.Cell<>("")};
        if (node instanceof ValueDataNode && isValuable(node)) {
            ValueDataNode dataNode = (ValueDataNode) node;

            if (isHaveExpected(dataNode)) {
                String expectedName = node.getVituralName();
                if (dataNode instanceof FunctionPointerDataNode && node.getName().isEmpty()) {
                    IFunctionPointerTypeNode typeNode = ((FunctionPointerDataNode) dataNode).getCorrespondingType();
                    String functionName = typeNode.getFunctionName();
                    expectedName = node.getVituralName() + functionName;
                }
                valueCells = findValuesExpectCase(dataNode, expectedName);
            } else
                valueCells = findValuesActualCase(dataNode);
        }

        row.getCells().addAll(Arrays.asList(valueCells));

        return row;
    }

    protected boolean isHaveExpected(ValueDataNode dataNode) {
        if (dataNode.getAssertMethod() == null)
            return false;

        if (notNeedValue(dataNode))
            return true;

        if (!dataNode.isExpected())
            return false;

        if (!dataNode.haveValue())
            return dataNode instanceof NormalDataNode
                    || dataNode instanceof EnumDataNode;

        return true;
    }

    protected boolean notNeedValue(ValueDataNode dataNode) {
        String assertMethod = dataNode.getAssertMethod();
        if (assertMethod != null) {
            assertMethod = assertMethod.split(": ")[0];
        }

        return IGeneralizedAssertion.assertMethods.contains(assertMethod)
                || IBooleanConditions.assertMethods.contains(assertMethod)
                || IExceptionAssertions.assertMethods.equals(assertMethod)
                || IPredicateAssertions.assertMethods.equals(assertMethod);
    }

    protected Table.Cell[] findValuesActualCase(ValueDataNode node) {
        String actualValue = getNodeValue(node);
        String expectedValue = SpecialCharacter.EMPTY;

        return new Table.Cell[]{
                new Table.Cell<>(expectedValue),
                new Table.Cell<>(actualValue),
                new Table.Cell<>(SpecialCharacter.EMPTY)
        };
    }

    protected Table.Cell[] findValuesExpectCase(ValueDataNode node, String expectedName) {
        IValueDataNode expectedNode = null;
        if (!(node instanceof SubprogramNode)) {
            expectedNode = Search2.getExpectedValue(node);
        }
        if (expectedNode == null) {
            expectedNode = node;
        }
        String expectedValue = getNodeValue(expectedNode);
        String expectedValueText;
        String[] assertMethod = node.getAssertMethod().split(" \u0394 |: ", 2);
        switch (assertMethod[0]) {
            case IGTestAssertMethod.EXPECT_TRUE:
            case IGTestAssertMethod.ASSERT_TRUE:
            case IGTestAssertMethod.EXPECT_FALSE:
            case IGTestAssertMethod.ASSERT_FALSE:
            case IGTestAssertMethod.EXPECT_THAT:
            case IGTestAssertMethod.ASSERT_THAT:
            case IGTestAssertMethod.EXPECT_NO_THROW:
            case IGTestAssertMethod.ASSERT_NO_THROW:
            case IGTestAssertMethod.EXPECT_ANY_THROW:
            case IGTestAssertMethod.ASSERT_ANY_THROW:
            case IGTestAssertMethod.EXPECT_THROW:
            case IGTestAssertMethod.ASSERT_THROW:
                expectedValueText = node.getAssertMethod();
                break;
            case IGTestAssertMethod.EXPECT_NEAR:
            case IGTestAssertMethod.ASSERT_NEAR:
                expectedValueText = assertMethod[0] + " " + expectedValue + " " + SpecialCharacter.DELTA + assertMethod[1];
                break;
            default:
                expectedValueText = node.getAssertMethod() + " " + expectedValue;
                break;
        }
        String actualValue = "<<Unknown>>";
        actualValue = getNodeValue(node);
        if (node instanceof ListBaseDataNode)
            expectedName += ".size()";

        String expectedOutputRegex = "\\Q" + SourceConstant.EXPECTED_OUTPUT + "\\E";
        expectedName = expectedName.replaceFirst(expectedOutputRegex, SourceConstant.ACTUAL_OUTPUT);
        String msg = "";
        boolean match = false;

        if (failures != null) {
            boolean found = false;

            for (IResultTrace failure : failures) {
                String fcallTag = "Aka function calls: " + fcalls;

                if (((Failure) failure).getAssertStm().equals(node.getAssertStm())) {

                    found = true;
//                    String[] values = findValuesFromFailure(node, failure);
//                    actualValue = values[1];
//                    expectedValue = values[0];
                    msg = failure.getMessage();
                    match = !msg.contains("Failure");
                    if (!match) {
                        actualValue = failure.getActual();
                        msg = msg.split("Failure")[1];
                    }
                    break;
                }
            }

            if (!found) {
                return new Table.Cell[]{
                        new Table.Cell<>(expectedValueText),
                        new Table.Cell<>(actualValue),
                        new Table.Cell<>(SpecialCharacter.EMPTY),
                };
            }
        }

        String bgColor = IElement.COLOR.RED;
        if (AssertMethod.USER_CODE.equals(node.getAssertMethod())) {
            bgColor = IElement.COLOR.LIGHT;
        } else {
            if (match) {
                bgColor = IElement.COLOR.GREEN;
//                results[0]++;
                results.increasePass();
            }

//            results[1]++;
            results.increaseTotal();
        }

        return new Table.Cell[]{
                new Table.Cell<>(expectedValueText, bgColor),
                new Table.Cell<>(actualValue, bgColor),
                new Table.Cell<>(msg, bgColor)
        };
    }

    protected boolean isValuable(IDataNode node) {
        if (node instanceof RootDataNode || node instanceof SubprogramNode)
            return false;

        else if (((ValueDataNode) node).isUseUserCode())
            return true;

        else if (((ValueDataNode) node).isExpected()) {
            ValueDataNode valueNode = (ValueDataNode) node;
            return valueNode.haveValue() || notNeedValue(valueNode);

        } else if (node instanceof StructDataNode || node instanceof ClassDataNode
                || node instanceof UnionDataNode)
            return false;

        else if (node instanceof ArrayDataNode || node instanceof PointerDataNode) {
            String value = getNodeValue(node);
            return value != null && !value.isEmpty();

        } else if (node instanceof NormalDataNode || node instanceof EnumDataNode)
            return true;

        else if (node instanceof VoidPointerDataNode) {
            return ((VoidPointerDataNode) node).getInputMethod() == VoidPointerDataNode.InputMethod.USER_CODE;

        } else
            return node instanceof ListBaseDataNode || node instanceof UnresolvedDataNode;
    }

    protected boolean isMatch(ValueDataNode node, IResultTrace failure) {
        String actual = failure.getActual();
        String expected = failure.getExpected();

        if (node instanceof EnumDataNode) {
            INode typeNode = node.getCorrespondingType();
            if (typeNode instanceof EnumNode) {
                EnumNode enumNode = (EnumNode) typeNode;
                for (String name : enumNode.getAllNameEnumItems()) {
                    String val = enumNode.getValueOfEnumItem(name);
                    if (val.equals(actual)) {
                        actual = name;
                    }
                    if (val.equals(expected)) {
                        expected = name;
                    }
                }
            }
        }

        return AssertMethod.isMatch(node.getAssertMethod(), actual, expected);
    }

    protected String[] findValuesFromFailure(ValueDataNode node, IResultTrace failure) {
        String[] values = new String[2];
        values[1] = failure.getActual();

//        if (node instanceof EnumDataNode) {
//            INode typeNode = node.getCorrespondingType();
//            if (typeNode instanceof EnumNode) {
//                EnumNode enumNode = (EnumNode) typeNode;
//                for (String name : enumNode.getAllNameEnumItems()) {
//                    String val = enumNode.getValueOfEnumItem(name);
//                    if (val.equals(values[1])) {
//                        values[1] = name;
//                    }
//                }
//            }
//        }

        values[0] = AssertMethod.findExpectedFromFailure(node.getAssertMethod(), failure.getExpected());

        return values;
    }

    public static final String FALSE_VALUE = "0";
    public static final String NULL_VALUE = "0";

    public AssertionResult getResults() {
        return results;
    }

    protected static final int OFFSET = 10;

    protected static final String PROBLEM_VALUE = "Can't find variable value";

    protected static final String MATCH = "MATCH";
}
