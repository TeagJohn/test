package com.dse.winams.UI.object;

public class PointerSetting {
    public enum Type {
        ADDRESS,
        ALLOCATE_MEMORY_NC,
        ALLOCATE_MEMORY_PIW,
    }

    private Type type;
    private int startIndex;
    private int endIndex;

    public PointerSetting(Type type, int startIndex, int endIndex) {
        this.type = type;
        if (type == Type.ALLOCATE_MEMORY_PIW) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        } else {
            this.startIndex = -1;
            this.endIndex = -1;
        }
    }

    public Type getType() {
        return type;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}
