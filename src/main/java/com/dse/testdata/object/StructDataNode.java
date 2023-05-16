package com.dse.testdata.object;

// import com.dse.parser.object.INode;
import com.dse.environment.Environment;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.resolver.DeclSpecSearcher;
import com.dse.search.Search;
import com.dse.util.SpecialCharacter;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represent struct variable
 *
 * @author DucAnh
 */
public class StructDataNode extends StructOrClassDataNode {


    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if (isDeclared && getCorrespondingType() instanceof StructOrClassNode
            && ((StructOrClassNode) getCorrespondingType()).isAbstract())
            return SpecialCharacter.EMPTY;

        if (Environment.getInstance().isC()) {
            return getCInputGTest(isDeclared);
        } else {
            return getCppInputGTest(isDeclared);
//            return super.getInputForGoogleTest(isDeclared);
        }
    }

    private String getCInputGTest(boolean isDeclared) throws Exception {
        String input = "";
        if (!isPassingVariable() || (isPassingVariable() && isDeclared)) {
            String typeVar = this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);
            typeVar = typeVar.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);

            // INode correspondingType = getCorrespondingType();
            // if (correspondingType instanceof StructureNode && !((StructureNode)
            // correspondingType).haveTypedef()) {
            // if (!typeVar.startsWith("struct"))
            // typeVar = "struct " + typeVar;
            // }

            if (isExternel())
                typeVar = "";

            if (this.isPassingVariable()) {
                if (subStructure != null && (subStructure instanceof SubStructDataNode))
                    input += "";
                else
                    input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (getParent() instanceof OneDimensionDataNode || getParent() instanceof PointerDataNode) {
                input += "";

            } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (isInstance()) {
                input += "";

            } else if (isVoidPointerValue()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        return input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }

    private String getCppInputGTest(boolean isDeclared) throws Exception {
        String input = "";
        if (!isPassingVariable() || (isPassingVariable() && isDeclared)) {
            String typeVar = this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);
            typeVar = typeVar.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);

            // INode correspondingType = getCorrespondingType();
            // if (correspondingType instanceof StructureNode && !((StructureNode)
            // correspondingType).haveTypedef()) {
            // if (!typeVar.startsWith("struct"))
            // typeVar = "struct " + typeVar;
            // }

            if (isExternel())
                typeVar = "";

            if (this.isPassingVariable()) {
                if (subStructure != null && (subStructure instanceof SubStructDataNode || subStructure instanceof SubClassDataNode))
                    input += "";
                else
                    input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (getParent() instanceof OneDimensionDataNode || getParent() instanceof PointerDataNode) {
                input += "";

            } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
                //loi khi struct khong co default constructor;
//                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (isInstance()) {
                input += "";

            } else if (isVoidPointerValue()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        return input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }

    @Override
    public StructDataNode clone() {
        StructDataNode clone = (StructDataNode) super.clone();

        for (IDataNode child : getChildren()) {
            if (child instanceof ValueDataNode) {
                ValueDataNode cloneChild = ((ValueDataNode) child).clone();
                clone.getChildren().add(cloneChild);
                cloneChild.setParent(clone);
            }
        }

        return clone;
    }

    @Override
    protected ISubStructOrClassDataNode createSubStructureDataNode() {
        return new SubStructDataNode();
    }
}
