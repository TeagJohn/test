package com.dse.winams.retriever;

import com.dse.parser.object.NumberOfCallNode;
import com.dse.search.Search2;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.object.*;
import com.dse.winams.ICsvInfo;
import com.dse.winams.IEntry;
import com.dse.winams.IInputEntry;
import com.dse.winams.IOutputEntry;
import com.dse.winams.converter.SimpleTreeBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvDataRetriever {

    private final ICsvInfo info;

    private final List<TestCase> testCases;

    private final SimpleTreeBuilder.Node root;

    public CsvDataRetriever(List<TestCase> testCases, ICsvInfo csvInfo) {
        this.info = csvInfo;
        this.testCases = testCases;
        SimpleTreeBuilder builder = new SimpleTreeBuilder();
        csvInfo.getInputs().forEach(builder::append);
        csvInfo.getOutputs().forEach(builder::append);
        this.root = builder.getRoot();
    }

    public TestSuiteRecord fetch() {
        TestSuiteRecord testSuiteRecord = new TestSuiteRecord();

        final List<IEntry> entries = new ArrayList<>();
        entries.addAll(info.getInputs());
        entries.addAll(info.getOutputs());

        for (TestCase testCase : testCases) {
            TestCaseRecord testCaseRecord = new TestCaseRecord();

            Map<String, IDataNode> data = new HashMap<>();
            traverse(testCase.getRootDataNode(), root, data);

            for (IEntry entry : entries) {
                IDataNode dataNode = null;
                if (entry instanceof IInputEntry) {
                    dataNode = data.get("IN@" + entry.getAlias());
                } else if (entry instanceof IOutputEntry) {
                    dataNode = data.get("OUT@" + entry.getAlias());
                }
                String value = getValue(dataNode);
                testCaseRecord.add(value);
            }

            testSuiteRecord.add(testCaseRecord);
        }

        return testSuiteRecord;
    }

    private String getValue(IDataNode dataNode) {
        String value = null;

        // first "if" to display "USER CODE" for parameters that use user code
        if (dataNode instanceof ValueDataNode && ((ValueDataNode) dataNode).isUseUserCode()) {
            value = ((ValueDataNode) dataNode).getUserCodeDisplayText();

        } else if (dataNode instanceof NumberOfCallNode) {
            value = ((NumberOfCallNode) dataNode).getValue();

        } else if (dataNode instanceof NormalDataNode) {
            value = ((NormalDataNode) dataNode).getValue();

        } else if (dataNode instanceof EnumDataNode) {
            if (((EnumDataNode) dataNode).isSetValue()) {
                value = ((EnumDataNode) dataNode).getValue();
            }

        } else if (dataNode instanceof UnionDataNode) {
            value = ((UnionDataNode) dataNode).getSelectedField();

        } else if (dataNode instanceof SubClassDataNode) {
            SubClassDataNode subClassDataNode = (SubClassDataNode) dataNode;
            if (subClassDataNode.getSelectedConstructor() != null) {
                // Show constructor class name
                value = subClassDataNode.getSelectedConstructor().getName();
            }

        } else if (dataNode instanceof ClassDataNode) {
            // class
            ClassDataNode classDataNode = (ClassDataNode) dataNode;
            if (classDataNode.getSubStructure() != null) {
                // Show class name
                value = classDataNode.getSubStructure().getRawType();
            }

        } else if (dataNode instanceof OneDimensionDataNode) {
            OneDimensionDataNode arrayNode = (OneDimensionDataNode) dataNode;
            if (arrayNode.isFixedSize() || arrayNode.isSetSize()) {
                if (dataNode instanceof OneDimensionNumberDataNode) {
                    StringBuilder valueBuilder = null;
                    for (IDataNode child : dataNode.getChildren()) {
                        if (valueBuilder == null) valueBuilder = new StringBuilder();
                        else valueBuilder.append("|");
                        if (child instanceof NormalDataNode) {
                            valueBuilder.append(((NormalDataNode) child).getValue());
                        }
                    }
                    assert valueBuilder != null;
                    value = valueBuilder.toString();
                } else if (dataNode instanceof OneDimensionCharacterDataNode) {
                    StringBuilder valueBuilder = new StringBuilder();
                    valueBuilder.append("'");
                    for (IDataNode child : dataNode.getChildren()) {
                        if (child instanceof NormalCharacterDataNode) {
                            valueBuilder.append(((NormalCharacterDataNode) child).getValue());
                        }
                    }
                    valueBuilder.append("'");
                    value = valueBuilder.toString();
                } else {
                    value = String.valueOf(arrayNode.getSize());
                }
            }

        } else if (dataNode instanceof PointerDataNode) {
            PointerDataNode pointerNode = (PointerDataNode) dataNode;
            value = String.valueOf(pointerNode.getAllocatedSize());

        } else if (dataNode instanceof MultipleDimensionDataNode) {
            MultipleDimensionDataNode arrayNode = (MultipleDimensionDataNode) dataNode;
            if (arrayNode.isSetSize()) {
                if (arrayNode.getSizeOfDimension(0) > 0) {
                    value = String.valueOf(arrayNode.getSizeOfDimension(0));
                }
            }

        } else if (dataNode instanceof FunctionPointerDataNode) {
            // TODO
        } else if (dataNode instanceof VoidPointerDataNode) {
            // TODO
        }

        if (value == null)
            value = "";

        return value;
    }

    private void traverse(IDataNode dataNode, SimpleTreeBuilder.Node silNode, Map<String, IDataNode> data) {
        for (IDataNode child : dataNode.getChildren()) {
            if (child instanceof NumberOfCallNode) {
                traverse(child, silNode, data);
                continue;
            }

            if (child instanceof IterationSubprogramNode) {
                traverse(child, silNode, data);
                continue;
            }

            SimpleTreeBuilder.Node silChild = silNode.findChild(child.getName());
            if (silChild != null) {
                if (silChild.isInput()) {
                    data.put("IN@" + silChild.getAlias(), child);
                } else if (silChild.isOutput()) {
                    data.put("OUT@" + silChild.getAlias(), Search2.getExpectedValue((ValueDataNode) child));
                }
                traverse(child, silChild, data);
            }
        }
    }


}