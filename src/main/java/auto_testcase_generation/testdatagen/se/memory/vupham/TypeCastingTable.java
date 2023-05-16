package auto_testcase_generation.testdatagen.se.memory.vupham;

import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.object.SimpleCfgNode;
import auto_testcase_generation.testdatagen.AbstractAutomatedTestdataGeneration;
import com.dse.parser.object.INode;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;

public class TypeCastingTable {
    public static boolean containCastExp(ICfgNode node) {
        if (node instanceof AbstractAutomatedTestdataGeneration) {
            node = node.getDefaultNode();
        }
        IASTNode ast;
        if (node instanceof ConditionCfgNode || node instanceof SimpleCfgNode) {
            ast = ((ConditionCfgNode) node).getAst();
            if (ast instanceof CPPASTDeclarationStatement) {
                IASTDeclaration declaration = ((CPPASTDeclarationStatement) ast).getDeclaration();
                if (declaration instanceof IASTSimpleDeclaration) {
                    IASTDeclarator[] declarators = ((IASTSimpleDeclaration) declaration).getDeclarators();
                    for (IASTDeclarator declarator : declarators) {
                        IASTInitializer initializer = declarator.getInitializer();
                        if (initializer instanceof IASTEqualsInitializer) {
                            IASTNode[] nodes = initializer.getChildren();
                            for (IASTNode iastNode : nodes) {
                                if (iastNode instanceof IASTCastExpression) {
                                    //TODO
                                }
                            }
                        }
                    }
                }
            } else if (ast instanceof IASTExpressionStatement) {
                IASTExpression expression = ((IASTExpressionStatement) ast).getExpression();
                if (expression instanceof IASTBinaryExpression) {
                    IASTExpression fOperand2 = ((IASTBinaryExpression) expression).getOperand2();
                    if (fOperand2 instanceof IASTCastExpression) {
                        //TODO
                    }
                }
            }
        }

        return false;
    }
}

