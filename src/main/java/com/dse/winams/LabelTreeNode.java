package com.dse.winams;

import com.dse.winams.IEntry.Type;

public class LabelTreeNode extends TreeNode {

    private IEntry.Type type;

    public LabelTreeNode(Type type) {
        String title = generateTitle(type);
        setTitle(title);
        setType(type);
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    private static String generateTitle(Type type) {
        switch (type) {
            case GLOBAL:
                return "Global Variables";

            case PARAMETER:
                return "Function Parameters";

            case STATIC:
                return "Static Local Variables";

            case FUNCTION_CALL:
                return "Stubs";

            case RETURN:
                return "Function Return";

            default:
                return null;
        }
    }

    public ITreeNode clone() {
        return new LabelTreeNode(type);
    }
}
