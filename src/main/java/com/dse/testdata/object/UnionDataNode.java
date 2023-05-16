package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.parser.object.INode;
import com.dse.parser.object.UnionNode;
import com.dse.parser.object.VariableNode;
import com.dse.search.Search;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

import java.util.List;

/**
 * Represent union variable
 *
 * @author ducanhnguyen
 */
public class UnionDataNode extends StructureDataNode {

    private String selectedField;

    protected SubUnionDataNode subUnion = null;

    public void setSubUnion() {
        String fullUnionName = Search.getScopeQualifier(getCorrespondingType());

        VariableNode correspondingVar = VariableTypeUtils.cloneAndReplaceType(fullUnionName, getCorrespondingVar(), getCorrespondingType());

        expandSubUnion(correspondingVar);
    }

    private void expandSubUnion(VariableNode correspondingVar) {
        subUnion = createSubUnionDataNode();

        subUnion.setName(correspondingVar.getName());
        subUnion.setRawType(correspondingVar.getRawType());
        subUnion.setRealType(correspondingVar.getRealType());
        subUnion.setCorrespondingVar(correspondingVar);

        subUnion.setParent(this);
        getChildren().clear();
        addChild(subUnion);
    }

    protected SubUnionDataNode createSubUnionDataNode() {
        return new SubUnionDataNode();
    }

    public void setSubUnion(SubUnionDataNode subUnion) {
        this.subUnion = subUnion;
        subUnion.setParent(this);
        getChildren().clear();
        addChild(subUnion);
    }

    /**
     * Get the real Union data node
     *
     * @return the real Union data node
     */
    public SubUnionDataNode getSubUnion() {
        return subUnion;
    }

    public void setField(String selectedField) {
        this.selectedField = selectedField;
    }

    public String getSelectedField() {
        return selectedField;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String input = "";

        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if (!isPassingVariable() || (isPassingVariable() && isDeclared)) {
            String typeVar = this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);

            if (isExternel())
                typeVar = "";

            if (Environment.getInstance().isC()) {
                typeVar = typeVar.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);

//            INode correspondingType = getCorrespondingType();
//            if (correspondingType instanceof StructureNode && !((StructureNode) correspondingType).haveTypedef()) {
//                if (!typeVar.startsWith("union"))
//                    typeVar = "union " + typeVar;
//            }
            }

            if (this.isPassingVariable()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (getParent() instanceof PointerDataNode) {
//                input += getVituralName() + " = " + VariableTypeUtils.deleteUnionKeyword(typeVar) + "()" + SpecialCharacter.END_OF_STATEMENT;

            } else if (getParent() instanceof OneDimensionDataNode) {
                input += "";
            } else if (isSutExpectedArgument() || isGlobalExpectedValue())
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            else if (isVoidPointerValue()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        String childCode = SpecialCharacter.EMPTY;
        if (selectedField != null) {
            IDataNode child = getChildren().stream()
                    .filter(n -> n.getName().equals(selectedField))
                    .findFirst()
                    .orElse(null);
            if (child != null) {
                childCode = child.getInputForGoogleTest(isDeclared);
            }
        }
        if (Environment.getInstance().isC()) {
            return input + SpecialCharacter.LINE_BREAK + childCode;
        } else {
            return super.getInputForGoogleTest(isDeclared);
        }
    }

    @Override
    public UnionDataNode clone() {
        UnionDataNode clone = (UnionDataNode) super.clone();

        for (IDataNode child : getChildren()) {
            if (child instanceof ValueDataNode) {
                ValueDataNode cloneChild = ((ValueDataNode) child).clone();
                clone.getChildren().add(cloneChild);
                cloneChild.setParent(clone);
            }
        }

        return clone;
    }
}
