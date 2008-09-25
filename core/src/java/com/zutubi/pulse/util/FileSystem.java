package com.zutubi.pulse.util;

import java.io.File;
import java.io.IOException;

/**
 * Wrap FileSystemUtils in an object so it can be injected for testing.
 */
public class FileSystem
{
    public boolean rmdir(File dir)
    {
        return FileSystemUtils.rmdir(dir);
    }

    public void delete(File file) throws IOException
    {
        FileSystemUtils.delete(file);
    }
}
