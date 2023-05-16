package com.dse.winams;

import com.dse.parser.object.IFunctionNode;
import java.util.List;

public interface ICsvInfo {

    /**
     * @return path to csv file
     */
    String getPath();
    String getName();

    IFunctionNode getFunction();

    List<IInputEntry> getInputs();
    List<IOutputEntry> getOutputs();
}
