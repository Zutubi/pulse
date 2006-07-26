package com.zutubi.pulse.util;

import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 */
public class FileSystemUtilsTest extends PulseTestCase
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

    //@Required(tmpDir)
    public void testExtractZip() throws Exception
    {
        File tmpDir = FileSystemUtils.createTempDirectory("FileSystemUtilsTest", getName());
        try
        {
            InputStream is = null;
            try
            {
                is = getClass().getResourceAsStream("FileSystemUtils.testExtractZip.zip");
                assertNotNull(is);

                FileSystemUtils.extractZip(new ZipInputStream(is), tmpDir);

                // ensure that the expected directories exist.
                assertTrue(new File(tmpDir, "dir").isDirectory());
                assertTrue(new File(tmpDir, "dir" + File.separator + "subdirectory").isDirectory());
                assertTrue(new File(tmpDir, "dir" + File.separator + "subdirectory" + File.separator + "emptyfile").isFile());
            }
            finally
            {
                IOUtils.close(is);
            }
        }
        finally
        {
            assertTrue(FileSystemUtils.removeDirectory(tmpDir));
        }
    }
}
