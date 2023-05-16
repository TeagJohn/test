package com.dse.util;

import com.dse.environment.Environment;
import com.dse.parser.object.*;
import com.dse.search.ISearchCondition;
import com.dse.search.LambdaFunctionNodeCondition;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.DefinitionFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;

import java.util.ArrayList;
import java.util.List;

public class FunctionNodeUtils {
    /**
     * Get function nodes in project based on conditions. If no condition is given, all function nodes will be return.
     *
     * @param conditions conditions to search, such as {@link com.dse.search.condition.DefinitionFunctionNodeCondition},
     * @return function nodes in project
     */
    public static List<ICommonFunctionNode> getFunctionNodesInProject(ISearchCondition ...conditions) {
        List<INode> functionNodes = new ArrayList<>();
        ProjectNode projectNode = Environment.getInstance().getProjectNode();

        if (conditions.length == 0) {
            functionNodes.addAll(Search.searchNodes(projectNode, new AbstractFunctionNodeCondition()));
            functionNodes.addAll(Search.searchNodes(projectNode, new DefinitionFunctionNodeCondition()));
            functionNodes.addAll(Search.searchNodes(projectNode, new MacroFunctionNodeCondition()));
            functionNodes.addAll(Search.searchNodes(projectNode, new LambdaFunctionNodeCondition()));

            SystemLibraryRoot libraryRoot = Environment.getInstance().getSystemLibraryRoot();
            if (libraryRoot != null)
                functionNodes.addAll(Search.searchNodes(libraryRoot, new AbstractFunctionNodeCondition()));

            INode dataRoot = Environment.getInstance().getUserCodeRoot();
            if (dataRoot != null)
                functionNodes.addAll(Search.searchNodes(dataRoot, new AbstractFunctionNodeCondition()));
        } else {
            for (ISearchCondition condition : conditions) {
                functionNodes.addAll(Search.searchNodes(projectNode, condition));
            }
        }

        List<ICommonFunctionNode> result = new ArrayList<>();
        for (INode node : functionNodes) {
            if (node instanceof ICommonFunctionNode)
                result.add((ICommonFunctionNode) node);
        }

        return result;
    }
}
