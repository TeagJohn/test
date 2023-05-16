package com.dse.make_build_system;

import com.dse.environment.object.EnviroMakeBuildSystemNode;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.io.IOException;

public abstract class BuildSystemHandler {
    final AkaLogger logger = AkaLogger.get(BuildSystemHandler.class);
    protected String buildSystem = "";
    protected String buildFile = "";
    protected String projectDirectory = "";
    protected String executableFilePath = "";

    public BuildSystemHandler() {

    }

    public boolean findBuildFile(File folder, boolean showDialogWhenError) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(buildFile)) {
                    return true;
                }
            }
        }

        if (showDialogWhenError) {
            logger.error("Cannot find " + buildFile + " in " + folder.getAbsolutePath());
            UIController.showErrorDialog("Cannot find " + buildFile + " in " + folder.getAbsolutePath(),
                    "Error", buildSystem + " Handler");
        }
        return false;
    }

    public boolean findBuildFile(File folder) {
        return findBuildFile(folder, false);
    }

    public String getProjectDirectory() {
        return projectDirectory;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public String getExecutableFilePath() {
        return executableFilePath;
    }
    public void importConfigFromNode(EnviroMakeBuildSystemNode node) {
        buildSystem = node.getBuildSystem();
        projectDirectory = node.getProjectDirectory();
        executableFilePath = node.getExecutableFilePath();
    }

    public void reset() {
        buildSystem = "";
        buildFile = "";
        projectDirectory = "";
        executableFilePath = "";
    }

    public abstract boolean verifyBuildSystemExist();
    public abstract void cloneCurrentProjectToInstrumentDirectory();
    public abstract void prepareForBuild();
    public abstract void doNecessaryModify();
    public abstract String buildProject(boolean cleanBuild);
    public abstract String buildProject(boolean cleanBuild, String projectDirectory);
    public abstract String generateExecutableFile();
    public abstract String linkTestDriverToMakeBuildFile(String testDriverPath) throws IOException;
}
