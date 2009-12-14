package com.zutubi.pulse.core.util;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.IOAssertions;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.ZipUtils;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipUtilsTest extends PulseTestCase
{
    File tmpDir;
    File inDir;
    File outDir;
    File unzipDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        PulseZipUtils.setDefaults();
        // Create a temporary working directory
        tmpDir = FileSystemUtils.createTempDir(ZipUtilsTest.class.getName(), "");
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
            PulseZipUtils.createZipInternal(getZipName(), new File(BASE_NAME), new File(DIR_NAME));
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
            PulseZipUtils.createZipInternal(getZipName(), new File("random base"), new File("."));
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

    public void testEmptyDirectoryNN() throws IOException
    {
        createExtractAndVerify(null, null);
    }

    public void testSimpleDirectory() throws IOException
    {
        Map<String, String> files = createSimpleDir();
        createAndVerifyZip(files, "simpleDir");
    }

    public void testSimpleDirectoryNN() throws IOException
    {
        createSimpleDir();
        createExtractAndVerify(null, null);
    }

    public void testSimpleDirectoryEE() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            createSimpleDir();
            createExtractAndVerify(true, true);
        }
    }

    public void testSimpleDirectoryEI() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            createSimpleDir();
            createExtractAndVerify(true, false);
        }
    }

    public void testSimpleDirectoryIE() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            createSimpleDir();
            createExtractAndVerify(false, true);
        }
    }

    public void testSimpleDirectoryII() throws IOException
    {
        createSimpleDir();
        createExtractAndVerify(false, false);
    }

    private Map<String, String> createSimpleDir() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("simpleDir", null);
        files.put(composePath("simpleDir", "file1"), "contents of file1");
        files.put(composePath("simpleDir", "file2"), "contents of file2");
        createDataFiles(files);
        return files;
    }

    public void testNestedDirectory() throws IOException
    {
        Map<String, String> files = createNestedDir();
        createAndVerifyZip(files, "topDir");
    }

    public void testRelative() throws IOException
    {
        Map<String, String> files = createNestedDir();
        files.remove("topDir");
        files.remove(composePath("topDir", "file1"));
        files.remove(composePath("topDir", "file2"));

        createAndVerifyZip(files, composePath("topDir", "nestedDir"));
    }

    public void testNestedDirectoryNN() throws IOException
    {
        createNestedDir();
        createExtractAndVerify(null, null);
    }

    public void testNestedDirectoryEE() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            createNestedDir();
            createExtractAndVerify(true, true);
        }
    }

    public void testNestedDirectoryEI() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            createNestedDir();
            createExtractAndVerify(true, false);
        }
    }

    public void testNestedDirectoryIE() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            createNestedDir();
            createExtractAndVerify(false, true);
        }
    }

    public void testNestedDirectoryII() throws IOException
    {
        createNestedDir();
        createExtractAndVerify(false, false);
    }

    private Map<String, String> createNestedDir() throws IOException
    {
        Map<String, String> files = new TreeMap<String, String>();
        files.put("topDir", null);
        files.put(composePath("topDir", "file1"), "contents of file1");
        files.put(composePath("topDir", "file2"), "contents of file2");
        files.put(composePath("topDir", "nestedDir"), null);
        files.put(composePath("topDir", "nestedDir", "file3"), "contents of file3");
        createDataFiles(files);
        return files;
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
        PulseZipUtils.createZipInternal(zip, inDir, new File(inDir, path));

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

    public void testZipWithExternalSymlink() throws IOException
    {
        if (SystemUtils.IS_LINUX)
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
                IOAssertions.assertDirectoriesEqual(inDir, unzipDir);
            }
        }
    }

    public void testZipWithInternalSymlink() throws IOException
    {
        if (SystemUtils.IS_LINUX)
        {
            Map<String, String> files = new TreeMap<String, String>();
            files.put("top", null);
            files.put(composePath("top", "file1"), "content of file 1");
            createDataFiles(files);

            File topDir = new File(inDir, "top");
            File symlink = new File(topDir, "link");
            if (FileSystemUtils.createSymlink(symlink, new File(topDir, "file1")))
            {
                createAndExtract("top");
                assertTrue(symlink.delete());
                IOAssertions.assertDirectoriesEqual(inDir, unzipDir);
            }
        }
    }

    public void testExternalZipPreservesPermissions() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            File f = new File(inDir, "f");
            FileSystemUtils.createFile(f, "content");
            FileSystemUtils.setPermissions(f, FileSystemUtils.PERMISSION_ALL_FULL);

            if (FileSystemUtils.getPermissions(f) != 0)
            {
                createAndExtract(true, true);
                f = new File(unzipDir, "f");
                assertEquals(FileSystemUtils.PERMISSION_ALL_FULL, FileSystemUtils.getPermissions(f));
            }
        }
    }

    public void testExtractEmptyFile() throws Exception
    {
        unzipInput(tmpDir);

        // ensure that the expected directories exist.
        assertTrue(new File(tmpDir, "dir").isDirectory());
        assertTrue(new File(tmpDir, FileSystemUtils.composeFilename("dir", "subdirectory")).isDirectory());
        assertTrue(new File(tmpDir, FileSystemUtils.composeFilename("dir", "subdirectory", "emptyfile")).isFile());
    }

    public void testExtractNonEmptyFiles() throws Exception
    {
        unzipInput(tmpDir);
        assertNonEmptyFiles(tmpDir);
    }

    public void testExtractToNonExistentDir() throws Exception
    {
        File dir = new File(tmpDir, "nonexistent");
        assertFalse(dir.isDirectory());
        unzipInput("testExtractNonEmptyFiles", dir);
        assertNonEmptyFiles(dir);
    }

    private void assertNonEmptyFiles(File dir)
    {
        // ensure that the expected directories exist.
        assertTrue(new File(dir, FileSystemUtils.composeFilename("config")).isDirectory());
        assertTrue(new File(dir, FileSystemUtils.composeFilename("pulse.config.properties")).isFile());
        assertTrue(new File(dir, FileSystemUtils.composeFilename("config", "pulse.properties")).isFile());
    }

    public void testZipBrokenSymlink() throws IOException
    {
        if(FileSystemUtils.LN_AVAILABLE)
        {
            createBrokenSymlink();
            PulseZipUtils.createZipInternal(getZipName(), inDir, inDir);
        }
    }

    public void testZipBrokenSymlinkExternal() throws IOException
    {
        if(FileSystemUtils.LN_AVAILABLE && FileSystemUtils.ZIP_AVAILABLE)
        {
            createBrokenSymlink();
            PulseZipUtils.createZip(getZipName(), inDir, null);
            PulseZipUtils.extractZip(getZipName(), unzipDir);
        }
    }

    private void createBrokenSymlink() throws IOException
    {
        File f = new File(inDir, "f");
        FileSystemUtils.createFile(f, "content");
        File l = new File(inDir, "l");
        FileSystemUtils.createFile(f, "content");
        FileSystemUtils.createSymlink(l, f);
        assertTrue(f.delete());
    }

    public void testBrokenZipCommand() throws IOException
    {
        File f = new File(inDir, "f");
        FileSystemUtils.createFile(f, "content");

        PulseZipUtils.setUseExternalArchiving(true);
        PulseZipUtils.setArchiveCommand("nonexistant");

        simpleTest();
    }

    public void testBrokenZipFlags() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            File f = new File(inDir, "f");
            FileSystemUtils.createFile(f, "content");

            PulseZipUtils.setUseExternalArchiving(true);
            PulseZipUtils.setArchiveCommand("zip -G ${zipfile} ${files}");

            simpleTest();
        }
    }

    public void testBrokenUnzipCommand() throws IOException
    {
        File f = new File(inDir, "f");
        FileSystemUtils.createFile(f, "content");

        PulseZipUtils.setUseExternalArchiving(true);
        PulseZipUtils.setUnarchiveCommand("nonexistant");

        simpleTest();
    }

    public void testBrokenUnzipFlags() throws IOException
    {
        if (FileSystemUtils.ZIP_AVAILABLE)
        {
            File f = new File(inDir, "f");
            FileSystemUtils.createFile(f, "content");

            PulseZipUtils.setUseExternalArchiving(true);
            PulseZipUtils.setUnarchiveCommand("unzip -G ${zipfile}");

            simpleTest();
        }
    }

    public void testExternalPreservesSymlink() throws IOException, InterruptedException
    {
        if(FileSystemUtils.LN_AVAILABLE && FileSystemUtils.ZIP_AVAILABLE)
        {
            File f = new File(inDir, "f");
            FileSystemUtils.createFile(f, "content");
            ProcessBuilder pb = new ProcessBuilder("ln", "-s", "f", "l");
            pb.directory(inDir);
            Process p = pb.start();
            assertEquals(0, p.waitFor());

            PulseZipUtils.setUseExternalArchiving(true);
            simpleTest();

            assertTrue(FileSystemUtils.isRelativeSymlink(new File(unzipDir, "l")));
        }
    }

    public void testInternalHandlingOfSymlink() throws IOException, InterruptedException
    {
        if(FileSystemUtils.LN_AVAILABLE && FileSystemUtils.ZIP_AVAILABLE)
        {
            File f = new File(inDir, "f");
            FileSystemUtils.createFile(f, "content");
            ProcessBuilder pb = new ProcessBuilder("ln", "-s", "f", "l");
            pb.directory(inDir);
            Process p = pb.start();
            assertEquals(0, p.waitFor());

            PulseZipUtils.createZipExternal(getZipName(), inDir, null);
            PulseZipUtils.extractZipInternal(getZipName(), unzipDir);

            // The symlink becomes a file with the path it was linking to
            File l = new File(unzipDir, "l");
            assertTrue(l.exists());
            assertEquals("f", IOUtils.fileToString(l));
        }
    }

    private void simpleTest() throws IOException
    {
        PulseZipUtils.createZip(getZipName(), inDir, null);
        PulseZipUtils.extractZip(getZipName(), unzipDir);
        IOAssertions.assertDirectoriesEqual(inDir, unzipDir);
    }

    private void createAndExtract(Boolean externalZip, Boolean externalUnzip) throws IOException
    {
        File zip = getZipName();
        if(externalZip == null)
        {
            PulseZipUtils.createZip(zip, inDir, null);
        }
        else if(externalZip)
        {
            PulseZipUtils.createZipExternal(zip, inDir, null);
        }
        else
        {
            PulseZipUtils.createZipInternal(zip, inDir, inDir);
        }

        if(externalUnzip == null)
        {
            PulseZipUtils.extractZip(zip, unzipDir);
        }
        else if(externalUnzip)
        {
            PulseZipUtils.extractZipExternal(zip, unzipDir);
        }
        else
        {
            PulseZipUtils.extractZipInternal(zip, unzipDir);
        }
    }

    private void createExtractAndVerify(Boolean externalZip, Boolean externalUnzip) throws IOException
    {
        createAndExtract(externalZip, externalUnzip);
        IOAssertions.assertDirectoriesEqual(inDir, unzipDir);
    }

    private void createAndExtract(String path) throws IOException
    {
        File zip = getZipName();
        PulseZipUtils.createZipInternal(zip, inDir, new File(inDir, path));

        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(zip));
            ZipUtils.extractZip(zin, unzipDir);
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    private void createExtractAndVerify(String path) throws IOException
    {
        createAndExtract(path);
        IOAssertions.assertDirectoriesEqual(inDir, unzipDir);
    }

    private File getZipName()
    {
        return new File(outDir, "test.zip");
    }
}
