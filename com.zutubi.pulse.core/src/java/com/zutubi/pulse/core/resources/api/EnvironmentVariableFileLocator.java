package com.zutubi.pulse.core.resources.api;

import com.zutubi.util.FileSystem;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A file locator that looks for an environment variable and takes its value as
 * the path to return.  If the variable does not exist or does not point to an
 * existing path, nothing is returned.
 */
public class EnvironmentVariableFileLocator implements FileLocator
{
    private String variableName;
    private FileSystem fileSystem = new FileSystem();

    /**
     * Creates a new locator for the given environment variable.
     * 
     * @param variableName name of the environment variable to look for
     */
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
