package com.dse.testcase_execution.testdriver;

import com.dse.environment.Environment;
import com.dse.parser.object.ConstructorNode;
import com.dse.parser.object.FunctionNode;
import com.dse.search.Search2;
import com.dse.stub_manager.StubManager;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testcase_execution.ITestcaseExecution;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.comparable.gmock.GmockUtils;
import com.dse.testdata.comparable.gtest.*;
import com.dse.testdata.object.*;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AssertableTestDriverGeneration extends TestDriverGeneration {

    protected String generateBodyScript(TestCase testCase) throws Exception {
        // STEP 1: assign aka test case name
        String testCaseNameAssign = String.format("%s=\"%s\";", StubManager.AKA_TEST_CASE_NAME, testCase.getName());

        // STEP 2: Generate initialization of variables
        String initialization = generateInitialization(testCase);

        //STEP 2.5: Generate EXPECT_CALL
        String expectCall = generateExpectCall(testCase);

        // STEP 3: Generate full function call
        String functionCall = generateFunctionCall(testCase);

        // STEP 4: FCALLS++ - Returned from UUT
        String increaseFcall;
        if (testCase.getFunctionNode() instanceof ConstructorNode) {
            increaseFcall = SpecialCharacter.EMPTY;
        } else {
            increaseFcall = SourceConstant.INCREASE_FCALLS + generateReturnMark(testCase);
        }


        // STEP 5: Generation assertion actual & expected values
        String assertion = generateAssertion(testCase);

        String beginMark = "";
        String endMark = "";
        if (Environment.getInstance().getCompiler().isUseGTest()) {
            String testCaseName = testCase.getName().replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);
            beginMark = String.format(DriverConstant.MARK + "(\"BEGIN OF %s\");\n", testCaseName);
            endMark = String.format(DriverConstant.MARK + "(\"END OF %s\");\n", testCaseName);
        }
        // STEP 6: Repeat iterator
        String singleScript = String.format(
                "{\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "}",
                testCaseNameAssign,
                beginMark,
                testCase.getTestCaseUserCode().getSetUpContent(),
                initialization,
                expectCall,
                functionCall,
                increaseFcall,
                endMark,
                assertion,
                GmockUtils.createVerifyAndClearExpectations(testCase),
                testCase.getTestCaseUserCode().getTearDownContent());

//        StringBuilder script = new StringBuilder();
//        for (int i = 0; i < iterator; i++)
//            script.append(singleScript).append(SpecialCharacter.LINE_BREAK);

        // STEP 7: mark beginning and end of test case
