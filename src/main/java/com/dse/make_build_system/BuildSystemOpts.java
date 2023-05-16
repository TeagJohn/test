package com.dse.make_build_system;

public class BuildSystemOpts {
    public static final String CMAKE_WARNING =
            "Warning: These are supported CMake generators, not all of them are installed on your system.";
    public static final String CMAKE_NOTE =
            "Note: We only support CMake 3.0 or higher and also recommend to use Ninja as a generator for both Windows and Linux.";

    public static String[] BuildSystemOpts = {
            "CMake",
            "GNU Make",
    };

    public static String[] CMakeGeneratorOpts = {
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

    public static class GnuMakeStrategyObject {
        public int id;
        public String description;
        public String detail;

        public GnuMakeStrategyObject(int id, String description, String detail) {
            this.id = id;
            this.description = description;
            this.detail = detail;
        }

        public String toString() {
            return description;
        }
    }

    public GnuMakeStrategyObject[] GnuMakeStrategyOpts = {
            new GnuMakeStrategyObject(0, "Use your project's Makefile_for_test_driver", "Use your project's Makefile_for_test_driver"),
            new GnuMakeStrategyObject(1, "Use our Makefile_for_test_driver with auto-detected header files", "This option will auto-detect your header files based on your source folders (directory containing .cpp files)."),
            new GnuMakeStrategyObject(2, "Use our Makefile_for_test_driver with manually setup", "This option will let you manually setup your header folder, source folder."),
    };
}
