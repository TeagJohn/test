package com.dse.testcase_execution.testdriver;

import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search;
import com.dse.search.Search2;
import com.dse.stub_manager.StubManager;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testcase_manager.*;
import com.dse.testdata.comparable.gmock.GmockUtils;
import com.dse.testdata.comparable.gmock.IGmockSpecialWords;
import com.dse.testdata.comparable.gtest.GTestStatementGenerator;
import com.dse.testdata.comparable.gtest.IDeathAssertions;
import com.dse.testdata.comparable.gtest.IExceptionAssertions;
import com.dse.testdata.comparable.gtest.IPredicateAssertions;
import com.dse.testdata.object.*;
import com.dse.testdata.object.Gmock.GmockObjectNode;
import com.dse.testdata.object.Gmock.TimesNode;
import com.dse.testdata.object.Gmock.WithNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.user_code.objects.TestCaseUserCode;
import com.dse.util.*;

import java.util.*;

import static com.dse.project_init.ProjectClone.wrapInIncludeGuard;

/**
 * Generate test driver for a function
 *
 * @author Vu + D.Anh
 */
public abstract class TestDriverGeneration extends DriverGeneration implements ITestDriverGeneration {

    protected final static AkaLogger logger = AkaLogger.get(TestDriverGeneration.class);

    protected List<String> testScripts;

    protected ITestCase testCase;

    protected String testPathFilePath;

    protected List<String> clonedFilePaths;

    @Override
    public void generate() throws Exception {
        testPathFilePath = testCase.getTestPathFile();

        testScripts = new ArrayList<>();
        clonedFilePaths = new ArrayList<>();

        if (testCase instanceof TestCase) {
            String script = generateTestScript((TestCase) testCase);
            testScripts.add(script);
        } else if (testCase instanceof CompoundTestCase) {
            List<TestCaseSlot> slots = ((CompoundTestCase) testCase).getSlots();
            for (TestCaseSlot slot : slots) {
                String name = slot.getTestcaseName();
                TestCase element = TestCaseManager.getBasicTestCaseByName(name);
                assert element != null;
                String testScript = generateTestScript(element);
                testScripts.add(testScript);
            }
        }

        StringBuilder testScriptPart = new StringBuilder();
        for (String item : testScripts) {
            testScriptPart.append(item).append(SpecialCharacter.LINE_BREAK);
        }

        String includedPart = generateIncludePaths();
        String additionalIncludes = generateAdditionalHeaders();

//        String mockClass = GmockUtils.createMockClass(testCase);
        String mockClass = "";

        testDriver = getTestDriverTemplate()
                .replace(TEST_PATH_TAG, Utils.doubleNormalizePath(testPathFilePath))
                .replace(CLONED_SOURCE_FILE_PATH_TAG, includedPart)
                .replace(MOCK_PRE_TEST_SCRIPTS_TAG, mockClass)
                .replace(TEST_SCRIPTS_TAG, testScriptPart.toString())
                .replace(ADDITIONAL_HEADERS_TAG, additionalIncludes)
                .replace(EXEC_TRACE_FILE_TAG, Utils.doubleNormalizePath(testCase.getExecutionResultTrace()))
                .replace(DriverConstant.ADD_TESTS_TAG, generateAddTestStm(testCase));

        if (testCase instanceof CompoundTestCase) {
            TestCaseUserCode userCode = testCase.getTestCaseUserCode();
            testDriver = testDriver
                    .replace(COMPOUND_TEST_CASE_SETUP, userCode.getSetUpContent())
                    .replace(COMPOUND_TEST_CASE_TEARDOWN, userCode.getTearDownContent());
        }
    }

    protected String generateTestScript(TestCase testCase) throws Exception {
        String body = generateBodyScript(testCase);

        String testCaseName = testCase.getName().replaceAll("\\W", SpecialCharacter.UNDERSCORE);

        return String.format("void " + AKA_TEST_PREFIX + "%s(void) {\n%s\n}\n", testCaseName, body);
    }

