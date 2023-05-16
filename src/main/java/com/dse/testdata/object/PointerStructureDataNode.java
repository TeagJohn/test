package com.dse.testdata.object;

import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.TypeDependency;
import com.dse.parser.object.ClassNode;
import com.dse.parser.object.ConstructorNode;
import com.dse.parser.object.INode;
import com.dse.search.Search2;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent variable as pointer (one level, two level, etc.)
 *
 * @author ducanhnguyen
 */
public class PointerStructureDataNode extends PointerDataNode {
    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String input = "";
        String allocation = "";
        if (!isPassingVariable() || (isPassingVariable() && isDeclared)) {
            String type = VariableTypeUtils
                    .deleteStorageClassesExceptConst(getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));

            String coreType = "";
            if (getChildren() != null && !getChildren().isEmpty())
                coreType = ((ValueDataNode) getChildren().get(0)).getRawType();
            else
                coreType = type.substring(0, type.lastIndexOf('*'));

            if (isExternel())
                type = "";

            String name = getVituralName();
            int size = getAllocatedSize();

            if (isPassingVariable() || isSTLListBaseElement() || isInConstructor() || isGlobalExpectedValue()
                    || isSutExpectedArgument()) {
                if (this.isNotNull()) {
                    allocation = String.format("%s %s = (%s) malloc(%d * sizeof(%s));", type, name, type, size,
                            coreType);
                } else {
                    allocation = String.format("%s %s = "
                            + IDataNode.NULL_POINTER_IN_CPP
                            + SpecialCharacter.END_OF_STATEMENT, type, name);
                }
            } else if (isArrayElement() || isAttribute()) {
                if (this.isNotNull())
                    allocation = String.format("%s = (%s) malloc(%d * sizeof(%s));", name, type, size, coreType);
                else
                    allocation = String.format("%s = "
                            + IDataNode.NULL_POINTER_IN_CPP
                            + SpecialCharacter.END_OF_STATEMENT, name);
            } else {
                if (this.isNotNull())
                    allocation = String.format("%s = (%s) malloc(%d * sizeof(%s));", name, type, size, coreType);
                else
                    allocation = name + " = " + IDataNode.NULL_POINTER_IN_CPP + SpecialCharacter.END_OF_STATEMENT;
            }

            input = reformatAllocationForIncompleteTypes(allocation, type);
        }

        List<IDataNode> constructorDataNodes = Search2.searchNodes(this, ConstructorDataNode.class);
        if (!isDeclared && isPointerClass()) {
            if (!constructorDataNodes.isEmpty()) {
                ConstructorDataNode cons = (ConstructorDataNode) constructorDataNodes.get(0);
                for (IDataNode arg : cons.getChildren()) {
                    input += arg.getInputForGoogleTest(true);
                }
                String argumentInput = cons.getConstructorArgumentsInputForGoogleTest();
                String realType = cons.getRealType();
                input += "\n" + getVituralName() + " = new " + realType
                        + argumentInput + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        if (getLevel() > 0 && getChildren().size() > 0) {
            IDataNode first = getChildren().get(0);
            if (first.getChildren().size() > 0) {
                first = first.getChildren().get(0);
                for (int i = 1; i < first.getChildren().size(); i++) {
                    IDataNode node = first.getChildren().get(i);
                    input += node.getInputForGoogleTest(false);
                }
            }
        }
//        //if (da chon cons) {
//            // lay node cons
//            String argumentInput = cons.getConstructorArgumentsInputForGoogleTest();
//            String realType = VariableTypeUtils.getFullRawType(subclassVar.getCorrespondingVar());
//            input += getVituralName() + " = new " + realType
//                    + argumentInput + SpecialCharacter.END_OF_STATEMENT;
//        }

        return input + SpecialCharacter.LINE_BREAK;

    }

    //    @Override
//    public String getInputForGoogleTest() throws Exception {
//        if (Environment.getInstance().isC()) {
//            String input = "";
//
//            String type = VariableTypeUtils
//                    .deleteStorageClasses(getType().replace(IDataNode.REFERENCE_OPERATOR, ""));
//
//            String coreType = "";
//            if (getChildren() != null && !getChildren().isEmpty())
//                coreType = ((ValueDataNode) getChildren().get(0)).getType();
//            else
//                coreType = type.substring(0, type.lastIndexOf('*'));
//
//            if (getCorrespondingType() instanceof StructNode) {
//                type = "struct " + type;
//                coreType = "struct " + coreType;
//            }
//
//            if (isExternel())
//                type = "";
//
//            String allocation = "";
//
//            String tempName = getVituralName() + "_temp";
//            tempName = tempName.replaceAll("[^\\w]", "_");
//
//            if (isPassingVariable() || isSTLListBaseElement() || isInConstructor() || isGlobalExpectedValue() || isSutExpectedArgument()) {
//
//                if (this.isNotNull()) {
//                    allocation = String.format("%s %s;\n", coreType, tempName);
//                    allocation += String.format("%s %s = &%s" + SpecialCharacter.END_OF_STATEMENT, type,
//                            this.getVituralName(), tempName);
//                } else {
//                    allocation = String.format("%s %s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT,
//                            type, this.getVituralName());
//                }
//                input += allocation;
//            } else if (isArrayElement() || isAttribute()) {
//                if (this.isNotNull()) {
//                    allocation = String.format("%s %s;\n", coreType, tempName);
//                    allocation += String.format("%s = &%s" + SpecialCharacter.END_OF_STATEMENT,
//                            this.getVituralName(), tempName);
//                } else
//                    allocation = String.format("%s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT
//                            , this.getVituralName());
//                input += allocation;
//            } else {
//                if (this.isNotNull()) {
//                    allocation = String.format("%s %s;\n", coreType, tempName);
//                    allocation += String.format("%s = &%s" + SpecialCharacter.END_OF_STATEMENT,
//                            this.getVituralName(), tempName);
//                } else
//                    allocation = String.format("%s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT
//                            , this.getVituralName());
//                input += allocation;
//            }
//
//            input = input.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);
//
//            return input + SpecialCharacter.LINE_BREAK + superSuperInputGTest();
//        } else
//            return super.getInputForGoogleTest();
//    }


    //	@Override
//	public String getInputForGoogleTest() throws Exception {
//		String input = "";
//		if (this.isAttribute()) {
//			input = "// does not support this struct initialization";
//
//		} else if (this.isPassingVariable()) {
//			// create one dimensional array
//			String type = VariableTypeUtils
//					.deleteStorageClasses(this.getType().replace(IDataNode.REFERENCE_OPERATOR, ""));
//
//			String coreType = type.replace(IDataNode.ONE_LEVEL_POINTER_OPERATOR, "");
//			if (this.isNotNull())
//				input += coreType + " " + getVituralName() + "[" + getAllocatedSize() + "]" + SpecialCharacter.END_OF_STATEMENT;
//			else
//				input += type + " " + getVituralName()  + " = NULL" + SpecialCharacter.END_OF_STATEMENT;
//		} else {
//			input = "// does not support this struct initialization";
//		}
//
//		return input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest();
//	}

    private boolean isPointerClass() {
        return getCorrespondingVar().getCorrespondingNode() instanceof ClassNode;
    }
}
