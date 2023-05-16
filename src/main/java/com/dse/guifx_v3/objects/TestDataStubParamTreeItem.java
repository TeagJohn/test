package com.dse.guifx_v3.objects;

import com.dse.testdata.object.IterationSubprogramNode;
import com.dse.testdata.object.ValueDataNode;

public class TestDataStubParamTreeItem extends TestDataGlobalVariableTreeItem {

    public TestDataStubParamTreeItem(ValueDataNode dataNode) {
        super(dataNode);

        if (dataNode.getParent() instanceof IterationSubprogramNode) {
            IterationSubprogramNode parent = (IterationSubprogramNode) dataNode.getParent();
            this.inputDataNode = dataNode;
            this.expectedOutputDataNode = parent.getInputToExpectedOutputMap().get(dataNode);
            if (expectedOutputDataNode == null) {
                logger.debug("Failed on get ExpectedOutputDataNode. Size of map: "
                    + parent.getInputToExpectedOutputMap().size());
            }
        }
    }
}
