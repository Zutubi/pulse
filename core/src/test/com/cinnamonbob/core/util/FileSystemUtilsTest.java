package com.zutubi.pulse.core.util;

import com.zutubi.pulse.test.BobTestCase;

import java.io.File;
import java.io.IOException;

/**
 */
public class FileSystemUtilsTest extends BobTestCase
{
    public void testGetPermissions()
    {
        if (System.getProperty("os.name").equals("Linux"))
        {
            int permissions = FileSystemUtils.getPermissions(new File("/bin/sh"));
            assertEquals(777, permissions);
        }
    }

    public void testSetPermissions() throws IOException
    {
        if (System.getProperty("os.name").equals("Linux"))
        {
            File tmpDir = FileSystemUtils.createTempDirectory(getClass().getName(), "");
            FileSystemUtils.setPermissions(tmpDir, 0);
            int permissions = FileSystemUtils.getPermissions(tmpDir);
            assertEquals(permissions, 0);
            assertTrue(FileSystemUtils.removeDirectory(tmpDir));
        }
    }
}
