package com.dse.winams.UI.object;

import com.dse.winams.*;
import javafx.scene.control.ListView;

import java.util.Arrays;

public abstract class EntryListItem implements IEntry {
    private String title;
    private String alias;
    private String name;
    private Type type;
    private String[] chainNames = null;

    public EntryListItem(ITreeNode treeNode, String alias, String additionalChainNames) {
        this.alias = alias;
        title = treeNode.getTitle();
        type = treeNode.getType();
        if (treeNode instanceof IAliasNode) {
            chainNames = ((IAliasNode) treeNode).getChainNames();
            // a pointer index will be added to the end of the chain
            if (additionalChainNames != null) {
                chainNames = Arrays.copyOf(chainNames, chainNames.length + 1);
                chainNames[chainNames.length - 1] = additionalChainNames;
            }
        }
        if (treeNode instanceof IVariableTreeNode) {
            name = ((IVariableTreeNode) treeNode).getVariable().getName();
        } else if (treeNode instanceof IFunctionTreeNode) {
            name = ((IFunctionTreeNode) treeNode).getFunction().getName();
        } else {
            name = title;
        }
    }

    @Override
    public String getTitle() {
        return title;
    }
    public String getAlias() {
        return alias;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    public String toString() {
        return getAlias();
    }

    public String[] getChainNames() {
        return chainNames;
    }
}
