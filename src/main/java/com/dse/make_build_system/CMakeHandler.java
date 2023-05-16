package com.dse.make_build_system;

import com.dse.cmake.CMakeBuilder;
import com.dse.compiler.Terminal;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroMakeBuildSystemNode;
import com.dse.guifx_v3.helps.UIController;
import com.dse.util.PathUtils;
import com.dse.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class CMakeHandler extends BuildSystemHandler {
    public static String CMAKE_CHECK_CMD = "cmake --version";
    public static String CMAKE_BUILD_FOLDER_NAME = "aka_cmake_build";
    public static String AKAIGNORE_CMAKE_LIST = "CMakeLists.akaignore.txt";
    public static String DEFAULT_CMAKE_LIST = "CMakeLists.txt";
    public static String CMAKE_CACHE_FILE_NAME = "CMakeCache.txt";
    public static String CMAKE_FILES_FOLDER_NAME = "CMakeFiles";
    private String cmakeGenerator = "";

    public CMakeHandler() {
        buildSystem = "CMake";
        buildFile = "CMakeLists.txt";
    }

    public void importConfigFromNode(EnviroMakeBuildSystemNode node) {
        super.importConfigFromNode(node);
        buildFile = "CMakeLists.txt";
        cmakeGenerator = node.getCMakeGenerator();
    }

    public void reset() {
        super.reset();
        cmakeGenerator = "";
    }

    @Override
    public boolean verifyBuildSystemExist() {
        try {
            Terminal terminal = new Terminal(CMAKE_CHECK_CMD);

            if (terminal.getStdout().contains("cmake version")) {
                return true;
            } else {
                logger.error("Cannot find cmake in your PATH environment variable!");
                UIController.showErrorDialog("Cannot find cmake in your PATH", "Error", "CMake Handler");
                return false;
            }

        } catch (InterruptedException | IOException e) {
            logger.error(e.getMessage());
            UIController.showErrorDialog(e.getMessage(), "Error", "CMake Handler");
            return false;
        }
    }

    public void cloneCurrentProjectToInstrumentDirectory() {
        try {
            String instrumentDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory();
            Utils.copy(new File(projectDirectory), new File(instrumentDirectory));
            logger.debug("Project cloned to instrument directory");

            logger.info("Starting cloning CMakeLists files");
            cloneAllCMakeListsFiles(instrumentDirectory);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void cloneAllCMakeListsFiles(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                cloneAllCMakeListsFiles(file.getAbsolutePath());
            } else {
                if (file.getName().equals(DEFAULT_CMAKE_LIST)) {
                    String cmakelistsCopyPath = directoryPath + File.separator + AKAIGNORE_CMAKE_LIST;

                    Utils.copy(file, new File(cmakelistsCopyPath));
                    logger.debug("CMakeLists.txt copied to " + cmakelistsCopyPath);
                }
            }
        }
    }

    public void doNecessaryModify() {
        String instrumentDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory();
        modifyAllCMakeListsFileInDirectory(new File(instrumentDirectory));
        setExecutableFilePathFromCMakeLists(new File(instrumentDirectory + File.separator + CMakeBuilder.AKAIGNORE_CMAKE_LIST));
    }

    public void prepareForBuild() {}

    /**
     * Replace these file with akaignore.ext (ex:
     *
     * .h => .akaignore.h,
     * .hpp => .akaignore.hpp,
     * .c => .akaignore.c,
     * .cpp => .akaignore.cpp,
     * )
     */
    private void modifyCMakeListsFile(String cmakeListsPath) {
        File cmakelistsFile = new File(cmakeListsPath);
        String cmakelistsContent = Utils.readFileContent(cmakelistsFile);
        StringBuilder finalContent = new StringBuilder();

        for (int i = 0; i < cmakelistsContent.length(); i++) {
            if (cmakelistsContent.charAt(i) == '.') {
                if (cmakelistsContent.substring(i, i + 2).equals(".h")) {
                    if (!cmakelistsContent.substring(i, i + 4).equals(".hpp")) {
                        finalContent.append(".akaignore.h");
                        i += 1;
                    } else {
                        finalContent.append(".akaignore.hpp");
                        i += 3;
                    }
                } else if (cmakelistsContent.substring(i, i + 2).equals(".c")) {
                    if (cmakelistsContent.substring(i, i + 6).equals(".cmake")) {
                        finalContent.append(".cmake");
                        i += 5;
                        continue;
                    }

                    if (!cmakelistsContent.substring(i, i + 4).equals(".cpp")) {
                        finalContent.append(".akaignore.c");
                        i += 1;
                    } else {
                        finalContent.append(".akaignore.cpp");
                        i += 3;
                    }
                } else {
                    finalContent.append(cmakelistsContent.charAt(i));
                }
            } else
                finalContent.append(cmakelistsContent.charAt(i));
        }

        Utils.writeContentToFile(finalContent.toString(), cmakeListsPath);
    }

    public void modifyAllCMakeListsFileInDirectory(File currentDirectory) {
        File[] files = currentDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    modifyAllCMakeListsFileInDirectory(file);
                } else {
                    if (file.getName().endsWith(".h.in")) {
                        file.renameTo(new File(file.getAbsolutePath().replace(".h.in", ".akaignore.h.in")));
                    } else if (file.getName().equals(AKAIGNORE_CMAKE_LIST)) {
                        modifyCMakeListsFile(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void setExecutableFilePathFromCMakeLists(File cmakeListsFile) {
        String cmakeListsContent = Utils.readFileContent(cmakeListsFile);
        String projectName = "";

        // Get project name from CMakeLists.txt
        for (int i = cmakeListsContent.indexOf("project(") + 9; cmakeListsContent.charAt(i) != ')'; i++) {
            projectName += cmakeListsContent.charAt(i);
        }

        String executableFileName = "";
        // Get executable file name from CMakeLists.txt
        if (cmakeListsContent.contains("add_executable")) {
            int index = cmakeListsContent.indexOf("add_executable(") + 15;
            while (cmakeListsContent.charAt(index) == ' ' || cmakeListsContent.charAt(index) == '\t'
                    || cmakeListsContent.charAt(index) == '\n') {
                index++;
            }
            for (; cmakeListsContent.charAt(index) != ' ' && cmakeListsContent.charAt(index) != '\t'
                    && cmakeListsContent.charAt(index) != '\n'; index++) {
                executableFileName += cmakeListsContent.charAt(index);
            }

            if (executableFileName.equals("${PROJECT_NAME}")) {
                executableFileName = projectName;
            }

            executableFilePath = new WorkspaceConfig().fromJson().getInstrumentDirectory()
                    + File.separator + CMAKE_BUILD_FOLDER_NAME + File.separator + executableFileName;

            if (Utils.isWindows()) {
                executableFilePath += ".exe";
            }

            logger.info("Executable file path: " + executableFilePath);
        }
    }

    public String buildProject(boolean cleanBuild) {
        return buildProject(cleanBuild, this.projectDirectory);
    }

    public String buildProject(boolean cleanBuild, String projectDirectory) {
        try {
            if (cleanBuild) {
                Utils.deleteFileOrFolder(
                        new File(projectDirectory + File.separator + CMAKE_BUILD_FOLDER_NAME + File.separator + CMAKE_CACHE_FILE_NAME));

                Utils.deleteFileOrFolder(
                        new File(projectDirectory + File.separator + CMAKE_BUILD_FOLDER_NAME + File.separator + CMAKE_FILES_FOLDER_NAME));
            }

            String buildDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + CMAKE_BUILD_FOLDER_NAME;
            String[] buildScripts = {"cmake", "-G", cmakeGenerator, "-S", projectDirectory, "-B", buildDirectory};

            logger.debug("Build directory: " + buildDirectory);
            logger.debug("Build script: " + Arrays.stream(buildScripts).reduce("", (a, b) -> a + " " + b));

            Terminal terminal = new Terminal(buildScripts);

            if (terminal.getStderr().contains("CMake Error")) {
                logger.error(terminal.getStderr());
                UIController.showErrorDialog("Project builded failed!", "Error", "CMake Builder");
                return terminal.get();
            } else {
                logger.debug("Project builded successfully");
                return terminal.get();
            }
        } catch (InterruptedException | IOException e) {
            logger.error(e.getMessage());
            UIController.showErrorDialog(e.getMessage(), "Error", "CMake Builder");
            return e.getMessage();
        }
    }

    public String generateExecutableFile() {
        try {
            String buildDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + CMAKE_BUILD_FOLDER_NAME;
            String[] compileScripts = {"cmake", "--build", buildDirectory};

            logger.debug(
                    "Generate executable file command: "
                            + Arrays.stream(compileScripts).reduce("", (a, b) -> a + " " + b));

            Terminal terminal = new Terminal(compileScripts);

            if (terminal.getStderr().contains("CMake Error")) {
                logger.error(terminal.getStderr());
                UIController.showErrorDialog("Compile project failed!", "Error", "CMake Builder");
                return terminal.get();
            } else {
                logger.debug("Project compiled successfully");
                return terminal.get();
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            UIController.showErrorDialog("Cannot compile project!", "Error", "CMake Builder");
            return e.getMessage();
        }
    }

    public String linkTestDriverToMakeBuildFile(String testDriverPath) throws IOException {
        String instrumentDir = new WorkspaceConfig().fromJson().getInstrumentDirectory();
        String cmakelistCopyPath = instrumentDir + File.separator + AKAIGNORE_CMAKE_LIST;
        String cmakelistClonePath = instrumentDir + File.separator + DEFAULT_CMAKE_LIST;

        Utils.copy(new File(cmakelistCopyPath), new File(cmakelistClonePath));
        return linkTestDriverToEachCMakeListsFile(instrumentDir, testDriverPath);
    }

    private String linkTestDriver(String testDriverPath, String cmakelistsPath) {
        File cmakeListFile = new File(cmakelistsPath);
        if (!cmakeListFile.exists()) {
            logger.error("CMakeLists.txt does not exist in " + cmakelistsPath);
            return "Error: CMakeLists.txt does not exist in " + cmakelistsPath;
        }

        String cmakeListsContent = Utils.readFileContent(cmakeListFile);

        if (cmakeListsContent.contains("add_executable")) {
            int index = cmakeListsContent.indexOf("add_executable(") + 15;
            while (cmakeListsContent.charAt(index) == ' ' || cmakeListsContent.charAt(index) == '\t'
                    || cmakeListsContent.charAt(index) == '\n') {
                index++;
            }

            // keep increase index through project name or ${PROJECT_NAME}
            while (cmakeListsContent.charAt(index) != ' ' && cmakeListsContent.charAt(index) != '\t'
                    && cmakeListsContent.charAt(index) != '\n') {
                index++;
            }

            String modifiedCMakeListContent = cmakeListsContent.substring(0, index)
                    + " \"" + PathUtils.replaceBackslashWithSlash(testDriverPath) + "\""
                    + cmakeListsContent.substring(index);

            logger.debug("Modified CMakeLists.txt: " + modifiedCMakeListContent);

            Utils.writeContentToFile(modifiedCMakeListContent, cmakelistsPath);
            logger.debug("Test driver linked to CMakeLists.txt in " + cmakelistsPath);
            return "Test driver linked to CMakeLists.txt in " + cmakelistsPath;
        }

        return "";
    }

    private String linkTestDriverToEachCMakeListsFile(String directoryPath, String testDriverPath) throws IOException {
        StringBuilder result = new StringBuilder();
        File directory = new File(directoryPath);
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                result.append(linkTestDriverToEachCMakeListsFile(file.getAbsolutePath(), testDriverPath));
                result.append("\n");
            } else {
                if (file.getName().equals(DEFAULT_CMAKE_LIST)) {
                    Utils.copy(new File(directoryPath + File.separator + AKAIGNORE_CMAKE_LIST), file);
                    result.append(linkTestDriver(testDriverPath, file.getAbsolutePath()));
                    result.append("\n");
                }
            }
        }

        return result.toString();
    }

    public String getCmakeGenerator() {
        return cmakeGenerator;
    }

    public void setCmakeGenerator(String cmakeGenerator) {
        this.cmakeGenerator = cmakeGenerator;
    }

    @Override
    public String getExecutableFilePath() {
        if (executableFilePath.equals("")) {
            String cmakeListsPath =
                    new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + DEFAULT_CMAKE_LIST;
            File cmakeListsFile = new File(cmakeListsPath);

            if (cmakeListsFile.exists()) {
                setExecutableFilePathFromCMakeLists(cmakeListsFile);
            } else {
                logger.error("Cannot get executable file path from /instrument/CMakeLists.txt");
            }
        }

        return executableFilePath;
    }
}
