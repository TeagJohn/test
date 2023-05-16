package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.resolver.DeclSpecSearcher;
import com.dse.search.Search;
import com.dse.testdata.object.StructureDataNode;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

import java.util.List;
import java.util.stream.Collectors;

public abstract class StructOrClassDataNode extends StructureDataNode {

    /**
     * SubStructure represent the real structure data node
     */
    protected ISubStructOrClassDataNode subStructure = null;

    public void setSubStructure(INode structureNode) {
        if (structureNode instanceof StructOrClassNode) {
            String fullStructName = Search.getScopeQualifier(structureNode);

            VariableNode correspondingVar = VariableTypeUtils.cloneAndReplaceType(fullStructName, getCorrespondingVar(), structureNode);

            expandSubStructure(correspondingVar);
        } else {
            System.out.println(structureNode.getAbsolutePath() + " is not a structure node");
        }
    }

    private void expandSubStructure(VariableNode correspondingVar) {
        subStructure = createSubStructureDataNode();

        subStructure.setName(correspondingVar.getName());
        subStructure.setRawType(correspondingVar.getRawType());
        subStructure.setRealType(correspondingVar.getRealType());
        subStructure.setCorrespondingVar(correspondingVar);

        subStructure.setParent(this);
        getChildren().clear();
        addChild(subStructure);
    }


    protected abstract ISubStructOrClassDataNode createSubStructureDataNode();

    public void setSubStructure(ISubStructOrClassDataNode subStructure) {
        this.subStructure = subStructure;
        subStructure.setParent(this);
        getChildren().clear();
        addChild(subStructure);
    }

    public void setSubStructure(String subStructureName) {
        if (subStructureName.isEmpty()) {
            subStructure = null;
            getChildren().clear();
        }

        List<INode> candidateClasses = getDerivedClass();
        if (!candidateClasses.contains(getCorrespondingType())) {
            candidateClasses.add(getCorrespondingType());
        }

        for (INode subStructureNode : candidateClasses) {
            if (subStructureNode.getName().equals(subStructureName)) {
                if (Environment.getInstance().getCompiler().isUseGTest() && subStructureNode instanceof StructOrClassNode) {
                    setSubStructure(subStructureNode);
                    return;
                }
                if (subStructureNode instanceof StructOrClassNode && !((StructOrClassNode) subStructureNode).isAbstract()) {
//                if (subStructureNode instanceof StructOrClassNode) {
                    setSubStructure(subStructureNode);
                    return;

                }
            }
        }
        for (INode subStructureNode : candidateClasses) {
            if (Environment.getInstance().getCompiler().isUseGTest() && subStructureNode instanceof StructOrClassNode) {
                setSubStructure(subStructureNode);
                return;
            }
            if (subStructureNode instanceof StructOrClassNode && !((StructOrClassNode) subStructureNode).isAbstract()) {
                setSubStructure(subStructureNode);
                return;

            }
        }
    }

    /**
     * Get the real struct data node
     *
     * @return the real struct data node
     */
    public ISubStructOrClassDataNode getSubStructure() {
        return subStructure;
    }

    /**
     * Get the list of the derived classes of current struct or class
     *
     * @return the list of the derived classes of current struct or class
     */
    public List<INode> getDerivedClass() {
        StructOrClassNode typeNode = (StructOrClassNode) getCorrespondingType();

        List<INode> derivedClass = Search.getDerivedNodesInSpace(typeNode, getTestCaseRoot().getFunctionNode());

        List<INode> listWithoutAbstract = derivedClass.stream()
                .filter(s -> !((StructOrClassNode) s).isAbstract())
                .collect(Collectors.toList());

        if (!listWithoutAbstract.isEmpty())
            derivedClass = listWithoutAbstract;

//        addTemplateDerivedClass(derivedClass);

        return derivedClass;
    }

    private void addTemplateDerivedClass(List<INode> derivedClass) {
        ICommonFunctionNode functionNode = getTestCaseRoot().getFunctionNode();

        for (INode classNode : derivedClass) {
            if (classNode instanceof StructOrClassNode && ((ClassNode) classNode).isTemplate()) {
                derivedClass.remove(classNode);

                String[] templateParams = TemplateUtils.getTemplateParameters(classNode);
                if (templateParams != null) {

                    VariableSearchingSpace space = new VariableSearchingSpace(functionNode);
                    DeclSpecSearcher searcher = new DeclSpecSearcher(String.format("%s<.+>", classNode.getName()),
                            space.generateExtendSpaces(), false);

                    for (IASTDeclSpecifier declSpec : searcher.getDeclSpecs()) {
                        String simpleType = VariableTypeUtils.getSimpleRealType(declSpec.getRawSignature());
                        String[] templateArguments = TemplateUtils.getTemplateArguments(simpleType);

                        if (templateArguments.length == templateParams.length) {
                            INode clone = classNode.clone();
                            clone.setName(simpleType);

                            derivedClass.add(clone);
                        }
                    }
                }
            }
        }
    }

    public boolean isSetSubStructure() {
        return getSubStructure() != null;
    }
}
