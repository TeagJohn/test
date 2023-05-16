package com.dse.guifx_v3.controllers.build_environment;

import com.dse.compiler.AvailableCompiler;
import com.dse.compiler.Compiler;
import com.dse.environment.Environment;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.*;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.logger.AkaLogger;
import com.dse.make_build_system.*;
import com.dse.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ChooseCompilerController extends AbstractCustomController implements Initializable {
    final static AkaLogger logger = AkaLogger.get(ChooseCompilerController.class);

    @FXML
    private TextField tfIncludeFlag, tfDefineFlag, tfDebugCommand, tfOutfileFlag, tfOutfileExtension, tfLinkCommand,
            tfMakeTarget;

    @FXML
    private Tab makeConfigTab;

    @FXML
    private CheckBox chbUseMake;

    @FXML
    private ComboBox<String> cbBuildSystem;

    @FXML
    private StackPane spBuildSystemConfig;

    @FXML
    private AnchorPane apGnuMake, apCmake, apDefaultStrategy;

    @FXML
    private ComboBox<String> cbCMakeGenerators;

    @FXML
    private ComboBox<BuildSystemOpts.GnuMakeStrategyObject> cbBuildStrategy;

    @FXML
    private Label lCmakeWarning, lCmakeNote, lBuildStrategyDetail;

    @FXML
    private ComboBox<String> cbCompilers;

    @FXML
    private TextField preprocessCmd, compileCmd;

    @FXML
    private ListView<EnviroDefinedVariableNode> lvDefinedVariable;

    @FXML
    private Button newDefinedVariableBtn;

    @FXML
    private Button editDefinedVariableBtn;

    @FXML
    private Button deleteDefinedVariableBtn;

    @FXML
    private Button parseCmdBtn;

    @FXML
    private Button testSettingBtn;

    private Compiler compiler;


    public void initialize(URL location, ResourceBundle resources) {
        // get all possible compilers
        for (Class<?> compiler : AvailableCompiler.class.getClasses()) {
            try{
                String name = compiler.getField("NAME").get(null).toString();
                cbCompilers.getItems().add(name);
            } catch (Exception ex) {
                logger.error("Cant parse " + compiler.toString() + " compiler setting");
            }
        }

        chbUseMake.setOnMouseClicked(event -> {
            makeConfigTab.setDisable(!chbUseMake.isSelected());
        });
        cbBuildSystem.getItems().addAll(BuildSystemOpts.BuildSystemOpts);

        spBuildSystemConfig.setVisible(false);
        for (Node child : spBuildSystemConfig.getChildren()) {
            child.setVisible(false);
        }

        // pre-config for cmake
        cbCMakeGenerators.getItems().addAll(BuildSystemOpts.CMakeGeneratorOpts);

        lCmakeWarning.setText(BuildSystemOpts.CMAKE_WARNING);
        lCmakeNote.setText(BuildSystemOpts.CMAKE_NOTE);

        cbBuildStrategy.getItems().addAll(new BuildSystemOpts().GnuMakeStrategyOpts);
        apDefaultStrategy.setVisible(false);

        // set the default compiler
        if (Utils.isWindows())
            cbCompilers.setValue(AvailableCompiler.C_GNU_NATIVE_WINDOWS_MINGW.NAME);
        else
            cbCompilers.setValue(AvailableCompiler.C_GNU_NATIVE.NAME);
        updateCommandCorrespondingToCompiler();

        // set event where we add defined variables
        lvDefinedVariable.setCellFactory(param -> new ListCell<EnviroDefinedVariableNode>() {
            @Override
            protected void updateItem(EnviroDefinedVariableNode item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getName() != null) {
                    String text = item.getName();

                    if (item.getValue() != null && !item.getValue().isEmpty())
                        text += "=" + item.getValue();

                    setText(text);
                }
            }
        });

        // add Hints to Buttons
        Hint.tooltipNode(newDefinedVariableBtn, HintContent.EnvBuilder.Compiler.NEW_DEFINED_VAR);
//        Hint.tooltipNode(editDefinedVariableBtn, "Edit an existing variable");
//        Hint.tooltipNode(deleteDefinedVariableBtn, "Delete a variable");
        Hint.tooltipNode(parseCmdBtn, HintContent.EnvBuilder.Compiler.PARSE_COMMAND);
        Hint.tooltipNode(testSettingBtn, HintContent.EnvBuilder.Compiler.TEST_SETTING);
    }

    @Override
    public void validate() {
        boolean valid = true;
        // nothing to do
        if (chbUseMake.isSelected()) {
            if (cbBuildSystem.getValue() == null) {
                valid = false;
                UIController.showErrorDialog("Please select a build system", "Error", "Make Build System");
            } else {
                switch (cbBuildSystem.getValue()) {
                    case "GNU Make":
                        if (cbBuildStrategy.getValue() == null) {
                            valid = false;
                            UIController.showErrorDialog("Please select a build strategy", "Error", "Make Build System");
                        }
                        break;
                    case "CMake":
                        if (cbCMakeGenerators.getValue() == null) {
                            valid = false;
                            UIController.showErrorDialog("Please select a CMake generator", "Error", "Make Build System");
                        }
                        break;
                }
            }
        }

        setValid(valid);
    }

    @Override
    public void save() {
        updateEnviroCompilerNodeInEnvironmentTree();
        updateDefinedVariableNodeInEnvironmentTree();
        updateEnviroMakeBuildSystemNodeInEnvironmentTree();
        validate();
    }

    /**
     * @return true if we can update/add compiler node in the environment tree
     */
    private boolean updateEnviroCompilerNodeInEnvironmentTree() {
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroCompilerNode());

        if (nodes.size() == 1) {
            updateEnviroCompilerNode(((EnviroCompilerNode) nodes.get(0)));
            logger.debug("Environment configuration:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
            return true;
        } else if (nodes.size() == 0) {
            EnviroCompilerNode compilerNode = updateEnviroCompilerNode(new EnviroCompilerNode());
            Environment.getInstance().getEnvironmentRootNode().addChild(compilerNode);
            logger.debug("Configuration of the environment:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
            return true;
        } else {
            logger.debug("There are more than two compiler options!");
            return false;
        }
    }

    private EnviroCompilerNode updateEnviroCompilerNode(EnviroCompilerNode node) {
        node.setName(cbCompilers.getValue());

        node.setPreprocessCmd(preprocessCmd.getText());
        node.setCompileCmd(compileCmd.getText());
        node.setLinkCmd(tfLinkCommand.getText());
        node.setDebugCmd(tfDebugCommand.getText());
        node.setIsCMakeProject(chbUseMake.isSelected());
        node.setCMakeGenerator(cbCMakeGenerators.getValue());

        System.out.println(cbCMakeGenerators.getValue());

        node.setIncludeFlag(tfIncludeFlag.getText());
        node.setDefineFlag(tfDefineFlag.getText());
        node.setOutputFlag(tfOutfileFlag.getText());
        node.setDebugFlag(AvailableCompiler.TEMPLATE.DEBUG_FLAG);

        node.setOutputExt(tfOutfileExtension.getText());

        return node;
    }

    private void updateDefinedVariableNodeInEnvironmentTree() {
        EnvironmentRootNode rootEnv = Environment.getInstance().getEnvironmentRootNode();
        // remove all defined variables
        List<IEnvironmentNode> children = rootEnv.getChildren();
        for (int i = children.size() - 1; i >= 0; i--)
            if (children.get(i) instanceof EnviroDefinedVariableNode)
                // defined variables are stored in the first children level
                children.remove(i);

        // save defined variable in GUI
        for (EnviroDefinedVariableNode definedVariableNode : lvDefinedVariable.getItems()) {
            children.add(definedVariableNode);
            definedVariableNode.setParent(rootEnv);
            rootEnv.addChild(definedVariableNode);
        }
    }

    private void updateEnviroMakeBuildSystemNodeInEnvironmentTree() {
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroMakeBuildSystemNode());

        if (chbUseMake.isSelected()) {
            if (nodes.size() == 1) {
                updateEnviroMakeBuildSystemNode(((EnviroMakeBuildSystemNode) nodes.get(0)));
                logger.debug("Environment configuration:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
            } else if (nodes.size() == 0) {
                EnviroMakeBuildSystemNode makeBuildSystemNode = updateEnviroMakeBuildSystemNode(new EnviroMakeBuildSystemNode());
                if (makeBuildSystemNode != null) Environment.getInstance().getEnvironmentRootNode().addChild(makeBuildSystemNode);
                logger.debug("Configuration of the environment:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
            } else {
                logger.debug("There are more than two compiler options!");
            }
        } else {
            for (IEnvironmentNode node : nodes) {
                Environment.getInstance().getEnvironmentRootNode().removeChild(node);
            }
            logger.debug("Configuration of the environment:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
        }
    }

    private EnviroMakeBuildSystemNode updateEnviroMakeBuildSystemNode(EnviroMakeBuildSystemNode node) {
        if (cbBuildSystem.getValue() != null) {
            BuildSystemHandler handler = Environment.getInstance().getMakeBuildSystemManager().getCurrentBSHandler();
            node.setBuildSystem(cbBuildSystem.getValue());
            switch (cbBuildSystem.getValue()) {
                case "CMake":
                    node.setCMakeGenerator(cbCMakeGenerators.getValue());
                    ((CMakeHandler) handler).setCmakeGenerator(cbCMakeGenerators.getValue());
                    break;
                case "GNU Make":
                    node.setGnuMakeBuildType((cbBuildStrategy.getValue() != null) ? cbBuildStrategy.getValue().id : -1);
                    node.setGnuMakeTarget(tfMakeTarget.getText());
                    ((GNUMakeHandler) handler).setBuildType(node.getGnuMakeBuildType());
                    ((GNUMakeHandler) handler).setBuildTarget(tfMakeTarget.getText());
                    break;
            }

            return node;
        }

        return null;
    }

    public void loadFromEnvironment() {
        loadCompilerFromEnvironment();
        loadMakeBuildSystemFromEnvironment();
    }

    private void loadCompilerFromEnvironment() {
        logger.debug("Load compiler from current environment");

        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroCompilerNode());

        if (nodes.size() == 1) {
            // load commands from .env file
            EnviroCompilerNode enviroCompilerNode = getEnviroCompilerNode();
            cbCompilers.setValue(enviroCompilerNode.getName());
            preprocessCmd.setText(enviroCompilerNode.getPreprocessCmd());
            compileCmd.setText(enviroCompilerNode.getCompileCmd());
            tfDefineFlag.setText(enviroCompilerNode.getDefineFlag());
            tfIncludeFlag.setText(enviroCompilerNode.getIncludeFlag());
            tfOutfileFlag.setText(enviroCompilerNode.getOutputFlag());
            tfOutfileExtension.setText(enviroCompilerNode.getOutputExt());
            tfLinkCommand.setText(enviroCompilerNode.getLinkCmd());
            tfDebugCommand.setText(enviroCompilerNode.getDebugCmd());

            // load defined variables from .env file
            List<IEnvironmentNode> definedVariableNodes = getEnviroDefinedVariableNode();
            for (IEnvironmentNode definedVariableNode : definedVariableNodes)
                if (definedVariableNode instanceof EnviroDefinedVariableNode)
                    lvDefinedVariable.getItems().add((EnviroDefinedVariableNode) definedVariableNode);
        }
    }

    private void loadMakeBuildSystemFromEnvironment() {
        logger.debug("Load make build system from current environment");

        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroMakeBuildSystemNode());

        if (nodes.size() == 1) {
            // load commands from .env file
            EnviroMakeBuildSystemNode enviroMakeBuildSystemNode = (EnviroMakeBuildSystemNode) nodes.get(0);
            chbUseMake.setSelected(true);
            makeConfigTab.setDisable(false);
            cbBuildSystem.setValue(enviroMakeBuildSystemNode.getBuildSystem());
            cbCMakeGenerators.setValue(enviroMakeBuildSystemNode.getCMakeGenerator());
            cbBuildStrategy.getSelectionModel().select(enviroMakeBuildSystemNode.getGnuMakeBuildType());
            tfMakeTarget.setText(enviroMakeBuildSystemNode.getGnuMakeTarget());
        } else {
            chbUseMake.setSelected(false);
            makeConfigTab.setDisable(true);
        }
    }

    private List<IEnvironmentNode> getEnviroDefinedVariableNode() {
        Map<String, String> definedVariables = new HashMap<>();
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> definedVariableNodes = EnvironmentSearch.searchNode(root, new EnviroDefinedVariableNode());
        return definedVariableNodes;
    }

    private EnviroCompilerNode getEnviroCompilerNode() {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroCompilerNode());
        if (nodes.size() == 1) {
            EnviroCompilerNode compilerNode = (EnviroCompilerNode) nodes.get(0);
            return compilerNode;
        } else
            return null;
    }

    public void updateCommandCorrespondingToCompiler() {
        // nothing to do because we have nothing to validate
        if (cbCompilers.getValue() != null) {
            Compiler compiler = createTemporaryCompiler(cbCompilers.getValue());
            preprocessCmd.setText(compiler.getPreprocessCommand());
            compileCmd.setText(compiler.getCompileCommand());
            tfDefineFlag.setText(compiler.getDefineFlag());
            tfIncludeFlag.setText(compiler.getIncludeFlag());
            tfOutfileFlag.setText(compiler.getOutputFlag());
            tfOutfileExtension.setText(compiler.getOutputExtension());
            tfLinkCommand.setText(compiler.getLinkCommand());
            tfDebugCommand.setText(compiler.getDebugCommand());
        }
    }

    private Compiler createTemporaryCompiler(String opt) {
        if (opt != null) {
            for (Class<?> c : AvailableCompiler.class.getClasses()) {
                try {
                    String name = c.getField("NAME").get(null).toString();

                    if (name.equals(opt))
                        return new Compiler(c);
                } catch (Exception ex) {
                    logger.error("Cant parse " + c.toString() + " compiler setting");
                }
            }
        }

        return null;
    }

    @FXML
    public void newDefinedVariable() {
        Stage popUpWindow = DefineVariablePopupController.getPopupWindowNew(lvDefinedVariable);

        // block the environment window
        assert popUpWindow != null;
        popUpWindow.initModality(Modality.WINDOW_MODAL);
        popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
        popUpWindow.show();
    }

    @FXML
    public void editDefinedVariable() {
        EnviroDefinedVariableNode variableNode = lvDefinedVariable.getSelectionModel().getSelectedItem();
        if (variableNode != null) {
            Stage popUpWindow = DefineVariablePopupController.getPopupWindowEdit(lvDefinedVariable, variableNode);

            // block the environment window
            assert popUpWindow != null;
            popUpWindow.initModality(Modality.WINDOW_MODAL);
            popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
            popUpWindow.show();
        }
    }

    @FXML
    public void deleteDefinedVariable() {
        EnviroDefinedVariableNode variableNode = lvDefinedVariable.getSelectionModel().getSelectedItem();
        if (variableNode != null) {
            Environment.getInstance().getEnvironmentRootNode().getChildren().remove(variableNode);
            lvDefinedVariable.getItems().remove(variableNode);
            lvDefinedVariable.refresh();
        }
    }

    private void updateCompiler() {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroCompilerNode());

        if (!nodes.isEmpty())
            compiler = Environment.getInstance().importCompiler((EnviroCompilerNode) nodes.get(0));

        for (EnviroDefinedVariableNode defineNode : lvDefinedVariable.getItems()) {
            String define = defineNode.getName();
            if (defineNode.getValue() != null)
                define += "=" + defineNode.getValue();

            compiler.getDefines().add(define);
        }
    }

    @FXML
    public void testSetting() {
        FXMLLoader loader;
        try {
            save();
            updateCompiler();

            loader = new FXMLLoader(Object.class.getResource("/FXML/envbuilding/TestSettings.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            TestSettingsController controller = loader.getController();
            controller.setCompiler(compiler);

            Stage testSettingStage = new Stage();
            controller.setStage(testSettingStage);

            testSettingStage.setScene(scene);
            testSettingStage.setTitle("Test Settings");
            testSettingStage.setResizable(false);
            testSettingStage.initModality(Modality.WINDOW_MODAL);
            testSettingStage.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
            testSettingStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseCmd(ActionEvent actionEvent) {
        FXMLLoader loader;
        try {
            save();
            updateCompiler();

            loader = new FXMLLoader(Object.class.getResource("/FXML/envbuilding/ParseCommandLine.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            ParseCommandLineController controller = loader.getController();
            controller.setCompiler(compiler);
            controller.setLvOriginDefines(lvDefinedVariable);

            Stage stage = new Stage();
            controller.setStage(stage);

            stage.setScene(scene);
            stage.setTitle("Parse Command Line");
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onChooseBuildSystem() {
        final String buildSystem = cbBuildSystem.getValue();
        if (buildSystem != null) {
            MakeBuildSystemManager manager = Environment.getInstance().getMakeBuildSystemManager();
            BuildSystemHandler handler = manager.createNewHandler(buildSystem);

            System.out.println(handler);

            if (handler != null) {
                if (handler instanceof CMakeHandler) {
                    spBuildSystemConfig.setVisible(true);
                    openStackPaneBuildSystemConfigFor("apCmake");
                } else if (handler instanceof GNUMakeHandler) {
                    spBuildSystemConfig.setVisible(true);
                    openStackPaneBuildSystemConfigFor("apGnuMake");
                } else {
                    spBuildSystemConfig.setVisible(false);
                }
            } else {
                spBuildSystemConfig.setVisible(false);
                openStackPaneBuildSystemConfigFor(null);
                cbBuildSystem.setValue(null);
            }
        }
    }

    /**
     * Open the stack pane for the build system configuration with the given id. <br>
     * If the id is null, all stack pane will be closed. <br>
     * Ex: stack pane id = "apCmake" will open the stack pane for CMake configuration.
     *
     * @param childId the id of the child to open
     */
    private void openStackPaneBuildSystemConfigFor(String childId) {
        for (Node node : spBuildSystemConfig.getChildren()) {
            if (childId == null) {
                node.setDisable(true);
                node.setVisible(false);
            } else {
                if (node.getId().equals(childId)) {
                    node.setDisable(false);
                    node.setVisible(true);
                } else {
                    node.setDisable(true);
                    node.setVisible(false);
                }
            }
        }
    }

    public void onChooseGnuMakeBuildStrategy() {
        if (cbBuildStrategy.getValue() != null) {
            lBuildStrategyDetail.setText(cbBuildStrategy.getValue().detail);
            apDefaultStrategy.setVisible(cbBuildStrategy.getValue().id == 0);
        }
    }
}