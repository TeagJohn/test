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
import com.dse.project_init.ProjectClone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LocateSourceFilesController extends AbstractCustomController implements Initializable {
    @FXML
    private TextField tfCmakeProjDirectory;
    @FXML
    private Label lDirectory;
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
    private Button bAddSubDir;
    @FXML
    private Button bBrowse;

    private final List<String> absolutePaths = new ArrayList<>();

    public void initialize(URL location, ResourceBundle resources) {
        lvPaths.setCellFactory(new LocateSourceFilesCellFactory());
        bAdd.setText(null);
        Hint.tooltipNode(bAdd, HintContent.EnvBuilder.SrcLocation.ADD);
        Image add = new Image(Factory.class.getResourceAsStream("/icons/add.png"));
        bAdd.setGraphic(new ImageView(add));
        bDelete.setText(null);
        Hint.tooltipNode(bDelete, HintContent.EnvBuilder.SrcLocation.DELETE);
        Image delete = new Image(Factory.class.getResourceAsStream("/icons/delete.png"));
        bDelete.setGraphic(new ImageView(delete));
        bAddRecursive.setText(null);
        Hint.tooltipNode(bAddRecursive, HintContent.EnvBuilder.SrcLocation.ADD_RECURSIVE);
        Image addRecursive = new Image(Factory.class.getResourceAsStream("/icons/addRecursive.png"));
        bAddRecursive.setGraphic(new ImageView(addRecursive));
        bAddSubDir.setText(null);
        Hint.tooltipNode(bAddSubDir, HintContent.EnvBuilder.SrcLocation.ADD_SUB_DIR);
        Image addSubDirImage = new Image(Factory.class.getResourceAsStream("/icons/addSubDir.png"));
        bAddSubDir.setGraphic(new ImageView(addSubDirImage));

        lDirectory.setDisable(true);
        tfCmakeProjDirectory.setDisable(true);
        bBrowse.setDisable(true);
        Hint.tooltipNode(bBrowse, HintContent.EnvBuilder.SrcLocation.BROWSE);

        Hint.tooltipNode(chbUseRelativePath, HintContent.EnvBuilder.SrcLocation.VIEW_RELATED);
        instance = this;
    }

    public File showChooseFolderDialog() {
        DirectoryChooser directoryChooser = PersistentDirectoryChooser.getInstance();
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File file = null;
        try {
            file = directoryChooser.showDialog(envBuilderStage);
        } catch (Exception e) {
            directoryChooser = PersistentDirectoryChooser.getNewInstance();
            file = directoryChooser.showDialog(envBuilderStage);
        }
        if (file != null) {
            directoryChooser.setInitialDirectory(file);
        }
        return file;
    }

    @FXML
    public void addTheRootFolder() {
        File file = showChooseFolderDialog();
        if (file != null) {
            addToListViewPaths(file.getAbsolutePath());
        }
        validate();
    }

    @FXML
    public void addRecursiveFolders() {
        File file = showChooseFolderDialog();
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

    @FXML
    public void addOnlySubDir() {
        DirectoryChooser directoryChooser = PersistentDirectoryChooser.getInstance();
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File file = directoryChooser.showDialog(envBuilderStage);
        if (file != null) {
            directoryChooser.setInitialDirectory(file);
        }
        addSubDir(file);
        validate();
    }

    @FXML
    private void addSubDir(File parentDir) {
        if (parentDir != null) {
            File[] listFiles = parentDir.listFiles(File::isDirectory);
            if (listFiles != null) {
                for (File file : listFiles) {
                    addRecursive(file);
                }
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

        if (Environment.getInstance().getCompiler().isCmakeProject()) {
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
        if (Environment.getInstance().getCompiler().isCmakeProject()) {
            EnviroCMakeProjDirectoryNode newCMakeProjectDirectoryNode = new EnviroCMakeProjDirectoryNode();
            newCMakeProjectDirectoryNode.setDirectoryPath(tfCmakeProjDirectory.getText());
            Environment.getInstance().getEnvironmentRootNode().addChild(newCMakeProjectDirectoryNode);
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
        if (Environment.getInstance().getCompiler().isCmakeProject()) {
            List<IEnvironmentNode> cmakeProjectDirectoryNodes =
                    EnvironmentSearch.searchNode(root, new EnviroCMakeProjDirectoryNode());
            if (cmakeProjectDirectoryNodes.size() > 0) {
                EnviroCMakeProjDirectoryNode cmakeProjectDirectoryNode =
                        (EnviroCMakeProjDirectoryNode) cmakeProjectDirectoryNodes.get(0);
                tfCmakeProjDirectory.setText(cmakeProjectDirectoryNode.getDirectoryPath());
                lDirectory.setDisable(false);
                tfCmakeProjDirectory.setDisable(false);
                bBrowse.setDisable(false);
            } else {
                tfCmakeProjDirectory.setText("");
                UIController.showErrorDialog("CMake project directory is not set", "Error", "Locate Source Files");
            }
        } else {
            tfCmakeProjDirectory.setText("");
            lDirectory.setDisable(true);
            tfCmakeProjDirectory.setDisable(true);
            bBrowse.setDisable(true);
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

    private boolean validateCMakeProjDirectory() {
        if (Environment.getInstance().getCompiler().isCmakeProject()) {
            if (tfCmakeProjDirectory.getText().isEmpty())
                return false;
            return new File(tfCmakeProjDirectory.getText()).isDirectory();
        }
        return true;
    }

    public void validate() {
        setValid(validateLVPaths() && validateCMakeProjDirectory());

        // highlight the label of this dialog if we found any error
        highlightInvalidStep();
    }

    public void switchCMakeMode(boolean isCMakeMode) {
        lDirectory.setDisable(!isCMakeMode);
        tfCmakeProjDirectory.setDisable(!isCMakeMode);
        bBrowse.setDisable(!isCMakeMode);
    }

    public void addCmakeProjectDirectory() {
        File file = showChooseFolderDialog();
        if (file != null) {
            if (findCMakeListsFile(file)) {
                tfCmakeProjDirectory.setText(file.getAbsolutePath());
                addRecursive(file, "build");
                validate();
            } else {
                UIController.showErrorDialog("The directory does not contain CMakeLists.txt file", "Error", "Locate Source Files");
            }
        }
    }

    private boolean findCMakeListsFile(File projectDirectory) {
        if (projectDirectory != null) {
            File[] files = projectDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().equals("CMakeLists.txt")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static LocateSourceFilesController instance;

    public static LocateSourceFilesController getInstance() {
        return instance;
    }
}
