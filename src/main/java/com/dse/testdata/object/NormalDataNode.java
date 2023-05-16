package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.search.Search2;
import com.dse.testcase_execution.ITestcaseExecution;
import com.dse.testdata.comparable.*;
import com.dse.testdata.comparable.gtest.*;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;

/**
 * Represent basic types belong to number of character
 *
 * @author DucAnh
 */
public abstract class NormalDataNode extends ValueDataNode implements IValueComparable, IBooleanComparable, IBinaryComparison {
    public static final String CHARACTER_QUOTE = "'";

    /**
     * Represent value of variable
     */
    private String value;

    private String generateAssignmentForDisplay() {
        String varInit = "";
        String valueVar = "";
        if (VariableTypeUtils.isCh(this.getRawType())) {
            int numberValue = Utils.toInt(this.getValue());

            if (VariableTypeUtils.isChBasic(this.getRawType()) && Utils.isVisibleCh(numberValue)
                    && !Utils.isSpecialChInVisibleRange(numberValue))
                valueVar = NormalDataNode.CHARACTER_QUOTE + (char) numberValue + NormalDataNode.CHARACTER_QUOTE;
            else
                valueVar = numberValue + "";
        } else
            valueVar = this.getValue() + "";

        varInit = this.getDotSetterInStr(valueVar) + SpecialCharacter.LINE_BREAK;
        return varInit;
    }

    @Override
    public String getInputForDisplay() throws Exception {
        String input = "";
        input = this.generateAssignmentForDisplay();
        return input;
    }

    @Override
    public String generareSourcecodetoReadInputFromFile() throws Exception {
        String typeVar = VariableTypeUtils.deleteStorageClasses(this.getRawType())
                .replace(IDataNode.REFERENCE_OPERATOR, "");

        String loadValueStm = "data.findValueByName<" + typeVar + ">(\"" + getVituralName() + "\")";

        String fullStm = typeVar + " " + this.getVituralName() + "=" + loadValueStm + SpecialCharacter.END_OF_STATEMENT;
        return fullStm;
    }

