package com.dse.testdata.comparable.gmock;

import com.dse.environment.Environment;
import com.dse.parser.FunctionCallParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.object.*;
import com.dse.project_init.ProjectClone;
import com.dse.search.Search2;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.object.Gmock.ArgumentNode;
import com.dse.testdata.object.Gmock.GmockObjectNode;
import com.dse.testdata.object.Gmock.MatcherMultipleNode;
import com.dse.testdata.object.Gmock.WithNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.NormalDataNode;
import com.dse.testdata.object.SubprogramNode;
import com.dse.util.VariableTypeUtils;
import org.apache.commons.math3.util.Pair;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmockUtils {
    private static TestCase testCase;


    public GmockUtils(ITestCase testCase) {
        this.testCase = (TestCase) testCase;
    }

    public static final int ARG = 1;
    public static final int ATTRIBUTE = 2;
    public static final int GLOBAL = 3;
    public static final int SINGLETON = 4;

    public static Map<String, Pair<String, Integer>> findMockObject(FunctionNode functionNode) {
        IASTFunctionDefinition fnAST = functionNode.getAST();
        FunctionCallParser visitor = new FunctionCallParser();
        fnAST.accept(visitor);
        Map<String, Pair<String, Integer>> listObj = new HashMap<>();
        for (IASTFunctionCallExpression expression : visitor.getExpressions()) {
            IASTExpression expression1 = expression.getFunctionNameExpression();
            String nameObj = ((CPPASTFieldReference) expression1).getFieldOwner().getRawSignature();
            for (IVariableNode arg : functionNode.getArgumentsAndGlobalVariables()) {
                if (arg.getName().equals(nameObj)) {
                    if (arg instanceof InternalVariableNode) {
                        listObj.put(nameObj, new Pair<>(arg.getCoreType(), ARG));
                        break;
                    }
                    if (arg instanceof ExternalVariableNode) {
                        listObj.put(nameObj, new Pair<>(arg.getCoreType(), GLOBAL));
                        break;
                    }
                }
            }
            if (functionNode.getParent() instanceof ClassNode) {
                for (INode child : functionNode.getParent().getChildren()) {
                    if (child instanceof AttributeOfStructureVariableNode) {
                        AttributeOfStructureVariableNode attribute = (AttributeOfStructureVariableNode) child;
                        if (attribute.getName().equals(nameObj)) {
                            listObj.put(nameObj, new Pair<>(attribute.getCoreType(), ATTRIBUTE));
                            break;
                        }
                    }
                }
            }
        }

        return listObj;
    }

    public static ArrayList<INode> findMockObjects(FunctionNode functionNode) {
        IASTFunctionDefinition fnAST = functionNode.getAST();
        FunctionCallParser visitor = new FunctionCallParser();
        fnAST.accept(visitor);
        Set<INode> listObj = new HashSet<>();
        for (IASTFunctionCallExpression expression : visitor.getExpressions()) {
            IASTExpression expression1 = expression.getFunctionNameExpression();
            String nameObj = "";
            if (expression1 instanceof CPPASTFieldReference) {
                nameObj = ((CPPASTFieldReference) expression1).getFieldOwner().getRawSignature();
            }
            for (IVariableNode arg : functionNode.getArgumentsAndGlobalVariables()) {
                if (arg.getName().equals(nameObj)) {
                    listObj.add(arg);
                    break;
                }
            }
            if (functionNode.getParent() instanceof ClassNode) {
                for (INode child : functionNode.getParent().getChildren()) {
                    if (child instanceof AttributeOfStructureVariableNode) {
                        AttributeOfStructureVariableNode attribute = (AttributeOfStructureVariableNode) child;
                        if (attribute.getName().equals(nameObj)) {
                            listObj.add(attribute);
                            break;
                        }
                    }
                }
            }
            for (Dependency dependency : functionNode.getDependencies()) {
                if (dependency instanceof FunctionCallDependency) {
                    ICommonFunctionNode functionCall = (ICommonFunctionNode) dependency.getEndArrow();
                    String nameClass = functionCall.getParent().getName();
                    String returnType = functionCall.getReturnType().replace("*", "");
                    if (nameClass.equals(returnType)) {
                        String name = nameClass + "::" + functionCall.getName();
                        if (nameObj.equals(name)) {
                            listObj.add(functionCall);
                            break;
                        }
                    }
                }
            }
        }

        return new ArrayList<>(listObj);
    }

    public static String createMockClass(ITestCase testCase) {
        String output = "";
        List<IDataNode> listMockObj = Search2.searchNodes(((TestCase) testCase).getRootDataNode(), GmockObjectNode.class);
        Set<ClassNode> classNodeSet = new HashSet<>();
        for (IDataNode node : listMockObj) {
            if (!node.getChildren().isEmpty() && ((GmockObjectNode) node).isMocked()) {
                classNodeSet.add((ClassNode) ((SubprogramNode) node.getChildren().get(0)).getFunctionNode().getParent());
            }
        }
        ArrayList<ClassNode> classNodes = new ArrayList<>(classNodeSet);
        for (ClassNode classNode : classNodes) {
//            String includePath = ProjectClone.getClonedFilePath(classNode.getParent().getAbsolutePath());
            INode node = classNode.getParent();
            String namespace = classNode.getName();
            if (classNode.getParent() instanceof NamespaceNode) {
                while (!(node instanceof ISourcecodeFileNode)) {
                    namespace = node.getName() + "::" + namespace +";";
                    node = node.getParent();
                }
            }

            String includePath = "";
            if (Environment.getInstance().isOnWhiteBoxMode()) {
                includePath = ProjectClone.getClonedFilePath(classNode.getParent().getAbsolutePath());
            } else {
                includePath = ProjectClone.getOriginFilePath(classNode.getParent().getAbsolutePath());
            }

            List<String> nameSpaceList = new ArrayList<>();
            if(classNode.getParent() instanceof NamespaceNode){
                NamespaceNode parent = (NamespaceNode) classNode.getParent();
                nameSpaceList.add(parent.getName());
                while(parent.getParent() instanceof NamespaceNode) {
                    parent = (NamespaceNode) parent.getParent();
                    nameSpaceList.add(parent.getName());
                }
            }
            output += "\n" + IGmockSpecialWords.INCLUDE + "\"" + includePath + "\"\n";
            if (!namespace.equals(classNode.getName())) {
                output += "using " + namespace + "\n\n";
            }

            //create namespace
            for(String name : nameSpaceList) {
                output += "namespace " + name + " {" + "\n";
            }

            // create mock_method
            // format: MOCK_METHOD(ReturnType, MethodName, (Args ...));
            //         MOCK_METHOD(ReturnType, MethodName, (Args ...), (Specs ...));
//            List<String> listMethod = new ArrayList<>();
            Map<String, Integer> listMethod = new HashMap<>();
            
            for (INode child : classNode.getChildren()) {
                String method = "";
                int visibility = ICPPASTVisibilityLabel.v_private;
                if (child instanceof AttributeOfStructureVariableNode) {
                    method += ((AttributeOfStructureVariableNode) child).getRawType() + " " + child.getName() + ";\n";
                } else if (child instanceof ConstructorNode) {
                    String newConstructor = child.toString().trim().replace(classNode.getName(), "Mock" + classNode.getName());
//                    String oldConstructor = classNode.getName() + "(";
//                    for (Node arg : child.getChildren()) {
//                        oldConstructor += arg.getName() + ",";
//                    }
//                    if (oldConstructor.endsWith(",")) {
//                        oldConstructor = oldConstructor.substring(0, oldConstructor.length() - 1);
//                    }
//                    oldConstructor += ")";
//                    method += newConstructor + " : " + oldConstructor + " {}\n";
                    method += newConstructor + " {}\n";
                    visibility = ((ConstructorNode) child).getVisibility();
                } else if (child instanceof FunctionNode
                        && ((FunctionNode) child).getReturnType().replace("*", "").equals(classNode.getName())) {
                    method += "static Mock" + child.toString() + "{\n\t"
                            + "static Mock" + ((FunctionNode) child).getReturnType().replace("*", "") + " obj;\n\t"
                            + "return &obj;\n}\n";
                    visibility = ((FunctionNode) child).getVisibility();
                } else if (child instanceof DefinitionFunctionNode || child instanceof FunctionNode) {
                    ICommonFunctionNode function = (ICommonFunctionNode) child;
                    String returnType = function.getReturnType();
                    if (returnType.isEmpty()) {
                        continue;
                    } else {
                        if (!VariableTypeUtils.isPrimitive(returnType)
                                && returnType.replace("*", "").equals(classNode.getName())
                                && function.toString().contains("static")) {
                            method += function.toString().replace(classNode.getName(), "Mock" + classNode.getName())
                                    .replace(";", "")
                                    + "{\n\t"
                                    + "static Mock" + classNode.getName() + " obj;\n\t"
                                    + "return &obj;\n}\n";
                        } else {

                            String methodName = function.getSimpleName();
                            String args = "(" + function.getName().split("\\(")[1];
                            String specs = "";
//                            if (function.toString().contains("virtual")) {
//                                specs += "override,";
//                            }
                            if (Pattern.matches(".*\\(.*\\).*const.*", function.toString())
                                    || Pattern.matches(".*const.*\\(.*\\).*", function.toString())) {
                                specs += "const,";
                            }
                            if (!specs.isEmpty()) {
                                specs = specs.substring(0, specs.length() - 1);
                                specs = ", (" + specs + ")";
                            }
                            method += IGmockSpecialWords.MOCK_METHOD_TEMPLATE
                                    .replace(IGmockSpecialWords.RETURN_TYPE, returnType)
                                    .replace(IGmockSpecialWords.METHOD_NAME, methodName)
                                    .replace(IGmockSpecialWords.ARGS, args)
                                    .replace(IGmockSpecialWords.SPECS, specs);
                        }
                    }
                    visibility = ((DefinitionFunctionNode) child).getVisibility();
                }
//                if (!listMethod.contains(method)) {
//                    listMethod.add(method);
//                }
                if (!listMethod.containsKey(method)) {
                    listMethod.put(method, visibility);
                }
            }
            String public_methods = "";
            String private_methods = "";
            String protected_methods = "";
            for (String method : listMethod.keySet()) {
                if (listMethod.get(method) == ICPPASTVisibilityLabel.v_public) {
                    public_methods += "\t" + method;
                } else if (listMethod.get(method) == ICPPASTVisibilityLabel.v_private) {
                    private_methods += "\t" + method;
                } else {
                    protected_methods += "\t" + method;
                }
            }

            String mockClass = IGmockSpecialWords.MOCK_CLASS_TEMPLATE
                    .replace(IGmockSpecialWords.NAME_CLASS, classNode.getName())
                    .replace(IGmockSpecialWords.PRIVATE_METHOD, private_methods)
                    .replace(IGmockSpecialWords.PROTECTED_METHOD, protected_methods)
                    .replace(IGmockSpecialWords.PUBLIC_METHOD, public_methods);

            output += mockClass;
            for(int i = 0; i < nameSpaceList.size(); i++){
                output += "}\n";
            }
        }
        return output;
    }

    public static String initalizeStaticVariables(ITestCase testcase) {
        return testcase.getName();
    }

    public static String createVerifyAndClearExpectations(ITestCase testCase) {
        String output = "";
        List<IDataNode> listMockObj = Search2.searchNodes(((TestCase) testCase).getRootDataNode(), GmockObjectNode.class);
        for (IDataNode node : listMockObj) {
            GmockObjectNode mockObj = (GmockObjectNode) node;
            if (mockObj.isMocked() && mockObj.getNameObj().contains("*")) {
                return IGmockSpecialWords.VerifyAndClearExpectations
                        .replace(IGmockSpecialWords.MOCK_OBJECT, mockObj.getNameObj().replace("*", ""));
            }
        }
        return "";
    }

    public static String replaceOldClassByMockClass(ITestCase testCase, String input) {
        List<IDataNode> listMockObj = Search2.searchNodes(((TestCase) testCase).getRootDataNode(), GmockObjectNode.class);
        for (IDataNode node : listMockObj) {
            GmockObjectNode obj = (GmockObjectNode) node;
            obj.setNameObj((TestCase) testCase);
            if (obj.isMocked()) {
                String name = obj.getNameObj().startsWith("*") ? obj.getNameObj().substring(1) : obj.getNameObj();

//            if (obj.getTypeObj() == GLOBAL) {
                Pattern pattern1 = Pattern.compile(name + " .*new.*" + obj.getNameClass());
                Matcher matcher1 = pattern1.matcher(input);
                while (matcher1.find()) {
                    String sOld = matcher1.group();
                    String sNew = sOld.replace(obj.getNameClass(), "Mock" + obj.getNameClass());
                    input = input.replace(sOld, sNew);
                }
//            }

                Pattern pattern2 = Pattern.compile(obj.getNameClass() + ".* " + name + " .*" + obj.getNameClass());
                Matcher matcher2 = pattern2.matcher(input);
                while (matcher2.find()) {
                    String sOld = matcher2.group();
                    String sNew = sOld.replace(obj.getNameClass(), "Mock" + obj.getNameClass());
                    input = input.replace(sOld, sNew);
                }
            }
        }
        return input;
    }

    public static String getExpectName(IDataNode arg){
        if (arg instanceof NormalDataNode) {
            StringBuilder sb = new StringBuilder();
            String value = ((NormalDataNode) arg).getValue();
            if (value == null) {
                return "::testing::_";
            }
            if (((NormalDataNode) arg).getAssertMethod() == null) {
                ((NormalDataNode) arg).setAssertMethod("EXPECT_EQ");
            }
            if (((NormalDataNode) arg).getAssertMethod().startsWith("EXPECT_THAT")) {
                return ((NormalDataNode) arg).getAssertMethod().replace("EXPECT_THAT: ", "");
            }
            sb.append(getAssert(((NormalDataNode) arg).getAssertMethod()) + "(" + value + ")");
            return sb.toString();
        }
        return "";
    }


    public static String getMathcherArgument(WithNode withNode) {
        StringBuilder with = new StringBuilder("\n\t.With(");
        if(withNode.getAssertMethod() == null) {
            withNode.setAssertMethod("ALL_OF");
        }
        // get with method
        switch (withNode.getAssertMethod()){
            case "ALL_OF":
                with.append("::testing::AllOf(");
                break;
            case "ANY_OF":
                with.append("::testing::AnyOf(");
                break;
            default:
                return with + withNode.getAssertMethod().replace("EXPECT_THAT: ", "") + ")";

        }

        for (IDataNode matcher : withNode.getChildren()) {
            // Initialize Args<?, ?>

            with.append("::testing::Args<");
            if (matcher.getChildren().get(0) == null || matcher.getChildren().get(1) == null)  return "";
            ICommonFunctionNode functionNode = (ICommonFunctionNode) ((SubprogramNode) withNode.getParent()).getFunctionNode();
            for(IDataNode arg : matcher.getChildren()){
                with.append(getParameterIndex(functionNode, arg));
                with.append(",");
            }
            with.deleteCharAt(with.length() - 1);
            with.append(">");

            // Initialize (Gt()) or other assert methods
            if(((MatcherMultipleNode) matcher).getAssertMethod() == null){
                ((MatcherMultipleNode) matcher).setAssertMethod("EXPECT_EQ");
            }
            with.append("(" + getAssert(((MatcherMultipleNode) matcher).getAssertMethod()) + "()),");
        }

        with.deleteCharAt(with.length() - 1);
        with.deleteCharAt(with.length() - 1);
        with.append(")))");
        return with.toString();
    }

    private static String getParameterIndex(ICommonFunctionNode functionNode, IDataNode arg){
        for (int i = 0; i < functionNode.getChildren().size(); i++) {
            if(functionNode.getChildren().get(i).getName().equals(((ArgumentNode) arg).getValue())){
                return String.valueOf(i);
            }
        }
        return String.valueOf(0);
    }

    /**
     * EXPECT_EQ -> Eq
     */
    private static String getAssert(String assertMethod) {
        String out = "::testing::";
        if (assertMethod.startsWith("EXPECT") && !assertMethod.equals("EXPECT_THAT")) {
            String strs[] = assertMethod.replace("EXPECT_", "").split("_");
            for (String str : strs) {
                out += str.charAt(0) + str.substring(1).toLowerCase();
            }
            return out;
        } else {
            return "::testing::Eq";
        }
    }

    public static String includeMockFile(ITestCase testCase) {
        String path = testCase.getPath().split("test-cases")[0];
        path += "instruments/mockclass.akaignore.h";
        File file = new File(path);
        // ten file : mockclass.aka...
        try {
            if(!file.exists()) file.createNewFile();
            FileWriter myWriter = new FileWriter(file.getAbsoluteFile());
            myWriter.write(createMockClass(testCase));
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String includePath = "#include \"" + file.getAbsolutePath() + "\"";
        return includePath;
    }

    public static HashSet<ClassNode> getMockClassSet(ITestCase testCase) {
        List<IDataNode> listMockObj = Search2.searchNodes(((TestCase) testCase).getRootDataNode(), GmockObjectNode.class);
        HashSet<ClassNode> classNodeSet = new HashSet<>();
        for (IDataNode node : listMockObj) {
            if (!node.getChildren().isEmpty() && ((GmockObjectNode) node).isMocked()) {
                classNodeSet.add((ClassNode) ((SubprogramNode) node.getChildren().get(0)).getFunctionNode().getParent());
            }
        }
        return classNodeSet;
    }

    public static String getMessageWithNode(ITestCase testCase) {
        String message = "";
        List<IDataNode> listMock = Search2.searchNodes(((TestCase) testCase).getRootDataNode(), GmockObjectNode.class);
        for (IDataNode mock : listMock) {
            GmockObjectNode mockObj = (GmockObjectNode) mock;
            List<IDataNode> listWithNode = Search2.searchNodes(mockObj, WithNode.class);
            for (IDataNode node : listWithNode) {
                if (node.getName().equals("With")) {
                    WithNode withNode = (WithNode) node;
                    for (IDataNode child : withNode.getChildren()) {
                        for (IDataNode arg : child.getChildren()) {
                            if (((ArgumentNode) arg).getValue() == null) {
                                message += mockObj.getName() + "/" + withNode.getParent().getName() + "/With/"
                                        + child.getName() + ": The " + arg.getName() + " is null\n";
                            }

                        }
                    }
                }
            }
        }
        return message;
    }
}
