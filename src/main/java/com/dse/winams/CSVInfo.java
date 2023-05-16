package com.dse.winams;

import com.dse.parser.object.IFunctionNode;

import java.util.ArrayList;
import java.util.List;

public class CSVInfo implements ICsvInfo {
    private String path;
    private String filename;
    private IFunctionNode functionNode;
    List<IInputEntry> inputsEntries;
    List<IOutputEntry> outputEntries;

    public CSVInfo() {
        inputsEntries = new ArrayList<>();
        outputEntries = new ArrayList<>();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFunctionNode(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public void setInputsEntries(List<IInputEntry> inputs) {
        inputsEntries.clear();
        inputsEntries.addAll(inputs);
    }

    public void setOutputEntries(List<IOutputEntry> outputs) {
        outputEntries.clear();
        outputEntries.addAll(outputs);
    }

    public void setName(String filename) {
        this.filename = filename;
    }

    @Override
    public String getPath() {
        return path;
    }

    public String getName() {
        return filename;
    }

    @Override
    public IFunctionNode getFunction() {
        return functionNode;
    }

    @Override
    public List<IInputEntry> getInputs() {
        return inputsEntries;
    }

    @Override
    public List<IOutputEntry> getOutputs() {
        return outputEntries;
    }
}
