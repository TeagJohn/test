package com.dse.cmake;

import com.dse.compiler.Terminal;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroCMakeProjDirectoryNode;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CMakeBuilder {
    final static AkaLogger logger = AkaLogger.get(CMakeBuilder.class);

    public static String CMAKE_CHECK_CMD = "cmake --version";
    public static String REMOVE_CMAKE_CACHE_AND_CMAKEFILES_CMD = "rm -rf CMakeCache.txt";
    public static String CMAKE_BUILD_FOLDER_NAME = "aka_cmake_build";
    public static String AKAIGNORE_CMAKE_LIST = "CMakeLists.akaignore.txt";
    public static String DEFAULT_CMAKE_LIST = "CMakeLists.txt";
    public static String CMAKE_CACHE_FILE_NAME = "CMakeCache.txt";
    public static String CMAKE_FILES_FOLDER_NAME = "CMakeFiles";

    private static String executableFilePath = "";

    public static boolean verifyCMakeExists() {
        Terminal terminal;

        try {
            terminal = new Terminal(CMAKE_CHECK_CMD);

            if (terminal.getStdout().contains("cmake version")) {
                return true;
            } else {
                logger.error("Cannot find cmake.exe in your PATH environment variable!");
                UIController.showErrorDialog("Cannot find cmake.exe in your PATH", "Error", "CMake Builder");
                return false;
            }

        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return false;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return false;
    }

    public static boolean buildProject(String projectPath) {
        return buildProject(projectPath, false);
    }

    public static boolean buildProject(String projectPath, boolean cleanBuild) {
        Terminal terminal;

        try {
            if (cleanBuild) {
                Utils.deleteFileOrFolder(
                        new File(projectPath + File.separator + CMAKE_BUILD_FOLDER_NAME + File.separator + CMAKE_CACHE_FILE_NAME));

                Utils.deleteFileOrFolder(
                        new File(projectPath + File.separator + CMAKE_BUILD_FOLDER_NAME + File.separator + CMAKE_FILES_FOLDER_NAME));
            }

            String cmakeGenerator = Environment.getInstance().getCompiler().getCmakeGenerator();
            String buildDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + CMAKE_BUILD_FOLDER_NAME;
            String buildScript = "cmake -G \"" + cmakeGenerator + "\" -S \"" + projectPath
                                    + "\" -B \"" + buildDirectory + "\"";

            if (Utils.isUnix()) {
                buildScript = "cmake -G " + cmakeGenerator + " -S " + projectPath + " -B " + buildDirectory;
            }

            logger.debug("Build directory: " + buildDirectory);
            logger.debug("Build script: " + buildScript);


            terminal = new Terminal(buildScript);

            if (terminal.getStderr().contains("CMake Error")) {
                logger.error(("Build project failed"));
                logger.error(terminal.getStderr());
                UIController.showErrorDialog("Project builded failed!", "Error", "CMake Builder");
                return false;
            } else {
                logger.debug("Project builded successfully");
                return true;
            }
        } catch (InterruptedException | IOException e) {
            logger.error(e.getMessage());
            UIController.showErrorDialog(e.getMessage(), "Error", "CMake Builder");
            return false;
        }
    }

    public static boolean generateExecutableFile() {
        Terminal terminal;

        try {
            String buildDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory() + File.separator + CMAKE_BUILD_FOLDER_NAME;
            String compileScript = "cmake --build \"" + buildDirectory + "\"";

            if (Utils.isUnix()) {
                compileScript = "cmake --build " + buildDirectory;
            }

            logger.debug("Generate executable file command: " + compileScript);

            terminal = new Terminal(compileScript);

            if (terminal.getStderr().contains("CMake Error")) {
                logger.error(("Project compiled failed"));
                logger.error(terminal.getStderr());
                UIController.showErrorDialog("Compile project failed!", "Error", "CMake Builder");
                return false;
            } else {
                logger.debug("Project compiled successfully");
                return true;
            }

        } catch (Exception e) {
            logger.error("Cannot compile project");
            logger.error(e.getMessage());
            UIController.showErrorDialog("Cannot compile project!", "Error", "CMake Builder");
            return false;
        }
    }

    public static void cloneAllCMakeListsFiles(String directoryPath) throws IOException {
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

    public static void cloneCurrentProjectToInstrumentDirectory() {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        EnviroCMakeProjDirectoryNode projDirNode =
                (EnviroCMakeProjDirectoryNode) EnvironmentSearch.searchNode(root, new EnviroCMakeProjDirectoryNode()).get(0);

        try {
            String instrumentDirectory = new WorkspaceConfig().fromJson().getInstrumentDirectory();
            logger.info("Cloning project to instrument directory");
            Utils.copy(new File(projDirNode.getDirectoryPath()), new File(instrumentDirectory));
            logger.debug("Project cloned to instrument directory");

            logger.info("Starting cloning CMakeLists files");
            cloneAllCMakeListsFiles(instrumentDirectory);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Replace these file with akaignore.ext (ex:
     *
     * .h => .akaignore.h,
     * .hpp => .akaignore.hpp,
     * .c => .akaignore.c,
     * .cpp => .akaignore.cpp,
     * )
     */
    private static void modifyCMakeListsFile(String cmakeListsPath) {
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

    public static void modifyAllCMakeListsFileInDirectory(File currentDirectory) {
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

    public static String linkTestDriver(String testDriverPath, String cmakelistPath) {
        File cmakeListFile = new File(cmakelistPath);
        if (!cmakeListFile.exists()) {
            logger.error("CMakeLists.txt does not exist");
            return "Error: CMakeLists.txt does not exist";
        }

        String cmakeListContent = Utils.readFileContent(cmakeListFile);

        int index = 0;
        for (int i = cmakeListContent.indexOf("add_executable(") + 15; cmakeListContent.charAt(i) != ' '; i++) {
            index = i;
        }

        ++index;
        String modifiedCMakeListContent = cmakeListContent.substring(0, index)
                + " \"" + PathUtils.replaceBackslashWithSlash(testDriverPath) + "\""
                + cmakeListContent.substring(index);

        logger.debug("Modified CMakeLists.txt: " + modifiedCMakeListContent);

        Utils.writeContentToFile(modifiedCMakeListContent, cmakelistPath);
        logger.debug("Test driver linked to CMakeLists.txt");
        return "Test driver linked to CMakeLists.txt";
    }

    public static String linkTestDriverToEachCMakeListsFile(String directoryPath, String testDriverPath) throws IOException {
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

    private static void forceStopBetweenScript(Terminal terminal) throws InterruptedException {
        Process p = terminal.getProcess();
        p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop

        if (p.isAlive()) {
            p.destroy(); // tell the process to stop
            p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop
            p.destroyForcibly(); // tell the OS to kill the process
            p.waitFor();
        }
    }

    public static void setExecutableFilePathFromCMakeLists(File cmakeListsFile) {
        String cmakeListsContent = Utils.readFileContent(cmakeListsFile);
        String projectName = "";

        // Get project name from CMakeLists.txt
        for (int i = cmakeListsContent.indexOf("project(") + 9; cmakeListsContent.charAt(i) != ')'; i++) {
            projectName += cmakeListsContent.charAt(i);
        }

        String executableFileName = "";

        // Get executable file name from CMakeLists.txt
        for (int i = cmakeListsContent.indexOf("add_executable(") + 15; cmakeListsContent.charAt(i) != ' '; i++) {
            executableFileName += cmakeListsContent.charAt(i);
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

    public static void resetExecutableFilePath() {
        executableFilePath = "";
    }

    public static String getExecutableFilePath() {
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

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(new File("/home/phongvan/Downloads/Step2-20220725T164650Z-001/aka-working-space/Step2_test/instruments/aka_cmake_build/Tutorial").exists());
    }
}
