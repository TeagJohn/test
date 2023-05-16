package com.dse.testdata.object.Gmock;

import com.dse.parser.object.*;
import com.dse.search.Search2;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.comparable.gmock.GmockUtils;
import com.dse.testdata.object.*;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;

import java.util.Arrays;
import java.util.List;

public class GmockObjectNode extends GmockUnitNode {
    private String nameObj;
    private int typeObj;

    private String nameClass;

    public GmockObjectNode() {

    }

    public GmockObjectNode(INode sourceNode, IVariableNode mockObj) {
        super(sourceNode);
        this.name = mockObj.getName();
        if (mockObj instanceof InternalVariableNode) {
            this.typeObj = GmockUtils.ARG;
        } else if (mockObj instanceof ExternalVariableNode) {
            this.typeObj = GmockUtils.GLOBAL;
        } else {
            this.typeObj = GmockUtils.ATTRIBUTE;
        }
        this.nameObj = mockObj.getName();
        if (mockObj.getRawType().contains("*")) {
            this.nameObj = "*" + this.nameObj;
        }
        this.nameClass = mockObj.getCoreType();
    }

    public GmockObjectNode(INode sourceNode, ICommonFunctionNode functionNode) {
        super(sourceNode);
        this.typeObj = GmockUtils.SINGLETON;
        this.name = functionNode.getReturnType().replace("*", "") + "::" + functionNode.getName();
        this.nameObj = "*Mock" + this.name;
        this.nameClass = functionNode.getReturnType().replace("*", "");
    }


    public GmockObjectNode(INode sourceNode, String nameObj, int typeObj) {
        super(sourceNode);
        this.name = nameObj;
        this.typeObj = typeObj;
    }

    public void setChild(ClassNode classNode, ICommonFunctionNode functionNode) {
        List<Node> functions = classNode.getChildren();

        for (Node child : functions) {
            if (child instanceof FunctionNode || child instanceof DefinitionFunctionNode) {
                ICommonFunctionNode function = (ICommonFunctionNode) child;
                SubprogramNode subprogramNode = new SubprogramNode(function);

                if (functionNode.isTemplate())
                    subprogramNode = new TemplateSubprogramDataNode((IFunctionNode) function);

                this.addChild(subprogramNode);
                subprogramNode.setParent(this);
            }
        }
    }

    public boolean isMocked() {
        List<IDataNode> listTimes = Search2.searchNodes(this, TimesNode.class);
        for (IDataNode time : listTimes) {
            if (((TimesNode) time).getValue() != null) {
                return true;
            }
        }
        return false;
    }

    public void setNameObj(TestCase testCase) {
        List<ConstructorDataNode> listConstructor = Search2.searchNodes(testCase.getRootDataNode(), ConstructorDataNode.class);
        for (ConstructorDataNode cons : listConstructor) {
            if (cons.getFunctionNode() instanceof ConstructorNode) {
                if (((ConstructorNode) cons.getFunctionNode()).getAST().getBody() != null) {
                    List<IASTNode> listStatement = Arrays.asList(((ConstructorNode) cons.getFunctionNode()).getAST().getBody().getChildren());
                    for (IASTNode statement : listStatement) {
                        if (statement instanceof CPPASTExpressionStatement) {
                            CPPASTBinaryExpression expression = (CPPASTBinaryExpression) ((CPPASTExpressionStatement) statement).getExpression();
                            if (expression.getOperand1().toString().equals(this.name)) {
                                if (nameObj.startsWith("*")) {
                                    nameObj = "*" + expression.getOperand2().toString();
                                } else {
                                    nameObj = expression.getOperand2().toString();
                                }
                            }
                        }
                    }
                }
            }
        }

        if (nameObj.startsWith("*")) {
            List<PointerStructureDataNode> list = Search2.searchNodes(testCase.getRootDataNode(), PointerStructureDataNode.class);
            for (PointerStructureDataNode node : list) {
                if (node.getName().equals(this.nameObj.replace("*", ""))) {
                    this.setNameObj("*" + node.getVituralName());
                }
            }
        } else {
            List<ClassDataNode> list = Search2.searchNodes(testCase.getRootDataNode(), ClassDataNode.class);
            for (ClassDataNode node : list) {
                if (node.getName().equals(this.nameObj)) {
                    this.setNameObj(node.getVituralName());
                }
            }
        }



    }

    public String getNameObj() {
        return nameObj;
    }

    public int getTypeObj() {
        return typeObj;
    }

    public void setNameObj(String nameObj) {
        this.nameObj = nameObj;
    }

    public void setTypeObj(int typeObj) {
        this.typeObj = typeObj;
    }

    public String getNameClass() {
        return nameClass;
    }

    public void setNameClass (String nameClass) {
        this.nameClass = nameClass;
    }

}
