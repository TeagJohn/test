package com.dse.winams.converter;

import com.dse.winams.ITreeNode;
import com.dse.winams.TreeNode;
import com.dse.winams.UI.object.EntryListItem;

import java.util.List;

public interface IConverter {
    void setTreeNode(ITreeNode treeNode);
    boolean isImportedToInput();
    /**
     * Convert the title in a {@link TreeNode} to its aliases based on the mapping rules in WinAMS.
     */
    void convert();

    /**
     * Convert the title in a {@link TreeNode} to its aliases based on the mapping rules in WinAMS.
     * @return the list of {@link EntryListItem}
     */
    List<EntryListItem> generateEntry();
}