    protected static final String AKA_TEST_PREFIX = "AKA_TEST_";

    private String generateAddTestStm(ITestCase testCase) {
        StringBuilder out = new StringBuilder();

        if (testCase instanceof TestCase) {
            String runStm = generateRunStatement((TestCase) testCase, 1);
            out.append(runStm);
        } else if (testCase instanceof CompoundTestCase) {
            List<TestCaseSlot> slots = ((CompoundTestCase) testCase).getSlots();
            for (TestCaseSlot slot : slots) {
                String name = slot.getTestcaseName();
                int iterator = slot.getNumberOfIterations();
                TestCase element = TestCaseManager.getBasicTestCaseByName(name);
                if (element != null) {
                    String runStm = generateRunStatement(element, iterator);
                    out.append(runStm);
                }
            }
        }

        return out.toString();
    }

    private String generateRunStatement(TestCase testCase, int iterator) {
        String testCaseName = testCase.getName();
        String testName = testCaseName.toUpperCase();
        testCaseName = testCaseName.replaceAll("\\W", SpecialCharacter.UNDERSCORE);
        String test = AKA_TEST_PREFIX + testCaseName;
        return String.format(RUN_FORMAT, testName, test, iterator);
    }

    private static final String RUN_FORMAT = "\t" + DriverConstant.RUN_TEST + "(\"%s\", &%s, %d);\n";

    private String generateAdditionalHeaders() {
        StringBuilder builder = new StringBuilder();

        if (testCase.getAdditionalHeaders() != null) {
            builder.append(testCase.getAdditionalHeaders()).append(SpecialCharacter.LINE_BREAK);
        }

        List<String> userCodeList = testCase.getAdditionalIncludes();
        for (String item : userCodeList) {
            String stm = String.format("#include \"%s\"\n", item);
            builder.append(stm);
        }

        return builder.toString();
    }

    protected String getCloneSourceCodeFile(TestCase testcase) {
        return testcase.getCloneSourcecodeFilePath();
    }

    protected String generateIncludePaths() {
        StringBuilder includedPart = new StringBuilder();

        if (testCase instanceof TestCase) {
            String path = getCloneSourceCodeFile((TestCase) testCase);
            clonedFilePaths.add(path);

            String includeClonedFile = String.format("#include \"%s\"\n", path);
            includedPart.append(includeClonedFile);

            String instanceDeclaration = generateInstanceDeclaration((TestCase) testCase);
            includedPart.append(instanceDeclaration);

            if (!Environment.getInstance().isC()) {
                ICommonFunctionNode sut = ((TestCase) testCase).getRootDataNode().getFunctionNode();

                if (sut instanceof AbstractFunctionNode) {
                    INode realParent = ((AbstractFunctionNode) sut).getRealParent();
                    if (realParent == null) realParent = sut.getParent();

                    while (!(realParent instanceof SourcecodeFileNode)) {
                        if (realParent instanceof NamespaceNode)
                            break;

                        realParent = realParent.getParent();
                    }

                    while (realParent instanceof NamespaceNode) {
                        includedPart.append(SpecialCharacter.LINE_BREAK);
                        String namespace = Search.getScopeQualifier(realParent);
                        String usingNamespace = String.format("using namespace %s;\n", namespace);
                        includedPart.append(usingNamespace);
                        realParent = realParent.getParent();
                    }
                }
            }

        } else if (testCase instanceof CompoundTestCase) {
            List<TestCaseSlot> slots = ((CompoundTestCase) testCase).getSlots();

            for (TestCaseSlot slot : slots) {
                String name = slot.getTestcaseName();
                TestCase element = TestCaseManager.getBasicTestCaseByName(name);

                assert element != null;
                String clonedFilePath = getCloneSourceCodeFile(element);

                if (!clonedFilePaths.contains(clonedFilePath)) {
                    clonedFilePaths.add(clonedFilePath);

                    String path = Utils.doubleNormalizePath(clonedFilePath);
                    String includeClonedFile = String.format("#include \"%s\"\n", path);
                    includedPart.append(includeClonedFile);

                    String instanceDeclaration = generateInstanceDeclaration(element);
                    includedPart.append(instanceDeclaration);
                }
            }
        }

        return includedPart.toString();
    }

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
        if (testCase.getFunctionNode() instanceof ConstructorNode)
            increaseFcall = SpecialCharacter.EMPTY;
        else
            increaseFcall = SourceConstant.INCREASE_FCALLS + generateReturnMark(testCase);


