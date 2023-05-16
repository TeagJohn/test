package com.dse.winams.UI;

import com.dse.winams.UI.object.PointerSetting;
import com.dse.winams.UI.object.VariableTreeItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PointerSettingController implements Initializable {
    @FXML
    private TextField tfVariableName;
    @FXML
    private TextField tfStartIndex;
    @FXML
    private TextField tfEndIndex;
    @FXML
    private CheckBox ckbAddress;
    @FXML
    private CheckBox ckbAllocateMemory;
    @FXML
    private CheckBox ckbNumChar;
    @FXML
    private CheckBox ckbPointerIndex;
    @FXML
    private AnchorPane apObjectSettings;
    @FXML
    private AnchorPane apFillIndex;
    private VariableTreeItem selectedVariable;
    private boolean isInput;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ckbAddress.setSelected(true);
        ckbAllocateMemory.setSelected(false);
        ckbNumChar.setSelected(false);
        ckbPointerIndex.setSelected(false);
        apObjectSettings.setDisable(true);
        apFillIndex.setDisable(true);

        ckbAddress.setOnAction(event -> {
            ckbAddress.setSelected(true);
            ckbAllocateMemory.setSelected(false);
            ckbNumChar.setSelected(false);
            ckbPointerIndex.setSelected(false);
            apObjectSettings.setDisable(true);
            apFillIndex.setDisable(true);
            tfStartIndex.setText("0");
            tfEndIndex.setText("0");
        });

        ckbAllocateMemory.setOnAction(event -> {
            ckbAddress.setSelected(false);
            ckbAllocateMemory.setSelected(true);
            ckbNumChar.setSelected(true);
            ckbPointerIndex.setSelected(false);
            apObjectSettings.setDisable(false);
            apFillIndex.setDisable(true);
        });

        ckbNumChar.setOnAction(event -> {
            ckbNumChar.setSelected(true);
            ckbPointerIndex.setSelected(false);
            apFillIndex.setDisable(true);
            tfStartIndex.setText("0");
            tfEndIndex.setText("0");
        });

        ckbPointerIndex.setOnAction(event -> {
            ckbNumChar.setSelected(false);
            ckbPointerIndex.setSelected(true);
            apFillIndex.setDisable(false);
            tfStartIndex.setText("0");
            tfEndIndex.setText("0");
        });
    }

    public void onCancelClicked() {
        ckbAddress.setSelected(false);
        ckbAllocateMemory.setSelected(false);
        ckbNumChar.setSelected(false);
        ckbPointerIndex.setSelected(false);
        apObjectSettings.setDisable(true);
        apFillIndex.setDisable(true);
        tfStartIndex.setText("0");
        tfStartIndex.setText("0");
        popUpWindow.close();
    }

    public void onOkClicked() {
        PointerSetting pointerSetting = null;
        if (ckbAddress.isSelected())
            pointerSetting = new PointerSetting(PointerSetting.Type.ADDRESS, -1, -1);
        else if (ckbAllocateMemory.isSelected()) {
            if (ckbNumChar.isSelected())
                pointerSetting = new PointerSetting(PointerSetting.Type.ALLOCATE_MEMORY_NC, -1, -1);
            else if (ckbPointerIndex.isSelected())
                pointerSetting = new PointerSetting(PointerSetting.Type.ALLOCATE_MEMORY_PIW, Integer.parseInt(tfStartIndex.getText()), Integer.parseInt(tfEndIndex.getText()));
        }

        ExportCsvSettingsController.getController().handleNewAddedVariable(selectedVariable, isInput, pointerSetting);
        popUpWindow.close();
    }

    private static PointerSettingController controller = null;
    private Stage popUpWindow = null;

    private void setPopUpWindow(Stage popUpWindow) {
        this.popUpWindow = popUpWindow;
    }

    public static PointerSettingController getController() {
        if (controller == null) {
            FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/winams/PointerSetting.fxml"));
            try {
                Parent parent = loader.load();
                Scene scene = new Scene(parent);
                controller = loader.getController();
                Stage popUpWindow = new Stage();
                popUpWindow.setScene(scene);
                popUpWindow.setTitle("Pointer Settings");
                popUpWindow.setResizable(false);
                controller.setPopUpWindow(popUpWindow);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return controller;
    }

    public void show(VariableTreeItem variableTreeItem, boolean isInput) {
        this.isInput = isInput;
        selectedVariable = variableTreeItem;
        init();
        tfVariableName.setText(variableTreeItem.getTreeNode().getTitle());
        popUpWindow.show();
    }

    private void init() {
        ckbAddress.setSelected(true);
        ckbAllocateMemory.setSelected(false);
        ckbNumChar.setSelected(false);
        ckbPointerIndex.setSelected(false);
        apObjectSettings.setDisable(true);
        apFillIndex.setDisable(true);
        tfStartIndex.setText("0");
        tfEndIndex.setText("0");
    }
}
