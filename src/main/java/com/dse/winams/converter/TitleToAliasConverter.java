package com.dse.winams.converter;

import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.ReturnVariableNode;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.object.*;
import com.dse.winams.*;
import com.dse.winams.UI.object.EntryListItem;
import com.dse.winams.UI.object.InputEntryListItem;
import com.dse.winams.UI.object.OutputEntryListItem;
import com.dse.winams.UI.object.PointerSetting;

import java.util.ArrayList;
import java.util.List;

public class TitleToAliasConverter implements IConverter {
    private ITreeNode treeNode;
    private boolean isImportedToInput;
    private PointerSetting pointerSetting = null;

    public final String POINTER_NC_PREFIX = "*";
    public final String POINTER_PREFIX = "$";
    public final String PARAMETER_PREFIX = "@";
    public final String CHAR_POINTER_PREFIX = "*";
    public final String STRING_OR_CHAR_ARRAY_PREFIX = "&";
    public final String NORMAL_ARRAY_PREFIX = "&";

    protected List<EntryListItem> entryList;

    public TitleToAliasConverter(ITreeNode treeNode, boolean isImportedToInput) {
        this.treeNode = treeNode;
        this.isImportedToInput = isImportedToInput;
        entryList = new ArrayList<>();
        pointerSetting = new PointerSetting(PointerSetting.Type.ADDRESS, -1, -1);
        convert();
    }

    public TitleToAliasConverter(ITreeNode treeNode, boolean isImportedToInput, PointerSetting pointerSetting) {
        this.treeNode = treeNode;
        this.isImportedToInput = isImportedToInput;
        this.pointerSetting = pointerSetting;
        entryList = new ArrayList<>();
        convert();
    }

    @Override
    public void setTreeNode(ITreeNode treeNode) {
        this.treeNode = treeNode;
    }

    @Override
    public boolean isImportedToInput() {
        return isImportedToInput;
    }

    @Override
    public void convert() {
        String result = "";
        if (treeNode instanceof IVariableTreeNode) {
            IVariableNode variable = ((IVariableTreeNode) treeNode).getVariable();
            IDataNode dataNode = ((TreeNode) treeNode).getDataNode();

            if (isImportedToInput)
                generateAliasForInput(dataNode, "", "", -1);
            else
                generateAliasForOutput(dataNode, "", "", -1);


        } else if (treeNode instanceof IFunctionTreeNode) {
            IDataNode dataNode = ((TreeNode) treeNode).getDataNode();
            IVariableNode variableNode = ((SubprogramNode) dataNode).getCorrespondingVar();

            // TODO: get return type of stub subprogram
            String alias = getNameFormat(dataNode);
            putNewEntry(alias);
        } else {
            // do nothing
        }
    }

    @Override
    public List<EntryListItem> generateEntry() {
        return entryList;
    }

    private void generateAliasForInput(IDataNode dataNode, String prefix, String suffix, int pointerIndex) {
        if (dataNode instanceof PointerDataNode) {
            generateAliasForPointer(dataNode, prefix, suffix, pointerIndex);
        } else if (dataNode instanceof NormalDataNode) {
            generateAliasForNormalData(dataNode, prefix, suffix, pointerIndex);
        } else if (dataNode instanceof StructureDataNode) {
            generateAliasForStructure(dataNode, prefix, suffix, pointerIndex);
        } else if (dataNode instanceof ArrayDataNode) {
            generateAliasForArray(dataNode, prefix, suffix, pointerIndex);
        }
    }

    private void generateAliasForOutput(IDataNode dataNode, String prefix, String suffix, int pointerIndex) {
        if (dataNode instanceof PointerDataNode) {
            generateAliasForPointer(dataNode, prefix, suffix, pointerIndex);
        } else if (dataNode instanceof NormalDataNode) {
            generateAliasForNormalData(dataNode, prefix, suffix, pointerIndex);
        } else if (dataNode instanceof StructureDataNode) {
            generateAliasForStructure(dataNode, prefix, suffix, pointerIndex);
        } else if (dataNode instanceof ArrayDataNode) {
            generateAliasForArray(dataNode, prefix, suffix, pointerIndex);
        }
    }

