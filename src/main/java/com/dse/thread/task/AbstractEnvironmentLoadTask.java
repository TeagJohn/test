package com.dse.thread.task;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.PhysicalTreeImporter;
import com.dse.environment.WorkspaceLoader;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.VectorCastProjectLoader;
import com.dse.parser.object.FolderNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.Node;
import com.dse.parser.object.ProjectNode;
import com.dse.regression.ChangesBetweenSourcecodeFiles;
import com.dse.regression.cia.WaveCIA;
import com.dse.thread.AbstractAkaTask;
import com.dse.user_code.envir.EnvironmentUserCode;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractEnvironmentLoadTask extends AbstractAkaTask<Void> {

    protected final static AkaLogger logger = AkaLogger.get(AbstractEnvironmentLoadTask.class);

    protected static final int MAX_PROGRESS = 10;

    protected WorkspaceLoader generateLoader() {
        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();
        File physicalTreeFile = new File(physicalTreePath);

        Node root = new PhysicalTreeImporter().importTree(physicalTreeFile);
        Environment.getInstance().setProjectNode((ProjectNode) root);

        // parse the source code file lists
        VectorCastProjectLoader load = new VectorCastProjectLoader();

        File fileRoot = new File(root.getAbsolutePath());
        List<File> sourceCodeListFolder = Utils.getAllFolderAndSubfolder(fileRoot);

//        load.setSourcecodeList(Arrays.asList(new File(root.getAbsolutePath())));
        load.setSourcecodeList(sourceCodeListFolder);
        INode projectRootNode = load.constructPhysicalTree();

//        for (Node node : projectRootNode.getChildren()) {
//            boolean isChild = false;
//            for (INode child : root.getChildren()) {
//                if (child.getAbsolutePath().equals(node.getAbsolutePath())) {
//                    isChild = true;
//                    break;
//                }
//            }
//            if (!isChild) {
//                root.getChildren().add(node);
//                node.setParent(root);
//                ChangesBetweenSourcecodeFiles.addedNodes.add(node);
//                logger.debug("Added new file " + node.getAbsolutePath());
//
//                for (Node nodeChild : node.getChildren()) {
//                    if (!ChangesBetweenSourcecodeFiles.addedNodes.contains(nodeChild)) {
//                        ChangesBetweenSourcecodeFiles.addedNodes.add(nodeChild);
//                        logger.debug("Added new file " + nodeChild.getAbsolutePath());
//                    }
//                }
//            }
//        }
        addNewFileAndNewSubs(root, projectRootNode);
        logger.debug("Load project physical tree successful");

        WorkspaceLoader loader = new WorkspaceLoader(root);
        loader.setPhysicalTreePath(physicalTreeFile);
        loader.setShouldCompileAgain(true);
        loader.setElementFolderOfOldVersion(new WorkspaceConfig().fromJson().getElementDirectory());

        return loader;
    }

    public void addNewFileAndNewSubs(INode rootNode, INode newNode) {
        if (!rootNode.getAbsolutePath().equals(newNode.getAbsolutePath())) {
            return;
        }
        for (Node newChild : newNode.getChildren()) {
            boolean isChild = false;
            Node child = null;
            for (Node rootChild : rootNode.getChildren()) {
                if (rootChild.getAbsolutePath().equals(newChild.getAbsolutePath())) {
                    isChild = true;
                    child = rootChild;
                    break;
                }
            }
            if (!isChild) {
                rootNode.getChildren().add(newChild);
                newChild.setParent(rootNode);
                logger.debug("Add new file " + newChild.getAbsolutePath());
            }
            else addNewFileAndNewSubs(child, newChild);
        }
    }

    @Override
    protected Void call() throws Exception {
        EnvironmentUserCode.getInstance().importFromFile();

        // STEP: load project
        // if there are any compilation errors, we show it to users
//        LoadingPopupController.getInstance().setText("Retrieve project data");
//        if (this instanceof PreUpdateEnvironmentTask)
//            Thread.sleep(1000);

        ChangesBetweenSourcecodeFiles.reset();
        WorkspaceLoader loader = generateLoader();

        // GUI
        updateProgress(1, MAX_PROGRESS);

//        LoadingPopupController.getInstance().setText("Construct project structure tree");
//        if (this instanceof PreUpdateEnvironmentTask)
//            Thread.sleep(1000);

        loader.load(loader.getPhysicalTreePath());

//        if (this instanceof PreUpdateEnvironmentTask)
//            Thread.sleep(1000);

        // todo: can hoi y kien anh Duc Anh
        while (!loader.isLoaded()) {
//            System.out.println("Loading");
            Thread.sleep(100);
        }

        if (loader.isCancel()) {
            // STOP LOADING ENVIRONMENT
            logger.debug("Rebuild Environment Task was canceled");
            Environment.restoreEnvironment();
            if (Environment.getInstance() == null) Environment.createNewEnvironment();
            return null;
        }

        for (INode addedNodeDetected : ChangesBetweenSourcecodeFiles.addedNodes) {
            WaveCIA.getWaveCIA().addAddedNode(addedNodeDetected);
        }
        ChangesBetweenSourcecodeFiles.addedNodes.clear();
        ChangesBetweenSourcecodeFiles.addedNodes.addAll(WaveCIA.getWaveCIA().getAddedNodes());

        INode root = loader.getRoot();
        updateProgress(6, MAX_PROGRESS);

        boolean findCompilationError = root == null;
        // so findCompilationError == true if there are error when compile OR the compile process was stopped
        if (findCompilationError) {
            Environment.restoreEnvironment();
            String compilationError = SpecialCharacter.EMPTY;
            File file = new File(new WorkspaceConfig().fromJson().getCompilationMessageWhenComplingProject());
            if (file.exists())
                compilationError = Utils.readFileContent(file);
            UIController.showDetailDialogInMainThread(Alert.AlertType.ERROR, "Compilation error",
                    "Found compilation error. The environment does not change!",
                    compilationError);
            updateProgress(MAX_PROGRESS, MAX_PROGRESS);
            // STOP LOADING ENVIRONMENT
            return null;
        }

        // STEP: If all source code files are compiled successfully,
        // we need to check whether any source code files are modified.
        // If we found at least one, show it to users
        if (isChanged()) {
            handleChanges();
        } else {
            handleNoChange();
        }
        updateProgress(MAX_PROGRESS, MAX_PROGRESS);
        return null;
    }

    protected boolean isChanged() {
        return !ChangesBetweenSourcecodeFiles.modifiedSourcecodeFiles.isEmpty();
    }

    protected abstract void handleNoChange();

    protected abstract void handleChanges();
}
