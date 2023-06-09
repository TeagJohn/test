package com.dse.parser.object;

import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.search.Search;
import com.dse.search.condition.TypedefNodeCondition;
import com.dse.util.tostring.DependencyTreeDisplayer;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;

import java.io.File;

/**
 * Represent single typedef declaration. Ex: typedef char char_t
 *
 * @author DucAnh
 */
public class TypedefDeclaration extends CustomASTNode<IASTSimpleDeclaration> implements ITypedefDeclaration {

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.TSDV_R1_4));
        System.out.println(new DependencyTreeDisplayer(parser.getRootTree()).getTreeInString());
        SingleTypedefDeclaration n = (SingleTypedefDeclaration) Search.searchNodes(parser.getRootTree(),
                new TypedefNodeCondition(), "TypeDef.cpp" + File.separator + "MyIntPtr").get(0);
        System.out.println(n.getNewType());
        System.out.println(n.getOldName());
    }

    /**
     * Ex1: "typedef int MyIntPtr;"----->"MyIntPtr"
     * <p>
     * <p>
     * Ex1: "typedef int *MyIntPtr;"----->"MyIntPtr"
     */
    @Override
    public String getNewType() {
        return getAST().getDeclarators()[0].getName().toString();
    }

    /**
     * Ex: "typedef int MyIntPtr;"----->"int"
     */
    @Override
    public String getOldType() {
        String oldName = getAST().getDeclSpecifier().getRawSignature();

		/*
         * Ex: "typedef int *MyIntPtr;"
		 */
        if (getAST().getChildren().length >= 2 && getAST().getDeclarators()[0].getChildren().length >= 2) {
            IASTNode firstChild = getAST().getDeclarators()[0].getChildren()[0];

            if (firstChild instanceof CPPASTPointer)
                oldName += firstChild.getRawSignature();
            else {
				/*
				 * Ex: "typedef int MyIntPtr[];"
				 */
                IASTNode secondChild = getAST().getDeclarators()[0].getChildren()[1];

                if (secondChild instanceof CPPASTArrayModifier)
                    oldName += secondChild.getRawSignature();
            }
        }

		/*
		 * Delete keyword typedef
		 */
        if (getAST().getDeclSpecifier() instanceof IASTElaboratedTypeSpecifier) {
            IASTElaboratedTypeSpecifier decl = (IASTElaboratedTypeSpecifier) getAST().getDeclSpecifier();
			/*
			 * Trường hợp declSpecifier của class là phức tạp, VD "typedef class
			 * thisinh SV"
			 */
            if (decl.getStorageClass() == IASTElaboratedTypeSpecifier.k_struct)
                oldName = oldName.replaceAll("typedef\\s*class\\s*", "");
        }
        return oldName.replaceAll("^typedef\\s*", "");
    }

    @Override
    public String toString() {
        return getAST().getRawSignature();
    }
}
