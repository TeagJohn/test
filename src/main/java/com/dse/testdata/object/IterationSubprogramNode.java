package com.dse.testdata.object;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.VariableNode;
import com.dse.search.Search2;
import com.dse.testdata.gen.module.subtree.InitialArgTreeGen;
import com.dse.util.VariableTypeUtils;

import java.util.List;

public class IterationSubprogramNode extends SubprogramNode {
    int index = 0;

    public IterationSubprogramNode() {}

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean isStubable() {
        return true;
    }

    public boolean putParamInput(ValueDataNode input) {
        if (input.getName().equals("RETURN"))
            return false;

        List<ValueDataNode> outputs = Search2.searchParameterNodes(this);
        ValueDataNode expectedOutput = outputs.stream()
            .filter(child -> child.getCorrespondingVar() == input.getCorrespondingVar())
            .findFirst()
            .orElse(null);

        inputToExpectedOutputMap.remove(input);
        inputToExpectedOutputMap.put(input, expectedOutput);
        return true;
    }


    @Override
    public void initInputToExpectedOutputMap() {
        inputToExpectedOutputMap.clear();

        /*
          create initial tree
         */
        try {
            ICommonFunctionNode castFunctionNode = (ICommonFunctionNode) functionNode;
            RootDataNode root = new RootDataNode();
            root.setFunctionNode(castFunctionNode);
            InitialArgTreeGen dataTreeGen = new InitialArgTreeGen();
            List<ValueDataNode> expectedNodes = Search2.searchParameterNodes(this);
            for (IVariableNode child : castFunctionNode.getArguments()) {
                if (VariableTypeUtils.isReference(child.getRealType())
                    || VariableTypeUtils.isPointer(child.getRealType())) {
                    ValueDataNode input = dataTreeGen.genInitialTree((VariableNode) child, root);
                    for (ValueDataNode expected : expectedNodes) {
                        if (input.getCorrespondingVar() == expected.getCorrespondingVar()) {
                            inputToExpectedOutputMap.put(input, expected);
                            input.setParent(expected.getParent());
                            input.setExternel(false);
                            expectedNodes.remove(expected);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
