package com.dse.environment.object;

import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;

public class EnviroMakeBuildSystemNode extends AbstractEnvironmentNode {
    final static AkaLogger logger = AkaLogger.get(EnviroMakeBuildSystemNode.class);

    private String buildSystem = "";
    private String executableFileName = "";
    private String executableFilePath = "";
    private String projectDirectory = "";
    private String cmakeGenerator = "";
    private String gnuMakeTarget = "";
    private int gnuMakeBuildStrategy = 0;
    private String gnuMakeIncludeDirectory = "";
    private String gnuMakeSourceDirectory = "";

    @Override
    public String toString() {
        return super.toString() + ": build system = " + buildSystem;
    }

    public String exportToFile() {
        String output = ENVIRO_MAKE_BS_NEW+ SpecialCharacter.LINE_BREAK;

        output += ENVIRO_MAKE_BS_NAME + " " + buildSystem + SpecialCharacter.LINE_BREAK;
//        output += ENVIRO_MAKE_EXECUTABLE_FILE_NAME + " " + executableFileName + SpecialCharacter.LINE_BREAK;
//        output += ENVIRO_MAKE_EXECUTABLE_FILE_PATH + " " + executableFilePath + SpecialCharacter.LINE_BREAK;
        output += ENVIRO_MAKE_BS_PROJECT_DIRECTORY + " " + projectDirectory + SpecialCharacter.LINE_BREAK;
        switch (buildSystem) {
            case "CMake":
                output += ENVIRO_MAKE_BS_CMAKE_GENERATOR + " " + cmakeGenerator + SpecialCharacter.LINE_BREAK;
                break;
            case "GNU Make":
                output += ENVIRO_MAKE_BS_GNU_MAKE_TARGET + " " + gnuMakeTarget + SpecialCharacter.LINE_BREAK;
                output += ENVIRO_MAKE_BS_GNU_MAKE_BUILD_TYPE + " " + gnuMakeBuildStrategy + SpecialCharacter.LINE_BREAK;
                output += ENVIRO_MAKE_BS_GNU_MAKE_INCLUDE_FOLDER + " " + gnuMakeIncludeDirectory + SpecialCharacter.LINE_BREAK;
                output += ENVIRO_MAKE_BS_GNU_MAKE_SOURCE_FOLDER + " " + gnuMakeSourceDirectory + SpecialCharacter.LINE_BREAK;
                break;
        }

        output += ENVIRO_MAKE_BS_END;
        return output;
    }

    public String getBuildSystem() {
        return buildSystem;
    }

    public String getExecutableFileName() {
        return executableFileName;
    }

    public String getExecutableFilePath() {
        return executableFilePath;
    }

    public String getProjectDirectory() {
        return projectDirectory;
    }

    public String getCMakeGenerator() {
        return cmakeGenerator;
    }

    public String getGnuMakeTarget() {
        return gnuMakeTarget;
    }

    public int getGnuMakeBuildType() {
        return gnuMakeBuildStrategy;
    }

    public String getGnuMakeIncludeDirectory() {
        return gnuMakeIncludeDirectory;
    }

    public String getGnuMakeSourceDirectory() {
        return gnuMakeSourceDirectory;
    }

    public void setBuildSystem(String buildSystem) {
        this.buildSystem = buildSystem;
    }

    public void setExecutableFileName(String executableFileName) {
        this.executableFileName = executableFileName;
    }

    public void setExecutableFilePath(String executableFilePath) {
        this.executableFilePath = executableFilePath;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public void setCMakeGenerator(String cmakeGenerator) {
        this.cmakeGenerator = cmakeGenerator;
    }

    public void setGnuMakeTarget(String gnuMakeTarget) {
        this.gnuMakeTarget = gnuMakeTarget;
    }

    public void setGnuMakeBuildType(int gnuMakeBuildStrategy) {
        this.gnuMakeBuildStrategy = gnuMakeBuildStrategy;
    }

    public void setGnuMakeIncludeDirectory(String gnuMakeIncludeDirectory) {
        this.gnuMakeIncludeDirectory = gnuMakeIncludeDirectory;
    }

    public void setGnuMakeSourceDirectory(String gnuMakeSourceDirectory) {
        this.gnuMakeSourceDirectory = gnuMakeSourceDirectory;
    }
}
