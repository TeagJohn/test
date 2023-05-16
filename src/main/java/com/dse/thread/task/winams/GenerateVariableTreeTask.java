package com.dse.thread.task.winams;

import com.dse.logger.AkaLogger;
import com.dse.parser.object.IFunctionNode;
import com.dse.thread.AbstractAkaTask;
import com.dse.winams.VariableTree;

public class GenerateVariableTreeTask extends AbstractAkaTask<GenerateVariableTreeResult> {
    private static final AkaLogger logger = AkaLogger.get(GenerateVariableTreeTask.class);
    private IFunctionNode function;

    private GenerateVariableTreeResult result = new GenerateVariableTreeResult();

    public GenerateVariableTreeTask(IFunctionNode function) {
        this.function = function;
    }

    @Override
    protected GenerateVariableTreeResult call() throws Exception {
        try {
            logger.debug("Generating variable tree for function " + function.getName());
            VariableTree variableTree = new VariableTree(function);
            logger.debug("Variable tree generated for function " + function.getName());
            result.setVariableTree(variableTree);
            result.setExitCode(GenerateVariableTreeResult.ExitCode.SUCCESS);
        } catch (Exception e) {
            result.setExitCode(GenerateVariableTreeResult.ExitCode.FAILURE);
        }

        return result;
    }
}
