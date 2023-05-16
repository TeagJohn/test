package com.dse.regression.cia;

import auto_testcase_generation.cfg.CFG;
import auto_testcase_generation.cfg.CFGImporter;
import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.*;
import auto_testcase_generation.instrument.FunctionInstrumentationForStatementvsBranch_Markerv2;
import auto_testcase_generation.pairwise.Testcase;
import auto_testcase_generation.testdata.object.StatementInTestpath_Mark;
import auto_testcase_generation.testdata.object.TestpathString_Marker;
import auto_testcase_generation.testdatagen.CFDSAutomatedTestdataGeneration;
import auto_testcase_generation.testdatagen.ConcolicAutomatedTestdataGeneration;
import auto_testcase_generation.testdatagen.DFSAutomatedTestdataGeneration;
import auto_testcase_generation.testdatagen.IAutomatedTestdataGeneration;
import auto_testcase_generation.testdatagen.coverage.CFGUpdaterv2;
import com.dse.config.AkaConfig;
import com.dse.coverage.CoverageDataObject;
import com.dse.coverage.CoverageManager;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.guifx_v3.controllers.TestCasesExecutionTabController;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.helps.TCExecutionDetailLogger;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.TestCaseExecutionDataNode;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.logger.AkaLogger;
import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.testcase_execution.TestCaseExecutionThread;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.util.CFGUtils;
import com.dse.util.PathUtils;
import com.dse.util.TestPathUtils;
import com.dse.util.Utils;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WaveCIA {

    public static List<String> paths_Folder_Demo = Arrays.asList(
            "/home/teag-john/Documents/regression_paper/test/demo/regression_test",
            "/home/teag-john/Documents/regression_paper/test/demo/regression_test/lib");
    private final AkaLogger logger = AkaLogger.get(WaveCIA.class);

    private static WaveCIA waveCIA  = null;

    private Environment backupEnvironment = null;
    private Environment newEnvironment = null;

    private List<INode> modifiedFunctionNodes = new ArrayList<>();

    private List<INode> addedNodes = new ArrayList<>();

    private List<INode> impactedFunctionNode = new ArrayList<>();

    private List<TestCaseExecutionThread> multiThread = new ArrayList<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static int LEVEL = 2;

    public static WaveCIA getWaveCIA() {
        if (waveCIA == null) {
            waveCIA = new WaveCIA();
            waveCIA.reset();
        }
        return waveCIA;
    }

    public void reset() {
        newEnvironment = Environment.getInstance();
        backupEnvironment = Environment.getBackupEnvironment();
        modifiedFunctionNodes = new ArrayList<>();
        impactedFunctionNode = new ArrayList<>();
        multiThread.clear();
    }

    public void refreshProject() {
        ProjectParser projectParser = new ProjectParser(newEnvironment.getProjectNode()); // truyền vào 1 ProjectNode là root cây cấu trúc của Environment
        projectParser.setSizeOfDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setGenerateSetterandGetter_enabled(true);
        projectParser.setParentReconstructor_enabled(true);
        projectParser.setFuncCallDependencyGeneration_enabled(true);
        projectParser.setGlobalVarDependencyGeneration_enabled(true);
        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);
        projectParser.setTypeDependency_enable(true);
        projectParser.getRootTree();

        TestCaseManager.clearMaps();
        TestCaseManager.initializeMaps();
    }

    public void runRegression() {
        //init
        TreeItem<ITestcaseNode> item = TestCasesNavigatorController.getInstance().getTestCasesNavigator().getRoot();
        if (item == null) logger.debug("Tree item is null, this will cause null pointer exception after");
        TestCasesTreeItem rootItem = (item instanceof TestCasesTreeItem) ? (TestCasesTreeItem) item : null;
        if (rootItem != null)
            rootItem.loadChildren(true);

        executeTestcases();
        generateNewTestData();
    }

    public void executeTestcases() {
        //execute and generate new test data for modified function
        for (INode node : modifiedFunctionNodes) {
            if (node instanceof FunctionNode) {
                logger.debug("Executing modified function " + node.getName());
                executeTestCaseInFunction((IFunctionNode) node);
            }
        }

        for (INode node : impactedFunctionNode) {
            if (!modifiedFunctionNodes.contains(node) && node instanceof FunctionNode) {
                logger.debug("Executing impacted function " + node.getName());
                executeTestCaseInFunction((IFunctionNode) node);
            }
        }
    }

    public void generateNewTestData() {
        for (INode node : modifiedFunctionNodes) {
            if (node instanceof FunctionNode) {
                try {
                    logger.debug("Generate new testcase for modified function " + node.getName());
                    generateNewTestData((IFunctionNode) node);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void executeTestCaseInFunction(IFunctionNode functionNode) {
        FunctionNode realFunctionNode = null;
        try {
            realFunctionNode = (FunctionNode) UIController.searchFunctionNodeByPath(functionNode.getAbsolutePath());
            logger.debug("Find function node by path " + realFunctionNode.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Can not find the function node");
            e.printStackTrace();
        }
        if (realFunctionNode == null) return;
        List<TestCase> listTestcases = TestCaseManager.getTestCasesByFunction(realFunctionNode);
        for (TestCase testCase : listTestcases) {
            try {
                CFG cfg = (CFG) CFGUtils.createCFG(functionNode, Environment.getInstance().getTypeofCoverage());
                CFG oldCFG = new CFGImporter().importCFGByFile(CFGUtils.createCFGFunctionPath((FunctionNode) functionNode));
                /* re-execute the testcases not affected by changes*/
                if (!isTestcaseAffectedByCFGChanges(testCase, cfg, oldCFG)) {
                    testCase.deleteOldDataExceptValue();
                    TestCaseExecutionThread task = new TestCaseExecutionThread(testCase);
                    task.setExecutionMode(com.dse.testcase_execution.ITestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE);

                    multiThread.add(task);
                    task.setOnPreSucceededEvent(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
//                    loadingPopup.close();
                            logger.debug("Execute testcase " + testCase.getName() + "successfully.");
                        }
                    });
//            AkaThreadManager.executedTestcaseThreadPool.execute(task);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (TestCaseExecutionThread task : multiThread)
            executorService.submit(task);
        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
            UIController.viewCoverageOfMultipleTestcases("Summary Coverage Report", listTestcases);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void generateNewTestData(IFunctionNode functionNode) throws Exception {
        FunctionNode realFunctionNode = null;
        try {
            realFunctionNode = (FunctionNode) UIController.searchFunctionNodeByPath(functionNode.getAbsolutePath());
            logger.debug("Find function node by path " + realFunctionNode.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Can not find the function node");
            e.printStackTrace();
        }
        if (realFunctionNode == null) return;
        ConcolicAutomatedTestdataGeneration dfs = new DFSAutomatedTestdataGeneration(realFunctionNode, Environment.getInstance().getTypeofCoverage());
        dfs.setShowReport(true);
//        dfs.generateTestdata(realFunctionNode);
//        List<TestCase> directedTestcases = dfs.getTestCases();

        List<TestCase> testCases = TestCaseManager.getTestCasesByFunction(realFunctionNode);
        List<String> generatedTestcases = new ArrayList<>();
        String coverageType = Environment.getInstance().getTypeofCoverage();
        List<String> analyzedTestpathMd5 = new ArrayList<>();

        // refresh executions
        TCExecutionDetailLogger.clearExecutions(functionNode);
        MDIWindowController.getMDIWindowController().removeTestCasesExecutionTabByFunction(functionNode);
        TCExecutionDetailLogger.initFunctionExecutions(functionNode);

        if (testCases.isEmpty()) {
            dfs.generateTestdata(realFunctionNode);
            dfs.onGenerateSuccess(true);
        }
        else {
//            dfs.generateDirectly(testCases, realFunctionNode, coverageType, generatedTestcases, analyzedTestpathMd5);
            dfs.setTestCases(testCases);
            directed(dfs, dfs.getTestCases(), realFunctionNode, coverageType, generatedTestcases, analyzedTestpathMd5);
            dfs.onGenerateSuccess(true);
        }
    }

    public void directed(ConcolicAutomatedTestdataGeneration concolicAutomatedTestdataGeneration
            , List<TestCase> testCases, FunctionNode fn, String coverageType
            , List<String> generatedTestcases, List<String> analyzedTestpathMd5) {
        /*
         * Generate directly
         */
        if (new File(new AkaConfig().fromJson().getZ3Path()).exists()) {
            long MAX_TESTCASES = fn.getFunctionConfig().getTheMaximumNumberOfIterations();
            int time=0;
            for (long i = 0; i < MAX_TESTCASES;) {
                logger.debug("Iterate " + i + " directly");
                logger.debug("Num of the existing test cases up to now = " + testCases.size());
                int iterationStatus = concolicAutomatedTestdataGeneration.generateDirectly(testCases, fn, coverageType, generatedTestcases, analyzedTestpathMd5);
                time++;

                switch (iterationStatus) {
                    case IAutomatedTestdataGeneration.AUTOGEN_STATUS.OTHER_ERRORS: {
                        logger.debug("Unexpected error when generating test case. Nove to the next iteration.");
                        i++;
                        break;
                    }
                    case IAutomatedTestdataGeneration.AUTOGEN_STATUS.NOT_ABLE_TO_GENERATE_CFG: {
                        logger.debug("Unable to generate cfg of function " + fn.getAbsolutePath() + ". Move to the next iteration.");
                        i++;
                        break;
                    }
                    case IAutomatedTestdataGeneration.AUTOGEN_STATUS.NO_SHORTEST_PATH: {
                        logger.debug("Not path to discover. Exit.");
                        i++;
                        break;
                    }
                    case IAutomatedTestdataGeneration.AUTOGEN_STATUS.FOUND_DUPLICATED_TESTPATH: {
                        logger.debug("The generated test path existed before. Move to the next iteration.");
                        i++;
                        break;
                    }
                    case IAutomatedTestdataGeneration.AUTOGEN_STATUS.SOLVING_STATUS.FOUND_DUPLICATED_TESTCASE: {
                        logger.debug("The generated test case existed before. Move to the next iteration.");
                        i++;
                        break;
                    }
                    case IAutomatedTestdataGeneration.AUTOGEN_STATUS.EXECUTION.COULD_NOT_CONSTRUCT_TREE_FROM_TESTCASE: {
                        logger.debug("Could not construct tree from generated test case. Move to the next iteration.");
                        i++;
                        break;
                    }
                    case IAutomatedTestdataGeneration.AUTOGEN_STATUS.EXECUTION.COUND_NOT_EXECUTE_TESTCASE: {
                        logger.debug("Could not execute test case. Move to the text iteration.");
                        i++;
                        break;
                    }
                    case IAutomatedTestdataGeneration.AUTOGEN_STATUS.EXECUTION.BE_ABLE_TO_EXECUTE_TESTCASE: {
                        logger.debug("Be able to execute test case. Save. Move to the text iteration.");
                        TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();
                        break;
                    }
                }
            }
            logger.debug("TIME: "+ time);
        }
    }

    protected int executeTestCase(TestCase testCase, TestcaseExecution executor, String additionalHeaders) {
        try {
            testCase.setStatus(TestCase.STATUS_EXECUTING);
            TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();

            testCase.setAdditionalHeaders(additionalHeaders);

            String coverage = Environment.getInstance().getTypeofCoverage();

            // add and initialize corresponding TestCaseExecutionDataNode
            ICommonFunctionNode functionNode = testCase.getFunctionNode();
            TCExecutionDetailLogger.addTestCase(functionNode, testCase);
            TestCaseExecutionDataNode executionDataNode = TCExecutionDetailLogger.getExecutionDataNodeByTestCase(testCase);
            TCExecutionDetailLogger.logDetailOfTestCase(testCase, "Name: " + testCase.getName());
            String value = testCase.getRootDataNode().getRoot().getInputForGoogleTest(false);
            TCExecutionDetailLogger.logDetailOfTestCase(testCase, "Value: " + value);
            executionDataNode.setValue(value);

            // Execute random values
            executor.setTestCase(testCase);
            executor.execute();
            // save test case to file
            testCase.setPathDefault();
            TestCaseManager.exportBasicTestCaseToFile(testCase);
            logger.debug("Save the testcase " + testCase.getName() + " to file " + testCase.getPath());

            if (testCase.getStatus().equals(TestCase.STATUS_SUCCESS)
                    || testCase.getStatus().equals(TestCase.STATUS_RUNTIME_ERR)) {
                // export highlighted source code and coverage to file
                CoverageManager.exportCoveragesOfTestCaseToFile(testCase, coverage);

                // read coverage information from file to display on GUI
                List<TestCase> testcases = new ArrayList<>();
                testcases.add(testCase);

                switch (coverage) {
                    case EnviroCoverageTypeNode.STATEMENT:
                    case EnviroCoverageTypeNode.BRANCH:
                    case EnviroCoverageTypeNode.BASIS_PATH:
                    case EnviroCoverageTypeNode.MCDC: {
                        CoverageDataObject coverageData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases, coverage);
                        double cov = Utils.round(coverageData.getVisited() * 1.0f / coverageData.getTotal() * 100, 4);
                        String msg = coverage + " cov: " + cov + "%";
                        TCExecutionDetailLogger.logDetailOfTestCase(testCase, msg);
                        executionDataNode.setCoverage(msg);
                        break;
                    }
                    case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                        String msg = "";
                        // stm cov
                        CoverageDataObject stmCovData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases, EnviroCoverageTypeNode.STATEMENT);
                        double stmCov = Utils.round(stmCovData.getVisited() * 1.0f / stmCovData.getTotal() * 100, 4);
                        msg = EnviroCoverageTypeNode.STATEMENT + " cov: " + stmCov + "%; ";

                        // branch cov
                        CoverageDataObject branchCovData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases, EnviroCoverageTypeNode.BRANCH);
                        double branchCov = Utils.round(branchCovData.getVisited() * 1.0f / branchCovData.getTotal() * 100, 4);
                        msg += EnviroCoverageTypeNode.BRANCH + " cov: " + branchCov + "%";

                        TCExecutionDetailLogger.logDetailOfTestCase(testCase, msg);
                        executionDataNode.setCoverage(msg);
                        break;
                    }
                    case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                        String msg = "";
                        // stm coverage
                        CoverageDataObject stmCovData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases, EnviroCoverageTypeNode.STATEMENT);
                        double stmCov = Utils.round(stmCovData.getVisited() * 1.0f / stmCovData.getTotal() * 100, 4);
                        msg = EnviroCoverageTypeNode.STATEMENT + " cov: " + stmCov + "%; ";

                        // mcdc coverage
                        CoverageDataObject branchCovData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases, EnviroCoverageTypeNode.MCDC);
                        double mcdcCov = Utils.round(branchCovData.getVisited() * 1.0f / branchCovData.getTotal() * 100, 4);
                        msg += EnviroCoverageTypeNode.MCDC + " cov: " + mcdcCov + "%";

                        TCExecutionDetailLogger.logDetailOfTestCase(testCase, msg);
                        executionDataNode.setCoverage(msg);
                        break;
                    }
                }

                // display on Execution View
                TestCasesExecutionTabController testCasesExecutionTabController = TCExecutionDetailLogger.getTCExecTabControllerByFunction(functionNode);
                if (testCasesExecutionTabController != null) {
                    ObservableList<TestCaseExecutionDataNode> data = testCasesExecutionTabController.getData();
                    executionDataNode.setId(data.size());
                    data.add(executionDataNode);
                }

                testCase.setExecutionResult(new AssertionResult());

                // update testcase on disk
                TestCaseManager.exportBasicTestCaseToFile(testCase);
                // export coverage of testcase to file
                CoverageManager.exportCoveragesOfTestCaseToFile(testCase, Environment.getInstance().getTypeofCoverage());
                // save to tst file and navigator tree
                TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();

                return IAutomatedTestdataGeneration.AUTOGEN_STATUS.EXECUTION.BE_ABLE_TO_EXECUTE_TESTCASE;
            } else {
                logger.debug("Do not add test case " + testCase.getName() + " because we can not execute it");
                return IAutomatedTestdataGeneration.AUTOGEN_STATUS.EXECUTION.COUND_NOT_EXECUTE_TESTCASE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return IAutomatedTestdataGeneration.AUTOGEN_STATUS.EXECUTION.COUND_NOT_EXECUTE_TESTCASE;
        }
    }


    public void addModifiedNode(INode node) {
        modifiedFunctionNodes.add(node);
    }

    public void addImpactedNode(INode node, int waveLevel) {
        if (!impactedFunctionNode.contains(node) && !modifiedFunctionNodes.contains(node)) impactedFunctionNode.add(node);

        for (Dependency dependency : node.getDependencies()) {
            if (dependency instanceof FunctionCallDependency) {
                INode nodeStart = dependency.getStartArrow();
                INode nodeEnd = dependency.getEndArrow();
                if (nodeEnd.equals(node)) {
                    addImpactedNode(nodeStart, waveLevel - 1);
                }
            }
        }
    }

    public void addAddedNode(INode node) {
        if (!addedNodes.contains(node)) {
            addedNodes.add(node);
            logger.debug("Added new file " + node.getAbsolutePath());
        }
        for (INode child : node.getChildren()) {
            addAddedNode(child);
        }
    }

    /**
     *
     * @param testcase
     * @param cfgVersion1 new CFG
     * @param cfgVersion2 old CFG
     * @return
     */
    public boolean isTestcaseAffectedByCFGChanges(TestCase testcase, CFG cfgVersion1, CFG cfgVersion2) {
        List<ICfgNode> affectedCfgNode = new ArrayList<>();
        /*Affected node in old CFG*/
        affectedCfgNode = compareCFG(cfgVersion1, cfgVersion2);

        TestpathString_Marker testPath = null;
//        CFG cfg = null;
        try {
            testPath = TestPathUtils.readTestpathFromFile(testcase);
//             cfg = new CFGImporter().importCFGByFile(
//                    CFGUtils.createCFGFunctionPath((FunctionNode) testcase.getFunctionNode()));
//             if (cfg.getFunctionNode() == null)
//                 cfg.setFunctionNode((IFunctionNode) testcase.getFunctionNode());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (testPath == null /*|| cfg == null*/) return false;
        updateVisitedCFGByTestPath(testPath, cfgVersion2);
        for (ICfgNode node : affectedCfgNode) {
            if (node.isVisited()) return true;
        }
        return false;
    }

    private void updateVisitedCFGByTestPath(TestpathString_Marker testPath, CFG cfg) {
        Set<String> visitedOffsets = new HashSet<>();
        List<StatementInTestpath_Mark> lines = testPath.getStandardTestpathSetByAllProperties();
        for (StatementInTestpath_Mark line : lines) {
            if (line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_SOURCE_CODE_FILE) != null &&
                    line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_SOURCE_CODE_FILE) != null) {
                // is statement or condition
                String path = line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.FUNCTION_ADDRESS).getValue();
                path = PathUtils.toAbsolute(path);
                path = Utils.normalizePath(path);
                String targetPath = Utils.normalizePath(cfg.getFunctionNode().getAbsolutePath());
                if (path.equals(targetPath)) {
                    visitedOffsets.add(line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_SOURCE_CODE_FILE).getValue());
                }
            }
        }

        for (ICfgNode cfgNode : cfg.getAllNodes()) {
            if (cfgNode instanceof NormalCfgNode
                    && visitedOffsets.contains(String.valueOf(((NormalCfgNode) cfgNode).getStartOffset())))
                cfgNode.setVisit(true);
        }

    }

    /**
     * compare two cfg
     * @param cfgVersion1 cfg 1
     * @param cfgVersion2 cfg 2
     * @return list of cfg nodes in cfg 1 which of them are different with/ changed in cfg 2
     */
    public List<ICfgNode> compareCFG(CFG cfgVersion1, CFG cfgVersion2) {
        List<ICfgNode> affectedStatements = new ArrayList<>();
        List<ICfgNode> stms1 = cfgVersion1.getAllNodes();
        List<ICfgNode> stms2 = cfgVersion2.getAllNodes();
//        for (int i = 0; i < stms1.size(); i++) {
//            if (!(stms1.get(i).getContent().equals(stms2.get(i).getContent()))) {
//                affectedStatements.add((CfgNode) stms1.get(i));
//            }
//        }
        List<ICfgNode> visitedNodes = new ArrayList<>();
        ICfgNode begin1 = cfgVersion1.getBeginNode();
        ICfgNode begin2 = cfgVersion2.getBeginNode();
        while (!visitedNodes.contains(begin1) && !visitedNodes.contains(begin2)) {
            visitedNodes.add(begin1);
            visitedNodes.add(begin2);
            if (!begin1.getContent().equals(begin2.getContent()))
                addDifferentCfgNodeToList(affectedStatements, begin2);

            begin1 = begin1.getTrueNode();
            begin2 = begin2.getTrueNode();
        }

        return affectedStatements;
    }

    public void addDifferentCfgNodeToList(List<ICfgNode> list, ICfgNode cfgNode) {
        if (list.contains(cfgNode)) return;
        list.add(cfgNode);
        if (cfgNode instanceof ConditionCfgNode) {
            addDifferentCfgNodeToList(list, cfgNode.getTrueNode());
            addDifferentCfgNodeToList(list, cfgNode.getFalseNode());
        }
        else {

            List<ICfgNode> tmpList = new ArrayList<>();
            ICfgNode parent = cfgNode;
            while (!(parent instanceof BeginFlagCfgNode || parent instanceof ConditionCfgNode)) {
                parent = parent.getParent();
            }

            ICfgNode lastScopeNodeOfThisParentCondition = null;

            if (parent instanceof BeginFlagCfgNode) {
                addCfgNodesAndChildrenNodesToList(tmpList, parent, null);
            }
            else {
                // find the last scopeNode of true and false branch at the end of a condition
                ICfgNode iterator = parent;
                while (iterator.getTrueNode() != null) {
                    tmpList.add(iterator.getTrueNode());
                    iterator = iterator.getTrueNode();
                }
                iterator = parent;
                while (iterator.getFalseNode() != null) {
                    if (tmpList.contains(iterator.getFalseNode())) {
                        lastScopeNodeOfThisParentCondition = iterator.getFalseNode();
                        break;
                    }
                    iterator = iterator.getFalseNode();
                }

            }
            for (ICfgNode node : tmpList) {
                if (!list.contains(node)) list.add(node);
            }
            if (lastScopeNodeOfThisParentCondition != null) {
                addCfgNodesAndChildrenNodesToList(list, cfgNode, lastScopeNodeOfThisParentCondition);
            }
        }
    }

    public void addCfgNodesAndChildrenNodesToList(List<ICfgNode> list, ICfgNode start, ICfgNode end) {
        if (list.contains(start)) return;
        if (end == null) {
            list.add(start);
            if (start instanceof EndFlagCfgNode) {
                return;
            }
        }
        else {
            if (start.equals(end)) return;
            list.add(start);
        }
        if (start instanceof ConditionCfgNode) {
            addCfgNodesAndChildrenNodesToList(list, start.getTrueNode(), end);
            addCfgNodesAndChildrenNodesToList(list, start.getFalseNode(), end);
        }
        else addCfgNodesAndChildrenNodesToList(list, start.getTrueNode(), end);
    }
    public Environment getBackupEnvironment() {
        return backupEnvironment;
    }

    public void setBackupEnvironment(Environment backupEnvironment) {
        this.backupEnvironment = backupEnvironment;
    }

    public Environment getNewEnvironment() {
        return newEnvironment;
    }

    public void setNewEnvironment(Environment newEnvironment) {
        this.newEnvironment = newEnvironment;
    }

    public List<INode> getModifiedFunctionNodes() {
        return modifiedFunctionNodes;
    }

    public void setModifiedFunctionNodes(List<INode> modifiedFunctionNodes) {
        this.modifiedFunctionNodes = modifiedFunctionNodes;
    }

    public List<INode> getImpactedFunctionNode() {
        return impactedFunctionNode;
    }

    public void setImpactedFunctionNode(List<INode> impactedFunctionNode) {
        this.impactedFunctionNode = impactedFunctionNode;
    }

    public List<INode> getAddedNodes() {
        return addedNodes;
    }

    public void setAddedNodes(List<INode> addedNodes) {
        this.addedNodes = addedNodes;
    }

    public static int getLEVEL() {
        return LEVEL;
    }

    public static void setLEVEL(int LEVEL) {
        WaveCIA.LEVEL = LEVEL;
    }
}