        // STEP 5: Repeat iterator
        String singleScript = String.format(
                "{\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "}",
                testCaseNameAssign,
                testCase.getTestCaseUserCode().getSetUpContent(),
                initialization,
                expectCall,
                functionCall,
                increaseFcall,
                testCase.getTestCaseUserCode().getTearDownContent());

//        StringBuilder script = new StringBuilder();
//        for (int i = 0; i < iterator; i++)
//            script.append(singleScript).append(SpecialCharacter.LINE_BREAK);

        // STEP 6: mark beginning and end of test case
//        script = new StringBuilder(wrapScriptInMark(testCase, script.toString()));
//        script = new StringBuilder(wrapScriptInTryCatch(script.toString()));
//
//        return script.toString();
        singleScript = wrapScriptInTryCatch(singleScript);
        return singleScript;
    }

    protected String generateReturnMark(TestCase testCase) {
        ICommonFunctionNode sut = testCase.getFunctionNode();

        String markStm;

        if (sut instanceof FunctionNode || sut instanceof MacroFunctionNode || sut instanceof LambdaFunctionNode) {
            String relativePath = PathUtils.toRelative(sut.getAbsolutePath());
            markStm = String.format(DriverConstant.MARK + "(\"Return from: %s\");", Utils.doubleNormalizePath(relativePath));
        } else {
            SubprogramNode subprogram = null;

            INode parent = sut.getParent();

            if (sut instanceof IFunctionNode && ((IFunctionNode) sut).getRealParent() != null)
                parent = ((IFunctionNode) sut).getRealParent();

            RootDataNode globalRoot = Search2.findGlobalRoot(testCase.getRootDataNode());

            assert globalRoot != null;
            for (IDataNode globalVar : globalRoot.getChildren()) {
                if (((ValueDataNode) globalVar).getCorrespondingVar() instanceof InstanceVariableNode
                        && ((ValueDataNode) globalVar).getCorrespondingType().equals(parent)
                        && !globalVar.getChildren().isEmpty()
                        && !globalVar.getChildren().get(0).getChildren().isEmpty()) {

                    subprogram = (SubprogramNode) globalVar.getChildren().get(0).getChildren().get(0);
                }
            }

            assert subprogram != null;

            String relativePath = PathUtils.toRelative(sut.getAbsolutePath());

            String functionPath = Utils.doubleNormalizePath(relativePath);
            String subprogramPath = subprogram.getPathFromRoot();
            markStm = String.format(DriverConstant.MARK + "(\"Return from: %s|%s\");", functionPath, subprogramPath);
        }

        return markStm;
    }

    protected abstract String wrapScriptInTryCatch(String script);

