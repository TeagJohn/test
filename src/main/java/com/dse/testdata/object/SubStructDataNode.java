package com.dse.testdata.object;

import com.dse.logger.AkaLogger;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.RealParentDependency;
import com.dse.parser.object.ConstructorNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.StructureNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent real struct variable
 */
public class SubStructDataNode extends StructDataNode implements IConstructorExpanableDataNode, ISubStructOrClassDataNode {

    private static final AkaLogger logger = AkaLogger.get(SubStructDataNode.class);

    protected ICommonFunctionNode selectedConstructor;

    public ConstructorDataNode getConstructorDataNode() {
        for (IDataNode child : getChildren()) {
            if (child instanceof ConstructorDataNode) {
                return (ConstructorDataNode) child;
            }
        }
        return null;
    }

    public List<ICommonFunctionNode> getConstructorsOnlyInCurrentClass() {
        List<ICommonFunctionNode> constructors = new ArrayList<>();

        INode correspondingType = getCorrespondingType();

        if (correspondingType != null) {
            ArrayList<ICommonFunctionNode> cons = ((StructureNode) correspondingType).getConstructors();
            for (ICommonFunctionNode con: cons) {
                if (!constructors.contains(con)) {
                    constructors.add(con);
                }
            }

            for (Dependency dependency: correspondingType.getDependencies()){
                if (dependency instanceof RealParentDependency) {
                    if (dependency.getEndArrow().getAbsolutePath().equals(correspondingType.getAbsolutePath())){
                        if (dependency.getStartArrow() instanceof ConstructorNode) {
                            constructors.add((ICommonFunctionNode) dependency.getStartArrow());
                        }
                    }
                }
            }
        } else {
            logger.error("Get null corresponding type");
        }

        return constructors;
    }

    public ICommonFunctionNode getSelectedConstructor() {
        return selectedConstructor;
    }

    private boolean isConstructor(ICommonFunctionNode constructor) {
        try {
            return getConstructorsOnlyInCurrentClass().contains(constructor);
        } catch (Exception e) {
            return false;
        }
    }

    public List<IDataNode> getAttributes() {
        List<IDataNode> attributes = new ArrayList<>();
        for (IDataNode child: getChildren()) {
            if (!(child instanceof ConstructorDataNode)){
                attributes.add(child);
            }
        }
        return attributes;
    }

    public void chooseConstructor(ICommonFunctionNode constructor) throws Exception {
        if (isConstructor(constructor)) {
            selectedConstructor = constructor;
        } else {
            throw new Exception("Constructor khong ton tai!");
        }
    }

    @Override
    public void chooseConstructor(String constructorName) throws Exception {
        List<ICommonFunctionNode> constructors = getConstructorsOnlyInCurrentClass();
        for (ICommonFunctionNode node : constructors) {
            // should not use equal because constructor name might have its scope "::"
            if (constructorName.endsWith(node.getName()) && node instanceof ConstructorNode /* not Constructor declaration*/) {
                chooseConstructor(node);
                break;
            }
        }
    }

    @Override
    public void chooseConstructor() throws Exception {
        List<ICommonFunctionNode> constructors = getConstructorsOnlyInCurrentClass();
        constructors.removeIf(c -> (c instanceof ConstructorNode && ((ConstructorNode) c).isInAbstractClass()));
        chooseConstructor(constructors.get(0));
    }
}
