package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

import javax.swing.*;
import java.io.File;

/**
 * Represent normal struct node
 * <p>
 * Ex:
 * <p>
 * <p>
 * <pre>
 * struct SinhVien{
 * 			int age;
 * }
 * </pre>
 *
 * @author ducanhnguyen
 */
public class StructNode extends StructOrClassNode {

    public StructNode() {
        try {
            Icon ICON_STRUCT = new ImageIcon(Node.class.getResource("/image/node/StructNode.png"));
            setIcon(ICON_STRUCT);
        } catch (Exception e) {
        }

    }

    @Override
    public String getNewType() {
        return ((IASTCompositeTypeSpecifier) getAST().getDeclSpecifier()).getName().toString();
    }

    @Override
    public IASTFileLocation getNodeLocation() {
        return ((IASTCompositeTypeSpecifier) getAST().getDeclSpecifier()).getName().getFileLocation();
    }

    @Override
    public File getSourceFile() {
        return new File(getAST().getContainingFilename());
    }

    @Override
    public String toString() {
        return this.getNewType();
//        return /* "struct " + */ super.toString();
    }

    @Override
    public INode clone() {
        StructNode clone = new StructNode();
        clone.setAST(getAST());
        clone.setName(getName());
        clone.setAbsolutePath(getAbsolutePath());
        clone.setParent(getParent());
        return clone;
    }
}
