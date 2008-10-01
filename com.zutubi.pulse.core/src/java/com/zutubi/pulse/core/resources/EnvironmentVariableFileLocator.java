package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.util.FileSystem;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 */
public class EnvironmentVariableFileLocator implements FileLocator
{
    private String variableName;
    private FileSystem fileSystem = new FileSystem();

    public EnvironmentVariableFileLocator(String variableName)
    {
        this.variableName = variableName;
    }

    public List<File> locate()
    {
        String variableValue = System.getenv(variableName);
        if (variableValue != null && fileSystem.exists(variableValue))
        {
            return Arrays.asList(new File(variableValue));
        }

        return Collections.emptyList();
    }

    public void setFileSystem(FileSystem fileSystem)
    {
        this.fileSystem = fileSystem;
    }
}
