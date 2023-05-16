package com.dse.winams.UI.object;

import com.dse.winams.IOutputEntry;
import com.dse.winams.ITreeNode;

public class OutputEntryListItem extends EntryListItem implements IOutputEntry {
    public OutputEntryListItem(ITreeNode treeNode, String alias, String additionalChainName) {
        super(treeNode, alias, additionalChainName);
    }
    public OutputEntryListItem(ITreeNode treeNode) {
        super(treeNode, treeNode.getTitle(), null);
    }
}
