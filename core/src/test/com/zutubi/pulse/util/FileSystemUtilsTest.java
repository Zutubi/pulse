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
    private File tmpDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDirectory("FileSystemUtilsTest", getName());
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

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
    public void testExtractEmptyFile() throws Exception
    {
        extractTestZipToTmp();

        // ensure that the expected directories exist.
        assertTrue(new File(tmpDir, "dir").isDirectory());
        assertTrue(new File(tmpDir, asPath("dir", "subdirectory")).isDirectory());
        assertTrue(new File(tmpDir, asPath("dir", "subdirectory", "emptyfile")).isFile());
    }

    public void testExtractNonEmptyFiles() throws Exception
    {
        extractTestZipToTmp();

        // ensure that the expected directories exist.
        assertTrue(new File(tmpDir, asPath("config")).isDirectory());
        assertTrue(new File(tmpDir, asPath("pulse.config.properties")).isFile());
        assertTrue(new File(tmpDir, asPath("config", "pulse.properties")).isFile());
    }

    public String asPath(String... pathElements)
    {
        StringBuffer buff = new StringBuffer();
        String sep = "";
        for (String pathElement : pathElements)
        {
            buff.append(sep);
            buff.append(pathElement);
            sep = File.separator;
        }
        return buff.toString();
    }

    private void extractTestZipToTmp() throws IOException
    {
        InputStream is = null;
        try
        {
            is = getClass().getResourceAsStream("FileSystemUtils."+getName()+".zip");
            assertNotNull(is);
            FileSystemUtils.extractZip(new ZipInputStream(is), tmpDir);
        }
        finally
        {
            IOUtils.close(is);
        }
    }
}
