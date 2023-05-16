package com.dse.winams.UI;

import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.PersistentDirectoryChooser;
import com.dse.parser.object.IFunctionNode;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.TestNormalSubprogramNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.PointerDataNode;
import com.dse.thread.AkaThread;
import com.dse.thread.task.winams.GenerateVariableTreeTask;
import com.dse.winams.*;
import com.dse.winams.UI.object.*;
import com.dse.winams.converter.SimpleTreeBuilder;
import com.dse.winams.converter.TitleToAliasConverter;
import com.dse.winams.retriever.CsvDataRetriever;
import com.dse.winams.retriever.TestSuiteRecord;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class ExportCsvSettingsController implements Initializable {
    final int DEFAULT_STAGE_HEIGHT = 513;
    private static ExportCsvSettingsController controller = null;
    private Stage popUpWindow;
    @FXML
    private TextField tfFilePath;
    @FXML
    private TextField tfFilename;
    @FXML
    private TextField tfFunction;
    @FXML
    private TreeView<String> tvVariables;
    @FXML
    private ListView<InputEntryListItem> lvInputs;
    @FXML
    private ListView<OutputEntryListItem> lvOutputs;
    @FXML
    private Button btnAddInput;
    @FXML
    private Button btnAddOutput;
    @FXML
    private AnchorPane apContainer;
    @FXML
    private AnchorPane apInOutArea;
    @FXML
    private AnchorPane apInputsArea;
    @FXML
    private AnchorPane apOutputsArea;

    private IFunctionNode function;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tvVariables.setShowRoot(false);
        btnAddInput.setDisable(true);
        btnAddOutput.setDisable(true);
        tvVariables.setOnMouseClicked(event -> {
            VariableTreeItem selectedItem = (VariableTreeItem) tvVariables.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (selectedItem.getChildren().size() > 0) {
                    btnAddInput.setDisable(true);
                    btnAddOutput.setDisable(true);
                } else {
                    btnAddInput.setDisable(false);
                    btnAddOutput.setDisable(false);
                }
            }
        });

        apInputsArea.setPrefHeight(apInOutArea.getPrefHeight() / 2);
        System.out.println(apInputsArea.getPrefHeight());
        apOutputsArea.setPrefHeight(apInOutArea.getPrefHeight() - apInputsArea.getPrefHeight());
        System.out.println(apOutputsArea.getPrefHeight());

        apInOutArea.heightProperty().addListener((observable, oldValue, newValue) -> {
            apInputsArea.setPrefHeight(newValue.doubleValue() / 2);
            apOutputsArea.setPrefHeight(newValue.doubleValue() - apInputsArea.getPrefHeight());
            apOutputsArea.setLayoutY(apInputsArea.getLayoutY() + apInputsArea.getPrefHeight());
        });
    }

    public static ExportCsvSettingsController getController() {
        if (controller == null) {
            FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/winams/ExportCsvSettings.fxml"));
            try {
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                controller = loader.getController();
                Stage popUpWindow = new Stage();
                popUpWindow.setScene(scene);
                popUpWindow.setTitle("Export CSV Settings");
                popUpWindow.setResizable(true);
                controller.setPopUpWindow(popUpWindow);

                popUpWindow.heightProperty().addListener((observable, oldValue, newValue) -> {

//            if (popUpWindow.getHeight() > DEFAULT_STAGE_HEIGHT) {
//                apInputsArea.setLayoutY();
//            }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return controller;
    }

    public void onChooseFilepathClicked() {
        DirectoryChooser directoryChooser = PersistentDirectoryChooser.getInstance();
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File file = directoryChooser.showDialog(envBuilderStage);
        if (file != null) {
            directoryChooser.setInitialDirectory(file);
            tfFilePath.setText(file.getAbsolutePath());
        }
    }

    public void onAddInputClicked() {
        VariableTreeItem selectedItem = (VariableTreeItem) tvVariables.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            IDataNode dataNode = ((TreeNode) selectedItem.getTreeNode()).getDataNode();
            if (dataNode instanceof PointerDataNode) {
                Platform.runLater(() -> PointerSettingController.getController().show(selectedItem, true));
            } else {
                handleNewAddedVariable(selectedItem, true);
            }
        }
    }

    public void onAddOutputClicked() {
        VariableTreeItem selectedItem = (VariableTreeItem) tvVariables.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            IDataNode dataNode = ((TreeNode) selectedItem.getTreeNode()).getDataNode();
            if (dataNode instanceof PointerDataNode) {
                Platform.runLater(() -> PointerSettingController.getController().show(selectedItem, false));
            } else {
                handleNewAddedVariable(selectedItem, false);
            }
        }
    }

    public void onDeleteInputClicked() {
        InputEntryListItem selectedItem = lvInputs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            lvInputs.getItems().remove(selectedItem);
        }
    }

    /**
     * Move the selected item up 1 position
     */
    public void onMoveUpInputClicked() {
        InputEntryListItem selectedItem = lvInputs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int index = lvInputs.getSelectionModel().getSelectedIndex();
            if (index > 0) {
                moveInputsItemToNewIndex(lvInputs, index, index - 1);
            }
        }
    }

    /**
     * Move the selected item down 1 position
     * */
    public void onMoveDownInputClicked() {
        InputEntryListItem selectedItem = lvInputs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int index = lvInputs.getSelectionModel().getSelectedIndex();
            if (index < lvInputs.getItems().size() - 1) {
                moveInputsItemToNewIndex(lvInputs, index, index + 1);
            }
        }
    }

    /**
     * Move the selected item down 1 position
     * */
    public void onMoveUpOutputClicked() {
        OutputEntryListItem selectedItem = lvOutputs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int index = lvOutputs.getSelectionModel().getSelectedIndex();
            if (index > 0) {
                moveOutputsItemToNewIndex(lvOutputs, index, index - 1);
            }
        }
    }

    public void onMoveDownOutputClicked() {
        OutputEntryListItem selectedItem = lvOutputs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int index = lvOutputs.getSelectionModel().getSelectedIndex();
            if (index < lvOutputs.getItems().size() - 1) {
                moveOutputsItemToNewIndex(lvOutputs, index, index + 1);
            }
        }
    }

    public void onClearInputClicked() {
        lvInputs.getItems().clear();
    }

    public void onDeleteOutputClicked() {
        OutputEntryListItem selectedItem = lvOutputs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            lvOutputs.getItems().remove(selectedItem);
        }
    }

    private void moveInputsItemToNewIndex(ListView<InputEntryListItem> listView, int oldIndex, int newIndex) {
        InputEntryListItem selectedItem = listView.getSelectionModel().getSelectedItem();
        listView.getItems().remove(oldIndex);
        listView.getItems().add(newIndex, selectedItem);
        listView.getSelectionModel().select(newIndex);
    }

    private void moveOutputsItemToNewIndex(ListView<OutputEntryListItem> listView, int oldIndex, int newIndex) {
        OutputEntryListItem selectedItem = listView.getSelectionModel().getSelectedItem();
        listView.getItems().remove(oldIndex);
        listView.getItems().add(newIndex, selectedItem);
        listView.getSelectionModel().select(newIndex);
    }

    public void onClearOutputClicked() {
        lvOutputs.getItems().clear();
    }

    public void onExportClicked() {
        if (!checkValidStateToExport()) {
            UIController.showErrorDialog("Please fill up necessary information", "Error", "Cannot export CSV file");
            return;
        }

        CSVInfo csvInfo = new CSVInfo();
        csvInfo.setPath(tfFilePath.getText());
        csvInfo.setName(tfFilename.getText());
        csvInfo.setFunctionNode(function);
        csvInfo.setInputsEntries(new ArrayList<>(lvInputs.getItems()));
        csvInfo.setOutputEntries(new ArrayList<>(lvOutputs.getItems()));

        List<TestCase> testCases = TestCaseManager.getTestCasesByFunction(function);
        CsvDataRetriever retriever = new CsvDataRetriever(testCases, csvInfo);
        new CsvGeneration(csvInfo, retriever.fetch()).generateCsv();

        popUpWindow.close();
    }

    public void handleNewAddedVariable(VariableTreeItem selectedItem, boolean isInput) {
        List<EntryListItem> newItems = new TitleToAliasConverter(selectedItem.getTreeNode(), isInput).generateEntry();
        lvInputs.getItems().addAll(newItems.stream()
                .filter(item -> item instanceof InputEntryListItem)
                .toArray(InputEntryListItem[]::new));
        lvOutputs.getItems().addAll(newItems.stream()
                .filter(item -> item instanceof OutputEntryListItem)
                .toArray(OutputEntryListItem[]::new));
    }

    public void handleNewAddedVariable(VariableTreeItem selectedItem, boolean isInput, PointerSetting pointerSetting) {
        List<EntryListItem> newItems = new TitleToAliasConverter(selectedItem.getTreeNode(), isInput, pointerSetting).generateEntry();
        lvInputs.getItems().addAll(newItems.stream()
                .filter(item -> item instanceof InputEntryListItem)
                .toArray(InputEntryListItem[]::new));
        lvOutputs.getItems().addAll(newItems.stream()
                .filter(item -> item instanceof OutputEntryListItem)
                .toArray(OutputEntryListItem[]::new));
    }

    private void setPopUpWindow(Stage popUpWindow) {
        this.popUpWindow = popUpWindow;
    }

    public void popup(TestNormalSubprogramNode node) {
        try {
            function = (IFunctionNode) UIController.searchFunctionNodeByPath(node.getName());
            tfFunction.setText(function.getSingleSimpleName());
            LoadingPopupController loadingPopupController = LoadingPopupController.newInstance("Loading variables...");
            loadingPopupController.initOwnerStage(popUpWindow);
            loadingPopupController.show();

            GenerateVariableTreeTask generateVariableTreeTask = new GenerateVariableTreeTask(function);
            new AkaThread(generateVariableTreeTask).start();

            generateVariableTreeTask.setOnSucceeded(event -> {
                try {
                    refreshVariableTreeView(generateVariableTreeTask.get().getVariableTree());
                    refreshBothListView();
                    loadingPopupController.close();
                    popUpWindow.show();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (FunctionNodeNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void refreshVariableTreeView(VariableTree variableTree) {
        VariableTreeItem root = new VariableTreeItem(variableTree);
        variableTree.getChildren().forEach(node -> addItemToTreeView(root, (TreeNode) node));
        tvVariables.setRoot(root);
    }

    private void refreshBothListView() {
        lvInputs.getItems().clear();
        lvOutputs.getItems().clear();
    }

    private void addItemToTreeView(VariableTreeItem parent, TreeNode node) {
        if (node instanceof LabelTreeNode) {
            LabelTreeNode labelTreeNode = (LabelTreeNode) node;
            VariableTreeItem item = new VariableTreeItem(labelTreeNode);
            parent.getChildren().add(item);
            for (ITreeNode child : labelTreeNode.getChildren()) {
                addItemToTreeView(item, (TreeNode) child);
            }
        } else {
            VariableTreeItem item = new VariableTreeItem(node);
            parent.getChildren().add(item);
            if (node.getChildren().size() > 0) {
                for (ITreeNode child : node.getChildren()) {
                    addItemToTreeView(item, (TreeNode) child);
                }
            }
        }
    }

    private boolean checkValidStateToExport() {
        if (tfFilePath.getText().isEmpty())
            return false;

        if (tfFilename.getText().isEmpty())
            return false;

        if (lvInputs.getItems().isEmpty())
            return false;

        return !lvOutputs.getItems().isEmpty();
    }
}
