package auto_testcase_generation.instrument;

import com.dse.config.AkaConfig;
import com.dse.environment.Environment;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.testcase_execution.DriverConstant;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.dse.project_init.ProjectClone.MAIN_REFACTOR_NAME;
import static com.dse.project_init.ProjectClone.MAIN_REGEX;

/**
 * Instrument function for constructor, destructor, normal function (not include macro function)
 * <p>
 * Extend the previous instrumentation function by adding extra information
 * (e.g., the line of statements) to markers. <br/>
 * Ex: int a = 0; ----instrument-----> mark("line 12:int a = 0"); int a = 0;
 * <p>
 * <br/>
 *
 * @author DucAnh
 */
public class FunctionInstrumentationForAllCoverages extends AbstractFunctionInstrumentation {

    private static final AkaLogger logger = AkaLogger.get(FunctionInstrumentationForAllCoverages.class);

    String mapStaticToGlobalCode = null;

    protected FunctionInstrumentationForAllCoverages() {
        prefix = "aka_static";
    }

    public FunctionInstrumentationForAllCoverages(IASTFunctionDefinition astFunctionNode, IFunctionNode functionNode) {
        this.functionNode = functionNode;

        this.prefix = "aka_static_" + functionNode.getNewType()
                .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE)
                .replaceAll("_+", SpecialCharacter.UNDERSCORE);

