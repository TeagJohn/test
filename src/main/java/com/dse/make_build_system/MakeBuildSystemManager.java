package com.dse.make_build_system;

import com.dse.logger.AkaLogger;

public class MakeBuildSystemManager {
    final AkaLogger logger = AkaLogger.get(MakeBuildSystemManager.class);

    private CMakeHandler cMakeHandler = new CMakeHandler();
    private GNUMakeHandler gnuMakeHandler = new GNUMakeHandler();
    private BuildSystemHandler currentBSHandler = null;

    public BuildSystemHandler createNewHandler(String buildSystem) {
        switch (buildSystem) {
            case "CMake":
                currentBSHandler = (cMakeHandler.verifyBuildSystemExist()) ? cMakeHandler : null;
                break;
            case "GNU Make":
                currentBSHandler = (gnuMakeHandler.verifyBuildSystemExist()) ? gnuMakeHandler : null;
                break;
            default:
                logger.error("Unknown build system: " + buildSystem);
        }
        return currentBSHandler;
    }

    public BuildSystemHandler getCurrentBSHandler() {
        return currentBSHandler;
    }

    public void reset() {
        currentBSHandler = null;
        cMakeHandler.reset();
        gnuMakeHandler.reset();
    }
}
