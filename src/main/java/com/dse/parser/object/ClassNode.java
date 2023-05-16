package com.dse.parser.object;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

public class ClassNode extends StructOrClassNode implements ISourceNavigable {

	public ClassNode() {
		try {
			Icon ICON_CLASS = new ImageIcon(Node.class.getResource("/image/node/ClassNode.png"));
			setIcon(ICON_CLASS);
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


	public IASTCompositeTypeSpecifier getSpecifiedAST() {
		return ((IASTCompositeTypeSpecifier) getAST().getDeclSpecifier());
	}

	@Override
	public String toString() {
		return this.getNewType();
	}

	@Override
	public INode clone() {
		ClassNode clone = new ClassNode();
		clone.setAST(getAST());
		clone.setName(getName());
		clone.setAbsolutePath(getAbsolutePath());
		clone.setParent(getParent());
		return clone;
	}

	// get static method return an instance in Singleton class
	public ICommonFunctionNode getInstaneMethod() {
		for (INode child : getChildren()) {
			if (child instanceof DefinitionFunctionNode) {
				DefinitionFunctionNode func = (DefinitionFunctionNode) child;
				if (VariableTypeUtils.removeRedundantKeyword(func.getReturnType()).replace("*", "").equals(getName())
						&& func.toString().contains("static")) {
					return func;
				}
			}
			if (child instanceof FunctionNode) {
				FunctionNode func = (FunctionNode) child;
				//todo: check getInstance()
				if (VariableTypeUtils.removeRedundantKeyword(func.getReturnType()).replace("*", "").equals(getName())
						&& (func.isStaticFunction() || func.getName().startsWith("getInstance"))) {
					return func;
				}
			}
		}
		return null;
	}

	public boolean isSingleton() {
		for (INode child : getChildren()) {
			if (child instanceof DefinitionFunctionNode) {
				DefinitionFunctionNode func = (DefinitionFunctionNode) child;
				if (VariableTypeUtils.removeRedundantKeyword(func.getReturnType()).replace("*", "").equals(getName())
						&& func.toString().contains("static")) {
					return true;
				}
			}
			if (child instanceof FunctionNode) {
				FunctionNode func = (FunctionNode) child;
				//todo: check getInstance()
				if (VariableTypeUtils.removeRedundantKeyword(func.getReturnType()).replace("*", "").equals(getName())
						&& (func.isStaticFunction() || func.getName().startsWith("getInstance"))) {
					return true;
				}
			}
		}
		return false;
	}
}
