package com.dse.environment.object;

import com.dse.util.PathUtils;

public class EnviroCMakeProjDirectoryNode extends AbstractEnvironmentNode {
    private String directoryPath;

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public String toString() {
        return super.toString() + " directory_path = " + getDirectoryPath();
    }

    @Override
    public String exportToFile() {
        return ENVIRO_CMAKE_PROJ_DIRECTORY + " " + PathUtils.toRelative(directoryPath);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnviroCMakeProjDirectoryNode) {
            return ((EnviroCMakeProjDirectoryNode) obj).getDirectoryPath().equals(getDirectoryPath());
        } else
            return false;
    }
}
