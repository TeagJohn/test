package com.dse.thread.task;

import auto_testcase_generation.testdatagen.*;
import com.dse.config.IFunctionConfig;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestPrototype;
import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This thread takes responsibility for generating test data for a function
 */
public class GenerateTestdataTask extends AbstractAkaTask<List<TestCase>> {

    //    public static final String BOUNDARY_GEN = "BOUNDARY";
//    public static final String BOUNDARY_GEN2 = "BOUNDARY2";
//    public static final String BOUNDARY_MIDMINMAX = "MIDMINMAX";
    final static AkaLogger logger = AkaLogger.get(GenerateTestdataTask.class);
    private TestCasesTreeItem treeNodeInTestcaseNavigator;
    private ICommonFunctionNode function;
    // a prototype of template function
    private TestPrototype selectedPrototype;
    private final boolean showReport;
    private List<AutoGeneratedTestCaseExecTask> testCaseExecTask = new ArrayList<>();

    public GenerateTestdataTask(boolean showReport) {
        this.showReport = showReport;
    }

    private boolean haveIteration(IASTStatement stm) {
        if (stm instanceof IASTWhileStatement
                || stm instanceof IASTForStatement
                || stm instanceof IASTDoStatement) {
            return true;
        } else if (stm instanceof IASTIfStatement) {
            IASTIfStatement ifStm = (IASTIfStatement) stm;
            IASTStatement thenStm = ifStm.getThenClause();
            IASTStatement elseStm = ifStm.getElseClause();
            return haveIteration(thenStm) || haveIteration(elseStm);
        } else if (stm instanceof IASTSwitchStatement) {
            return haveIteration(((IASTSwitchStatement) stm).getBody());
        } else if (stm instanceof IASTCompoundStatement) {
            for (IASTStatement childStm : ((IASTCompoundStatement) stm).getStatements()) {
                if (haveIteration(childStm)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected List<TestCase> call() {
        List<TestCase> testCases = null;
        try {
            switch (function.getFunctionConfig().getTestdataGenStrategy()) {
                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BEST_TIME: {
                    boolean haveIteration = false;
                    if (function instanceof IFunctionNode) {
                        IASTStatement body = (((IFunctionNode) function).getAST()).getBody();
                        haveIteration = haveIteration(body);
                    } else if (function instanceof MacroFunctionNode) {
                        IFunctionNode corresponding = ((MacroFunctionNode) function).getCorrespondingFunctionNode();
                        IASTStatement body = corresponding.getAST().getBody();
                        haveIteration = haveIteration(body);
                    }

                    if (haveIteration) {
                        logger.debug("Concolic CFDS test data generation");
                        CFDSAutomatedTestdataGeneration gen = new CFDSAutomatedTestdataGeneration(function, Environment.getInstance().getTypeofCoverage());
                        gen.setShowReport(showReport);
                        gen.getAllPrototypes().add(this.selectedPrototype);
                        logger.debug("Generate test data automatically for function " + function.getSimpleName());
                        gen.setFunctionExecThread(this);
                        gen.generateTestdata(function);
                        testCases = gen.getTestCases();
                    } else {
                        logger.debug("Concolic DFS test data generation");
                        DFSAutomatedTestdataGeneration gen = new DFSAutomatedTestdataGeneration(function, Environment.getInstance().getTypeofCoverage());
                        gen.setShowReport(showReport);
                        gen.getAllPrototypes().add(this.selectedPrototype);
                        logger.debug("Generate test data automatically for function " + function.getSimpleName());
                        gen.setFunctionExecThread(this);
                        gen.generateTestdata(function);
                        testCases = gen.getTestCases();
                    }

                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BEST_COVERAGE: {
                    logger.debug("Concolic CFDS test data generation");
                    CFDSAutomatedTestdataGeneration gen = new CFDSAutomatedTestdataGeneration(function, Environment.getInstance().getTypeofCoverage());
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.RANDOM: {
                    logger.debug("Random test data generation");
                    RandomAutomatedTestdataGeneration gen = new RandomAutomatedTestdataGeneration(function);
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_DIJKSTRA: {
                    logger.debug("Concolic Dijkstra test data generation");
                    DirectedAutomatedTestdataGeneration gen = new DirectedAutomatedTestdataGeneration(function, Environment.getInstance().getTypeofCoverage());
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BASIS_PATH_TESTING: {
                    logger.debug("Basis Path test data generation");
                    BasisPathTestdataGeneration gen = new BasisPathTestdataGeneration(function, Environment.getInstance().getTypeofCoverage());
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_CFDS: {
                    logger.debug("Concolic CFDS test data generation");
                    CFDSAutomatedTestdataGeneration gen = new CFDSAutomatedTestdataGeneration(function, Environment.getInstance().getTypeofCoverage());
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_DFS: {
                    logger.debug("Concolic DFS test data generation");
                    DFSAutomatedTestdataGeneration gen = new DFSAutomatedTestdataGeneration(function, Environment.getInstance().getTypeofCoverage());
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND: {
                    logger.debug("Generate boundary test data for function " + function.getSimpleName());
                    PopularBoundaryTestDataGeneration gen = new PopularBoundaryTestDataGeneration(
                            function, Environment.getInstance().getTypeofCoverage());

                    gen.setShowReport(showReport);
                    gen.setFunctionExecThread(this);
                    gen.setType(IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA: {
                    logger.debug("Generate boundary test data for function " + function.getSimpleName());
                    PopularBoundaryTestDataGeneration gen = new PopularBoundaryTestDataGeneration(
                            function, Environment.getInstance().getTypeofCoverage());

                    gen.setShowReport(showReport);
                    gen.setFunctionExecThread(this);
                    gen.setType(IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA_BOUNDARYCONDITION: {
                    logger.debug("Boundary test data generation");
                    BoundaryConditionTestDataGeneration gen = new BoundaryConditionTestDataGeneration(function, Environment.getInstance().getTypeofCoverage());
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.ROBUSTNESS: {
                    logger.debug("Generate boundary test data for function " + function.getSimpleName());
                    PopularBoundaryTestDataGeneration gen = new PopularBoundaryTestDataGeneration(
                            function, Environment.getInstance().getTypeofCoverage());

                    gen.setShowReport(showReport);
                    gen.setFunctionExecThread(this);
                    gen.setType(IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.ROBUSTNESS);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.WHITEBOX_BOUNDARY: {
                    logger.debug("Boundary test data generation");
                    WhiteboxBoundaryTestDataGeneration gen = new WhiteboxBoundaryTestDataGeneration(function, Environment.getInstance().getTypeofCoverage());
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }

                case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.MID_MIN_MAX: {
                    logger.debug("Data type boundary test data generation");
                    MidMinMaxTestGen gen = new MidMinMaxTestGen(function, Environment.getInstance().getTypeofCoverage());
                    gen.setShowReport(showReport);
                    gen.getAllPrototypes().add(this.selectedPrototype);
                    logger.debug("Generate test data automatically for function " + function.getSimpleName());
                    gen.setFunctionExecThread(this);
                    gen.generateTestdata(function);
                    testCases = gen.getTestCases();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int nTestcases = TestCaseManager.getTestCasesByFunction(function).size();
        UILogger.getUiLogger().info("[DONE] Generate test cases for function " + function.getName() + ". Total of test cases in this function: " + nTestcases);

        return testCases;
    }

    public boolean isDoneAll() {
        if (testCaseExecTask.size() >= 1)
            for (AutoGeneratedTestCaseExecTask execTask : testCaseExecTask)
                if (execTask.isRunning())
                    return false;
        return true;
    }

    public String getStatus() {
        int runningCount = 0;
        for (AutoGeneratedTestCaseExecTask execTask : testCaseExecTask)
            if (execTask.isDone())
                runningCount++;
        return runningCount + "/" + testCaseExecTask.size();
    }

    public boolean isStillRunning() {
        if (testCaseExecTask.size() >= 1)
            for (AutoGeneratedTestCaseExecTask execTask : testCaseExecTask)
                if (execTask.isRunning())
                    return true;
        return false;
    }

    public TestCasesTreeItem getTreeNodeInTestcaseNavigator() {
        return treeNodeInTestcaseNavigator;
    }

    public void setTreeNodeInTestcaseNavigator(TestCasesTreeItem treeNodeInTestcaseNavigator) {
        this.treeNodeInTestcaseNavigator = treeNodeInTestcaseNavigator;
    }

    public ICommonFunctionNode getFunction() {
        return function;
    }

    public void setFunction(ICommonFunctionNode function) {
        this.function = function;
    }

    public List<AutoGeneratedTestCaseExecTask> getTestCaseExecTask() {
        return testCaseExecTask;
    }

    public void setTestCaseExecTask(List<AutoGeneratedTestCaseExecTask> testCaseExecTask) {
        this.testCaseExecTask = testCaseExecTask;
    }

    public TestPrototype getSelectedPrototype() {
        return selectedPrototype;
    }

    public void setSelectedPrototype(TestPrototype selectedPrototype) {
        this.selectedPrototype = selectedPrototype;
    }
}
