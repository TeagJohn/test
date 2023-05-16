package auto_testcase_generation.cfg.object;

import auto_testcase_generation.cfg.ICFG;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent normal statements (not flag statement, scope statement, etc.)
 *
 * @author ducanh
 */
public abstract class NormalCfgNode extends CfgNode {

	private IASTNode ast;
	private int startOffset = -1;
	private int lineOffset = -1;

	private Map<IASTFunctionCallExpression, ICFG> subCFGs;

	public NormalCfgNode(){}

	public NormalCfgNode(IASTNode node) {
		ast = node;
		setContent(ast.getRawSignature());
		setAstLocation(node.getFileLocation());
		subCFGs = new HashMap<>();
	}

//	@Override
//	public int getId() {
//		return ast.getFileLocation().getNodeOffset() * -1 - 1;
//	}

	public int getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}

	public int getLineOffset() {
		return lineOffset;
	}

	public void setLineOffset(int lineOffset) {
		this.lineOffset = lineOffset;
	}

	public IASTNode getAst() {
		return ast;
	}

	public void setAst(IASTNode ast) {
		if (ast != null) {
			this.ast = ast;
			setStartOffset(ast.getFileLocation().getNodeOffset());
			setLineOffset(ast.getFileLocation().getStartingLineNumber());
			setContent(ast.getRawSignature());
		}
	}

	@Override
	public String toString() {
		if (ast != null) {
			return ast.getRawSignature();
		} else
			return getContent();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		NormalCfgNode cloneNode = (NormalCfgNode) super.clone();
		cloneNode.setAst(ast);
		return cloneNode;
	}

	public void setSubCFGs(Map<IASTFunctionCallExpression, ICFG> subCFGs) {
		this.subCFGs = subCFGs;
	}

	public Map<IASTFunctionCallExpression, ICFG> getSubCFGs() {
		return subCFGs;
	}
}
