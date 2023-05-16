package com.dse.make_build_system;

import com.dse.compiler.Terminal;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.object.EnviroMakeBuildSystemNode;
import com.dse.guifx_v3.helps.UIController;
import com.dse.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class GNUMakeHandler extends BuildSystemHandler {
    public static String GNU_MAKE_CHECK_CMD = "make --version";
    public static String AKAIGNORE_MAKEFILE_BUILD_ENV = "Makefile.akaignore.build_env";
    public static String AKAIGNORE_MAKEFILE_TEST_DRIVER = "Makefile.akaignore.test_driver";
    public static String MAKE_BUILD_FOLDER_NAME = "aka_make_build";
    public static String EXECUTABLE_NAME = "app";
    private int buildType = -1;
    private String buildTarget = "";
    private String includeDirectory = "";
    private String sourceDirectory = "";
    public GNUMakeHandler() {
        buildSystem = "GNU Make";
        buildFile = "Makefile";
    }

    public String getIncludeDirectory() {
        return includeDirectory;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public int getBuildType() {
        return buildType;
    }

    public String getBuildTarget() {
        return buildTarget;
    }

    public void setIncludeDirectory(String includeDirectory) {
        this.includeDirectory = includeDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setBuildType(int buildType) {
        this.buildType = buildType;
    }

    public void setBuildTarget(String buildTarget) {
        this.buildTarget = buildTarget;
    }

    @Override
    public boolean verifyBuildSystemExist() {
        try {
            Terminal terminal = new Terminal(GNU_MAKE_CHECK_CMD);

            if (terminal.getStdout().contains("GNU Make")) {
                return true;
            } else {
                logger.error("Cannot find GNU Make in your PATH environment variable!");
                UIController.showErrorDialog("Cannot find GNU Make in your PATH", "Error", "GNU Make Handler");
                return false;
            }

        } catch (InterruptedException | IOException e) {
            logger.error(e.getMessage());
            UIController.showErrorDialog(e.getMessage(), "Error", "GNU Make Handler");
            return false;
        }
    }

    public void cloneCurrentProjectToInstrumentDirectory() {
        try {
            String instrumentDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory();
            Utils.deleteFileOrFolder(new File(instrumentDirectory));
            Utils.copy(new File(projectDirectory), new File(instrumentDirectory));
            logger.debug("Project cloned to instrument directory");

            logger.info("Starting cloning Makefile files");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void importConfigFromNode(EnviroMakeBuildSystemNode node) {
        super.importConfigFromNode(node);
        buildFile = "Makefile";
        buildType = node.getGnuMakeBuildType();
        buildTarget = node.getGnuMakeTarget();
        includeDirectory = node.getGnuMakeIncludeDirectory();
        sourceDirectory = node.getGnuMakeSourceDirectory();
    }

    public void reset() {
        super.reset();
        buildType = -1;
        buildTarget = "";
        includeDirectory = "";
        sourceDirectory = "";
    }

    public void doNecessaryModify() {}

    public void prepareForBuild() {
        String instrumentDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory();

        if (buildType == 0) {

        } else {
            String buildEnvMakefilePath =
                    new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + AKAIGNORE_MAKEFILE_BUILD_ENV;
            String testDriverMakefilePath =
                    new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + AKAIGNORE_MAKEFILE_TEST_DRIVER;
            File buildEnvTemplateMakefile =
                    new File(Objects.requireNonNull(GNUMakeHandler.class.getResource("/make-template/Makefile_for_build_env")).getFile());
            File testDriverTemplateMakefile =
                    new File(Objects.requireNonNull(GNUMakeHandler.class.getResource("/make-template/Makefile_for_test_driver")).getFile());
            try {
                Utils.copy(buildEnvTemplateMakefile, new File(buildEnvMakefilePath));
                Utils.copy(testDriverTemplateMakefile, new File(testDriverMakefilePath));
                modifyMakefile(new File(buildEnvMakefilePath));
                modifyMakefile(new File(testDriverMakefilePath));
                executableFilePath = instrumentDirectory + File.separator + EXECUTABLE_NAME;
                if (Utils.isWindows()) {
                    executableFilePath += ".exe";
                }
                Utils.copy(new File(buildEnvMakefilePath), new File(instrumentDirectory + File.separator + buildFile));
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

    }

    private void modifyMakefile(File makefile) {
        String content = Utils.readFileContent(makefile);
        //content = content.replaceFirst("SRCDIRS               :=", "SRCDIRS               := " + sourceDirectory);
        content = content.replaceFirst("INCLUDE                =", "INCLUDE                = -I " + includeDirectory);
        Utils.writeContentToFile(content, makefile.getAbsolutePath());
    }

    @Override
    public String buildProject(boolean cleanBuild) {
        return buildProject(cleanBuild, this.projectDirectory);
    }

    @Override
    public String buildProject(boolean cleanBuild, String projectDirectory) {
        String buildScript = "make " + buildTarget;
        String cleanScript = "make clean";
        StringBuilder result = new StringBuilder();
        try {
            Terminal terminal;
            if (cleanBuild) {
                terminal = new Terminal(cleanScript, projectDirectory);
                result.append(terminal.get());
            }

            terminal = new Terminal(buildScript, projectDirectory);
            result.append(terminal.get());
            logger.debug("Build result: " + result.toString());
            return result.toString();
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
            UIController.showErrorDialog(e.getMessage(), "Error", "GNU Make Handler");
            return e.getMessage();
        }
    }

    public String generateExecutableFile() {
        return "";
    }

    public String linkTestDriverToMakeBuildFile(String testDriverPath) {
        String instrumentDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory();
        String testDriverMakefilePath = instrumentDirectory + File.separator + AKAIGNORE_MAKEFILE_TEST_DRIVER;
        StringBuilder result = new StringBuilder();
        if (buildType == 0) {

        } else {
            try {
                Utils.copy(new File(testDriverPath),
                        new File(instrumentDirectory + File.separator + sourceDirectory + File.separator + "test_driver.cpp"));
                result.append("Test driver has been linked to the instrument directory");

                Utils.copy(new File(testDriverMakefilePath), new File(instrumentDirectory + File.separator + buildFile));
            } catch (IOException e) {
                logger.error(e.getMessage());
                result.append(e.getMessage());
            }
        }

        return result.toString();
    }
}
