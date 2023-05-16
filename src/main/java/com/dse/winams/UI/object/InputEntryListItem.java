package com.dse.winams.UI.object;

import com.dse.winams.IInputEntry;
import com.dse.winams.ITreeNode;

public class InputEntryListItem extends EntryListItem implements IInputEntry {
    public InputEntryListItem(ITreeNode treeNode, String alias, String additionalChainName) {
        super(treeNode, alias, additionalChainName);
    }
    public InputEntryListItem(ITreeNode treeNode) {
        super(treeNode, treeNode.getTitle(), null);
    }
}
