package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.project_init.ProjectClone;
import com.dse.testdata.comparable.gtest.*;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OneDimensionCharacterDataNode extends OneDimensionDataNode implements IStringComparison {
    private String generateDetailedInputforDisplay() throws Exception {
        StringBuilder input = new StringBuilder();
        for (IDataNode child : this.getChildren())
            input.append(child.getInputForDisplay());
        return input.toString();

    }

//    private String generateDetailedInputforGTest() throws Exception {
//        String input;
//
//        String type = VariableTypeUtils
//                .deleteStorageClasses(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));
//        type = type.substring(0, type.indexOf("["));
//        if (isExternel()) {
//            type = "";
//        }
//        String declaration;
//        if (this.getSize() > 0) {
//            /*
//              Máº·c Ä‘á»‹nh táº¥t cáº£ má»�i pháº§n tá»­ trong máº£ng Ä‘á»�u lÃ  kÃ­ tá»±
//              tráº¯ng. Ta BUá»˜C pháº£i lÃ m Ä‘iá»�u nÃ y Ä‘á»ƒ cÃ¡c pháº§n tá»­ trong
//              máº£ng liÃªn tá»¥c
//             */
//            StringBuilder space = new StringBuilder();
//            for (int i = 0; i < this.getSize(); i++)
//                space.append(" ");
//            /*
//
//             */
//            declaration = String.format("%s %s[%s]=\"%s\"", type, this.getVituralName(), this.getSize(), space.toString());
//        } else
//            declaration = String.format("%s %s[%s]", type, this.getVituralName(), this.getSize());
//        StringBuilder initialization = new StringBuilder();
//
//        for (IDataNode child : this.getChildren())
//            initialization.append(child.getInputForGoogleTest());
//        input = declaration + SpecialCharacter.END_OF_STATEMENT + initialization;
//
//        if (this.isAttribute())
//            input += this.getSetterInStr(this.getVituralName()) + SpecialCharacter.END_OF_STATEMENT;
//        return input;
//
//    }

    private String generateSimplifyInputforDisplay() {
        StringBuilder input = new StringBuilder();

        Map<Integer, String> values = new TreeMap<>();
        for (IDataNode child : this.getChildren()) {
            NormalDataNode nChild = (NormalDataNode) child;

            String index = Utils.getIndexOfArray(nChild.getName()).get(0);
            values.put(Utils.toInt(index), nChild.getValue());
        }

        for (Integer key : values.keySet()) {
            int ASCII = Utils.toInt(values.get(key));

            switch (ASCII) {
                case 34:/* nhay kep */
                    input.append("\\\"");
                    break;

                case 92:/* gach cheo */
                    input.append("\\\\");
                    break;

                case 39:
                    /* nhay don */
                    input.append("\\'");
                    break;

                default:
                    input.append((char) ASCII);
            }
        }
        input = new StringBuilder(this.getDotSetterInStr("\"" + input + "\"") + SpecialCharacter.LINE_BREAK);
        return input.toString();

    }

//    private String generateSimplifyInputforGTest() {
//        String input = "";
//
//        String type = VariableTypeUtils
//                .deleteStorageClasses(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));
//        type = type.substring(0, type.indexOf("["));
//        if (isExternel()) {
//            type = "";
//        }
//
//        StringBuilder initialization = new StringBuilder();
//
//        Map<Integer, String> values = new TreeMap<>();
//        for (IDataNode child : this.getChildren()) {
//            NormalDataNode nChild = (NormalDataNode) child;
//
//            String index = Utils.getIndexOfArray(nChild.getName()).get(0);
//            values.put(Utils.toInt(index), nChild.getValue());
//        }
//
//        for (Integer key : values.keySet()) {
//            int ASCII = Utils.toInt(values.get(key));
//            switch (ASCII) {
//                case 34:/* nhay kep */
//                    initialization.append("\\\"");
//                    break;
//                case 92:/* gach cheo */
//                    initialization.append("\\\\");
//                    break;
//                case 39:
//				/* nhay don */
//                    initialization.append("\\'");
//                    break;
//                default:
//                    initialization.append((char) ASCII);
//            }
//        }
//
//        if (this.isAttribute())
//            input = this.getSetterInStr(Utils.putInString(initialization.toString())) + SpecialCharacter.END_OF_STATEMENT;
//        else if (this.isPassingVariable())
//            input = type + " " + this.getVituralName() + "[]=" + Utils.putInString(initialization.toString())
//                    + SpecialCharacter.END_OF_STATEMENT;
//        return input;
//
//    }

    @Override
    public String getInputForDisplay() throws Exception {
        String input;

        if (this.canConvertToString() && this.isVisible())
            input = this.generateSimplifyInputforDisplay();
        else
            input = this.generateDetailedInputforDisplay();
        return input;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String declaration = "";

        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if (!isPassingVariable() || (isPassingVariable() && isDeclared)) {
            // get type
            String type = VariableTypeUtils
                    .deleteStorageClassesExceptConst(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));
            String coreType = type.replaceAll("\\[.*\\]", "");
            if (isExternel()) {
                coreType = "";
            }

            // get indexes
            List<String> indexes = Utils.getIndexOfArray(type);
            if (indexes.size() > 0) {
                StringBuilder dimension = new StringBuilder();
                for (String index : indexes) {
                    if (index.length() == 0)
                        dimension.append(Utils.asIndex(this.getSize())); // set the configured size of array
                    else
                        dimension.append(Utils.asIndex(index));
                }

                // generate declaration
                if (this.isAttribute()) {
                    declaration += "";
                } else if (this.isPassingVariable()) {
                    if (getSize() > 0) {
                        declaration += String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                                this.getVituralName(), dimension);
                    } else {
                        declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                                this.getVituralName());
                    }
                } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
//                    String firstIndex = indexes.get(0);
                    if (getSize() > 0) {
                        declaration += String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                                this.getVituralName(), dimension);
                    } else {
                        declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                                this.getVituralName());
                    }
                } else if (isVoidPointerValue()) {
                    declaration += String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName(), dimension);
                }
            }
        }

        return declaration + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }

    private boolean isVisible() {
        for (IDataNode child : this.getChildren()) {

            int ASCII = Utils.toInt(((NormalDataNode) child).getValue());

            if (!Utils.isVisibleCh(ASCII))
                return false;
        }
        return true;
    }

    @Override
    public String generareSourcecodetoReadInputFromFile() throws Exception {
        if (getParent() instanceof RootDataNode) {
            String typeVar = VariableTypeUtils.deleteStorageClasses(this.getRawType())
                    .replace(IDataNode.REFERENCE_OPERATOR, "")
                    .replace(IDataNode.ONE_LEVEL_POINTER_OPERATOR, "");
            typeVar = typeVar.substring(0, typeVar.indexOf("["));

            String loadValueStm = "data.findOneDimensionOrLevelBasicByName<" + typeVar + ">(\"" + getVituralName()
                    + "\", DEFAULT_VALUE_FOR_CHARACTER)";

            String fullStm = typeVar + "* " + this.getVituralName() + "=" + loadValueStm
                    + SpecialCharacter.END_OF_STATEMENT;
            return fullStm;
        } else {
            // belong to structure node
            // Handle later;
            return "";
        }
    }

    @Override
    public String getBinaryComparisonAssertion() {
        return new BinaryComparisonStatementGenerator(this).getBinaryComparisonAssertion();
    }

    @Override
    public String getAssertion() {
        StringBuilder assertion = new StringBuilder();
        if (getAssertMethod() != null) {
            if (Environment.getInstance().getCompiler().isUseGTest()) {
                assertion.append(getAssertStm() + "\n");
            }
        }

        for (IDataNode child : this.getChildren()) {
            if (child instanceof ValueDataNode) {
                String childAssertion = ((ValueDataNode) child).getAssertion() + SpecialCharacter.LINE_BREAK;
                assertion.append(childAssertion);
            }
        }

        return assertion.toString();
    }

    @Override
    public String getAssertStm() {
        String assertMethod = getAssertMethod();
        if (assertMethod != null) {
            if (Environment.getInstance().getCompiler().isUseGTest()) {
                assertMethod = assertMethod.split(": ", 2)[0];
                if (IBinaryComparison.assertMethods.contains(assertMethod) || IStringComparison.assertMethods.contains(assertMethod)) {
                    return this.getBinaryComparisonAssertion();
                }
            }
        }
        return SpecialCharacter.EMPTY;
    }
}