    private void generateAliasForPointer(IDataNode dataNode, String prefix, String suffix, int pointerIndex) {
        switch (pointerSetting.getType()) {
            case ADDRESS:
                String alias = prefix + getNameFormat(dataNode) + suffix;
                putNewEntry(alias);
                break;
            case ALLOCATE_MEMORY_NC:
                alias = prefix + POINTER_NC_PREFIX + getNameFormat(dataNode) + suffix;
                putNewEntry(alias);
                break;
            case ALLOCATE_MEMORY_PIW:
                if (dataNode instanceof PointerNumberDataNode || dataNode instanceof PointerCharacterDataNode) {
                    if (pointerIndex < 0) {
                        alias = prefix + POINTER_PREFIX + getNameFormat(dataNode) + suffix;
                        entryList.add(new InputEntryListItem(treeNode, alias, null));

                        for (int i = pointerSetting.getStartIndex(); i <= pointerSetting.getEndIndex(); i++) {
                            generateAliasForPointer(dataNode, prefix, suffix, i);
                        }
                    } else {
                        alias = prefix + getNameFormat(dataNode) + "[" + pointerIndex + "]" + suffix;
                        putNewEntry(alias, dataNode.getName() + "[" + pointerIndex + "]");
                    }
                } else if (dataNode instanceof PointerStructureDataNode) {
                    if (pointerIndex < 0) {
                        alias = prefix + POINTER_PREFIX + getNameFormat(dataNode) + suffix;
                        entryList.add(new InputEntryListItem(treeNode, alias, null));
                        ((PointerStructureDataNode) dataNode).setAllocatedSize(pointerSetting.getEndIndex() - pointerSetting.getStartIndex() + 1);
                        ((PointerStructureDataNode) dataNode).setAllocatedSize(pointerSetting.getEndIndex() - pointerSetting.getStartIndex() + 1);

                        try {
                            new TreeExpander().expandTree((ValueDataNode) dataNode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        for (int i = pointerSetting.getStartIndex(); i <= pointerSetting.getEndIndex(); i++) {
                            generateAliasForPointer(dataNode, prefix, suffix, i);
                        }
                    } else {
                        alias = prefix + getNameFormat(dataNode) + "[" + pointerIndex + "]";
                        //putNewEntry(alias);
                        if (isImportedToInput)
                            generateAliasForInput(dataNode.getChildren().get(pointerIndex - pointerSetting.getStartIndex()), alias, suffix, -1);
                        else {
                            generateAliasForOutput(dataNode.getChildren().get(pointerIndex - pointerSetting.getStartIndex()), alias, suffix, -1);
                        }
                    }
                } else {
                    alias = prefix +getNameFormat(dataNode) + suffix;
                    putNewEntry(alias);
                }
                break;
        }
    }

    private void generateAliasForNormalData(IDataNode dataNode, String prefix, String suffix, int pointerIndex) {
        String alias;
        if (dataNode instanceof NormalStringDataNode) {
            alias = prefix + STRING_OR_CHAR_ARRAY_PREFIX + getNameFormat(dataNode) + suffix;
        } else {
            alias = prefix + getNameFormat(dataNode) + suffix;
        }
        putNewEntry(alias);
    }

    private void generateAliasForStructure(IDataNode dataNode, String prefix, String suffix, int pointerIndex) {
        for (IDataNode child : dataNode.getChildren()) {
            if (isImportedToInput) {
                generateAliasForInput(child, prefix + ".", suffix, pointerIndex);
            } else {
                generateAliasForOutput(child, prefix + ".", suffix, pointerIndex);
            }
        }
    }

    private void generateAliasForArray(IDataNode dataNode, String prefix, String suffix, int pointerIndex) {
        // TODO: generate alias for pointer array or structure array
        if (dataNode instanceof OneDimensionDataNode) {
            if (dataNode instanceof OneDimensionStructureDataNode) {
                // TODO: generate alias for structure array
                String alias = prefix + NORMAL_ARRAY_PREFIX + getNameFormat(dataNode) + suffix;
                putNewEntry(alias);
            } else if (dataNode instanceof OneDimensionPointerDataNode) {
                // TODO: generate alias for pointer array
                String alias = prefix + NORMAL_ARRAY_PREFIX + getNameFormat(dataNode) + suffix;
                putNewEntry(alias);
            } else {
                String alias = prefix + NORMAL_ARRAY_PREFIX + getNameFormat(dataNode) + suffix;
                putNewEntry(alias);
            }
        } else if (dataNode instanceof MultipleDimensionDataNode) {
            // TODO: generate alias for multiple dimension array
            String alias = prefix + NORMAL_ARRAY_PREFIX + getNameFormat(dataNode) + suffix;
            putNewEntry(alias);
        }
    }

    private String getNameFormat(IDataNode dataNode) {
        if (treeNode instanceof IVariableTreeNode) {
            IVariableNode variable = ((IVariableTreeNode) treeNode).getVariable();
            // first check if the data node is a return variable
            if (dataNode instanceof ValueDataNode
                    && ((ValueDataNode) dataNode).getCorrespondingVar() instanceof ReturnVariableNode) {
                return treeNode.getTitle();
            }

            // check if the data node is the same as the variable, or it is the child of the variable
            if (dataNode.getVituralName().equals(variable.getName())
                    || dataNode.getVituralName().equals(treeNode.getTitle())
                    || dataNode.getName().equals(variable.getName())
                    || dataNode.getName().equals(treeNode.getTitle())) {
                if (treeNode.getType() == IEntry.Type.PARAMETER) {
                    String nodeName = dataNode.getVituralName();

//                    if (dataNode.getVituralName().equals(variable.getName()) || dataNode.getVituralName().equals(treeNode.getTitle()))
//                        nodeName = dataNode.getVituralName();
//                    else if (dataNode.getName().equals(variable.getName()) || dataNode.getName().equals(treeNode.getTitle()))
//                        nodeName = dataNode.getName();

                    return PARAMETER_PREFIX + nodeName;
                } else if (treeNode.getType() == IEntry.Type.FUNCTION_CALL) {
                    return treeNode.getTitle();
                } else {
                    return dataNode.getVituralName();
                }
            }
        } else if (treeNode instanceof IFunctionTreeNode) {
            return treeNode.getTitle();
        }
        return dataNode.getName();
    }

    private void putNewEntry(String alias) {
        putNewEntry(alias, null);
    }

    private void putNewEntry(String alias, String additionalChainName) {
        if (isImportedToInput)
            entryList.add(new InputEntryListItem(treeNode, alias, additionalChainName));
        else {
            entryList.add(new OutputEntryListItem(treeNode, alias, additionalChainName));
        }
    }
}
