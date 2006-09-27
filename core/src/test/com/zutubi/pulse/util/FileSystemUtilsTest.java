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

    public void testCopyFileToFile() throws Exception
    {
        File from = new File(tmpDir, "from");
        File to = new File(tmpDir, "to");

        FileSystemUtils.createFile(from, "test");
        FileSystemUtils.copyRecursively(from, to);
        assertFilesEqual(from, to);
    }

    public void testCopyDirectoryToDirectory() throws Exception
    {
        File fromDir = new File(tmpDir, "from");
        File toDir = new File(tmpDir, "to");
        File f1 = new File(fromDir, "f1");
        File dir = new File(fromDir, "dir");
        File f2 = new File(dir, "f2");
        File f3 = new File(dir, "f3");
        File nested = new File(dir, "nested");
        File f4 = new File(nested, "f4");

        assertTrue(nested.mkdirs());
        FileSystemUtils.createFile(f1, "test f1");
        FileSystemUtils.createFile(f2, "test f2");
        FileSystemUtils.createFile(f3, "test f3");
        FileSystemUtils.createFile(f4, "test f4");

        FileSystemUtils.copyRecursively(fromDir, toDir);
        assertDirectoriesEqual(fromDir, toDir);
    }

    public void testCopyDirectoryToExistingDirectory() throws Exception
    {
        File fromDir = new File(tmpDir, "from");
        File toDir = new File(tmpDir, "to");
        File f1 = new File(fromDir, "f1");
        File dir = new File(fromDir, "dir");
        File f2 = new File(dir, "f2");
        File f3 = new File(dir, "f3");
        File nested = new File(dir, "nested");
        File f4 = new File(nested, "f4");

        assertTrue(toDir.mkdirs());
        assertTrue(nested.mkdirs());
        FileSystemUtils.createFile(f1, "test f1");
        FileSystemUtils.createFile(f2, "test f2");
        FileSystemUtils.createFile(f3, "test f3");
        FileSystemUtils.createFile(f4, "test f4");

        FileSystemUtils.copyRecursively(fromDir, toDir);
        assertDirectoriesEqual(fromDir, toDir);
    }

    public void testRecursiveCopyPreservesPermissions() throws Exception
    {
        if(SystemUtils.isLinux())
        {
            File from = new File(tmpDir, "from");
            File to = new File(tmpDir, "to");

            FileSystemUtils.createFile(from, "test");
            FileSystemUtils.setPermissions(from, 777);
            FileSystemUtils.copyRecursively(from, to);
            assertEquals(777, FileSystemUtils.getPermissions(to));
        }
    }

    public void testFilesMatchBothEmpty() throws IOException
    {
        filesMatchHelper("", "", true);
    }

    public void testFilesMatchFirstShorter() throws IOException
    {
        filesMatchHelper("short", "longer", false);
    }

    public void testFilesMatchSecondShorter() throws IOException
    {
        filesMatchHelper("longer", "short", false);
    }

    public void testFilesMatchSameContent() throws IOException
    {
        filesMatchHelper("content", "content", true);
    }

    public void testFilesMatchSameLength() throws IOException
    {
        filesMatchHelper("content", "CONTENT", false);
    }

    public void testFilesMatchLongFiles() throws IOException
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 10000; i++)
        {
            builder.append("some random string");
        }

        String content = builder.toString();
        filesMatchHelper(content, content, true);
    }

    private void filesMatchHelper(String s1, String s2, boolean expected) throws IOException
    {
        File f1 = new File(tmpDir, "f1");
        File f2 = new File(tmpDir, "f2");

        FileSystemUtils.createFile(f1, s1);
        FileSystemUtils.createFile(f2, s2);

        assertEquals(expected, FileSystemUtils.filesMatch(f1, f2));
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