//    protected String wrapScriptInMark(TestCase testCase, String script) {
//        String beginMark = generateTestPathMark(MarkPosition.BEGIN, testCase);
//        String endMark = generateTestPathMark(MarkPosition.END, testCase);
//
//        return beginMark + SpecialCharacter.LINE_BREAK + script + endMark;
//    }
//
//    enum MarkPosition {
//        BEGIN,
//        END
//    }
//
//    private String generateTestPathMark(MarkPosition pos, TestCase testCase) {
//        return String.format(DriverConstant.MARK + "(\"%s OF %s\");", pos, testCase.getName().toUpperCase());
//    }

    private String generateInstanceDeclaration(TestCase testCase) {
        if (Environment.getInstance().isC())
            return SpecialCharacter.EMPTY;

        RootDataNode root = testCase.getRootDataNode();
        IDataNode globalRoot = Search2.findGlobalRoot(root);

        // get sut real parent
        ICommonFunctionNode sut = root.getFunctionNode();
        INode realParent = sut.getParent();
        if (sut instanceof AbstractFunctionNode) {
            realParent = ((AbstractFunctionNode) sut).getRealParent();
            if (realParent == null) realParent = sut.getParent();
        }

        StringBuilder init = new StringBuilder();

        if (globalRoot != null) {
            for (IDataNode child : globalRoot.getChildren()) {
                if (child instanceof ValueDataNode) {
                    VariableNode varNode = ((ValueDataNode) child).getCorrespondingVar();
                    INode varTypeNode = varNode.resolveCoreType();

                    if (varNode instanceof InstanceVariableNode
                            && (varTypeNode == realParent
                            || child instanceof StructDataNode
                            || (child instanceof ClassDataNode
                            && !child.getChildren().isEmpty()))
                    ) {
                        SubprogramNode subprogramNode = Search2.findSubprogramUnderTest(root);
                        String type = (subprogramNode instanceof TemplateSubprogramDataNode)
                                ? ((ValueDataNode) child).getRealType()
                                : varNode.getRawType();
                        String name = child.getVituralName();
                        String instanceDefinition = String.format("%s* %s;", type, name);

                        init.append(wrapInIncludeGuard(SourceConstant.GLOBAL_PREFIX + name, instanceDefinition));
                    }
                }
            }
        }

        return init.toString();
    }

    protected String generateInitialization(TestCase testCase) throws Exception {
        StringBuilder initialization = new StringBuilder();

        RootDataNode root = testCase.getRootDataNode();
        GlobalRootDataNode globalRoot = Search2.findGlobalRoot(root);
        IDataNode sutRoot = Search2.findSubprogramUnderTest(root);

        if (globalRoot != null) {
//            List <IDataNode> bfsList = new ArrayList<>() ;
//            bfsList.addAll(globalRoot.getChildren());
//            while(!bfsList.isEmpty()){
//                IDataNode child = bfsList.get(0);
//                if (Environment.getInstance().isC()
//                        && child instanceof ValueDataNode
//                        && ((ValueDataNode) child).getCorrespondingVar() instanceof InstanceVariableNode) {
//                    continue;
//                }
//                initialization += child.getInputForGoogleTest();
//                bfsList.remove(bfsList.get(0));
//                bfsList.addAll(child.getChildren());
//            }
            for (IDataNode child : globalRoot.getChildren()) {
                if (Environment.getInstance().isC()
                        && child instanceof ValueDataNode
                        && ((ValueDataNode) child).getCorrespondingVar() instanceof InstanceVariableNode) {
                    continue;
                }

                if (child instanceof ValueDataNode
                        && !((ValueDataNode) child).haveValue()
                        && !globalRoot.isRelatedVariable(((ValueDataNode) child).getCorrespondingVar()))
                    continue;

                initialization.append(child.getInputForGoogleTest(false));
            }
        }

        if (sutRoot == null)
            initialization = new StringBuilder("/* error initialization */");
        else {

            for(IDataNode child : sutRoot.getChildren()){
                initialization.append(child.getInputForGoogleTest(true));
            }
            initialization.append(sutRoot.getInputForGoogleTest(false));
        }

        initialization = new StringBuilder(initialization.toString().replace(DriverConstant.MARK + "(\"<<PRE-CALLING>>\");",
                String.format(DriverConstant.MARK + "(\"<<PRE-CALLING>> Test %s\");", testCase.getName())));

		initialization = new StringBuilder(initialization.toString().replaceAll("\\bconst\\s+\\b", SpecialCharacter.EMPTY));

        initialization = new StringBuilder(GmockUtils.replaceOldClassByMockClass(testCase, initialization.toString()));
        return initialization.toString();
    }

    protected String generateExpectCall(TestCase testCase) {
        String expects = "";
//        GmockUtils.getMessageWithNode(testCase);
        List<IDataNode> listMock = Search2.searchNodes(testCase.getRootDataNode(), GmockObjectNode.class);
        for (IDataNode mock : listMock) {
            GmockObjectNode mockObj = (GmockObjectNode) mock;
            List<IDataNode> listTimes = Search2.searchNodes(mockObj, TimesNode.class);
            for (IDataNode timeNode : listTimes) {
                if (((TimesNode) timeNode).getValue() != null) {
//                     format: EXPECT_CALL(mock_object, method(matchers))
//                                  .Times()
//                                  .WillOnce(action);

                    SubprogramNode subprogramNode = (SubprogramNode) timeNode.getParent();
                    String method = subprogramNode.getName();
                    if (!subprogramNode.getFunctionNode().getChildren().isEmpty()) {
                        method = method.replaceAll("\\(.*\\)", "(");
                        for (IDataNode arg : subprogramNode.getChildren()) {
                            if(arg instanceof NormalDataNode){
                                method += GmockUtils.getExpectName(arg) + ",";
                            }
                        }
                        method = method.substring(0, method.length() - 1) + ")";
                    }
                    String with = "";
                    // Todo:
//                    WithNode withNode = (WithNode) Search2.searchNodes(subprogramNode, WithNode.class);
                    List<IDataNode> listContainsWithNode = Search2.searchNodes(subprogramNode, WithNode.class);
                    for (IDataNode node : listContainsWithNode){
                        if(node instanceof WithNode){
                            WithNode withNode = (WithNode) node;
                            with = GmockUtils.getMathcherArgument(withNode);
                            break;
                        }
                    }

                    String expect = IGmockSpecialWords.EXPECT_CALL_TEMPLATE
                            .replace(IGmockSpecialWords.MOCK_OBJECT, mockObj.getNameObj())
                            .replace(IGmockSpecialWords.METHOD, method)
                            .replace(IGmockSpecialWords.WITH, with)
                            .replace(IGmockSpecialWords.TIMES, ((TimesNode) timeNode).getValue());
                    for (IDataNode returnNode : timeNode.getChildren()) {
                        expect += "\n\t" + IGmockSpecialWords.WILLONCE + IGmockSpecialWords.RETURN
                                + ((NormalNumberDataNode) returnNode).getValue() + "))";
                    }
                    expect += SpecialCharacter.END_OF_STATEMENT + "\n\n";
                    expects += expect;
                }
            }
        }


        return expects;
    }

    protected String generateFunctionCall(TestCase testCase) {
        ICommonFunctionNode functionNode = testCase.getFunctionNode();
        String declareReturnVariable = SpecialCharacter.EMPTY;
        String functionCall;

        if (functionNode instanceof ConstructorNode) {
            return SpecialCharacter.EMPTY;
        }

        VariableNode returnVar = Search2.getReturnVarNode(functionNode);

        String returnType;

        if (returnVar != null) {
            if (Environment.getInstance().isC()) {
                returnType = returnVar.getRealType();
                returnType = VariableTypeUtils.deleteStorageClasses(returnType);
                returnType = VariableTypeUtils.deleteVirtualAndInlineKeyword(returnType);
            } else {
                returnType = VariableTypeUtils.getSimpleRealType(returnVar);
            }
        } else
            returnType = functionNode.getReturnType();

        returnType = returnType.trim();
        returnType = VariableTypeUtils.deleteVirtualAndInlineKeyword(returnType);
        returnType = VariableTypeUtils.deleteStorageClassesExceptConst(returnType);

        if (functionNode instanceof MacroFunctionNode || functionNode.isTemplate()) {
            SubprogramNode sut = Search2.findSubprogramUnderTest(testCase.getRootDataNode());

            if (sut != null)
                returnType = sut.getRawType();

            if (functionNode.isTemplate()) {
                TemplateSubprogramDataNode templateSubprogramDataNode = (TemplateSubprogramDataNode) Search2.findSubprogramUnderTest(testCase.getRootDataNode());
                Map<String, String> typeMapping = templateSubprogramDataNode.getRealTypeMapping();
                for (String key : typeMapping.keySet()) {
                    if (returnType.equals(key)) {
                        returnType = typeMapping.get(key);
                        break;
                    }
                }
            }
        }

        if (functionNode instanceof DestructorNode) {
            functionCall = Utils.getFullFunctionCall(functionNode);

        } else if (!returnType.equals(VariableTypeUtils.VOID_TYPE.VOID)) {
            if (Environment.getInstance().getCompiler().isUseGTest()) {
                if (returnType.equals("string")) returnType = "std::" + returnType;
                declareReturnVariable = String.format("%s %s;", returnType, SourceConstant.ACTUAL_OUTPUT);
                functionCall = SourceConstant.ACTUAL_OUTPUT;
            } else {
                functionCall = returnType + " " + SourceConstant.ACTUAL_OUTPUT;
            }
            if (functionNode.isTemplate()) {
                TemplateSubprogramDataNode templateSubprogramDataNode = (TemplateSubprogramDataNode) Search2.findSubprogramUnderTest(testCase.getRootDataNode());
                functionCall += "=" + Utils.getFullFunctionCallTemplate(functionNode, testCase);
            } else functionCall += "=" + Utils.getFullFunctionCall(functionNode);
        } else {
            if (functionNode.isTemplate()) {
                TemplateSubprogramDataNode templateSubprogramDataNode = (TemplateSubprogramDataNode) Search2.findSubprogramUnderTest(testCase.getRootDataNode());
                functionCall = Utils.getFullFunctionCallTemplate(functionNode, testCase);
            } else functionCall = Utils.getFullFunctionCall(functionNode);
        }
        functionCall = functionCall.replaceAll(ProjectClone.MAIN_REGEX, ProjectClone.MAIN_REFACTOR_NAME);

        if (Environment.getInstance().getCompiler().isUseGTest()) {
            declareReturnVariable = declareReturnVariable.replaceAll(ProjectClone.MAIN_REGEX, ProjectClone.MAIN_REFACTOR_NAME) + "\n";
            SubprogramNode sut = Search2.findSubprogramUnderTest(testCase.getRootDataNode());
            if (sut.getAssertMethod() != null) {
                if (IExceptionAssertions.assertMethods.contains(sut.getAssertMethod().split(": ", 2)[0])) {
                    functionCall = "\n" + sut.getExceptionAssertion(functionCall);
                } else if (IPredicateAssertions.assertMethods.contains(sut.getAssertMethod())) {
                    declareReturnVariable = "";
                    functionCall = Utils.getFunctionCallWithPredAssert(functionNode);
                    functionCall = functionCall.replaceAll(ProjectClone.MAIN_REGEX, ProjectClone.MAIN_REFACTOR_NAME);
                    functionCall = "\n" + sut.getPredicateAssertion(functionCall);
                } else if (IDeathAssertions.assertMethods.contains(sut.getAssertMethod().split(": ",2)[0])) {
                    functionCall = "\n" + sut.getDeathAssertion(functionCall);
                }
            }

        }

        functionCall = String.format(declareReturnVariable + DriverConstant.MARK + "(\"<<PRE-CALLING>> Test %s\");%s", testCase.getName(), functionCall);

        return functionCall;
    }

    public void setTestCase(ITestCase testCase) {
        this.testCase = testCase;
    }
}
