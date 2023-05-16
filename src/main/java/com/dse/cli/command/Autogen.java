package com.dse.cli.command;

import auto_testcase_generation.testdatagen.RandomAutomatedTestdataGeneration;
import com.dse.config.FunctionConfig;
import com.dse.config.FunctionConfigDeserializer;
import com.dse.config.FunctionConfigSerializer;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.controllers.FunctionConfigurationController;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.helps.TCExecutionDetailLogger;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testcasescript.SelectionUpdater;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.thread.AkaThread;
import com.dse.thread.AkaThreadManager;
import com.dse.thread.task.GenerateTestdataTask;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.concurrent.Task;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.dse.cli.command.ICommand.AUTOGEN;

@CommandLine.Command(name = AUTOGEN,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Generate test data automatically in current environment.")
public class Autogen extends AbstractCommand<Environment> {

    @CommandLine.Option(names = {"-w", "--workspace"}, arity = "0",
            description = "Enable generate test data automatically for all functions")
    private boolean workspace;

    @CommandLine.Option(names = {"--strategy"}, paramLabel = "<strategy>",
            arity = "1",
            completionCandidates = Config.Strategy.class,
            description = "Test data generation strategy.\nStrategies: ${COMPLETION-CANDIDATES}")
    private String strategy;

    @CommandLine.Option(names = {"-u", "--unit"}, paramLabel = "<unit>",
            required = false, arity = "1",
            completionCandidates = Unit.class,
            description = "The unit under test.\nPossible values: ${COMPLETION-CANDIDATES}")
    private String unit;

    @CommandLine.Option(names = {"-s", "--subprogram"}, paramLabel = "<subprogram>",
            required = false, arity = "1",
            description = "The subprogram under test.")
    private String subprogram;

    public Autogen() {
        super();
    }

    @Override
    public Environment call() throws Exception {
        if (Environment.getInstance().getProjectNode() == null) {
            akaLogger.error("Please open an environment first.");
             return null;
        }
        List<ITestcaseNode> selectedFunctions = getSelectedFunctions();
        if (selectedFunctions.size() != 0) {
            for (ITestcaseNode selectedFunction : selectedFunctions) {
                if (strategy != null) {
                    if (isValid(strategy) && selectedFunction instanceof TestNormalSubprogramNode) {
                        FunctionConfig functionConfig = setFunctionConfig(((TestNormalSubprogramNode) selectedFunction).getName());
                        if (functionConfig != null) {
                            FunctionConfigurationController.exportFunctionConfigToJson(functionConfig);
                        }
                        generateTestdataAutomaticallyForFunction((TestNormalSubprogramNode) selectedFunction, false);
                    }
                } else {
                    generateTestdataAutomaticallyForFunction((TestNormalSubprogramNode) selectedFunction, false);
                }
            }
        }
        return null;
    }

//    private void generateTestCaseAutomatic(String unit, String subprogram) {
//        if
//    }

    private List<ITestcaseNode> getSelectedFunctions() {
        List<ITestcaseNode> selectedFunctions = new ArrayList<>();
        TestcaseRootNode testcaseRootNode = Environment.getInstance().getTestcaseScriptRootNode();
        if (workspace) {
            if (Environment.getInstance().isCoverageModeActive()) {
                selectedFunctions.addAll(SelectionUpdater.getAllSelectedFunctions(testcaseRootNode));
            } else {
                selectedFunctions = TestcaseSearch.searchNode(testcaseRootNode, new TestNormalSubprogramNode());
            }
        } else if (unit != null) {
            String unitRelativePath = File.separator + unit;
            List<ITestcaseNode> testUnitNodes = testcaseRootNode.getChildren().stream()
                    .filter(u -> u instanceof TestUnitNode
                            && ((TestUnitNode) u).getShortNameToDisplayInTestcaseTree().equals(unitRelativePath))
                    .collect(Collectors.toList());
            if (testUnitNodes.size() == 0) {
                akaLogger.error("File " + unit + " does not exist.");

                return selectedFunctions;
            }
            if (subprogram != null) {
                for (ITestcaseNode testUnitNode : testUnitNodes) {
                    selectedFunctions.addAll(
                            testUnitNode.getChildren().stream()
                                    .filter(s -> {
                                        try {
                                            return s instanceof TestNormalSubprogramNode
                                                    && ((TestNormalSubprogramNode) s)
                                                    .getSimpleNameToDisplayInTestcaseView().equals(subprogram);
                                        } catch (FunctionNodeNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .collect(Collectors.toList()));
                }
                if (selectedFunctions.size() == 0) {
                    akaLogger.error("Functions " + unitRelativePath + "/" + subprogram + " does not exist.");
                    List<TestNormalSubprogramNode> availableNodes = new ArrayList<>();
                    testUnitNodes.forEach(u -> {
                        u.getChildren().forEach(s -> {
                            if (s instanceof TestNormalSubprogramNode) {
                                availableNodes.add((TestNormalSubprogramNode) s);
                            }
                        });
                    });
                    List<String> functionList = availableNodes.stream()
                            .map(s -> "\n" + s.getName().split(((TestUnitNode) s.getParent()).getName() + "/")[1])
                            .collect(Collectors.toList());
                    akaLogger.info("Functions in " + unit + ": " + functionList);
                }
            } else {
                for (ITestcaseNode testUnitNode : testUnitNodes) {
                    if (Environment.getInstance().isCoverageModeActive()) {
                        selectedFunctions.addAll(SelectionUpdater.getAllSelectedFunctions(testUnitNode));
                    } else {
                        selectedFunctions = TestcaseSearch.searchNode(testUnitNode, new TestNormalSubprogramNode());
                    }
                }

            }
        } else if (subprogram != null) {
            akaLogger.error("Please select an unit.");
        } else {
            akaLogger.info("Please select at least one function");
        }
        return selectedFunctions;
    }

    private void generateTestdataAutomaticallyForFunction(TestNormalSubprogramNode selectedFunction, boolean showReport) {
        try {
            ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(selectedFunction.getName());
            if (functionNode.isTemplate() || functionNode instanceof MacroFunctionNode) {
                generateTestcaseAutomaticallyForTemplateFunction(functionNode, showReport);

            } else if (functionNode.hasVoidPointerArgument() || functionNode.hasFunctionPointerArgument()) {
                ExecutorService executorService = Executors.newSingleThreadExecutor(); // must be one to avoid concurrent problem
                List<Task> tasks = new ArrayList<>();

                for (ITestcaseNode child : selectedFunction.getChildren())
                    if (child instanceof TestNewNode &&
                            ((TestNewNode) child).getName().startsWith(TestPrototype.PROTOTYPE_SIGNAL)) {

                        boolean autogen = true;
                        if (Environment.getInstance().isCoverageModeActive() && !child.isSelectedInTestcaseNavigator())
                            autogen = false;

                        if (autogen) {
                            Task t = new Task() {
                                @Override
                                protected Object call() throws Exception {
                                    String name = ((TestNewNode) child).getName();
                                    TestPrototype tc = TestCaseManager.getPrototypeByName(name);
                                    if (tc != null)
                                        generateTestcaseAutomaticallyForNormalFunction(functionNode, showReport, tc);
                                    else
                                        akaLogger.info(name + " not found");
                                    return null;
                                }
                            };
                            tasks.add(t);
                        }
                    }
                if (tasks.size() > 0) {
                    // has at least one prototype
                    for (Task task : tasks) {
                        executorService.execute(task);
                    }
                } else {
                    // there is no prototype, then choose the default configuration
                    generateTestcaseAutomaticallyForNormalFunction(functionNode, showReport, null);
                }

            } else {
                generateTestcaseAutomaticallyForNormalFunction(functionNode, showReport, null);
            }
        } catch (FunctionNodeNotFoundException fe) {
            akaLogger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateTestcaseAutomaticallyForTemplateFunction(ICommonFunctionNode functionNode, boolean shouldViewTestCaseExecution) {
        // if a function is a template function, we will generate test cases for all prototypes
        String templatePath = functionNode.getTemplateFilePath();
        if (!(new File(templatePath).exists())) {
            UIController.showErrorDialog("You must create a prototype for function " + functionNode.getAbsolutePath() + " before generating test data automatically.\n" +
                            "Aka generates test data for these prototypes",
                    "Autogen for a function", "Fail");
            return;
        }

        List<TestPrototype> prototypes = RandomAutomatedTestdataGeneration.getAllPrototypesOfTemplateFunction(functionNode);
        if (prototypes.size() == 0)
            UIController.showErrorDialog("Not find out any prototypes of this template function. Please right click > Insert a prototype",
                    "Autogen for a function", "Fail");
        else {
            for (TestPrototype selectedPrototype : prototypes) {
                UILogger.getUiLogger().logToBothUIAndTerminal("Prototype: " + selectedPrototype.getName());
                generateTestcaseAutomaticallyForNormalFunction(functionNode, shouldViewTestCaseExecution, selectedPrototype);
            }
        }
    }

    private void generateTestcaseAutomaticallyForNormalFunction(ICommonFunctionNode functionNode,
                                                                boolean shouldViewReport,
                                                                TestPrototype selectedPrototype) {
        akaLogger.info("Start generate test cases for function: " + functionNode.getSingleSimpleName());

        if (functionNode.isTemplate() && selectedPrototype == null)
            return;
        if (functionNode instanceof MacroFunctionNode && selectedPrototype == null)
            return;
//        if (functionNode.hasVoidPointerArgument() && selectedPrototype == null)
//            return;

//        LoadingPopupController loadingPopup = LoadingPopupController.getInstance();
//        if (!loadingPopup.getStage().isShowing()) {
//            loadingPopup.getStage().setTitle("Automatic generate test case");
////            loadingPopup.setOwnerStage(UIController.getPrimaryStage());
//            loadingPopup.show();
//        }

        // refresh executions
        TCExecutionDetailLogger.clearExecutions(functionNode);

        MDIWindowController.getMDIWindowController().removeTestCasesExecutionTabByFunction(functionNode);
        TCExecutionDetailLogger.initFunctionExecutions(functionNode);

        akaLogger.info("Getting function configuration of function " + functionNode.getName() + " ...");
        FunctionConfig functionConfig = null;

        // search for the function config file of the current function node
        String functionConfigDir = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() +
                File.separator + functionNode.getNameOfFunctionConfigJson() + ".json";
        if (new File(functionConfigDir).exists()) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigDeserializer());
            Gson customGson = gsonBuilder.create();
            functionConfig = customGson.fromJson(Utils.readFileContent(functionConfigDir), FunctionConfig.class);
            functionNode.setFunctionConfig(functionConfig);
            akaLogger.info("Loading the function config of " + functionNode.getAbsolutePath() + ": " + functionNode.getAbsolutePath());

        } else {
            akaLogger.info("Create new function config of " + functionNode.getAbsolutePath());
            functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
            functionConfig.setFunctionNode(functionNode);
            functionNode.setFunctionConfig(functionConfig);
            functionConfig.createBoundOfArgument(functionConfig, functionNode);

            //
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigSerializer());
            Gson gson = builder.setPrettyPrinting().create();
            String json = gson.toJson(functionConfig, FunctionConfig.class);

            String functionConfigFile = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() + File.separator +
                    functionConfig.getFunctionNode().getNameOfFunctionConfigJson() + ".json";
            akaLogger.info("Export the config of function " + functionConfig.getFunctionNode().getAbsolutePath() + " to " + functionConfigFile);
            Utils.writeContentToFile(json, functionConfigFile);
        }

        akaLogger.info("Create new thread to run the test data");
        GenerateTestdataTask task = new GenerateTestdataTask(shouldViewReport);
        task.setSelectedPrototype(selectedPrototype);
        task.setFunction(functionNode);
        AkaThread thread = new AkaThread(task);
        thread.setName(functionNode.getSimpleName());
        AkaThreadManager.akaThreadList.add(thread);
        AkaThreadManager.autoTestdataGenForSrcFileThreadPool.execute(thread);

    }

    private FunctionConfig setFunctionConfig(String functionPath) {
        if (isValid(strategy)) {
            try {
                ICommonFunctionNode function = UIController.searchFunctionNodeByPath(functionPath);
                FunctionConfig functionConfig = FunctionConfigurationController.loadOrInitFunctionConfig(function);
                functionConfig.setTestdataGenStrategy(normalize(strategy));
                akaLogger.info("Test data generation strategy for function "
                        + functionConfig.getFunctionNode().getName() + ": " + normalize(strategy));
                return functionConfig;
            } catch (FunctionNodeNotFoundException fe) {
                akaLogger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
            }
        }
        return null;
    }

    private boolean isValid(String strategy) {
        if (Config.Strategy.strategies.contains(strategy)) {
            return true;
        }
        akaLogger.error(strategy + " strategy does not exist.");
        akaLogger.info("Test data generation strategies:\n" + Config.Strategy.strategies);
        return false;
    }

    private String normalize(String strategy) {
        return strategy.replaceAll("_", " ");
    }

    static class Unit implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            if (Environment.getInstance().getProjectNode() != null) {
                List<ITestcaseNode> list = Environment.getInstance().getTestcaseScriptRootNode().getChildren();
                List<TestUnitNode> testUnitNodes = new ArrayList<>();
                list.forEach(n -> {
                    if (n instanceof TestUnitNode) {
                        testUnitNodes.add((TestUnitNode) n);
                    }
                });
                return testUnitNodes.stream()
                        .map(TestUnitNode::getShortNameToDisplayInTestcaseTree)
                        .map(s -> s.substring(1)).iterator();
            } else {
                return new ArrayList<String>(Arrays.asList("none")).iterator();
            }
        }
    }

}
