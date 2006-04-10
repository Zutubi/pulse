package com.zutubi.pulse.core.util;

import com.zutubi.pulse.test.BobTestCase;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 */
public class ZipTest extends BobTestCase
{
    File tmpDir;
    File inDir;
    File outDir;
    File unzipDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // Create a temporary working directory
        tmpDir = FileSystemUtils.createTempDirectory(ZipTest.class.getName(), "");
        inDir = new File(tmpDir, "in");
        inDir.mkdir();
        outDir = new File(tmpDir, "out");
        outDir.mkdir();
        unzipDir = new File(tmpDir, "unzip");
        unzipDir.mkdir();
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testZipNonExistant() throws IOException
    {
        final String BASE_NAME = "non existant base";
        final String DIR_NAME = BASE_NAME + File.separatorChar + "this directory does not exist";

        try
        {
            FileSystemUtils.createZip(getZipName(), new File(BASE_NAME), new File(DIR_NAME));
            fail();
        }
        catch (FileNotFoundException e)
        {
            assertTrue(e.getMessage().contains(DIR_NAME));
        }
    }

    public void testZipBadBase() throws IOException
    {
        try
        {
            FileSystemUtils.createZip(getZipName(), new File("random base"), new File("."));
            fail();
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("is not a parent of source"));
        }
    }

    public void testZipSingleFile() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("singleFile", "some random content");
        createDataFiles(files);
        createAndVerifyZip(files, "singleFile");
    }

    public void testEmptyDirectory() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("emptyDir", null);
        createDataFiles(files);
        createAndVerifyZip(files, "emptyDir");
    }

    public void testSimpleDirectory() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("simpleDir", null);
        files.put(composePath("simpleDir", "file1"), "contents of file1");
        files.put(composePath("simpleDir", "file2"), "contents of file2");
        createDataFiles(files);
        createAndVerifyZip(files, "simpleDir");
    }

    public void testNestedDirectory() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("topDir", null);
        files.put(composePath("topDir", "file1"), "contents of file1");
        files.put(composePath("topDir", "file2"), "contents of file2");
        files.put(composePath("topDir", "nestedDir"), null);
        files.put(composePath("topDir", "nestedDir", "file3"), "contents of file3");
        createDataFiles(files);
        createAndVerifyZip(files, "topDir");
    }

    public void testRelative() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("topDir", null);
        files.put(composePath("topDir", "file1"), "contents of file1");
        files.put(composePath("topDir", "file2"), "contents of file2");
        files.put(composePath("topDir", "nestedDir"), null);
        files.put(composePath("topDir", "nestedDir", "file3"), "contents of file3");

        createDataFiles(files);

        files.remove("topDir");
        files.remove(composePath("topDir", "file1"));
        files.remove(composePath("topDir", "file2"));

        createAndVerifyZip(files, composePath("topDir", "nestedDir"));
    }

    public void testBaseAndSourceAreSameDir() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("file1", "content of file1");
        files.put("file2", "content of file2");
        files.put("baseDir", null);
        files.put(composePath("baseDir", "file3"), "contents of file3");
        files.put(composePath("baseDir", "file4"), "contents of file4");

        createDataFiles(files);

        createAndVerifyZip(files, "");
    }

    private String composePath(String ...components)
    {
        String result = "";
        String sep = "";

        for (String component : components)
        {
            result += sep + component;
            // this separator will be used to locate the entries within the zip file, so
            // needs to be the zip standard. The java.io.File object is able to handle this
            // incorrect separator correctly when on windows.
            sep = "/";
        }

        return result;
    }

    private void createAndVerifyZip(Map<String, String> files, String path) throws IOException
    {
        File zip = getZipName();
        FileSystemUtils.createZip(zip, inDir, new File(inDir, path));

        ZipFile zipFile = null;
        ZipInputStream zipIn = null;
        try
        {
            zipFile = new ZipFile(zip);
            zipIn = new ZipInputStream(new FileInputStream(zip));
            ZipEntry entry;

            while ((entry = zipIn.getNextEntry()) != null)
            {
                String name;

                if (entry.isDirectory())
                {
                    name = entry.getName().substring(0, entry.getName().length() - 1);
                    assertEquals(files.get(name), null);
                }
                else
                {
                    name = entry.getName();
                    assertEquals(files.get(entry.getName()), readContents(zipFile, entry));
                }

                assertTrue(files.containsKey(name));
                files.remove(name);
            }

        }
        finally
        {
            IOUtils.close(zipFile);
            IOUtils.close(zipIn);
        }
        assertTrue(files.isEmpty());
    }

    private String readContents(ZipFile zipFile, ZipEntry entry) throws IOException
    {
        return IOUtils.inputStreamToString(zipFile.getInputStream(entry));
    }

    private void createDataFiles(Map<String, String> files) throws IOException
    {
        for (Map.Entry<String, String> entry : files.entrySet())
        {
            File f = new File(inDir, entry.getKey());
            String value = entry.getValue();

            if (value != null)
            {
                File parent = f.getCanonicalFile().getParentFile();
                if (!parent.exists() && !parent.mkdirs())
                {
                    fail();
                }

                FileWriter w = null;

                try
                {
                    w = new FileWriter(f);
                    w.write(value);
                }
                finally
                {
                    IOUtils.close(w);
                }
            }
            else
            {
                if (!f.mkdirs())
                {
                    fail();
                }
            }
        }
    }

    public void testUnzipSingleFile() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("singleFile", "some random content");
        createDataFiles(files);

        createExtractAndVerify("singleFile");
    }

    public void testUnzipWithDirectory() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("simpleDir", null);
        files.put(composePath("simpleDir", "file1"), "content of file1");
        createDataFiles(files);

        createExtractAndVerify("simpleDir");
    }

    public void testUnzipWithNestedDirectory() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("simpleDir", null);
        files.put(composePath("simpleDir", "file1"), "content of file1");
        files.put(composePath("simpleDir", "nestedDir"), null);
        files.put(composePath("simpleDir", "nestedDir", "file2"), "content of file 2");
        files.put(composePath("simpleDir", "nestedDir", "file3"), "content of file 3");
        createDataFiles(files);

        createExtractAndVerify("simpleDir");
    }

    public void testZipWithSymlink() throws IOException
    {
        if (SystemUtils.isLinux())
        {
            Map<String, String> files = new TreeMap<String, String>();
            files.put("top", null);
            files.put(composePath("top", "file1"), "content of file 1");
            createDataFiles(files);

            File topDir = new File(inDir, "top");
            File symlink = new File(topDir, "link");
            if (FileSystemUtils.createSymlink(symlink, tmpDir))
            {
                createAndExtract("top");
                assertTrue(symlink.delete());
                assertDirectoriesEqual(inDir, unzipDir);
            }
        }
    }

    public void testZipPreservesPermissions() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("top", null);
        files.put(composePath("top", "file1"), "content of file 1");
        createDataFiles(files);

        File topDir = new File(inDir, "top");
        File file1 = new File(topDir, "file1");

        if (FileSystemUtils.getPermissions(file1) != 0)
        {
            FileSystemUtils.setPermissions(file1, 777);

            createExtractAndVerify("top");
            File outTop = new File(unzipDir, "top");
            File out1 = new File(outTop, "file1");
            assertEquals(777, FileSystemUtils.getPermissions(out1));
        }
    }

    private void createAndExtract(String path)
            throws IOException
    {
        File zip = getZipName();
        FileSystemUtils.createZip(zip, inDir, new File(inDir, path));

        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(zip));
            FileSystemUtils.extractZip(zin, unzipDir);
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    private void createExtractAndVerify(String path)
            throws IOException
    {
        createAndExtract(path);
        assertDirectoriesEqual(inDir, unzipDir);
    }

    private File getZipName()
    {
        return new File(outDir, "test.zip");
    }
}
