package com.dse.cmake;

import com.dse.compiler.Terminal;
import com.dse.guifx_v3.controllers.build_environment.ChooseCompilerController;
import com.dse.logger.AkaLogger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeneratorsChecker {
    final static AkaLogger logger = AkaLogger.get(ChooseCompilerController.class);

    public static String[] GeneratorOtps = {
        "Visual Studio 17 2022",
        "Visual Studio 15 2017",
        "NMake Makefiles",
        "Unix Makefiles",
        "MinGW Makefiles",
        "Ninja",
        "CodeBlocks - MinGW Makefiles",
        "CodeBlocks - Unix Makefiles",
        "CodeBlocks - NMake Makefiles"
    };

    public static String CMAKE_CHECK_COMMAND = "cmake --version";
    public static String CMAKE_GENERATOR_COMMAND = "cmake -G";
    public static String CMAKE_TEST_DIRECTORY_COMMAND = " /resource/cmakeGeneratorTest -B /cmakeGeneratorTest/build";
    public static String REMOVE_CMAKE_CACHE_AND_CMAKEFILES_COMMAND = "rm -rf CMakeCache.txt CMakeFiles";

    private static GeneratorsChecker instance = null;

    private GeneratorsChecker() {}

    public void getInstance() {
        if (instance == null) {
            instance = new GeneratorsChecker();
        }
    }

}
