package com.dse.search.condition;

import com.dse.parser.object.*;
import com.dse.search.SearchCondition;

public class ClassStructUnionvsNamespaceCondition extends SearchCondition {
    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof ClassNode
                || n instanceof StructNode
                || n instanceof UnionNode
                || n instanceof NamespaceNode;
    }
}