        if (astFunctionNode != null && astFunctionNode.getFileLocation() != null) {
            this.astFunctionNode = Utils.disableMacroInFunction(astFunctionNode, functionNode);
        }
    }

    public static void main(String[] args) throws Exception {
        ProjectParser projectParser = new ProjectParser(new File("datatest/giang/src"));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();
        Environment.getInstance().setProjectNode(projectRoot);

        List<INode> nodes = Search.searchNodes(projectRoot, new FunctionNodeCondition(), "test(char,int,int)");
        FunctionNode foo = (FunctionNode) nodes.get(0);
        System.out.println("function = " + foo.getAST().getRawSignature());
//
        FunctionInstrumentationForAllCoverages fnIns = new FunctionInstrumentationForAllCoverages(foo.getAST(), foo);
        String instrument = fnIns.generateInstrumentedFunction();
        System.out.println("instrument = " + instrument);
    }

    private int scope = 0;

    private final VariableList varList = new VariableList();
    private final Map<String, String> staticVarDeclaration = new HashMap<>();

    private final String prefix;

    @Override
    public String generateInstrumentedFunction() {
        try {
            return instrumentUnConstexprFunction();

        } catch (Exception e) {
            e.printStackTrace();
            // return the original function without instrumentation
            return astFunctionNode.getRawSignature();
        }
    }

    private String instrumentUnConstexprFunction() {
        IASTFunctionDefinition astFunctionNode = (IASTFunctionDefinition) this.astFunctionNode;
        int functionOffset = astFunctionNode.getFileLocation().getNodeOffset();
        IASTCompoundStatement astBody = (IASTCompoundStatement) astFunctionNode.getBody();
        int bodyOffset = astBody.getFileLocation().getNodeOffset();
        String prototype = astFunctionNode.getRawSignature().substring(0, bodyOffset - functionOffset);
        prototype = prototype.replaceAll(MAIN_REGEX, MAIN_REFACTOR_NAME);
        String markerBeginFunction = String.format("/* << Aka begin of function %s >> */\n", functionNode.getName());
        String body = parseBlock(astBody, null, "");
        String mapStaticToGlobal = mapStaticToGlobal();

        for (String old : staticVarDeclaration.keySet()) {
            String newDeclaration = staticVarDeclaration.get(old);
            body = body.replace(old, newDeclaration);
        }

        // insert marker at the beginning of the function


        return mapStaticToGlobal + prototype + markerBeginFunction + body;
    }

    public String getMapStaticToGlobalCode() {
        if (mapStaticToGlobalCode == null) {
            mapStaticToGlobalCode = mapStaticToGlobal();
        }
        return mapStaticToGlobalCode;
    }

    private String mapStaticToGlobal() {
        StringBuilder b = new StringBuilder();

        if (STATIC_REFACTOR) {
            List<Node> children = functionNode.getParent().getChildren();
            children = children.stream()
                    .filter(c -> c instanceof VariableNode || c instanceof MacroDefinitionNode)
                    .collect(Collectors.toList());

            boolean isOnCppFile = isOnCppFile();

            for (StaticVariableNode v : functionNode.getStaticVariables()) {
                String declaration = v.getAST().getRawSignature();
                String origin = v.getName();
                String instrument = v.getInstrument();
                declaration = declaration.replaceAll("\\b\\Q" + origin + "\\E\\b", instrument)
                        .replaceAll("^static\\s+", SpecialCharacter.EMPTY)
                        .replaceAll("\\s+static\\s+", " ");

                if (isOnCppFile && !isAssignedToKnownValueVariable(children, v)) {
                    String newDeclaration = declaration.replaceAll("\\s*=\\s*.+", "");
                    //declaration = declaration.replaceAll("\\s*=\\s*[\\S\\s]*", "");
                    String currentInstrument = String.format("/* instrument variable %s */", v.getAST().getRawSignature());
                    staticVarDeclaration.put(currentInstrument, declaration.replaceAll("^[^=]*=", instrument + " = "));
                    declaration = newDeclaration;
                }

                        //.replaceAll("\\s*=\\s*.+", "");
                        //.replaceAll("\\s*=\\s*[\\S\\s]*", "");

                if (!declaration.endsWith(SpecialCharacter.END_OF_STATEMENT))
                    declaration += SpecialCharacter.END_OF_STATEMENT;
                declaration += SpecialCharacter.LINE_BREAK;

                b.append(declaration);
            }
        }

        this.mapStaticToGlobalCode = b.toString();
        return b.toString();
    }

    private boolean isOnCppFile() {
        INode fileHolder = functionNode;
        while (fileHolder.getParent() != null) {
            fileHolder = fileHolder.getParent();
            if (fileHolder instanceof CFileNode) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if an initializer is assigned to a variable that is a known value.
     * <p>
     *     Ex: <br/>
     *     #define ZERO 0 <br/>
     *     const int zero = 0; <br/>
     *
     *     void foo(int d) { <br/>
     *          static int a = ZERO; // => true <br/>
     *          static int b = 0;    // => true <br/>
     *          static int a = d;    // => false; <br/>
     *     }
     * </p>
     *
     * @param possibleValues list of possible values, such as macro, const variable, ...
     * @param v static variable
     * @return true/false.
     */
    private boolean isAssignedToKnownValueVariable(List<Node> possibleValues, StaticVariableNode v) {
        IASTNode ast = v.getAST();

        if (ast instanceof IASTSimpleDeclaration) {
            IASTDeclarator[] declarators =  ((IASTSimpleDeclaration) v.getAST()).getDeclarators();
            for (IASTDeclarator declarator : declarators) {
                IASTEqualsInitializer initializer = (IASTEqualsInitializer) declarator.getInitializer();
                IASTInitializerClause clause = initializer.getInitializerClause();

                if (clause instanceof IASTLiteralExpression)
                    return true;
                else if (clause instanceof IASTIdExpression) {
                    String name = ((IASTIdExpression) clause).getName().toString();
                    for (Node possibleValue : possibleValues) {
                        if (possibleValue instanceof VariableNode) {
                            VariableNode var = (VariableNode) possibleValue;
                            if (var.getName().equals(name)) {
                                return true;
                            }
                        }
                    }
                } else {
                    // TODO: handle more cases
                    // temporary solution: return true
                    return true;
                }
            }
        }

        return false;

    }

    private String parseMemberInits(IASTFunctionDefinition astFunctionNode) {
        StringBuilder builder = new StringBuilder();

        if (astFunctionNode instanceof ICPPASTFunctionDefinition) {
            ICPPASTConstructorChainInitializer[] memberInits =
                    ((ICPPASTFunctionDefinition) astFunctionNode).getMemberInitializers();

            if (memberInits.length > 0) {
                builder.append(" : ");
                for (int i = 0; i < memberInits.length; i++) {
                    builder.append(memberInits[i].getRawSignature());
                    if (i != memberInits.length - 1)
                        builder.append(", ");
                    else
                        builder.append(SpecialCharacter.SPACE);
                }
            }
        }

        return builder.toString();
    }

    protected String addExtraCall(IASTStatement stm, String extra, String margin) {
        if (extra != null)
            extra = putInMark(extra, true);

        if (stm instanceof IASTCompoundStatement)
            return parseBlock((IASTCompoundStatement) stm, extra, margin);
        else {
            String inside = margin + SpecialCharacter.TAB;

            return SpecialCharacter.OPEN_BRACE + SpecialCharacter.LINE_BREAK + inside /*+ inside*/ +
                    parseStatement(stm, inside) + SpecialCharacter.LINE_BREAK + margin +
                    SpecialCharacter.CLOSE_BRACE;
        }
    }

    //fix macroFunction in block
    protected String parseBlock(IASTCompoundStatement block, String extra, String margin) {
        scope++;

        if (block == null) {
            return "";
        }

        StringBuilder b = new StringBuilder(block.getRawSignature());
        String inside = margin + SpecialCharacter.TAB;
        int searchIndex = 0;
        for (IASTStatement stm : block.getStatements()) {
            String content = stm.getRawSignature();
            StringBuilder newContent = new StringBuilder();
            if (stm instanceof IASTProblemHolder) {
                if (!content.equals(")")) {
                    if (content.equals("t")) {
                        continue;
                    }
                    newContent
                            .append("/* Cant instrument this following code */\n")
                            .append(stm.getRawSignature());
                }
            } else {
                newContent
                        .append(inside)
                        .append(parseStatement(stm, inside))
                        .append(SpecialCharacter.LINE_BREAK);
            }
            int index = b.indexOf(content, searchIndex);
            int endStmIndex = index + content.length();
            b.replace(index, endStmIndex, newContent.toString());
            searchIndex = index + newContent.length();
        }

//        if (block.getStatements().length > 0 && !b.toString().contains(SpecialCharacter.END_OF_STATEMENT)) {
//            b.append(SpecialCharacter.END_OF_STATEMENT);
//        }

        varList.removeAtScope(scope);
        scope--;

        return b.toString();
    }

//    protected String parseBlock(IASTCompoundStatement block, String extra, String margin) {
//        scope++;
//
//        if (block == null)
//            return "";
//        StringBuilder b = new StringBuilder("{" + SpecialCharacter.LINE_BREAK);
//        String inside = margin + SpecialCharacter.TAB;
////        if (extra != null)
////            b.append(inside);
//
//        for (IASTStatement stm : block.getStatements()) {
//            String content = stm.getRawSignature();
//            if (stm instanceof IASTProblemHolder) {
//                if (!content.equals(")")) {
//                    // ignore
//                    b.append("/* Cant instrument this following code */\n");
//                    b.append(stm.getRawSignature());
//                }
//            } else {
//                b.append(inside)
//                        .append(parseStatement(stm, inside))
//                        .append(SpecialCharacter.LINE_BREAK);
//            }
//        }
//
//        b.append(margin).append(SpecialCharacter.CLOSE_BRACE);
//
//        if (block.getStatements().length > 0 && !b.toString().contains(SpecialCharacter.END_OF_STATEMENT)) {
//            b.append(SpecialCharacter.END_OF_STATEMENT);
//        }
//
//        varList.removeAtScope(scope);
//        scope--;
//
//        return b.toString();
//    }

    private boolean isAssign(IASTNode astNode) {
        if (astNode instanceof IASTUnaryExpression && ((IASTUnaryExpression) astNode).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
            astNode = ((IASTUnaryExpression) astNode).getOperand();
        }

        if (astNode instanceof IASTBinaryExpression) {
            return ((IASTBinaryExpression) astNode).getOperator() == IASTBinaryExpression.op_assign;
        }

        return false;
    }

    protected String parseStatement(IASTStatement stm, String margin) {
        StringBuilder b = new StringBuilder();

        if (stm instanceof IASTNullStatement) {
            return stm.getRawSignature();
        } else if (stm instanceof IASTCompoundStatement) {
            b.append(parseBlock((IASTCompoundStatement) stm, null, margin));
        } else if (stm instanceof IASTLabelStatement) {
            IASTStatement nestedStm = ((IASTLabelStatement) stm).getNestedStatement();
            String origin = stm.getRawSignature();
            String markNestedStm = parseStatement(nestedStm, SpecialCharacter.EMPTY);
            String markStm = origin.replace(nestedStm.getRawSignature(), markNestedStm);
            b.append(markStm);

        } else if (stm instanceof IASTIfStatement) {
            IASTIfStatement astIf = (IASTIfStatement) stm;
            b.append(astIf.getRawSignature());
            int searchIndex = 0;
            IASTStatement astElse = astIf.getElseClause();
            IASTNode cond = astIf.getConditionExpression();
            IASTNode decla = ((CPPASTIfStatement) stm).getConditionDeclaration();
            StringBuilder newContent = new StringBuilder();
            int index = 0;
            int endStmIndex = 0;
            if (cond != null) {
                index = b.indexOf(cond.getRawSignature(), searchIndex);
                endStmIndex = index + cond.getRawSignature().length();
                newContent
                        .append(putInMark(addContentOfMarkFunction(cond, astFunctionNode, functionPath, true, false), false))
                        .append(" && (").append(createMarkForSubCondition(cond)).append(")");
            } else if (decla != null) {
                index = b.indexOf(decla.getRawSignature(), searchIndex);
                endStmIndex = index + decla.getRawSignature().length();
                newContent.append(putInMark(addContentOfMarkFunction(decla, astFunctionNode, functionPath, true, true), false)).append(";");
                String declaStr = refactorStatic(decla.getRawSignature());
                newContent.append(declaStr);
            }
            b.replace(index, endStmIndex, newContent.toString());
            searchIndex = index + newContent.toString().length();
            newContent = new StringBuilder();

            IASTStatement astThen = astIf.getThenClause();
            index = b.indexOf(astThen.getRawSignature(), searchIndex);
            endStmIndex = index + astThen.getRawSignature().length();

            // empty body
            if (astThen instanceof IASTCompoundStatement && ((IASTCompoundStatement) astThen).getStatements().length == 0)
                newContent.append("{")
                        .append(putInMark(addAdditionContent(cond, astFunctionNode, functionPath, "+"), true))
                        .append("}");
            else
                newContent.append(addExtraCall(astThen, "", margin));

            b.replace(index, endStmIndex, newContent.toString());
            searchIndex = index + newContent.toString().length();
            newContent = new StringBuilder();

            if (astElse != null) {
                index = b.indexOf(astElse.getRawSignature(), searchIndex);
                endStmIndex = index + astElse.getRawSignature().length();
//                b.append(SpecialCharacter.LINE_BREAK).append(margin).append("else ");
                // empty body
                if (astElse instanceof IASTCompoundStatement && ((IASTCompoundStatement) astElse).getStatements().length == 0)
                    newContent.append("{")
                            .append(putInMark(addAdditionContent(cond, astFunctionNode, functionPath, "-"), true))
                            .append("}");
                else
                    newContent.append(addExtraCall(astElse, "", margin));
            } else {
                index = searchIndex;
                endStmIndex = index;
                newContent.append(SpecialCharacter.LINE_BREAK).append(margin).append("else ");
                newContent.append("{")
                        .append(putInMark(addAdditionContent(cond, astFunctionNode, functionPath, "-"), true))
                        .append("}");
            }
            b.replace(index, endStmIndex, newContent.toString());

        } else if (stm instanceof IASTForStatement) {
            IASTForStatement astFor = (IASTForStatement) stm;

            // Add marker for initialization
            IASTStatement astInit = astFor.getInitializerStatement();
            if (!(astInit instanceof IASTNullStatement)) {
                b.append(putInMark(addContentOfMarkFunction(astInit, astFunctionNode, functionPath, false, false), true));
            }

            String init = refactorStatic(getShortenContent(astInit));
            b.append("for (").append(init);
            // Add marker for condition
            IASTExpression astCond = (IASTExpression) Utils.shortenAstNode(astFor.getConditionExpression());
            if (astCond != null) {
                //b.append(SpecialCharacter.LINE_BREAK).append("\t\t\t");
                b.append(putInMark(addContentOfMarkFunction(astCond, astFunctionNode, functionPath, true, false), false)).append(" && ")
                        .append(createMarkForSubCondition(astCond)).append(";");
            } else
                b.append(";");

            // Add marker for increment
            IASTExpression astIter = astFor.getIterationExpression();
            if (astIter != null) {
                String iter = refactorStatic(getShortenContent(astIter));
                //b.append(SpecialCharacter.LINE_BREAK).append("\t\t\t");
                b.append("({")
                        .append(putInMark(addContentOfMarkFunction(astIter, astFunctionNode, functionPath, false, false), false))
                        .append(";")
                        .append(iter)
                        .append(";})");
            }
            b.append(") ");

            // For loop: no condition
            if (astCond == null)
                b.append(parseStatement(astFor.getBody(), margin));
            else
                b.append(addExtraCall(astFor.getBody(), "", margin));

        } else if (stm instanceof IASTWhileStatement) {
            IASTWhileStatement astWhile = (IASTWhileStatement) stm;
            IASTNode cond = astWhile.getCondition();

            b.append("while (")
                    .append(putInMark(addContentOfMarkFunction(cond, astFunctionNode, functionPath, true, false), false))
                    .append(" && (").append(createMarkForSubCondition(cond)).append(")) ");

            b.append(addExtraCall(astWhile.getBody(), "", margin));

        } else if (stm instanceof IASTDoStatement) {
            IASTDoStatement astDo = (IASTDoStatement) stm;
            IASTNode cond = astDo.getCondition();

            boolean isAssign = isAssign(cond);

            b.append("do ").append(addExtraCall(astDo.getBody(), "", margin)).append(SpecialCharacter.LINE_BREAK)
                    .append(margin).append("while (");

            if (!isAssign) {
                b.append(putInMark(addContentOfMarkFunction(cond, astFunctionNode, functionPath, true, false), false))
                        .append(" && (");
            }

            b.append(createMarkForSubCondition(cond));

            if (!isAssign) {
                b.append(")");
            }

            b.append(");");

        } else if (stm instanceof ICPPASTTryBlockStatement) {
            ICPPASTTryBlockStatement astTry = (ICPPASTTryBlockStatement) stm;

            b.append(DriverConstant.MARK + "(\"start try;\");");

            b.append(SpecialCharacter.LINE_BREAK).append(margin).append("try ");
            b.append(addExtraCall(astTry.getTryBody(), null, margin));

            for (ICPPASTCatchHandler catcher : astTry.getCatchHandlers()) {
                b.append(SpecialCharacter.LINE_BREAK).append(margin).append("catch (");

                String exception = catcher.isCatchAll() ? "..." : getShortenContent(catcher.getDeclaration());
                exception = refactorStatic(exception);
                b.append(exception).append(") ");

                b.append(addExtraCall(catcher.getCatchBody(), exception, margin));
            }

            b.append(SpecialCharacter.LINE_BREAK).append(margin).append(DriverConstant.MARK + "(\"end catch;\");");

        } else if (stm instanceof IASTBreakStatement || stm instanceof IASTContinueStatement) {
            b.append(putInMark(addContentOfMarkFunction(stm, astFunctionNode, functionPath, false, false), true));
            String content = refactorStatic(getShortenContent(stm));
            b.append(content);

        } else if (stm instanceof IASTReturnStatement) {
            b.append(putInMark(addContentOfMarkFunction(stm, astFunctionNode, functionPath, false, false), true));
            String content = refactorStatic(getShortenContent(stm));
            b.append(content);
        } else if (AkaConfig.IS_SUPPORT_SWITCH_CASE && stm instanceof IASTSwitchStatement) {
            IASTNode ce = ((IASTSwitchStatement) stm).getControllerExpression();
            String content = getShortenContent(ce);
            b.append(putInMark(addContentOfMarkFunction(ce, astFunctionNode, functionPath, false, false), true))
                    .append("switch(")
                    .append(content)
                    .append(")")
                    .append(addExtraCall(((IASTSwitchStatement) stm).getBody(), "", margin));
        } else if (stm instanceof IASTCaseStatement) {
            String content = stm.getRawSignature();
            IASTNode buffer = stm.getParent();
            while (!(buffer instanceof IASTSwitchStatement)) {
                buffer = buffer.getParent();
            }
            String switchParam = getShortenContent(((IASTSwitchStatement) buffer).getControllerExpression());
            IASTNode thisCase = ((IASTCaseStatement) stm).getExpression();
            b.append(content)
                    .append(" if(")
                    .append(switchParam).append(" == ").append(thisCase.getRawSignature())
                    .append(")")
                    .append(putInMark(addContentOfMarkFunction(stm, astFunctionNode, functionPath, true, true), true));
        } else if (stm instanceof IASTDefaultStatement) {
            b.append(getShortenContent(stm));

            IASTNode buffer = stm.getParent();
            while (!(buffer instanceof IASTSwitchStatement)) {
                buffer = buffer.getParent();
            }
            IASTSwitchStatement switchStm = (IASTSwitchStatement) buffer;
            String controlValue = switchStm.getControllerExpression().getRawSignature();
            List<String> caseValues = new ArrayList<>();
            if (switchStm.getBody() instanceof IASTCompoundStatement) {
                IASTStatement[] statements = ((IASTCompoundStatement) switchStm.getBody()).getStatements();
                for (IASTStatement statement : statements) {
                    if (statement instanceof IASTCaseStatement) {
                        String caseValue = ((IASTCaseStatement) statement).getExpression().getRawSignature();
                        caseValues.add(caseValue);
                    }
                }
            }
            String defaultContent = Utils.generateContentForDefaultCase(controlValue, caseValues);

            b.append(" if(").append(defaultContent).append(")")
                    .append(putInMark(addContentOfMarkFunction(stm, astFunctionNode, functionPath, true, true), true));

        } else {
            String raw = getShortenContent(stm);
            boolean isStatic = false;

            if (stm instanceof IASTDeclarationStatement) {
                IASTDeclaration declaration = ((IASTDeclarationStatement) stm).getDeclaration();
                if (declaration instanceof IASTSimpleDeclaration) {
                    IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
                    IASTDeclSpecifier declSpec = simpleDeclaration.getDeclSpecifier();
                    isStatic = declSpec.getStorageClass() == IASTDeclSpecifier.sc_static && !declSpec.isConst();

                    for (IASTDeclarator declarator : simpleDeclaration.getDeclarators()) {
                        String name = declarator.getName().getRawSignature();
                        VariableEntry e = new VariableEntry(name, scope);
                        e.setStatic(isStatic);
                        varList.add(e);
                    }
                }
            }

            // only run if the stm is declaration and variable is static
            if (isStatic && STATIC_REFACTOR) {
                raw = String.format("/* instrument variable %s */", raw);
            } else {
                raw = refactorStatic(raw);
            }

            b.append(putInMark(addContentOfMarkFunction(stm, astFunctionNode, functionPath, false, false), true));// add markers
            b.append(raw);
        }

        return b.toString();
    }

    public static final boolean STATIC_REFACTOR = true;

    private String refactorStatic(String stm) {
        if (STATIC_REFACTOR) {
            List<VariableEntry> statics = varList.stream()
                    .filter(v -> v.isStatic)
                    .collect(Collectors.toList());

            for (VariableEntry v : statics) {
                if (varList.getNearest(v.getName()) == v) {
                    String instrument = prefix + v.getName();
                    stm = stm.replaceAll("\\b\\Q" + v.getName() + "\\E\\b", instrument);
                }
            }
        }

        return stm;
    }

    protected String addAdditionContent(IASTNode node, IASTNode astFunctionNode, String functionPath, String flag) {
        if (node == null || node.getFileLocation() == null)
            return "";

        String lineInSourcecodeFile = flag + node.getFileLocation().getStartingLineNumber() + flag;
        String startOffsetInSourcecodeFile = flag + node.getFileLocation().getNodeOffset() + flag;
        String endOffsetInSourcecodeFile = flag + node.getFileLocation().getNodeOffset() + node.getFileLocation().getNodeLength() + flag;

        String lineInFunction = flag + (node.getFileLocation().getStartingLineNumber() - astFunctionNode.getFileLocation().getStartingLineNumber()) + flag;
        String startOffsetInFunction = flag /*+ (node.getFileLocation().getNodeOffset() - astFunctionNode.getFileLocation().getNodeOffset()) + flag*/;
        String endOffsetInFunction = flag + (node.getFileLocation().getNodeOffset() + node.getFileLocation().getNodeLength() - astFunctionNode.getFileLocation().getNodeOffset()) + flag;

        String relativePath = PathUtils.toRelative(functionPath);

        String marker =
                // paramater
                LINE_NUMBER_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + lineInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        START_OFFSET_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + startOffsetInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        END_OFFSET_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + endOffsetInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        LINE_NUMBER_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + lineInFunction + DELIMITER_BETWEEN_PROPERTIES
                        // paramater
                        + START_OFFSET_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + startOffsetInFunction + DELIMITER_BETWEEN_PROPERTIES
                        // parameter
                        + END_OFFSET_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + endOffsetInFunction;

        marker += DELIMITER_BETWEEN_PROPERTIES + IS_NORMAL_STATEMENT + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "true";

        if (functionPath != null && functionPath.length() > 0)
            marker += DELIMITER_BETWEEN_PROPERTIES + FUNCTION_ADDRESS + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + Utils.doubleNormalizePath(relativePath);

        return marker;
    }

    private String createMarkForSubCondition(IASTNode astCon) {
        StringBuilder tempStr = new StringBuilder();
        astCon = Utils.shortenAstNode(astCon);
        if (isCondition(astCon)) {
            if (astCon instanceof IASTBinaryExpression) {
                int operator = ((IASTBinaryExpression) astCon).getOperator();

                switch (operator) {
                    case IASTBinaryExpression.op_greaterEqual:
                    case IASTBinaryExpression.op_greaterThan:
                    case IASTBinaryExpression.op_lessEqual:
                    case IASTBinaryExpression.op_lessThan:
                        tempStr.append("	(").append(astCon)
                                .append("&&").append(Utils.shortenAstNode(astCon).getRawSignature()).append(")");
                        break;

                    case IASTBinaryExpression.op_logicalAnd:
                    case IASTBinaryExpression.op_logicalOr:
                        IASTExpression operand1 = ((IASTBinaryExpression) astCon).getOperand1();
                        IASTExpression operand2 = ((IASTBinaryExpression) astCon).getOperand2();

                        tempStr.append("(").append(createMarkForSubCondition(operand1)).append(")")
                                .append(operator == IASTBinaryExpression.op_logicalAnd ? "	&&" : "	||").append("(").append(createMarkForSubCondition(operand2)).append(")");
                        break;
                }
            } else {
                // unary expression
                tempStr.append(DriverConstant.MARK + "(\"")
                        .append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, true)).
                        append("\")&&").
                        append(astCon.getRawSignature());
            }
        } else {
            if (isAssign(astCon)) {
                IASTBinaryExpression astBin = (IASTBinaryExpression) astCon;
                String operand1 = astBin.getOperand1().getRawSignature();
                String operand2 = astBin.getOperand2().getRawSignature();
                tempStr.append(operand1)
                        .append("=")
                        .append("(")
                        .append(operand2)
                        .append(") && ")
                        .append(DriverConstant.MARK + "(\"")
                        .append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, true)).
                        append("\")");

            } else {
                tempStr.append(DriverConstant.MARK + "(\"")
                        .append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, true)).
                        append("\")&&").
                        append(astCon.getRawSignature());
            }
        }

        return refactorStatic(tempStr.toString());
    }

    private static class VariableList extends ArrayList<VariableEntry> {

        public void removeAtScope(int scope) {
            removeIf(v -> v.getScope() == scope);
        }

        public VariableEntry getNearest(String name) {
            return stream()
                    .sorted(Comparator.reverseOrder())
                    .filter(v -> v.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static class VariableEntry implements Comparable<VariableEntry> {

        private final String name;

        private final int scope;

        private boolean isStatic;

        public VariableEntry(String name, int scope) {
            this.scope = scope;
            this.name = name;
        }

        public void setStatic(boolean aStatic) {
            isStatic = aStatic;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public String getName() {
            return name;
        }

        public int getScope() {
            return scope;
        }

        @Override
        public int compareTo(VariableEntry o) {
            return Integer.compare(scope, o.scope);
        }
    }

}