    @Override
    public String generateInputToSavedInFile() throws Exception {
        if (this.getValue() != null)
            return this.getName() + "=" + this.getValue() + SpecialCharacter.LINE_BREAK;
        else
            return "";
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean haveValue() {
        return value != null;
    }

    public void setValue(int value) {
        this.value = value + "";
    }

    @Override
    public String assertEqual(String expected, String actual) {
        return new ValueStatementGenerator(this).assertEqual(expected, actual);
    }

    @Override
    public String assertNotEqual(String expected, String actual) {
        return new ValueStatementGenerator(this).assertNotEqual(expected, actual);
    }

    @Override
    public String assertTrue(String name) {
        return new BooleanStatementGenerator(this).assertTrue(name);
    }

    @Override
    public String assertFalse(String name) {
        return new BooleanStatementGenerator(this).assertFalse(name);
    }

    @Override
    public String assertLower(String expected, String actual) {
        return new ValueStatementGenerator(this).assertLower(expected, actual);
    }

    @Override
    public String assertGreater(String expected, String actual) {
        return new ValueStatementGenerator(this).assertGreater(expected, actual);
    }

    @Override
    public String assertLowerOrEqual(String expected, String actual) {
        return new ValueStatementGenerator(this).assertLowerOrEqual(expected, actual);
    }

    @Override
    public String assertGreaterOrEqual(String expected, String actual) {
        return new ValueStatementGenerator(this).assertGreaterOrEqual(expected, actual);
    }

//    @Override
//    public String getAssertionForGoogleTest(String method, String source, String target) throws Exception {
//        String assertMethod = getAssertMethod();
//
//        if (assertMethod != null) {
//            switch (assertMethod) {
//                case AssertMethod.ASSERT_EQUAL:
//                    return assertEqual(source, target);
//                case AssertMethod.ASSERT_NOT_EQUAL:
//                    return assertNotEqual(source, target);
//                case AssertMethod.ASSERT_TRUE:
//                    return assertTrue(source);
//                case AssertMethod.ASSERT_FALSE:
//                    return assertFalse(source);
//            }
//        }
//
//        return SpecialCharacter.EMPTY;
//    }

    @Override
    public String getAssertion() {
        String assertMethod = getAssertMethod();

        if (assertMethod != null) {
            if (Environment.getInstance().getCompiler().isUseGTest()) {
                return getAssertStm();
            }
//                    assertMethod = assertMethod.split(" \\u00B1 ")[0];
//                    if (IBinaryComparison.assertMethods.contains(assertMethod)) {
//                        return getBinaryComparisonAssertionWithMark();
//                    } else if (IFloatingPointComparison.assertMethods.contains(assertMethod)) {
//                        if (assertMethod.equals(IGTestAssertMethod.EXPECT_NEAR) || assertMethod.equals(IGTestAssertMethod.ASSERT_NEAR)) {
//                            if (this instanceof NormalNumberDataNode) {
//                                return ((NormalNumberDataNode) this).getFloatingPointComparisonAssertionWithMark();
//                            } else {
//                                return ((NormalCharacterDataNode) this).getFloatingPointComparisonAssertionWithMark();
//                            }
//                        } else {
//                            return getBinaryComparisonAssertionWithMark();
//                        }
//                    } else if (IBooleanConditions.assertMethods.contains(assertMethod)) {
//                        return ((NormalNumberDataNode) this).getBooleanConditionAssertionWithMark();
//                    }
            String expectedName = getVituralName();
            String actualName = getActualName();
            switch (assertMethod) {
                case AssertMethod.ASSERT_EQUAL:
                    return assertEqual(expectedName, actualName);
                case AssertMethod.ASSERT_NOT_EQUAL:
                    return assertNotEqual(expectedName, actualName);
                case AssertMethod.ASSERT_TRUE:
                    return assertTrue(actualName);
                case AssertMethod.ASSERT_FALSE:
                    return assertFalse(actualName);
                case AssertMethod.ASSERT_LOWER:
                    return assertLower(expectedName, actualName);
                case AssertMethod.ASSERT_GREATER:
                    return assertGreater(expectedName, actualName);
                case AssertMethod.ASSERT_LOWER_OR_EQUAL:
                    return assertLowerOrEqual(expectedName, actualName);
                case AssertMethod.ASSERT_GREATER_OR_EQUAL:
                    return assertGreaterOrEqual(expectedName, actualName);
                case AssertMethod.USER_CODE:
                    return getAssertUserCode().normalize();
            }
        }

        return SpecialCharacter.EMPTY;
    }

    @Override
    public String getAssertStm() {
        String assertMethod = getAssertMethod();

        if (assertMethod != null) {
            if (Environment.getInstance().getCompiler().isUseGTest()) {
                assertMethod = assertMethod.split(" " + SpecialCharacter.DELTA + " ")[0];
                assertMethod = assertMethod.split(": ")[0];
                if (IBinaryComparison.assertMethods.contains(assertMethod) || IStringComparison.assertMethods.contains(assertMethod)) {

                    return this.getBinaryComparisonAssertion();

                } else if (IFloatingPointComparison.assertMethods.contains(assertMethod)) {
                    if (assertMethod.equals(IGTestAssertMethod.EXPECT_NEAR) || assertMethod.equals(IGTestAssertMethod.ASSERT_NEAR)) {
                        if (this instanceof NormalNumberDataNode) {
                            return ((NormalNumberDataNode) this).getFloatingPointComparisonAssertion();
                        } else {
                            return ((NormalCharacterDataNode) this).getFloatingPointComparisonAssertion();
                        }
                    } else {
                        return this.getBinaryComparisonAssertion();
                    }
                } else if (IBooleanConditions.assertMethods.contains(assertMethod)) {
                    return ((NormalNumberDataNode) this).getBooleanConditionAssertion();
                } else if (IGeneralizedAssertion.assertMethods.contains(assertMethod)) {
                    return this.getGeneralizedAssertion();
                }
            } else {
                String expectedName = Search2.getExpectedValue(this).getVituralName();
                String actualName = getActualName();
                switch (assertMethod) {
                    case AssertMethod.ASSERT_EQUAL:
                        return assertEqual(expectedName, actualName);
                    case AssertMethod.ASSERT_NOT_EQUAL:
                        return assertNotEqual(expectedName, actualName);
                    case AssertMethod.ASSERT_TRUE:
                        return assertTrue(actualName);
                    case AssertMethod.ASSERT_FALSE:
                        return assertFalse(actualName);
                    case AssertMethod.ASSERT_LOWER:
                        return assertLower(expectedName, actualName);
                    case AssertMethod.ASSERT_GREATER:
                        return assertGreater(expectedName, actualName);
                    case AssertMethod.ASSERT_LOWER_OR_EQUAL:
                        return assertLowerOrEqual(expectedName, actualName);
                    case AssertMethod.ASSERT_GREATER_OR_EQUAL:
                        return assertGreaterOrEqual(expectedName, actualName);
                    case AssertMethod.USER_CODE:
                        return getAssertUserCode().normalize();
                }
            }
        }

        return SpecialCharacter.EMPTY;
    }

    @Override
    public String getBinaryComparisonAssertion() {
        return new BinaryComparisonStatementGenerator(this).getBinaryComparisonAssertion();
    }
}
