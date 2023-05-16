package com.dse.testdata.object;

import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.resolver.DeclSpecSearcher;
import com.dse.search.Search;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represent class variable
 */
public class ClassDataNode extends StructOrClassDataNode {

    @Override
    protected ISubStructOrClassDataNode createSubStructureDataNode() {
        return new SubClassDataNode();
    }
}