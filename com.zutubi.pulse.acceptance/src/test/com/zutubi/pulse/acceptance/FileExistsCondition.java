package com.zutubi.pulse.acceptance;

import com.zutubi.util.Condition;

import java.io.File;

/**
 * A condition to test for the existence of a file.
 */
public class FileExistsCondition implements Condition
{
    private File file;

    public FileExistsCondition(File file)
    {
        this.file = file;
    }

    public boolean satisfied()
    {
        return file.exists();
    }
}
