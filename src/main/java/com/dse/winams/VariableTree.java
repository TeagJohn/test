package com.dse.winams;

import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.funcdetail.FunctionDetailTree;
import com.dse.parser.object.*;
import com.dse.search.Search2;
import com.dse.stub_manager.StubManager;
import com.dse.testdata.DataTree;
import com.dse.testdata.gen.module.subtree.InitialStubTreeGen;
import com.dse.testdata.object.*;
import com.dse.util.NodeType;
import com.dse.winams.IEntry.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VariableTree extends TreeNode {

    private final IFunctionNode fn;

    public VariableTree(IFunctionNode fn) {
        this.fn = fn;
        setTitle(fn.getSingleSimpleName());
        generateTree();
    }

    private void generateTree() {
        FunctionDetailTree functionDetailTree = new FunctionDetailTree(fn);
        DataTree dataTree = new DataTree(functionDetailTree);
        RootDataNode dataRoot = dataTree.getRoot();

        for (Type type : Type.values()) {
            LabelTreeNode child = new LabelTreeNode(type);
            getChildren().add(child);

            // TODO
            String titlePrefix = "";

            switch (type) {
                case GLOBAL:
                    GlobalRootDataNode globalDataRoot = Search2.findGlobalRoot(dataRoot);
                    List<IDataNode> usedDataNodes = globalDataRoot.getChildren().stream()
                        .filter(n -> n instanceof ValueDataNode)
                        .map(n -> (ValueDataNode) n)
                        .filter(n -> globalDataRoot.isRelatedVariable(n.getCorrespondingVar()))
                        .collect(Collectors.toList());
                    addChildren(child, titlePrefix, usedDataNodes, type);
                    break;

                case PARAMETER:
                    List<IDataNode> parameterDataNodes = Search2.findArgumentNodes(dataRoot);
                    titlePrefix = fn.getSimpleName() + "@";
                    addChildren(child, titlePrefix, parameterDataNodes, type);
                    break;

                case RETURN:
                    ValueDataNode returnDataNode = Search2.findReturnNode(dataRoot);
                    titlePrefix = fn.getSingleSimpleName() + "@@";
                    addChildren(child, titlePrefix, Collections.singletonList(returnDataNode), type);
                    break;

                case STATIC:
                    RootDataNode staticDataRoot = Search2.findStaticRoot(dataRoot);
                    addChildren(child, titlePrefix, staticDataRoot.getChildren(), type);
                    break;

                case FUNCTION_CALL:
                    List<INode> calledFunctions = new ArrayList<>();
                    fn.getDependencies().forEach(d -> {
                        if (d instanceof FunctionCallDependency) {
                            if (d.getStartArrow().equals(fn)) {
                                calledFunctions.add(d.getEndArrow());
                            }
                        }
                    });

                    List<SubprogramNode> stubDataNodes = Search2.searchStubableSubprograms(dataRoot);
                    stubDataNodes.removeIf(n -> !calledFunctions.contains(n.getFunctionNode()));
                    stubDataNodes.forEach(n -> {
                        try {
                            new InitialStubTreeGen().addSubprogram(n);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    addChildren(child, titlePrefix, stubDataNodes, type);
                    break;
            }
        }

    }

    private void addChildren(ITreeNode parent, String prefix, Collection<? extends IDataNode> dataNodes, IEntry.Type type) {
        for (IDataNode child : dataNodes) {
            IAliasNode node = null;
            String title = null;
            String newPrefix = prefix;

            if (child instanceof SubprogramNode) {
                SubprogramNode subprogramNode = (SubprogramNode) child;
                if (subprogramNode.getFunctionNode() instanceof ICommonFunctionNode) {
                    ICommonFunctionNode functionNode = (ICommonFunctionNode) subprogramNode.getFunctionNode();
                    node = new FunctionTreeNode(functionNode, child);
                    // TODO
                    String stubFileName = StubManager.getStubCodeFileName((AbstractFunctionNode) functionNode);
                    newPrefix = "AMSTB_" + stubFileName + "/AMSTB_" + functionNode.getSingleSimpleName() + "@";
                    title = "AMSTB_" + functionNode.getSingleSimpleName();
                }
            } else if (child instanceof ValueDataNode) {
                ValueDataNode valueDataNode = (ValueDataNode) child;
                IVariableNode variableNode = valueDataNode.getCorrespondingVar();
                node = new VariableTreeNode(variableNode, child);
                node.setType(type);
                // in a return node, the title will be {function_name}@@, which have been set in the prefix
                if (type == Type.RETURN) {
                    if (child.getName().equals("RETURN")) {
                        title = prefix;
                        newPrefix = title;
                    } else {
                        title = prefix + valueDataNode.getVituralName().replace("AKA_EXPECTED_OUTPUT.", "");
                    }
                }
                else
                    title = prefix + valueDataNode.getVituralName();
            }

            if (node != null) {
                String[] chainNames = retrieveChainNames(child);
                node.setChainNames(chainNames);
                node.setTitle(title);
                parent.getChildren().add(node);
                if (child instanceof ArrayDataNode) {
                    // NOTE: according to winams rule, child of array node is not added
                } else
                    addChildren(node, newPrefix, child.getChildren(), type);
            }
        }
    }

    private static String[] retrieveChainNames(IDataNode node) {
        if (node instanceof RootDataNode && ((RootDataNode) node).getLevel() == NodeType.ROOT)
            return new String[] {node.getName()};

        String[] parentChainNames = retrieveChainNames(node.getParent());
        int parentChainLength = parentChainNames.length;
        String[] chainNames = new String[parentChainLength + 1];
        System.arraycopy(parentChainNames, 0, chainNames, 0, parentChainLength);
        chainNames[parentChainLength] = node.getName();
        return chainNames;
    }

    public IFunctionNode getFunction() {
        return fn;
    }
}