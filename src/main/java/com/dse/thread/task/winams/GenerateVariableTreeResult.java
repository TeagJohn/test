package com.dse.thread.task.winams;

import com.dse.winams.VariableTree;

public class GenerateVariableTreeResult {
    private ExitCode exitCode;
    private VariableTree variableTree;

    public ExitCode getExitCode() {
        return exitCode;
    }

    public void setExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    public VariableTree getVariableTree() {
        return variableTree;
    }

    public void setVariableTree(VariableTree variableTree) {
        this.variableTree = variableTree;
    }

    enum ExitCode {
        SUCCESS,
        FAILURE
    }
}
