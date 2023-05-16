package com.dse.cli.command;

import com.dse.config.FunctionConfig;
import com.dse.config.IFunctionConfig;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.controllers.FunctionConfigurationController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.environment.Environment;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcasescript.SelectionUpdater;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.dse.cli.command.ICommand.CONFIG;

/**
 * This command is used to execute a specific testcase (ok)
 * or multiples testcases with specific names (ok)
 * or testcases of a subprogram (not yet)
 * or testcases of a unit undertest (not yet)
 * or all testcases in whole environment (not yet)
 */

@Command(name = CONFIG,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Set function config in current environment.")
public class Config extends AbstractCommand {

    @Option(names = {"-w", "--workspace"}, arity = "0",
            description = "Enable config function for workspace.")
    private boolean setWorkspaceConfig;

    @CommandLine.Option(names = {"--strategy"}, paramLabel = "<strategy>",
            required = true, arity = "1",
            completionCandidates = Strategy.class,
            description = "Test data generation strategy.\nStrategies: ${COMPLETION-CANDIDATES}")
    private String strategy;
    @CommandLine.Option(names = {"-u", "--unit"}, paramLabel = "<unit>",
            required = false, arity = "1",
            completionCandidates = Autogen.Unit.class,
            description = "The unit under test.\nPossible values: ${COMPLETION-CANDIDATES}")
    private String unit;

    @CommandLine.Option(names = {"-s", "--subprogram"}, paramLabel = "<subprogram>",
            required = false, arity = "1",
            description = "The subprogram under test.")
    private String subprogram;

    public Config() {
        super();
    }

    @Override
    public Object call() throws Exception {
        if (Environment.getInstance().getProjectNode() == null) {
            akaLogger.error("Please open an environment first.");
            return null;
        }
        if (setWorkspaceConfig) {
            setStrategy();
            return null;
        }
        List<ITestcaseNode> selectedFunctions = getSelectedFunctions();
        if (selectedFunctions.size() != 0) {
            for (ITestcaseNode selectedFunction : selectedFunctions) {
                if (selectedFunction instanceof TestNormalSubprogramNode) {
                    setFunctionConfig(((TestNormalSubprogramNode) selectedFunction).getName());
                }
            }
        }
        return null;
    }

    private List<ITestcaseNode> getSelectedFunctions() {
        List<ITestcaseNode> selectedFunctions = new ArrayList<>();
        TestcaseRootNode testcaseRootNode = Environment.getInstance().getTestcaseScriptRootNode();
        if (unit != null) {
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

    private void setStrategy() {
        if (isValid(strategy)) {
            FunctionConfig defaultFunctionConfig = Environment.getInstance().getDefaultFunctionConfig();
            defaultFunctionConfig.setTestdataGenStrategy(normalize(strategy));
            FunctionConfigurationController.exportFunctionConfigToJson(defaultFunctionConfig);
            akaLogger.info("Default test data generation strategy: " + normalize(strategy));
        }
    }

    private boolean isValid(String strategy) {
        if (Strategy.strategies.contains(strategy)) {
            return true;
        }
        akaLogger.error(strategy + " strategy does not exist.");
        akaLogger.info("Test data generation strategies:\n" + Strategy.strategies);
        return false;
    }

    private String normalize(String strategy) {
        return strategy.replaceAll("_", " ");
    }

    private void setFunctionConfig(String functionPath) {
        if (isValid(strategy)) {
            try {
                ICommonFunctionNode function = UIController.searchFunctionNodeByPath(functionPath);
                FunctionConfig functionConfig = FunctionConfigurationController.loadOrInitFunctionConfig(function);
                functionConfig.setTestdataGenStrategy(normalize(strategy));
                FunctionConfigurationController.exportFunctionConfigToJson(functionConfig);
                akaLogger.info("Test data generation strategy for function "
                        + functionConfig.getFunctionNode().getName() + ": " + normalize(strategy));
            } catch (FunctionNodeNotFoundException fe) {
                akaLogger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
            }
        }
    }

    static class Strategy implements Iterable<String> {

        static List<String> strategies = new ArrayList<>(
                Arrays.asList(IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BEST_TIME.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BEST_COVERAGE.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_DIJKSTRA.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.RANDOM.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA_BOUNDARYCONDITION.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.ROBUSTNESS.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.WHITEBOX_BOUNDARY.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.MID_MIN_MAX.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_CFDS.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.CONCOLIC_TESTING_DFS.replaceAll(" ", "_"),
                        IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BASIS_PATH_TESTING.replaceAll(" ", "_")
                )
        );


        @Override
        public Iterator<String> iterator() {
            return strategies.stream().iterator();
        }
    }
}
