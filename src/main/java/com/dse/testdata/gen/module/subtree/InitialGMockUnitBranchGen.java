package com.dse.testdata.gen.module.subtree;

import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.dependency.GlobalVariableDependency;
import com.dse.parser.funcdetail.IFunctionDetailTree;
import com.dse.parser.object.ClassNode;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.NamespaceNode;
import com.dse.testdata.comparable.gmock.GmockUtils;
import com.dse.testdata.object.Gmock.GmockObjectNode;
import com.dse.testdata.object.Gmock.GmockUnitNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.testdata.object.UnitNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class InitialGMockUnitBranchGen extends AbstractInitialTreeGen {
    @Override
    public void generateCompleteTree(RootDataNode root, IFunctionDetailTree functionTree) throws Exception {

    }

    public IDataNode generate(IDataNode root, ICommonFunctionNode functionNode) {
        Set<ClassNode> listClassNode = new HashSet<>();
        for (Dependency dependency : functionNode.getDependencies()) {
            if (dependency instanceof FunctionCallDependency) {
                listClassNode.add((ClassNode) dependency.getEndArrow().getParent());
            } else if (dependency instanceof GlobalVariableDependency && dependency.getEndArrow() instanceof IVariableNode) {
                INode correspondingNode = ((IVariableNode) dependency.getEndArrow()).getCorrespondingNode();
                if (correspondingNode instanceof ClassNode) {
                    listClassNode.add((ClassNode) correspondingNode);
                }
            }
        }
        ArrayList<ClassNode> classNodes = new ArrayList<>(listClassNode);

        ArrayList<INode> listObj = GmockUtils.findMockObjects((FunctionNode) functionNode);

//        Set<ClassNode> classNodeSet = new HashSet<>();
//        for (INode node : listObj) {
//            if (!node.getChildren().isEmpty()) {
//                classNodeSet.add((ClassNode) ((SubprogramNode) node.getChildren().get(0)).getFunctionNode().getParent());
//            }
//        }
//        ArrayList<ClassNode> classNodes = new ArrayList<>(classNodeSet);

        for (ClassNode classNode : classNodes) {
            if (classNode.getParent() instanceof ISourcecodeFileNode || classNode.getParent() instanceof NamespaceNode) {
                // Todo: namespace
                INode node = classNode.getParent();
                if (classNode.getParent() instanceof NamespaceNode) {

                    while (!(node instanceof ISourcecodeFileNode)) {
                        node = node.getParent();
                    }
                }
                UnitNode unitNode = new GmockUnitNode(node);

                for (INode obj : listObj) {
                    if (obj instanceof IVariableNode && ((IVariableNode) obj).getCoreType().equals(classNode.getName())) {
                        GmockObjectNode mockObjNode = new GmockObjectNode(unitNode.getSourceNode(), (IVariableNode) obj);
                        if (mockObjNode.getTypeObj() != GmockUtils.GLOBAL) {
                            mockObjNode.setChild(classNode, functionNode);
                            unitNode.addChild(mockObjNode);
                            mockObjNode.setParent(unitNode);
                        }
                    }

                    if (obj instanceof ICommonFunctionNode) {
                        GmockObjectNode mockObjNode = new GmockObjectNode(unitNode.getSourceNode(), (ICommonFunctionNode) obj);
                        mockObjNode.setChild(classNode, (ICommonFunctionNode) obj);
                        unitNode.addChild(mockObjNode);
                        mockObjNode.setParent(unitNode);
                    }

                }

                if (!unitNode.getChildren().isEmpty()) {
                    root.addChild(unitNode);
                    unitNode.setParent(root);
                }
            }
        }
        return root;
    }
}
