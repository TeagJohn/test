package auto_testcase_generation.testdatagen.templateType;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.ICfgNode;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.Search2;
import com.dse.search.condition.ClassvsStructvsNamespaceCondition;
import com.dse.search.condition.StructNodeCondition;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testdata.object.*;
import com.dse.util.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dse.util.TemplateUtils.deleteTemplateParameters;

public class TestDataPrototypeAutomatedGenerationForTemplate {

    private AkaLogger logger = AkaLogger.get(TestDataPrototypeAutomatedGenerationForTemplate.class);

    private ICommonFunctionNode functionNode;
    private String name;
    private int nameID = 0;
    private List<TestPrototype> primitiveDefaultPrototype = new ArrayList<>();
    private List<TestPrototype> structurePrototype = new ArrayList<>();

    public static String DEFAULT_VAL = "0";


    public TestDataPrototypeAutomatedGenerationForTemplate(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
        this.name = functionNode.getName();
        genData();
    }

    public TestDataPrototypeAutomatedGenerationForTemplate(ICommonFunctionNode functionNode, String name) {
        this.functionNode = functionNode;
        this.name = name;
        genData();
    }

    private void genData() {
        try {
//            generatePrimitivePrototypes();
            generateStructurePrototypes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TestPrototype createEmptyPrototype(ICommonFunctionNode functionNode) {
        TestPrototype prototype = TestCaseManager.createPrototype(genPrefixName(), functionNode);
        if (functionNode instanceof FunctionNode) {
            IASTNode[] astChildren = ((FunctionNode) functionNode).getAST().getParent().getChildren();
            for (int i = 0; i < astChildren.length - 1; i++) {
                ASTNode child = (ASTNode) astChildren[i];
                ValueDataNode dataNode = null;
                /*if (child instanceof CPPASTSimpleTypeTemplateParameter) {
                    String rawSignature = child.getRawSignature();
                    if (rawSignature.startsWith("class")) {
                        dataNode = new ClassDataNode();
                        dataNode.setParent(Search2.findSubprogramUnderTest(prototype.getRootDataNode()));
                        dataNode.setVirtualName();
                    }
                    else if (rawSignature.startsWith("struct")) {
                        dataNode = new StructDataNode();
                        dataNode.setParent(Search2.findSubprogramUnderTest(prototype.getRootDataNode()));
                        dataNode.setVirtualName();
                    }

                }
                else */if (child instanceof CPPASTParameterDeclaration) {
                    String type = String.valueOf(((CPPASTParameterDeclaration) child).getDeclSpecifier());
                    if (VariableTypeUtils.isNumBasic(type)) {
                        dataNode = new NormalNumberDataNode();
                    }
                    else if (VariableTypeUtils.isChBasic(type)) {
                        dataNode = new NormalCharacterDataNode();
                    }
                    else if (VariableTypeUtils.isStrBasic(type)) {
                        dataNode = new NormalStringDataNode();
                    }
                    String name = ((CPPASTParameterDeclaration) child).getDeclarator().getName().getRawSignature();
                    dataNode.setName(name);
//                    dataNode.setVirtualName();
                    dataNode.setRealType(type);
                    dataNode.setRawType(type);
                }
                if (dataNode != null) {
                    TemplateSubprogramDataNode templateSubprogramDataNode = (TemplateSubprogramDataNode) Search2.findSubprogramUnderTest(prototype.getRootDataNode());
                    dataNode.setParent(templateSubprogramDataNode);
                    dataNode.setVituralName("TEMPLATE_ARG_" + dataNode.getName());
                    templateSubprogramDataNode.getListArgOfTemplate().add(dataNode);
                }
            }
        }
        return prototype;
    }

    private void generatePrimitivePrototypes() {
        primitiveDefaultPrototype = new ArrayList<>();
        TestPrototype intPrototype = genPrimitivePrototype("int");
        TestCaseManager.exportPrototypeToFile(intPrototype);
        TestPrototype stringPrototype = genPrimitivePrototype("double");
        TestCaseManager.exportPrototypeToFile(stringPrototype);
        primitiveDefaultPrototype.add(intPrototype);
        primitiveDefaultPrototype.add(stringPrototype);
    }

    public TestPrototype genPrimitivePrototype(String type) {
        TestPrototype intPrototype = createEmptyPrototype(functionNode);
        //Todo:
        String definition = TestDataPrototypeAutomatedGenerationForTemplate.createPrototypeWithSpecificParameter(type, functionNode);
        //
        DefinitionFunctionNode definitionFunctionNode = null;
        if (definition != null) {
            IASTNode ast = Utils.convertToIAST(definition);
            if (ast instanceof IASTDeclarationStatement)
                ast = ((IASTDeclarationStatement) ast).getDeclaration();

            if (ast instanceof IASTSimpleDeclaration) {
                definitionFunctionNode = new DefinitionFunctionNode();
                definitionFunctionNode.setAbsolutePath(functionNode.getAbsolutePath());
                definitionFunctionNode.setAST((CPPASTSimpleDeclaration) ast);
                definitionFunctionNode.setName(definitionFunctionNode.getNewType());
            }
        }
        //
        RootDataNode rootDataNode = intPrototype.getRootDataNode();
        TemplateSubprogramDataNode templateSubprogramDataNode = (TemplateSubprogramDataNode) Search2.searchNodes(rootDataNode, TemplateSubprogramDataNode.class).get(0);

        try {
            //todo: need implement both the type mapping value as an args, maybe stored in global vars
            templateSubprogramDataNode.setDefinition(definition);
            templateSubprogramDataNode.setRealFunctionNode(definitionFunctionNode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (functionNode instanceof FunctionNode) {
            Map<String, String> typeMapping = templateSubprogramDataNode.getRealTypeMapping();
            INode realParent = ((FunctionNode) functionNode).getRealParent() == null ? ((FunctionNode) functionNode).getParent() : ((FunctionNode) functionNode).getRealParent();
            if (realParent instanceof StructOrClassNode) {
                if (((StructOrClassNode) realParent).isTemplate()) {
                    String[] templateParams = TemplateUtils.getTemplateParameters(realParent);
                    if (templateParams != null) {
                        for (String templateParam : templateParams) {
                            if (!VariableTypeUtils.isBasic(templateParam)) {
                                typeMapping.put(templateParam, type);
                            }
                        }
                    }
                }
            }

            if (typeMapping.isEmpty()) {
                IASTNode[] astNodes = ((FunctionNode) functionNode).getAST().getParent().getChildren();
                for (IASTNode astNode : astNodes) {
                    if (astNode instanceof CPPASTSimpleTypeTemplateParameter) {
                        typeMapping.put(((CPPASTSimpleTypeTemplateParameter) astNode).getName().getRawSignature(), type);
                    }
                    else if (astNode instanceof CPPASTParameterDeclaration) {
                        String name = ((CPPASTParameterDeclaration) astNode).getDeclarator().getName().getRawSignature();
                        for (IValueDataNode valueDataNode : templateSubprogramDataNode.getListArgOfTemplate()) {
                            if (valueDataNode instanceof NormalDataNode && valueDataNode.getName().equals(name)) {
                                if (((NormalDataNode) valueDataNode).getValue() == null) {
                                    ((NormalDataNode) valueDataNode).setValue(DEFAULT_VAL);
                                }
                                typeMapping.put(name, ((NormalDataNode) valueDataNode).getValue());
                                break;
                            }
                        }
                    }
                }
            }
        }
        //set corresponding variable
        List<IVariableNode> listVarArg = functionNode.getArguments();
        List<IDataNode> listDataArg = templateSubprogramDataNode.getChildren();
        for (int i = 0; i < Math.abs(listDataArg.size() - listVarArg.size()); i++) {
            IDataNode data = templateSubprogramDataNode.getChildren().get(i);
            IValueDataNode dataNode = null;
            if (data instanceof ValueDataNode) dataNode = (IValueDataNode) data;
            else break;
            IVariableNode variableNode = listVarArg.get(i);
            dataNode.setCorrespondingVar((VariableNode) variableNode);
            dataNode.setName(variableNode.getName());
            dataNode.setVituralName(variableNode.getNewType());
            VariableNode cloneVar = (VariableNode) variableNode.clone();
            cloneVar.setRawType(type);
            cloneVar.setCoreType(type);
            cloneVar.setReducedRawType(type);
            cloneVar.setParent(variableNode);
            dataNode.setCorrespondingVar(cloneVar);
        }


        refactorRealTypeTemplate(intPrototype);

        return intPrototype;
    }

    private void generateStructurePrototypes() {
        structurePrototype.clear();
        List<TestPrototype> list = getStructureVsClassNodeByStrategy(functionNode);
        for (TestPrototype prototype : list) {
            TestCaseManager.exportPrototypeToFile(prototype);
        }
        structurePrototype.addAll(Objects.requireNonNull(list)) ;
        logger.debug("Generate structure prototype automatically.");
    }

    private List<TestPrototype> getStructureVsClassNodeByStrategy(ICommonFunctionNode functionNode) {
        List<TestPrototype> prototypeList = new ArrayList<>();
        Map<String , Map<String, Set<String>>> listFeature= getFeaturesOfFunction((FunctionNode) functionNode);
        List<INode> listStructureNode = findStructureNodeInProject(Environment.getInstance().getProjectNode());
        List<INode> listAfterReduce = new ArrayList<>();
        //Search.searchNodes(Environment.getInstance().getProjectNode(), new ClassvsStructvsNamespaceCondition())
        for (int i = 0; i<listStructureNode.size();i++) {
            INode node = listStructureNode.get(i);
            boolean valid = false;
            if(node instanceof StructNode) {
                valid = true;
            } else if(node instanceof ClassNode) {
                if(!((ClassNode) node).isTemplate()) valid=true;
            }
//            ClassNode node = (ClassNode) listStructureNode.get(i);
            if(valid) {
                for (int j=0; j<node.getChildren().size();j++) {
                    String str = node.getChildren().get(j).getName();
                    boolean flag = false;
                    int idx = str.indexOf("(");
                    if (idx!=-1) str= str.substring(0,idx);
                    for(Map.Entry<String, Map<String, Set<String>>> entry: listFeature.entrySet()) {
                        if(entry.getValue().get("attribute").contains(str)||entry.getValue().get("operator").contains(str)) {
                            flag = true;
                        }
                    }
                    if(flag) {
                        listAfterReduce.add(node);
                        break;
                    }
                }
            }
        }
        logger.debug("NUMBER OF STRUCT CLASS: " + listStructureNode.size());
        logger.debug("NUMBER OF STRUCT CLASS: " + listAfterReduce.size());

        for (int i = 0; i < listAfterReduce.size(); i++) {
            INode node = listAfterReduce.get(i);
            if (node instanceof StructOrClassNode) {
                StructureDataNode structureDataNode = null;
                structureDataNode = node instanceof StructNode ? new StructDataNode() : new ClassDataNode();
                node = node instanceof StructNode ? (StructNode)node : (ClassNode)node;
                if (node instanceof ClassNode) {
                    if (((ClassNode) node).isTemplate()) {
                        continue;
                    }
                }
            }
            TestPrototype prototype = createEmptyPrototype(functionNode);
            TemplateSubprogramDataNode templateSubprogramDataNode = (TemplateSubprogramDataNode) Search2.findSubprogramUnderTest(prototype.getRootDataNode());
            if (templateSubprogramDataNode == null) break;
            String type = node.getName();
            String definition = createPrototypeWithSpecificParameter(type, functionNode);
            try {
                templateSubprogramDataNode.setRealFunction(definition);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //create value node
//            generateData(node, templateSubprogramDataNode);
            if (functionNode instanceof FunctionNode) {
                Map<String, String> typeMapping = templateSubprogramDataNode.getRealTypeMapping();
                INode realParent = ((FunctionNode) functionNode).getRealParent() == null ? ((FunctionNode) functionNode).getParent() : ((FunctionNode) functionNode).getRealParent();
                if (realParent instanceof StructOrClassNode) {
                    if (((StructOrClassNode) realParent).isTemplate()) {
                        String[] templateParams = TemplateUtils.getTemplateParameters(realParent);
                        if (templateParams != null) {
                            for (String templateParam : templateParams) {
                                if (!VariableTypeUtils.isBasic(templateParam)) {
                                    typeMapping.put(templateParam, type);
                                }
                            }
                        }
                    }
                }
                if (typeMapping.isEmpty()) {

                    IASTNode[] astNodes = ((FunctionNode) functionNode).getAST().getParent().getChildren();
                    for (IASTNode astNode : astNodes) {
                        if (astNode instanceof CPPASTSimpleTypeTemplateParameter) {
                            typeMapping.put(((CPPASTSimpleTypeTemplateParameter) astNode).getName().getRawSignature(), type);
                        }
                        else if (astNode instanceof CPPASTParameterDeclaration) {
                            String name = ((CPPASTParameterDeclaration) astNode).getDeclarator().getName().getRawSignature();
                            for (IValueDataNode valueDataNode : templateSubprogramDataNode.getListArgOfTemplate()) {
                                if (valueDataNode instanceof NormalDataNode && valueDataNode.getName().equals(name)) {
                                    if (((NormalDataNode) valueDataNode).getValue() == null) {
                                        ((NormalDataNode) valueDataNode).setValue(DEFAULT_VAL);
                                    }
                                    typeMapping.put(name, ((NormalDataNode) valueDataNode).getValue());
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            for (IVariableNode variableNode : functionNode.getArguments()) {
                IValueDataNode dataNode = generateData(node);
                if (dataNode == null) {
                    System.out.println();
                }
                dataNode.setCorrespondingVar((VariableNode) variableNode);
                dataNode.setName(variableNode.getName());
                dataNode.setVituralName(variableNode.getNewType());
                VariableNode cloneVar = (VariableNode) variableNode.clone();
                cloneVar.setRawType(type);
                cloneVar.setCoreType(type);
                cloneVar.setReducedRawType(type);
                cloneVar.setParent(variableNode);
                dataNode.setCorrespondingVar(cloneVar);
                templateSubprogramDataNode.getChildren().add(dataNode);
            }
            refactorRealTypeTemplate(prototype);

            prototypeList.add(prototype);
        }
        return prototypeList;
    }

    private List<INode> findStructureNodeInProject(INode root) {
        List<INode> list = new ArrayList<>();
        if (root instanceof ClassNode || root instanceof StructNode) {
            List<Node> children = root.getChildren();
            list.add(root);
        }
        for (INode child : root.getChildren()) {
            List<INode> listChild = findStructureNodeInProject(child);
            for (INode node : listChild) {
                boolean isContain = false;
                for (INode in : list) {
                    if (in instanceof StructureNode && node instanceof StructureNode) {
                        if (((StructureNode) in).getAST().equals(((StructureNode) node).getAST())) {
                            isContain = true;
                            break;
                        }
                    }
                }
                if (isContain == false) {
                    list.add(node);
                }
            }

        }
        return list;
    }

    // extract attribute and method, which parameter using
    private Map<String , Map<String, Set<String>>> getFeaturesOfFunction (FunctionNode functionNode) {
        CPPASTFunctionDefinition AST = (CPPASTFunctionDefinition)functionNode.getAST();
        List<Node> param = functionNode.getChildren();
        Set<String> variableRelativeToTemplate = new HashSet<>();
        Map<String , Map<String, Set<String>>> mapping = new HashMap<>();
        for (IASTNode iastNode: AST.getParent().getChildren()){
            if (iastNode instanceof  CPPASTSimpleTypeTemplateParameter) {
                Map<String, Set<String>> element = new HashMap<>();
                element.put("attribute",new HashSet<>());
                element.put("operator",new HashSet<>());
                element.put("variable", new HashSet<>());
                mapping.put(((CPPASTSimpleTypeTemplateParameter) iastNode).getName().getRawSignature(), element);
            }}
        for (Node node: param){
            if(node instanceof VariableNode) {
                String type = ((VariableNode) node).getCoreType();
                if (mapping.containsKey(type) ){
                    Set<String> set = mapping.get(type).get("variable");
                    set.add(node.getName());
                }
            }
        }

        ASTVisitor astVisitor = new ASTVisitor() {
            @Override
            public int visit(IASTExpression expression) {
                if(expression instanceof  CPPASTFunctionCallExpression) {
                    System.out.println(expression.getRawSignature());
                }
                if(expression instanceof CPPASTBinaryExpression) {
                    String statement = expression.getRawSignature().replaceAll("\\s+", " ");
                    statement = statement.contains(" .")?statement.replace(" .", "."):statement;
                    System.out.println("Convert to quy chuan:"+statement);
                    System.out.println();

                    IASTNode[] children = expression.getChildren();
                    for (int i=0;i<children.length;i++) {
                        String nameVariable = children[i].getRawSignature();
                        int indexOfSquareBracket = nameVariable.indexOf("[");
                        if (indexOfSquareBracket!=-1) nameVariable = nameVariable.substring(0,indexOfSquareBracket);
                        for (Map.Entry<String, Map<String, Set<String>>> entry: mapping.entrySet()) {
                            if (entry.getValue().get("variable").contains(nameVariable)) {
                                entry.getValue().get("operator").add(convertToOperatorZIcon(((CPPASTBinaryExpression) expression).getOperator()));
                            }
                        }
                    }
                }
                return super.visit(expression);
            }
        };

        ASTVisitor astVisitor1 =new ASTVisitor() {
            @Override
            public int visit(IASTDeclaration declaration) {
                if(declaration instanceof CPPASTSimpleDeclaration) {
                    System.out.println("This is declar: "+declaration.getRawSignature());
                    String statement = declaration.getRawSignature().replaceAll("\\s+", " ");
                    statement = statement.contains(" .")?statement.replace(" .", "."):statement;
                    System.out.println("Convert to quy chuan:"+statement);
                    System.out.println();
                    String type = String.valueOf(((CPPASTSimpleDeclaration) declaration).getDeclSpecifier().getRawSignature());

                    if(mapping.containsKey(type)) {
                        IASTNode declarator = declaration.getChildren()[1];
                        if (declarator instanceof CPPASTDeclarator) {
                            String express1 = String.valueOf(declarator.getChildren()[0].getRawSignature());
//                            String express2 = declarator.getChildren()[1].getRawSignature();
                            variableRelativeToTemplate.add(express1);
                            mapping.get(type).get("variable").add(express1);
                        }
                    }
                }
                return super.visit(declaration);
            }
        };
        ASTVisitor astVisitor2 = new ASTVisitor() {
            @Override
            public int visit(IASTName name) {
                if(name.getParent() instanceof  CPPASTFieldReference) {
                    String fOwner = ((CPPASTFieldReference) name.getParent()).getFieldOwner().getRawSignature();
                    int indexOfSquareBracket = fOwner.indexOf("[");
                    if (indexOfSquareBracket!=-1) {
                        fOwner = fOwner.substring(0,indexOfSquareBracket);
                    }
                    String fName = ((CPPASTFieldReference) name.getParent()).getFieldName().getRawSignature();
                    for (Map.Entry<String, Map<String, Set<String>>> entry: mapping.entrySet()) {
                        if (entry.getValue().get("variable").contains(fOwner)) {
                            entry.getValue().get("attribute").add(fName);
                        }
                    }
                }
                return super.visit(name);
            }
        };
        astVisitor.shouldVisitExpressions = true;
        astVisitor1.shouldVisitDeclarations = true;
        astVisitor2.shouldVisitNames = true;
        AST.accept(astVisitor1);
        AST.accept(astVisitor);
        AST.accept(astVisitor2);


//        try {
//            ICFG cfg = CFGUtils.createCFG(functionNode, EnviroCoverageTypeNode.STATEMENT);
//            cfg.setIdforAllNodes();
//
//            List<ICfgNode> list = cfg.getAllNodes();
//            for (ICfgNode node: list){
//                String statement = String.format("%s",node.getContent());
//                statement = statement.replaceAll("\\s+", " ");
//                statement = statement.contains(" .")?statement.replace(" .", "."):statement;
//                System.out.print("This is statement exported from csv report file: ");
//                System.out.print(statement+"\n");
//
//                String REGEX = "(((\\w+\\[\\w+\\])|(\\w+))\\.\\w+)";
//
//
//
//                Pattern pattern = Pattern.compile(REGEX);
//                Matcher matcher = pattern.matcher(statement);
//                while(matcher.find()) {
//                    String substring = statement.substring(matcher.start(),matcher.end());
//                    System.out.println(substring);
//                    String frontDot = substring.split("\\.")[0];
//                    System.out.println(frontDot);
//                    for (Map.Entry<String, Map<String, Set<String>>> entry: mapping.entrySet()) {
//                        int indexOfSquareBracket = frontDot.indexOf("[");
//                        if (indexOfSquareBracket!=-1){
//                            frontDot = frontDot.substring(0, indexOfSquareBracket);
//                            System.out.print("abc"+statement+"\n");
//                        }
//
//                        if(entry.getValue().get("variable").contains(frontDot)) {
//                            entry.getValue().get("attribute").add(substring.split("\\.")[1]);
//                        }
//                    }
//                }
//            }
////            System.out.println("This is feture final");
////            System.out.println(Arrays.asList(listFeature));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        logger.debug("Mapping features:"+Arrays.asList(mapping));
        return mapping;
    }



    public IValueDataNode generateData(INode fromNode) {
        IValueDataNode toDataNode = null;
        if (fromNode instanceof ClassNode) {
            toDataNode = new ClassDataNode();
            toDataNode.setRealType(fromNode.getName());
            toDataNode.setRawType(fromNode.getNewType());
        }
        else if (fromNode instanceof StructNode) {
            toDataNode = new StructDataNode();
            toDataNode.setRealType(fromNode.getName());
            toDataNode.setRawType(fromNode.getNewType());
        }
//        else if (fromNode instanceof ConstructorNode) {
//            toDataNode = new SubClassDataNode();
////            ConstructorDataNode constructorDataNode =
//        }
        else if (fromNode instanceof AttributeOfStructureVariableNode) {
            AttributeOfStructureVariableNode attribute = (AttributeOfStructureVariableNode) fromNode;
            String realType = attribute.getRealType();

            if (VariableTypeUtils.isNumBasic(realType)) {
                toDataNode = new NormalNumberDataNode();
            }
            else if (VariableTypeUtils.isChBasic(realType)) {
                toDataNode = new NormalCharacterDataNode();
            }

            if (toDataNode != null) {
                toDataNode.setCorrespondingVar(attribute);
                toDataNode.setRawType(attribute.getRawType());
                toDataNode.setRealType(attribute.getRealType());
                toDataNode.setName(fromNode.getName());
                toDataNode.setVituralName(fromNode.getName());
            }
        }
        if (toDataNode == null) return null;
//        for (INode child : fromNode.getChildren()) {
//            IValueDataNode childDataNode = generateData(child);
//            if (childDataNode != null) {
//                childDataNode.setParent(toDataNode);
//                toDataNode.addChild(childDataNode);
//            }
//        }

//        VariableNode variableNode = (VariableNode) fromNode.clone();
//        variableNode.setParent(fromNode);
//        toDataNode.setCorrespondingVar(variableNode);
        return toDataNode;
    }

    public static void refactorRealTypeTemplate(TestPrototype testPrototype) {
        SubprogramNode sut = Search2.findSubprogramUnderTest(testPrototype.getRootDataNode());
        if (sut == null) return;
        TemplateSubprogramDataNode templateSubprogramDataNode = (TemplateSubprogramDataNode) sut;
        GlobalRootDataNode globalRootDataNode = Search2.findGlobalRoot(testPrototype.getRootDataNode());
        Map<String, String> typeMapping = templateSubprogramDataNode.getRealTypeMapping();
        for (IDataNode dataNode : globalRootDataNode.getChildren()) {
            try {
                if (dataNode instanceof StructureDataNode
                        && ((StructureDataNode) dataNode).getCorrespondingVar().getCorrespondingNode() instanceof StructOrClassNode
                        &&((StructOrClassNode) ((StructureDataNode) dataNode).getCorrespondingVar().getCorrespondingNode()).isTemplate()) {
                    VariableNode correspondingVar = ((StructureDataNode) dataNode).getCorrespondingVar();
                    String name = (correspondingVar == null) ? ((StructureDataNode) dataNode).getRawType() : correspondingVar.getRealType();
                    int index = name.indexOf("<");
                    if (index >= 0) name = name.substring(0, index);
                    StringBuilder combination = new StringBuilder(name).append("<");
                    for (String key : typeMapping.keySet()) {
                        String value = (typeMapping.get(key) == null) ? "" : typeMapping.get(key);
                        combination.append(value + ",");
                    }
                    combination.append(">");
                    String combination_pair = String.valueOf(combination).replaceAll(",>", ">");
                    ((StructureDataNode) dataNode).setRealType(combination_pair);
                    ((StructureDataNode) dataNode).setRawType(combination_pair);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static String createPrototypeWithSpecificParameter(String parameterReplacement, ICommonFunctionNode functionNode) {
        List<IVariableNode> parameters = functionNode.getArguments();
        String prototype = "";
        List<String> listTemplateParams = new ArrayList<>();
        if (((FunctionNode)functionNode).getRealParent() instanceof StructOrClassNode && ((StructOrClassNode) ((FunctionNode)functionNode).getRealParent()).isTemplate()) {
            listTemplateParams = Arrays.asList(TemplateUtils.getTemplateParameters(((FunctionNode) functionNode).getRealParent()));
        }
        else listTemplateParams = Arrays.asList(TemplateUtils.getTemplateParameters(functionNode));
        for (int i = 0; i < parameters.size(); i++) {
            String paramName = parameters.get(i).getName();
            String paramType = parameters.get(i).getRealType();
            String type = paramType.replaceAll("\\*", "").replaceAll("&", "");
            paramType = (listTemplateParams.contains(type)) ? paramType.replaceAll(type, parameterReplacement) : paramType;
            paramType = VariableTypeUtils.deleteStorageClasses(paramType);
            paramType = paramType.replaceAll(" \\*", "*")
                    .replaceAll(" \\[", "[");

            String parameterDeclaration;

            List<String> indexes = Utils.getIndexOfArray(deleteTemplateParameters(paramType));

//                                        List<String> indexes = Utils.getIndexOfArray(deleteTemplateParameters(parameters.get(i).getRawType()));

            if (indexes.size() > 0) {
                int idx = paramType.length() - 1;
                while (paramType.charAt(idx) == SpecialCharacter.CLOSE_SQUARE_BRACE
                        || paramType.charAt(idx) == SpecialCharacter.OPEN_SQUARE_BRACE
                        || Character.isDigit(paramType.charAt(idx)))
                    idx--;
                parameterDeclaration = paramType.substring(0, idx + 1) + " " + paramName;
                for (String index : indexes)
                    parameterDeclaration += "[" + index + "]";

            } else {
                parameterDeclaration = paramType + " " + paramName;
            }


            prototype += parameterDeclaration + ", ";
        }
        String returnType = functionNode.getReturnType();
        if (returnType != null) {

            returnType = VariableTypeUtils.deleteSizeFromArray(returnType);
            returnType = returnType.replaceAll(" \\*", "*").replaceAll(" \\[]", "*").replaceAll("\\[]", "*");
            if (!isStructureType(returnType) && listTemplateParams.contains(returnType)) {
                returnType = parameterReplacement;
            }

            prototype = String.format("%s %s(%s)", returnType, functionNode.getSimpleName(), prototype);
            prototype = prototype.replace(", )", ")");
        } else prototype = null;

        return prototype;
    }

    public String genPrefixName() {
        nameID++;
        return "AUTO_" + name + String.valueOf(nameID);
    }

    public static boolean isStructureType(String type) {
        List<INode> structureVsClassNodes = new ArrayList<>(Search.searchNodes(Environment.getInstance().getProjectNode(), new StructNodeCondition()));
        structureVsClassNodes.addAll(Search.searchNodes(Environment.getInstance().getProjectNode(), new ClassvsStructvsNamespaceCondition()));

        for (int i = 0; i < structureVsClassNodes.size(); i++) {
            if (structureVsClassNodes.get(i).getName().equals(type)) {
                return true;
            }
        }
        return false;
    }
    public String convertToOperatorZIcon(int operatorNumber) {
        switch (operatorNumber) {
            case 1:
                return "*";
            case 2:
                return "/";
            case 3:
                return "%";
            case 4:
                return "+";
            case 5:
                return "-";
            case 6:
                return "<<";
            case 7:
                return ">>";
            case 8:
                return "<";
            case 9:
                return ">";
            case 10:
                return ">=";
            case 11:
                return "<=";
            case 12:
                return "&";
            case 13:
                return "^";
            case 14:
                return "|";
            case 15:
                return "&&";
            case 16:
                return "||";
            default:
                return String.valueOf(operatorNumber);
        }
    }

    public ICommonFunctionNode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public String getName() {
        return name;
    }

    public List<TestPrototype> getPrimitiveDefaultPrototype() {
        return primitiveDefaultPrototype;
    }

    private void setPrimitiveDefaultPrototype(List<TestPrototype> primitiveDefaultPrototype) {
        this.primitiveDefaultPrototype = primitiveDefaultPrototype;
    }

    public List<TestPrototype> getStructurePrototype() {
        return structurePrototype;
    }

    private void setStructurePrototype(List<TestPrototype> structurePrototype) {
        this.structurePrototype = structurePrototype;
    }

    public void setName(String name) {
        this.name = name;
    }


}