//        script = new StringBuilder(wrapScriptInMark(testCase, script.toString()));
//        script = new StringBuilder(wrapScriptInTryCatch(script.toString()));
//
//        return script.toString();
        singleScript = wrapScriptInTryCatch(singleScript);

        return singleScript;
    }

    protected String generateAssertion(TestCase testCase) {
        String assertion = "/* error assertion */";

        IValueDataNode expectedOutputDataNode = Search2.getExpectedOutputNode(testCase.getRootDataNode());

        // not void function
        if (expectedOutputDataNode != null) {
            if (!(expectedOutputDataNode.getParent() instanceof SubprogramNode
                    && IPredicateAssertions.assertMethods
                    .contains(((SubprogramNode) expectedOutputDataNode.getParent()).getAssertMethod()))) {
                assertion = expectedOutputDataNode.getAssertion();
            }
//            if (expectedOutputDataNode.getRawType().equals("void*")){
//                assertion = "/*Does not support CU_ASSERT for void pointer comparison*/";
//            } else {
//            assertion = expectedOutputDataNode.getAssertion();
//            }
        }

        // expected values
        assertion += generateExpectedValueInitialize(testCase);

        return assertion;
    }

    private String generateExpectedValueInitialize(TestCase testCase) {
        String initialize = "\n/* error expected initialize */";

        SubprogramNode sut = Search2.findSubprogramUnderTest(testCase.getRootDataNode());

        Map<ValueDataNode, ValueDataNode> globalExpectedMap = testCase.getGlobalInputExpOutputMap();

        if (sut != null) {
            initialize = SpecialCharacter.LINE_BREAK;

//            List<IDataNode> expectedGlobals = Search2.findGlobalRoot(testCase.getRootDataNode()).getChildren();
//            List<IDataNode> expectedGlobals = new ArrayList<>(globalExpectedMap.values()); //fix classes
            try {
                // sut arguments
                initialize += addParamAssert(sut, true);

                // global variables
                for (Map.Entry expectedMap : globalExpectedMap.entrySet()) {
                    IDataNode actualDataNode = (IDataNode) expectedMap.getKey();
                    IDataNode expected = (IDataNode) expectedMap.getValue();
                    if (actualDataNode instanceof ValueDataNode) {
                        boolean shouldInit = shouldInitializeExpected((ValueDataNode) actualDataNode);
                        boolean haveMethod = unnecessaryInitializeExpected((ValueDataNode) actualDataNode);
                        if (shouldInit || haveMethod) {
                            initialize += expected.getInputForGoogleTest(false);
                            initialize += SpecialCharacter.LINE_BREAK;
                            initialize += ((ValueDataNode) actualDataNode).getAssertion();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return initialize;
    }

    private String addParamAssert(IDataNode current, boolean isFirstLevel) throws Exception {
        StringBuilder output = new StringBuilder();

        List<ValueDataNode> parameters;

        if (isFirstLevel && current instanceof SubprogramNode) {
            parameters = Search2.searchParameterNodes((SubprogramNode) current);
        } else {
            parameters = current.getChildren().stream()
                    .filter(n -> n instanceof ValueDataNode)
                    .map(n -> (ValueDataNode) n)
                    .collect(Collectors.toList());
        }

        for (ValueDataNode param : parameters) {
            boolean haveMethod = unnecessaryInitializeExpected(param);

            ValueDataNode expected = Search2.getExpectedValue(param);
            SubprogramNode subprogramNode = Search2.findSubprogramUnderTest(((TestCase) testCase).getRootDataNode());
            TemplateSubprogramDataNode templateSubprogramDataNode = (subprogramNode != null && subprogramNode instanceof TemplateSubprogramDataNode) ? (TemplateSubprogramDataNode) subprogramNode : null;
            if (templateSubprogramDataNode != null && expected != null) {
                Map<String, String> typeMap = templateSubprogramDataNode.getRealTypeMapping();
                for (String key : typeMap.keySet()) {
                    if (key.equals(expected.getRealType())) {
                        expected.setRealType(typeMap.get(key));
                        expected.setRawType(typeMap.get(key));
                    }
                }
            }
            if (isFirstLevel) {
                if (param.getName().equals("RETURN"))
                    continue;

                if (expected != null) {
                    if (shouldInitializeExpected(expected)) {
                        output.append(expected.getInputForGoogleTest(false));
                        output.append(SpecialCharacter.LINE_BREAK);
                    }
                }
            }

            if (param.getAssertMethod() != null) {
                if (haveMethod) {
                    output.append(param.getAssertion());
                } else {
                    if (expected != null) {
                        output.append(expected.getAssertion());
                    } else {
                        output.append(addParamAssert(param, false));
                    }
                }
            } else {
                output.append(addParamAssert(param, false));
            }

        }

        return output.toString();
    }

    private boolean unnecessaryInitializeExpected(ValueDataNode dataNode) {
        if (dataNode.getAssertMethod() == null)
            return false;

        String assertMethod = dataNode.getAssertMethod();
        if (Environment.getInstance().getCompiler().isUseGTest()) {
            assertMethod = assertMethod.split(": ")[0];
            return IGeneralizedAssertion.assertMethods.contains(assertMethod)
                    || IBooleanConditions.assertMethods.contains(assertMethod)
                    || IExceptionAssertions.assertMethods.contains(assertMethod)
                    || IPredicateAssertions.assertMethods.contains(assertMethod)
                    || IStringComparison.assertMethods.contains(assertMethod)
                    || IDeathAssertions.assertMethods.contains(assertMethod);
        }

        return assertMethod.equals(AssertMethod.ASSERT_TRUE)
                || assertMethod.equals(AssertMethod.ASSERT_FALSE)
                || assertMethod.equals(AssertMethod.ASSERT_NULL)
                || assertMethod.equals(AssertMethod.ASSERT_NOT_NULL)
                || assertMethod.equals(AssertMethod.USER_CODE);
    }

    private boolean shouldInitializeExpected(ValueDataNode dataNode) {
        if (dataNode.isUseUserCode())
            return true;

        if (dataNode instanceof ArrayDataNode)
            return ((ArrayDataNode) dataNode).isSetSize();

        if (dataNode instanceof PointerDataNode)
            return ((PointerDataNode) dataNode).isSetSize();

        if (dataNode instanceof NormalDataNode)
            return ((NormalDataNode) dataNode).getValue() != null;

        if (dataNode instanceof ClassDataNode) {
            ISubStructOrClassDataNode subClass = ((ClassDataNode) dataNode).getSubStructure();

            if (subClass == null)
                return false;

            ConstructorDataNode constructor = subClass.getConstructorDataNode();

            if (constructor == null)
                return false;

//            if (constructor.getChildren().size() == 0)
//                return false;

            for (IDataNode argument : constructor.getChildren()) {
                if (!shouldInitializeExpected((ValueDataNode) argument))
                    return false;
            }

            return true;
        }

        if (dataNode instanceof StructDataNode) {
            ISubStructOrClassDataNode subStruct = ((StructDataNode) dataNode).getSubStructure();

            if (subStruct == null)
                return false;

            ConstructorDataNode constructor = subStruct.getConstructorDataNode();

            if (constructor == null)
                return false;

//            if (constructor.getChildren().size() == 0)
//                return false;

            for (IDataNode argument : constructor.getChildren()) {
                if (!shouldInitializeExpected((ValueDataNode) argument))
                    return false;
            }

            return true;
        }
        if (dataNode instanceof UnionDataNode) {
            SubUnionDataNode subUnion = ((UnionDataNode) dataNode).getSubUnion();

            if (subUnion == null)
                return false;

            ConstructorDataNode constructor = subUnion.getConstructorDataNode();

            if (constructor == null)
                return false;

            if (constructor.getChildren().size() == 0)
                return false;

            for (IDataNode argument : constructor.getChildren()) {
                if (!shouldInitializeExpected((ValueDataNode) argument))
                    return false;
            }

            return true;
        }


        if (dataNode instanceof EnumDataNode) {
            ((EnumDataNode) dataNode).getValue();
            return true;
        }

        return true;
    }
}
