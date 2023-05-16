package com.dse.testdata.object;

import com.dse.parser.object.*;
import com.dse.search.Search2;
import com.dse.testdata.gen.module.subtree.InitialArgTreeGen;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateSubprogramDataNode extends SubprogramNode {
    public static final String NAME_TEMPLATE_TYPE = "template_type";
    public static final String LIST_ARG_TEMPLATE = "listArgOfTemplate";
    public static final String REAL_FUNC = "realFunction";




    private DefinitionFunctionNode realFunctionNode;

    // store the template type to real type in template function, e.g, "T"->"int"
    // key: template type
    // value: real type
    private Map<String, String> realTypeMapping = new HashMap<>();
    private List<IValueDataNode> listArgOfTemplate = new ArrayList<>();

    public TemplateSubprogramDataNode() {}

    public TemplateSubprogramDataNode(IFunctionNode fn) {
        super(fn);
    }

    public List<IValueDataNode> getListArgOfTemplate() {
        return listArgOfTemplate;
    }

    public void setListArgOfTemplate(List<IValueDataNode> listArgOfTemplate) {
        this.listArgOfTemplate = listArgOfTemplate;
    }

    public void setDefinition(String prototype) throws Exception {
        setRealFunction(prototype);
        if (realFunctionNode != null) {
            setDefinition(realFunctionNode);
            initInputToExpectedOutputMap(realFunctionNode);
        }
    }

    public void setRealFunction(String definition) {
        IASTNode ast = Utils.convertToIAST(definition);
        if (ast instanceof IASTDeclarationStatement)
            ast = ((IASTDeclarationStatement) ast).getDeclaration();

        if (ast instanceof IASTSimpleDeclaration) {
            DefinitionFunctionNode suggestion = new DefinitionFunctionNode();
            suggestion.setAbsolutePath(functionNode.getAbsolutePath());
            suggestion.setAST((CPPASTSimpleDeclaration) ast);
            suggestion.setName(suggestion.getNewType());
//            setDefinition(suggestion);
            setRealFunctionNode(suggestion);
//            initInputToExpectedOutputMap(suggestion);
        }
    }

    public void setDefinition(DefinitionFunctionNode definition) throws Exception {
        for (IVariableNode node : definition.getArguments())
            new InitialArgTreeGen().genInitialTree((VariableNode) node, this);

        VariableNode returnVar = Search2.getReturnVarNode(definition);

        if (returnVar != null) {
            new InitialArgTreeGen().genInitialTree(returnVar, this);
            setRealType(returnVar.getRealType());
        }

        setRawType(definition.getReturnType());
    }

    public void initInputToExpectedOutputMap(DefinitionFunctionNode definition) {
        inputToExpectedOutputMap.clear();
        try {
            ICommonFunctionNode castFunctionNode = (ICommonFunctionNode) functionNode;
            RootDataNode root = new RootDataNode();
            root.setFunctionNode(castFunctionNode);
            InitialArgTreeGen dataTreeGen = new InitialArgTreeGen();
            dataTreeGen.generateCompleteTree(root, null);
            for (IDataNode node : root.getChildren()) {
                if (node instanceof SubprogramNode && ((SubprogramNode) node).getFunctionNode() == functionNode) {

                    // generate datanode to use as expected output
                    for (IVariableNode n : definition.getArguments())
                        new InitialArgTreeGen().genInitialTree((VariableNode) n, (DataNode) node);

                    List<ValueDataNode> inputs = getChildren().stream()
                            .filter(n -> n instanceof ValueDataNode)
                            .map(n -> (ValueDataNode) n)
                            .collect(Collectors.toList());

                    List<ValueDataNode> expecteds = node.getChildren().stream()
                            .filter(n -> n instanceof ValueDataNode)
                            .map(n -> (ValueDataNode) n)
                            .collect(Collectors.toList());

                    for (ValueDataNode eo : expecteds) {
                        for (ValueDataNode input : inputs) {
                            if (input.getCorrespondingVar() == eo.getCorrespondingVar()) {
                                inputToExpectedOutputMap.put(input, eo);
                                eo.setParent(input.getParent());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DefinitionFunctionNode getRealFunctionNode() {
        return realFunctionNode;
    }

    public void setRealFunctionNode(DefinitionFunctionNode realFunctionNode) {
        this.realFunctionNode = realFunctionNode;
    }

    public Map<String, String> getRealTypeMapping() {
        return realTypeMapping;
    }

    public void setRealTypeMapping(Map<String, String> realTypeMapping) {
        this.realTypeMapping = realTypeMapping;
    }

    public void updateRealTypeMapping() {
        for (IValueDataNode valueDataNode : listArgOfTemplate) {
            if (valueDataNode instanceof NormalDataNode) {
                for (String key : realTypeMapping.keySet()) {
                    if (valueDataNode.getName().equals(key)) {
                        realTypeMapping.put(key, ((NormalDataNode) valueDataNode).getValue());
                    }
                }
            }
        }
    }
}
