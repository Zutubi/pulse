package com.zutubi.pulse.core.resources;

import com.zutubi.util.SystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 */
public class BinaryInPathFileLocator implements FileLocator
{
    private String binaryName;

    public BinaryInPathFileLocator(String binaryName)
    {
        this.binaryName = binaryName;
    }

    public List<File> locate()
    {
        File binaryFile = SystemUtils.findInPath(binaryName);
        if (binaryFile == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList(binaryFile);
        }
    }
}
