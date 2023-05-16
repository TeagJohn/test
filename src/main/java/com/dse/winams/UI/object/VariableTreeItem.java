package com.dse.winams.UI.object;

import com.dse.guifx_v3.helps.Factory;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.IVariableNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.PointerDataNode;
import com.dse.winams.*;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class VariableTreeItem extends TreeItem<String> {
    private final ITreeNode node;

    public VariableTreeItem(ITreeNode node) {
        this.node = node;
        setValue(node.getTitle());
        addGraphic();
    }

    @Override
    public String toString() {
        if (node instanceof IAliasNode) {
            return ((IAliasNode) node).getAlias();
        } else {
            return node.getTitle();
        }
    }

    public ITreeNode getTreeNode() {
        return node;
    }

    private void addGraphic() {
        String iconPath = "";
        if (node instanceof LabelTreeNode)
            iconPath = "/icons/directory.png";
        else if (node instanceof IFunctionTreeNode) {
            iconPath = "/icons/subprogram.png";
        } else if (node instanceof IVariableTreeNode) {
            IDataNode dataNode = ((TreeNode) node).getDataNode();
            if (dataNode instanceof PointerDataNode)
                iconPath = "/icons/gdb_point_var_16.png";
            else
                iconPath = "/icons/gdb_var_16.png";
        } else {
            iconPath = "/icons/gdb_var_16.png";
        }

        setGraphic(new ImageView(new Image(Objects.requireNonNull(Object.class.getResourceAsStream(iconPath)))));
    }
}
