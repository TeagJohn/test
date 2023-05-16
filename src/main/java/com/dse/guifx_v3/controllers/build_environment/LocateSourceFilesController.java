package com.dse.guifx_v3.controllers.build_environment;

import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.*;
import com.dse.guifx_v3.controllers.object.build_environment.LocateSourceFilesCellFactory;
import com.dse.guifx_v3.controllers.object.build_environment.SourcePath;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.PersistentDirectoryChooser;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.make_build_system.BuildSystemHandler;
import com.dse.make_build_system.CMakeHandler;
import com.dse.make_build_system.GNUMakeHandler;
import com.dse.project_init.ProjectClone;

import com.dse.regression.cia.WaveCIA;
import com.dse.util.PathUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class LocateSourceFilesController extends AbstractCustomController implements Initializable {
    @FXML
    private TextField tfProjectDirectory, tfIncludeDirectory, tfSourceDirectory;
    @FXML
    private ListView<SourcePath> lvPaths; // contain all added source code files
    @FXML
    private CheckBox chbUseRelativePath;
    @FXML
    private Button bAdd;
    @FXML
    private Button bDelete;
    @FXML
    private Button bAddRecursive;
    @FXML
    private Button bBrowse, bBrowseSource, bBrowseInclude, bSaveAndShowDirs, bShowIncludeAndSource;
    @FXML
    private AnchorPane apIncludeSourceDirectory;
    @FXML
    private Label lProjectDirectory;

    private String buildSystem = "";

    private final List<String> absolutePaths = new ArrayList<>();

    public void initialize(URL location, ResourceBundle resources) {
        lvPaths.setCellFactory(new LocateSourceFilesCellFactory());
        bAdd.setText(null);
        Hint.tooltipNode(bAdd, HintContent.EnvBuilder.SrcLocation.ADD);
        Image add = new Image(Objects.requireNonNull(Factory.class.getResourceAsStream("/icons/add.png")));
        bAdd.setGraphic(new ImageView(add));
        bDelete.setText(null);
        Hint.tooltipNode(bDelete, HintContent.EnvBuilder.SrcLocation.DELETE);
        Image delete = new Image(Objects.requireNonNull(Factory.class.getResourceAsStream("/icons/delete.png")));
        bDelete.setGraphic(new ImageView(delete));
        bAddRecursive.setText(null);
        Hint.tooltipNode(bAddRecursive, HintContent.EnvBuilder.SrcLocation.ADD_RECURSIVE);
        Image addRecursive = new Image(Objects.requireNonNull(Factory.class.getResourceAsStream("/icons/addRecursive.png")));
        bAddRecursive.setGraphic(new ImageView(addRecursive));

        Hint.tooltipNode(bBrowse, HintContent.EnvBuilder.SrcLocation.BROWSE);

        Hint.tooltipNode(chbUseRelativePath, HintContent.EnvBuilder.SrcLocation.VIEW_RELATED);

        apIncludeSourceDirectory.setVisible(false);
        bShowIncludeAndSource.setOnMouseClicked(event -> {
            apIncludeSourceDirectory.setVisible(true);
            lvPaths.setVisible(false);
            bShowIncludeAndSource.setVisible(false);
        });
        bSaveAndShowDirs.setOnMouseClicked(event -> {
            addRecursive(new File(tfProjectDirectory.getText()));
            apIncludeSourceDirectory.setVisible(false);
            lvPaths.setVisible(true);
            bShowIncludeAndSource.setVisible(true);
        });

        for (String path : WaveCIA.paths_Folder_Demo) addToListViewPaths(path);
    }

    @FXML
    public void addTheRootFolder() {
        DirectoryChooser directoryChooser = PersistentDirectoryChooser.getInstance();
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File file = directoryChooser.showDialog(envBuilderStage);
        if (file != null) {
            directoryChooser.setInitialDirectory(file);
        }
        if (file != null) {
            addToListViewPaths(file.getAbsolutePath());
        }

        validate();
    }

    @FXML
    public void addRecursiveFolders() {
        DirectoryChooser directoryChooser = PersistentDirectoryChooser.getInstance();
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File file = directoryChooser.showDialog(envBuilderStage);
        if (file != null) {
            directoryChooser.setInitialDirectory(file);
        }
        addRecursive(file);
        validate();
    }

    private void addToListViewPaths(String absolutePath) {
        if (!absolutePaths.contains(absolutePath)) {
            if (!absolutePaths.contains(absolutePath)) {
                absolutePaths.add(absolutePath);
                SourcePath item = new SourcePath(absolutePath);
                item.setType(SourcePath.SEARCH_DIRECTORY); // by default
                lvPaths.getItems().add(item);
            }
        }
    }

    private void addRecursive(File parentDirectory) {
        if (parentDirectory != null) {
            addToListViewPaths(parentDirectory.getAbsolutePath());
            File[] listFiles = parentDirectory.listFiles(File::isDirectory);
            if (listFiles != null) {
                for (File file : listFiles) {
                    addRecursive(file);
                }
            }
        }
    }

    private void addRecursive(File parentDirectory, String excludePattern) {
        if (parentDirectory != null) {
            addToListViewPaths(parentDirectory.getAbsolutePath());
            File[] listFiles = parentDirectory.listFiles(File::isDirectory);
            if (listFiles != null) {
                for (File file : listFiles) {
                    if (!file.getName().contains(excludePattern)) {
                        addRecursive(file, excludePattern);
                    }
                }
            }
        }
    }

    public void delete() {
        int itemId = lvPaths.getSelectionModel().getSelectedIndex();
        if (itemId >= 0) {
            absolutePaths.remove(lvPaths.getSelectionModel().getSelectedItem().getAbsolutePath());
            lvPaths.getItems().remove(itemId);
        }

        validateLVPaths();
    }

    @Override
    public void save() {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> children = Environment.getInstance().getEnvironmentRootNode().getChildren();

        if (Environment.getInstance().isUsingMakeBuildSystem()) {
            List<IEnvironmentNode> cmakeProjectDirectoryNodes =
                    EnvironmentSearch.searchNode(root, new EnviroCMakeProjDirectoryNode());
            children.removeAll(cmakeProjectDirectoryNodes);
        }

        // Remove all the existing nodes in the current environment
        List<IEnvironmentNode> searchListNodes = EnvironmentSearch.searchNode(root, new EnviroSearchListNode());
        children.removeAll(searchListNodes);

        List<IEnvironmentNode> enviroTypeHandledSourceNodes = EnvironmentSearch.searchNode(root,
                new EnviroTypeHandledSourceDirNode());
        children.removeAll(enviroTypeHandledSourceNodes);

        List<IEnvironmentNode> enviroLibraryIncludeDirNodes = EnvironmentSearch.searchNode(root,
                new EnviroLibraryIncludeDirNode());
        children.removeAll(enviroLibraryIncludeDirNodes);

        System.out.println(Environment.getInstance().getCompiler().toString());

        // Get all new search directories
        if (Environment.getInstance().isUsingMakeBuildSystem()) {
            EnviroMakeBuildSystemNode makeBuildSystemNode = (EnviroMakeBuildSystemNode) EnvironmentSearch.searchNode(root,
                    new EnviroMakeBuildSystemNode()).get(0);
            BuildSystemHandler handler = Environment.getInstance().getMakeBuildSystemManager().getCurrentBSHandler();
            makeBuildSystemNode.setProjectDirectory(tfProjectDirectory.getText());
            handler.setProjectDirectory(tfProjectDirectory.getText());

            if (handler instanceof GNUMakeHandler && makeBuildSystemNode.getGnuMakeBuildType() == 2) {
                ((GNUMakeHandler) handler).setIncludeDirectory(tfIncludeDirectory.getText());
                ((GNUMakeHandler) handler).setSourceDirectory(tfSourceDirectory.getText());
                makeBuildSystemNode.setGnuMakeIncludeDirectory(tfIncludeDirectory.getText());
                makeBuildSystemNode.setGnuMakeSourceDirectory(tfSourceDirectory.getText());
            }
        }

        List<IEnvironmentNode> newSearchListNodes = new ArrayList<>();
        List<IEnvironmentNode> newLibraryListNodes = new ArrayList<>();
        List<IEnvironmentNode> newTypeListNodes = new ArrayList<>();
        for (SourcePath item : lvPaths.getItems()) {
            switch (item.getType()) {
                case SourcePath.SEARCH_DIRECTORY: {
                    EnviroSearchListNode node = new EnviroSearchListNode();
                    node.setSearchList(item.getAbsolutePath());
                    Environment.getInstance().getEnvironmentRootNode().addChild(node);

                    newSearchListNodes.add(node);
                    break;
                }
                case SourcePath.TYPE_HANDLED_DIRECTORY: {
                    EnviroTypeHandledSourceDirNode node = new EnviroTypeHandledSourceDirNode();
                    node.setTypeHandledSourceDir(item.getAbsolutePath());
                    Environment.getInstance().getEnvironmentRootNode().addChild(node);

                    newTypeListNodes.add(node);
                    break;
                }
                case SourcePath.LIBRARY_INCLUDE_DIRECTORY: {
                    EnviroLibraryIncludeDirNode node = new EnviroLibraryIncludeDirNode();
                    node.setLibraryIncludeDir(item.getAbsolutePath());
                    Environment.getInstance().getEnvironmentRootNode().addChild(node);

                    newLibraryListNodes.add(node);
                    break;
                }
            }
        }

        // Save the state of modifying search directories
        if (newSearchListNodes.containsAll(searchListNodes)
                && searchListNodes.containsAll(newSearchListNodes)
                && newLibraryListNodes.containsAll(enviroLibraryIncludeDirNodes)
                && enviroLibraryIncludeDirNodes.containsAll(newLibraryListNodes)
                && newTypeListNodes.containsAll(enviroTypeHandledSourceNodes)
                && enviroTypeHandledSourceNodes.containsAll(newTypeListNodes)) {
            Environment.WindowState.isSearchListNodeUpdated(true); // to avoid analyzing search directories again
        } else
            Environment.WindowState.isSearchListNodeUpdated(true);

        logger.debug("Environment script:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
        logger.debug("Updating the next window (Choose UUTs)");
        logger.debug("Update done!");

        validate();

        ProjectClone.removeLibraries();

        if (!isValid()) {
            if (lvPaths.getItems().isEmpty())
                UIController.showErrorDialog("Directory list can't be empty", "Error", "Locate Source Files");
            else
                UIController.showErrorDialog("Can't select an nonexist directory", "Error", "Locate Source Files");
        }
    }

    @Override
    public void loadFromEnvironment() {
        lvPaths.getItems().clear();

        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();

        // Get CMake project directory
        if (Environment.getInstance().isUsingMakeBuildSystem()) {
            EnviroMakeBuildSystemNode makeBuildSystemNode = (EnviroMakeBuildSystemNode) EnvironmentSearch.searchNode(root,
                    new EnviroMakeBuildSystemNode()).get(0);
            BuildSystemHandler handler = Environment.getInstance().getMakeBuildSystemManager().getCurrentBSHandler();
            tfProjectDirectory.setText(makeBuildSystemNode.getProjectDirectory());
            handler.setProjectDirectory(makeBuildSystemNode.getProjectDirectory());

            if (handler instanceof GNUMakeHandler && makeBuildSystemNode.getGnuMakeBuildType() == 2) {
                tfIncludeDirectory.setText(makeBuildSystemNode.getGnuMakeIncludeDirectory());
                tfSourceDirectory.setText(makeBuildSystemNode.getGnuMakeSourceDirectory());
                ((GNUMakeHandler) handler).setIncludeDirectory(makeBuildSystemNode.getGnuMakeIncludeDirectory());
                ((GNUMakeHandler) handler).setSourceDirectory(makeBuildSystemNode.getGnuMakeSourceDirectory());
            }
        }

        // get search nodes and add to screen
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroSearchListNode());
        for (IEnvironmentNode node : nodes) {
            String path = ((EnviroSearchListNode) node).getSearchList();
            SourcePath sourcePath = new SourcePath(path);
            sourcePath.setType(SourcePath.SEARCH_DIRECTORY);
            lvPaths.getItems().add(sourcePath);
        }

        // get type handled nodes and add to screen
        nodes = EnvironmentSearch.searchNode(root, new EnviroTypeHandledSourceDirNode());
        for (IEnvironmentNode node : nodes) {
            String path = ((EnviroTypeHandledSourceDirNode) node).getTypeHandledSourceDir();
            SourcePath sourcePath = new SourcePath(path);
            sourcePath.setType(SourcePath.TYPE_HANDLED_DIRECTORY);
            lvPaths.getItems().add(sourcePath);
        }

        // get library include nodes and add to screen
        nodes = EnvironmentSearch.searchNode(root, new EnviroLibraryIncludeDirNode());
        for (IEnvironmentNode node : nodes) {
            String path = ((EnviroLibraryIncludeDirNode) node).getLibraryIncludeDir();
            SourcePath sourcePath = new SourcePath(path);
            sourcePath.setType(SourcePath.LIBRARY_INCLUDE_DIRECTORY);
            lvPaths.getItems().add(sourcePath);
        }

        // check the correctness of these above nodes
        validate();
    }

    @FXML
    public void useRelativePath() {
        SourcePath.setUseRelativePath(chbUseRelativePath.isSelected());
        lvPaths.refresh();
    }

    private boolean validateLVPaths() {
        if (lvPaths.getItems().size() == 0)
            return false;
        for (SourcePath path : lvPaths.getItems()) {
            if (!path.isExisted()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateProjectDirectory() {
        if (Environment.getInstance().isUsingMakeBuildSystem()) {
            BuildSystemHandler handler = Environment.getInstance().getMakeBuildSystemManager().getCurrentBSHandler();
            if (handler instanceof GNUMakeHandler) {
                if (tfProjectDirectory.getText() == null || tfIncludeDirectory.getText() == null
                        || tfSourceDirectory.getText() == null)
                    return false;
            } else {
                return tfProjectDirectory.getText() != null;
            }
        }
        return true;
    }

    public void validate() {
        setValid(validateLVPaths() && validateProjectDirectory());

        // highlight the label of this dialog if we found any error
        highlightInvalidStep();
    }

    /**
     * Update locate source files UI base on selected option from Choose Compiler window.
     */
    public void update() {
        boolean isUsingMakeBuildSystem = Environment.getInstance().isUsingMakeBuildSystem();
        lProjectDirectory.setVisible(isUsingMakeBuildSystem);
        lvPaths.setLayoutY(60);
        lvPaths.setVisible(true);
        apIncludeSourceDirectory.setVisible(false);
        tfProjectDirectory.setVisible(isUsingMakeBuildSystem);
        tfProjectDirectory.setText("");
        tfSourceDirectory.setText("");
        tfIncludeDirectory.setText("");
        bBrowse.setVisible(isUsingMakeBuildSystem);
        bSaveAndShowDirs.setDisable(!isUsingMakeBuildSystem);
        bShowIncludeAndSource.setVisible(false);
        if (isUsingMakeBuildSystem) {
            lvPaths.setLayoutY(90);
            EnviroMakeBuildSystemNode node = (EnviroMakeBuildSystemNode) EnvironmentSearch.searchNode(
                    Environment.getInstance().getEnvironmentRootNode(), new EnviroMakeBuildSystemNode()).get(0);
            System.out.println(buildSystem + " " + node.getBuildSystem());
            System.out.println(lvPaths.getItems());
            if (buildSystem.equals("") || node.getBuildSystem().equals(buildSystem))
                tfProjectDirectory.setText(node.getProjectDirectory());
            else lvPaths.getItems().clear();

            if (node.getBuildSystem().equals("GNU Make") && node.getGnuMakeBuildType() == 2) {
                apIncludeSourceDirectory.setVisible(true);
                lvPaths.setVisible(false);
                if (node.getBuildSystem().equals(buildSystem)) {
                    tfIncludeDirectory.setText(node.getGnuMakeIncludeDirectory());
                    tfSourceDirectory.setText(node.getGnuMakeSourceDirectory());
                }
                bSaveAndShowDirs.setDisable(true);
            }

            buildSystem = node.getBuildSystem();
        } else {
            lvPaths.setLayoutY(60);
            lvPaths.setPrefHeight(330);
        }
    }

    public void onBrowseProjectDirectory() {
        DirectoryChooser directoryChooser = PersistentDirectoryChooser.getInstance();
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File file = directoryChooser.showDialog(envBuilderStage);
        if (file != null) {
            BuildSystemHandler handler = Environment.getInstance().getMakeBuildSystemManager().getCurrentBSHandler();
            if (handler.findBuildFile(file, true)) {
                tfProjectDirectory.setText(file.getAbsolutePath());
                if (handler instanceof CMakeHandler)
                    addRecursive(file);
                else {
                    lvPaths.setVisible(false);
                    tfIncludeDirectory.setText(null);
                    tfSourceDirectory.setText(null);
                }
            }
        }
    }

    public void onChooseSourceDirectory() {
        File file = chooseChildDirectoryOfParentFolder(tfProjectDirectory.getText());
        if (file != null) {
            String sourceDir = file.getAbsolutePath().replace(tfProjectDirectory.getText(), "");
            sourceDir = sourceDir.replaceFirst("/", "");
            tfSourceDirectory.setText(file.getAbsolutePath().replace(tfProjectDirectory.getText(), ""));
        }
        verifyIncludeAndSourceDirectoryChosen();
    }

    public void onChooseIncludeDirectory() {
        File file = chooseChildDirectoryOfParentFolder(tfProjectDirectory.getText());
        if (file != null) {
            String includeDir = file.getAbsolutePath().replace(tfProjectDirectory.getText(), "");
            includeDir = includeDir.replaceFirst("/", "");
            tfIncludeDirectory.setText(includeDir);
        }
        verifyIncludeAndSourceDirectoryChosen();
    }

    private void verifyIncludeAndSourceDirectoryChosen() {
        bSaveAndShowDirs.setDisable(tfSourceDirectory.getText() == null || tfIncludeDirectory.getText() == null);
    }

    private File chooseChildDirectoryOfParentFolder(String parentPath) {
        DirectoryChooser directoryChooser = PersistentDirectoryChooser.getInstance();
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File file = directoryChooser.showDialog(envBuilderStage);
        if (file != null && !PathUtils.isParentFolderContainsChild(parentPath, file.getAbsolutePath())) {
            logger.error("The " + file.getAbsolutePath() + " is not a child of " + tfProjectDirectory.getText());
            UIController.showErrorDialog("The " + file.getAbsolutePath() + " is not a child of " + tfProjectDirectory.getText(),
                    "Error", "Locate Source Files");

            return null;
        }

        return file;
    }
}
