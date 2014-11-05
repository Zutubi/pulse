package com.zutubi.util.io;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.io.Files;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.junit.ZutubiTestCase;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.zutubi.util.io.FileSystemUtils.NORMAL_SEPARATOR;
import static com.zutubi.util.io.FileSystemUtils.relativePath;
import static java.util.Arrays.asList;

public class FileSystemUtilsTest extends ZutubiTestCase
{
    private static final String TEST_FILE_CONTENT = "content";

    private static final String DIRNAME_NESTED = "d1";
    private static final String DIRNAME_NESTED_NESTED = "d2";

    private static final String DIR_NESTED = DIRNAME_NESTED;
    private static final String DIR_NESTED_NESTED = StringUtils.join(NORMAL_SEPARATOR, DIRNAME_NESTED, DIRNAME_NESTED_NESTED);

    private static final String FILENAME_TOP = "top.xml";
    private static final String FILENAME_NESTED = "nested.xml";
    private static final String FILENAME_NESTED_NESTED = "nestednested.xml";

    private static final String PATH_TOP = FILENAME_TOP;
    private static final String PATH_NESTED = StringUtils.join(NORMAL_SEPARATOR, DIR_NESTED, FILENAME_NESTED);
    private static final String PATH_NESTED_NESTED = StringUtils.join(NORMAL_SEPARATOR, DIR_NESTED_NESTED, FILENAME_NESTED_NESTED);

    private File tmpDir;
    private Copier[] copiers;

    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDir("FileSystemUtilsTest", getName());

        if (FileSystemUtils.CP_AVAILABLE)
        {
            copiers = new Copier[]{new DefaultCopier(), new JavaCopier(), new UnixCopier()};
        }
        else
        {
            copiers = new Copier[]{new DefaultCopier(), new JavaCopier()};
        }
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testGetPermissions()
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            int permissions = FileSystemUtils.getPermissions(new File("/bin/sh"));
            // It is a link, hence the full permissions
            if (SystemUtils.IS_LINUX)
            {
                assertEquals(FileSystemUtils.PERMISSION_ALL_FULL, permissions);
            }
            else
            {
                assertEquals(FileSystemUtils.PERMISSION_ALL_EXECUTE | FileSystemUtils.PERMISSION_ALL_READ, permissions);
            }
        }
    }

    public void testSetPermissions() throws IOException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            File tmpDir = FileSystemUtils.createTempDir(getClass().getName(), "");
            FileSystemUtils.setPermissions(tmpDir, 0);
            int permissions = FileSystemUtils.getPermissions(tmpDir);
            junit.framework.Assert.assertEquals(permissions, 0);
            FileSystemUtils.rmdir(tmpDir);
        }
    }

    public void testSetExecutableOn() throws IOException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            File temp = File.createTempFile(FileSystemUtils.class.getName(), ".tmp");
            try
            {
                FileSystemUtils.setPermissions(temp, FileSystemUtils.PERMISSION_ALL_READ);
                assertEquals(FileSystemUtils.PERMISSION_ALL_READ, FileSystemUtils.getPermissions(temp));
                FileSystemUtils.setExecutable(temp, true);
                assertEquals(FileSystemUtils.PERMISSION_ALL_READ | FileSystemUtils.PERMISSION_ALL_EXECUTE, FileSystemUtils.getPermissions(temp));
            }
            finally
            {
                temp.delete();
            }
        }
    }

    public void testSetExecutableOff() throws IOException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            File temp = File.createTempFile(FileSystemUtils.class.getName(), ".tmp");
            try
            {
                FileSystemUtils.setPermissions(temp, FileSystemUtils.PERMISSION_ALL_FULL);
                assertEquals(FileSystemUtils.PERMISSION_ALL_FULL, FileSystemUtils.getPermissions(temp));
                FileSystemUtils.setExecutable(temp, false);
                assertEquals(FileSystemUtils.PERMISSION_ALL_READ | FileSystemUtils.PERMISSION_ALL_WRITE, FileSystemUtils.getPermissions(temp));
            }
            finally
            {
                temp.delete();
            }
        }
    }

    public void testGetFilenameExtensionEmpty()
    {
        assertEquals("", FileSystemUtils.getFilenameExtension(""));
    }

    public void testGetFilenameExtensionJustDot()
    {
        assertEquals("", FileSystemUtils.getFilenameExtension("."));
    }

    public void testGetFilenameExtensionEmptyExtension()
    {
        assertEquals("", FileSystemUtils.getFilenameExtension("foo."));
    }

    public void testGetFilenameExtensionEmptyFilename()
    {
        assertEquals("", FileSystemUtils.getFilenameExtension(".txt"));
    }

    public void testGetFilenameExtensionNormal()
    {
        assertEquals("txt", FileSystemUtils.getFilenameExtension("foo.txt"));
    }

    public void testGetFilenameMultiplePeriods()
    {
        assertEquals("txt", FileSystemUtils.getFilenameExtension("foo.bar.txt"));
    }

    public void testOverwritePreservesPermissions() throws IOException
    {
        if (SystemUtils.IS_LINUX)
        {
            File f = null;
            try
            {
                f = File.createTempFile(FileSystemUtils.class.getName(), ".txt");
                Assert.assertTrue(FileSystemUtils.PERMISSION_ALL_FULL != FileSystemUtils.getPermissions(f));
                FileSystemUtils.setPermissions(f, FileSystemUtils.PERMISSION_ALL_FULL);
                assertEquals(FileSystemUtils.PERMISSION_ALL_FULL, FileSystemUtils.getPermissions(f));

                FileOutputStream os = null;
                try
                {
                    os = new FileOutputStream(f);
                    os.write("hello".getBytes());
                }
                finally
                {
                    IOUtils.close(os);
                }

                assertEquals(FileSystemUtils.PERMISSION_ALL_FULL, FileSystemUtils.getPermissions(f));
            }
            finally
            {
                if (f != null)
                {
                    f.delete();
                }
            }
        }
    }

    public void testRenameOverNukesPermissions() throws IOException
    {
        if (SystemUtils.IS_LINUX)
        {
            File f = null;
            try
            {
                f = File.createTempFile(FileSystemUtils.class.getName(), ".txt");
                Assert.assertTrue(FileSystemUtils.PERMISSION_ALL_FULL != FileSystemUtils.getPermissions(f));
                FileSystemUtils.setPermissions(f, FileSystemUtils.PERMISSION_ALL_FULL);
                assertEquals(FileSystemUtils.PERMISSION_ALL_FULL, FileSystemUtils.getPermissions(f));

                File temp = File.createTempFile(FileSystemUtilsTest.class.getName(), "");
                temp.renameTo(f);
                Assert.assertTrue(FileSystemUtils.PERMISSION_ALL_FULL != FileSystemUtils.getPermissions(f));
            }
            finally
            {
                if (f != null)
                {
                    f.delete();
                }
            }
        }
    }

    public void testGetAndSetPermissionPerformance() throws IOException
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            File f = null;
            try
            {
                f = File.createTempFile(FileSystemUtils.class.getName(), ".txt");

                long startTime = System.currentTimeMillis();
                for (int i = 0; i < 100; i++)
                {
                    FileSystemUtils.getPermissions(f);
                }

                long runningTime = System.currentTimeMillis() - startTime;
                System.out.printf("Can get permissions %.2f times/second\n", 100000.0 / runningTime);

                startTime = System.currentTimeMillis();
                for (int i = 0; i < 100; i++)
                {
                    FileSystemUtils.setPermissions(f, FileSystemUtils.PERMISSION_ALL_EXECUTE);
                }

                runningTime = System.currentTimeMillis() - startTime;
                System.out.printf("Can set permissions %.2f times/second\n", 100000.0 / runningTime);

                startTime = System.currentTimeMillis();
                for (int i = 0; i < 100; i++)
                {
                    FileSystemUtils.getPermissions(f);
                    FileSystemUtils.setPermissions(f, FileSystemUtils.PERMISSION_ALL_EXECUTE);
                }

                runningTime = System.currentTimeMillis() - startTime;
                System.out.printf("Can get and set permissions %.2f times/second\n", 100000.0 / runningTime);
            }
            finally
            {
                if (f != null)
                {
                    f.delete();
                }
            }
        }
    }

    public void testTranslateEOLLF() throws IOException
    {
        simpleEOLTest(SystemUtils.LF_BYTES, "line 1\nline 2\nline 3\nline 4\nline 5\nline 6\nline 7\nline 8\n");
    }

    public void testTranslateEOLCR() throws IOException
    {
        simpleEOLTest(SystemUtils.CR_BYTES, "line 1\rline 2\rline 3\rline 4\rline 5\rline 6\rline 7\rline 8\r");
    }

    public void testTranslateEOLCRLF() throws IOException
    {
        simpleEOLTest(SystemUtils.CRLF_BYTES, "line 1\r\nline 2\r\nline 3\r\nline 4\r\nline 5\r\nline 6\r\nline 7\r\nline 8\r\n");
    }

    public void testTranslateEOLPreservePermissions() throws IOException
    {
        if (SystemUtils.IS_LINUX)
        {
            File test = null;
            try
            {
                test = FileSystemUtils.createTempFile(FileSystemUtilsTest.class.getName(), ".tmp", "line 1\nline 2\nline 3\r\nline 4\nline 5\rline 6\r\nline 7\rline 8\r");
                FileSystemUtils.setPermissions(test, FileSystemUtils.PERMISSION_ALL_FULL);
                FileSystemUtils.translateEOLs(test, SystemUtils.CRLF_BYTES, true);
                Assert.assertEquals("line 1\r\nline 2\r\nline 3\r\nline 4\r\nline 5\r\nline 6\r\nline 7\r\nline 8\r\n", Files.toString(test, Charset.defaultCharset()));
                assertEquals(FileSystemUtils.PERMISSION_ALL_FULL, FileSystemUtils.getPermissions(test));
            }
            finally
            {
                if (test != null)
                {
                    test.delete();
                }
            }
        }
    }

    public void testTranslateEOLEmptyFile() throws IOException
    {
        File test = null;
        try
        {
            test = FileSystemUtils.createTempFile(FileSystemUtilsTest.class.getName(), ".tmp", "");
            FileSystemUtils.translateEOLs(test, SystemUtils.CRLF_BYTES, true);
            Assert.assertEquals("", Files.toString(test, Charset.defaultCharset()));
        }
        finally
        {
            if (test != null)
            {
                test.delete();
            }
        }
    }

    public void testTranslateEOLJustLF() throws IOException
    {
        File test = null;
        try
        {
            test = FileSystemUtils.createTempFile(FileSystemUtilsTest.class.getName(), ".tmp", "\n");
            FileSystemUtils.translateEOLs(test, SystemUtils.CRLF_BYTES, true);
            Assert.assertEquals("\r\n", Files.toString(test, Charset.defaultCharset()));
        }
        finally
        {
            if (test != null)
            {
                test.delete();
            }
        }
    }

    public void testTranslateEOLTwoCRs() throws IOException
    {
        File test = null;
        try
        {
            test = FileSystemUtils.createTempFile(FileSystemUtilsTest.class.getName(), ".tmp", "\r\r");
            FileSystemUtils.translateEOLs(test, SystemUtils.LF_BYTES, true);
            Assert.assertEquals("\n\n", Files.toString(test, Charset.defaultCharset()));
        }
        finally
        {
            if (test != null)
            {
                test.delete();
            }
        }
    }

    public void testTranslateEOLCRLFAcrossBoundary() throws IOException
    {
        byte[] in = new byte[1030];
        byte[] out = new byte[1029];

        Arrays.fill(in, (byte) 'x');
        Arrays.fill(out, (byte) 'x');

        in[1023] = '\r';
        in[1024] = '\n';
        out[1023] = '\n';

        File test = null;
        try
        {
            test = FileSystemUtils.createTempFile(FileSystemUtilsTest.class.getName(), ".tmp", in);
            FileSystemUtils.translateEOLs(test, SystemUtils.LF_BYTES, true);

            byte[] got = Files.toByteArray(test);
            Assert.assertEquals(out.length, got.length);
            for (int i = 0; i < out.length; i++)
            {
                Assert.assertEquals(out[i], got[i]);
            }
        }
        finally
        {
            if (test != null)
            {
                test.delete();
            }
        }
    }

    private void runCopyTest(CopyTest test) throws IOException
    {
        for (Copier copier : copiers)
        {
            FileSystemUtils.rmdir(tmpDir);
            Assert.assertTrue(tmpDir.mkdir());
            System.out.println("Trying '" + getName() + "' with copier '" + copier.getName() + "'");
            test.execute(copier);
        }
    }

    public void testCopyFileToFile() throws Exception
    {
        runCopyTest(new CopyTest()
        {
            public void execute(Copier copier) throws IOException
            {
                File from = new File(tmpDir, "from");
                File to = new File(tmpDir, "to");

                Files.write("test", from, Charset.defaultCharset());
                copier.copy(to, from);
                assertFilesEqual(from, to);
            }
        });
    }

    public void testCopyDirectoryToDirectory() throws Exception
    {
        runCopyTest(new CopyTest()
        {
            public void execute(Copier copier) throws IOException
            {
                File fromDir = new File(tmpDir, "from");
                File toDir = new File(tmpDir, "to");
                File f1 = new File(fromDir, "f1");
                File dir = new File(fromDir, "dir");
                File f2 = new File(dir, "f2");
                File f3 = new File(dir, "f3");
                File nested = new File(dir, "nested");
                File f4 = new File(nested, "f4");

                Assert.assertTrue(nested.mkdirs());
                Files.write("test f1", f1, Charset.defaultCharset());
                Files.write("test f2", f2, Charset.defaultCharset());
                Files.write("test f3", f3, Charset.defaultCharset());
                Files.write("test f4", f4, Charset.defaultCharset());

                copier.copy(toDir, fromDir);
                assertDirectoriesEqual(fromDir, toDir);
            }
        });
    }

    public void testCopyDirectoryToExistingDirectory() throws Exception
    {
        runCopyTest(new CopyTest()
        {
            public void execute(Copier copier) throws IOException
            {
                File fromDir = new File(tmpDir, "from");
                File toDir = new File(tmpDir, "to");
                File f1 = new File(fromDir, "f1");
                File dir = new File(fromDir, "dir");
                File f2 = new File(dir, "f2");
                File f3 = new File(dir, "f3");
                File nested = new File(dir, "nested");
                File f4 = new File(nested, "f4");

                Assert.assertTrue(toDir.mkdirs());
                Assert.assertTrue(nested.mkdirs());
                Files.write("test f1", f1, Charset.defaultCharset());
                Files.write("test f2", f2, Charset.defaultCharset());
                Files.write("test f3", f3, Charset.defaultCharset());
                Files.write("test f4", f4, Charset.defaultCharset());

                copier.copy(toDir, fromDir);
                assertDirectoriesEqual(fromDir, toDir);
            }
        });
    }

    public void testCopyFileToNonExistantFile() throws IOException
    {
        runCopyTest(new CopyTest()
        {
            public void execute(Copier copier) throws IOException
            {
                File src = new File(tmpDir, "src.txt");
                Files.write("Some text.", src, Charset.defaultCharset());

                File dest = new File(tmpDir, "dest.txt");
                Files.write("Other text.", dest, Charset.defaultCharset());

                copier.copy(dest, src);
                assertFilesEqual(src, dest);
            }
        });
    }

    public void testCopyFileToDirectory() throws IOException
    {
        runCopyTest(new CopyTest()
        {
            public void execute(Copier copier) throws IOException
            {
                File src = new File(tmpDir, "src.txt");
                Files.write("Some text.", src, Charset.defaultCharset());

                File dest = new File(tmpDir, "dest");
                Assert.assertTrue(dest.mkdirs());

                Assert.assertTrue(dest.isDirectory());
                copier.copy(dest, src);
                Assert.assertTrue(new File(dest, "src.txt").isFile());
            }
        });
    }

    public void testCopyMultipleFiles() throws IOException
    {
        runCopyTest(new CopyTest()
        {
            public void execute(Copier copier) throws IOException
            {
                File srcA = new File(tmpDir, "srca.txt");
                Files.write("Some text.", srcA, Charset.defaultCharset());
                File srcB = new File(tmpDir, "srcb.txt");
                Files.write("Some text.", srcB, Charset.defaultCharset());

                File dest = new File(tmpDir, "dest");

                Assert.assertFalse(dest.isDirectory());
                copier.copy(dest, srcA, srcB);

                Assert.assertTrue(dest.isDirectory());
                Assert.assertTrue(new File(dest, "srca.txt").isFile());
                Assert.assertTrue(new File(dest, "srcb.txt").isFile());
            }
        });
    }

    public void testCopyMultipleFilesNonExistantNestedDestination() throws IOException
    {
        runCopyTest(new CopyTest()
        {
            public void execute(Copier copier) throws IOException
            {
                File srcA = new File(tmpDir, "srca.txt");
                Files.write("Some text.", srcA, Charset.defaultCharset());
                File srcB = new File(tmpDir, "srcb.txt");
                Files.write("Some text.", srcB, Charset.defaultCharset());

                File dest = new File(tmpDir, FileSystemUtils.composeFilename("dest", "nested"));

                Assert.assertFalse(dest.isDirectory());
                copier.copy(dest, srcA, srcB);

                Assert.assertTrue(dest.isDirectory());
                Assert.assertTrue(new File(dest, "srca.txt").isFile());
                Assert.assertTrue(new File(dest, "srcb.txt").isFile());
            }
        });
    }

    public void testCopyDirectoryToNonExistantFile() throws IOException
    {
        runCopyTest(new CopyTest()
        {
            public void execute(Copier copier) throws IOException
            {
                File dir = new File(tmpDir, "dir");
                dir.mkdirs();

                Files.write("Text file a", new File(dir, "a.txt"), Charset.defaultCharset());
                Files.write("Text file a", new File(dir, "b.txt"), Charset.defaultCharset());

                File dest = new File(tmpDir, "dest");
                Assert.assertFalse(dest.exists());
                copier.copy(dest, dir);
                Assert.assertTrue(dest.isDirectory());
                Assert.assertTrue(new File(dest, "a.txt").isFile());
                Assert.assertTrue(new File(dest, "b.txt").isFile());
            }
        });
    }

    public void testRecursiveCopyPreservesPermissions() throws Exception
    {
        if (SystemUtils.IS_LINUX)
        {
            File from = new File(tmpDir, "from");
            File to = new File(tmpDir, "to");

            Files.write("test", from, Charset.defaultCharset());
            FileSystemUtils.setPermissions(from, 777);
            FileSystemUtils.unixCopy(to, from);
            assertEquals(777, FileSystemUtils.getPermissions(to));
        }
    }

    public void testCopyManyFiles() throws Exception
    {
        File from = new File(tmpDir, "from");
        File to = new File(tmpDir, "to");

        Files.write("test", from, Charset.defaultCharset());
        for (int i = 0; i < 2500; i++)
        {
            FileSystemUtils.copy(to, from);
        }
    }

    public void testCopySymlinkFileToFile() throws IOException
    {
        if (FileSystemUtils.LN_AVAILABLE)
        {
            File link = createSymlink(TEST_FILE_CONTENT);
            File dest = new File(tmpDir, "dest");

            FileSystemUtils.copy(dest, link);
            assertSymlinkContentCopied(dest, TEST_FILE_CONTENT);
        }
    }

    public void testCopySymlinkFileToDirectory() throws IOException
    {
        if (FileSystemUtils.LN_AVAILABLE)
        {
            File link = createSymlink(TEST_FILE_CONTENT);
            File destDir = new File(tmpDir, "dest");
            File dest = new File(destDir, link.getName());
            assertTrue(destDir.mkdir());

            FileSystemUtils.copy(destDir, link);
            assertSymlinkContentCopied(dest, TEST_FILE_CONTENT);
        }
    }

    private File createSymlink(String content) throws IOException
    {
        File linkTarget = new File(tmpDir, "target");
        Files.write(content, linkTarget, Charset.defaultCharset());
        File link = new File(tmpDir, "link");
        assertTrue(FileSystemUtils.createSymlink(link, linkTarget));
        return link;
    }

    private void assertSymlinkContentCopied(File dest, String content) throws IOException
    {
        assertTrue(dest.isFile());
        assertFalse(FileSystemUtils.isRelativeSymlink(dest));
        assertEquals(content, Files.toString(dest, Charset.defaultCharset()));
    }

    public void testIsSymlinkRegularFile() throws IOException
    {
        if (FileSystemUtils.LN_AVAILABLE)
        {
            File file = new File(tmpDir, "file");
            Files.write("data", file, Charset.defaultCharset());
            Assert.assertFalse(FileSystemUtils.isRelativeSymlink(file));
        }
    }

    public void testIsSymlinkRegularDir() throws IOException
    {
        if (FileSystemUtils.LN_AVAILABLE)
        {
            Assert.assertFalse(FileSystemUtils.isRelativeSymlink(tmpDir));
        }
    }

    public void testIsSymlinkSimpleLink() throws IOException
    {
        if (FileSystemUtils.LN_AVAILABLE)
        {
            File file = new File(tmpDir, "file");
            File link = new File(tmpDir, "link");
            Files.write("data", file, Charset.defaultCharset());
            FileSystemUtils.createSymlink(link, file);

            Assert.assertTrue(FileSystemUtils.isRelativeSymlink(link));
        }
    }

    public void testIsSymlinkDirLink() throws IOException
    {
        if (FileSystemUtils.LN_AVAILABLE)
        {
            File dir = new File(tmpDir, "file");
            File link = new File(tmpDir, "link");
            dir.mkdir();
            FileSystemUtils.createSymlink(link, dir);
            Assert.assertTrue(FileSystemUtils.isRelativeSymlink(link));
        }
    }

    public void testIsSymlinkFileWithParentLink() throws IOException
    {
        if (FileSystemUtils.LN_AVAILABLE)
        {
            File dir = new File(tmpDir, "dir");
            File link = new File(tmpDir, "link");
            File file = new File(link, "file");
            dir.mkdir();
            FileSystemUtils.createSymlink(link, dir);
            Files.write("data", file, Charset.defaultCharset());

            Assert.assertTrue(FileSystemUtils.isRelativeSymlink(link));
            Assert.assertFalse(FileSystemUtils.isRelativeSymlink(file));
        }
    }

    public void testIsSymlinkLinkWithParentLink() throws IOException
    {
        if (FileSystemUtils.LN_AVAILABLE)
        {
            File dir = new File(tmpDir, "dir");
            File dirLink = new File(tmpDir, "dirlink");
            File file = new File(dirLink, "file");
            File fileLink = new File(dirLink, "filelink");
            dir.mkdir();
            FileSystemUtils.createSymlink(dirLink, dir);
            Files.write("data", file, Charset.defaultCharset());
            FileSystemUtils.createSymlink(fileLink, file);

            Assert.assertTrue(FileSystemUtils.isRelativeSymlink(dirLink));
            Assert.assertTrue(FileSystemUtils.isRelativeSymlink(fileLink));
        }
    }

    public void testCreateTmpDirectory() throws IOException
    {
        File tmp = FileSystemUtils.createTempDir();
        assertTrue(tmp.isDirectory());
        assertTrue(tmp.delete());
    }

    public void testCreateTmpDirectoryWithPrefix() throws IOException
    {
        File tmp = FileSystemUtils.createTempDir("prefix", null);
        assertTrue(tmp.getName().startsWith("prefix"));
        assertFalse(tmp.getName().endsWith("null"));
        assertTrue(tmp.isDirectory());
        assertTrue(tmp.delete());
    }

    public void testCreateTmpDirectoryWithSuffix() throws IOException
    {
        File tmp = FileSystemUtils.createTempDir(null, "suffix");
        assertTrue(tmp.getName().endsWith("suffix"));
        assertFalse(tmp.getName().startsWith("null"));
        assertTrue(tmp.isDirectory());
        assertTrue(tmp.delete());
    }

    public void testCreateTmpDirectoryWithBase() throws IOException
    {
        File tmp = FileSystemUtils.createTempDir(null, null, tmpDir);
        assertEquals(tmpDir, tmp.getParentFile());
        assertTrue(tmp.isDirectory());
        assertTrue(tmp.delete());
    }

    public void testLocaliseSeparatorsEmpty()
    {
        assertEquals("", FileSystemUtils.localiseSeparators(""));
    }

    public void testLocaliseSeparatorsSingleSeparator()
    {
        // In this case a trailing slash is significant!
        assertEquals(File.separator, FileSystemUtils.localiseSeparators(File.separator));
    }

    public void testLocaliseSeparatorsTwoSeparators()
    {
        // In this case a trailing slash is significant!
        assertEquals(File.separator, FileSystemUtils.localiseSeparators(File.separator + File.separator));
    }

    public void testLocaliseSeparatorsOtherSeparator()
    {
        // In this case a trailing slash is significant!
        assertEquals(File.separator, FileSystemUtils.localiseSeparators(getOtherSeparator()));
    }

    public void testLocaliseSeparatorsMixedSeparators()
    {
        assertEquals("one" + File.separator + "two" + File.separator + "three", FileSystemUtils.localiseSeparators("one" + File.separator + "two" + getOtherSeparator() + "three"));
    }

    public void testDuplicatedSeparators()
    {
        assertEquals("one" + File.separator + "two", FileSystemUtils.localiseSeparators("one" + File.separator + File.separator + "two"));
    }

    public void testLocaliseSeparatorsDuplicatedOtherSeparators()
    {
        assertEquals("one" + File.separator + "two", FileSystemUtils.localiseSeparators("one" + getOtherSeparator() + getOtherSeparator() + "two"));
    }

    public void testLocaliseSeparatorsDuplicatedMixedSeparators()
    {
        assertEquals("one" + File.separator + "two", FileSystemUtils.localiseSeparators("one" + File.separator + getOtherSeparator() + "two"));
    }

    public void testGetNormalisedAbsolutePath() throws IOException
    {
        String expected = new File("one" + File.separator + "two").getCanonicalPath();
        assertEquals(expected, FileSystemUtils.getNormalisedAbsolutePath(new File("one", File.separator + getOtherSeparator() + "two" + File.separator)));
    }

    public void testRmdirNormal() throws IOException
    {
        File dir = new File(tmpDir, "dir");
        createDirectoryLayout(dir);

        FileSystemUtils.rmdir(dir);
        assertFalse(dir.exists());
    }

    public void testRmdirWholeDirectoryUnderSymlink() throws IOException
    {
        for (LinkMaker linkMaker: getLinkMakers())
        {
            File linkTarget = new File(tmpDir, "target");
            assertTrue(linkTarget.mkdir());

            File dir = new File(linkTarget, "dir");
            createDirectoryLayout(dir);

            File link = new File(tmpDir, "link");
            linkMaker.makeLink(link, linkTarget);

            File underLink = new File(link, "dir");
            FileSystemUtils.rmdir(underLink);
            assertFalse(underLink.exists());
            assertFalse(dir.exists());
            assertTrue(link.exists());
            assertTrue(linkTarget.exists());
        }
    }

    public void testRmdirDirectoryContainsSymlinkToOutside() throws IOException
    {
        for (LinkMaker linkMaker: getLinkMakers())
        {
            File linkTarget = new File(tmpDir, "target");
            assertTrue(linkTarget.mkdir());

            File fileUnderLinkTarget = new File(linkTarget, "f.txt");
            Files.write("some text", fileUnderLinkTarget, Charset.defaultCharset());
            assertTrue(fileUnderLinkTarget.exists());

            File dir = new File(tmpDir, "dir");
            createDirectoryLayout(dir);

            File link = new File(dir, "link");
            linkMaker.makeLink(link, linkTarget);

            FileSystemUtils.rmdir(dir);
            assertFalse(link.exists());
            assertFalse(dir.exists());
            assertTrue(linkTarget.exists());
            assertTrue(fileUnderLinkTarget.exists());
        }
    }

    public void testRmdirDirectoryContainsSymlinkToSibling() throws IOException
    {
        for (LinkMaker linkMaker: getLinkMakers())
        {
            File dir = new File(tmpDir, "dir");
            createDirectoryLayout(dir);

            File linkTarget = new File(dir, "target");
            assertTrue(linkTarget.mkdir());

            File link = new File(dir, "link");
            linkMaker.makeLink(link, linkTarget);

            FileSystemUtils.rmdir(dir);
            assertFalse(dir.exists());
        }
    }

    public void testRmdirDirectoryContainsSymlinkToUnderSibling() throws IOException
    {
        for (LinkMaker linkMaker: getLinkMakers())
        {
            File dir = new File(tmpDir, "dir");
            createDirectoryLayout(dir);

            File sibling = new File(dir, "sibling");
            assertTrue(sibling.mkdir());
            File linkTarget = new File(sibling, "target");
            assertTrue(linkTarget.mkdir());

            File link = new File(dir, "link");
            linkMaker.makeLink(link, linkTarget);

            FileSystemUtils.rmdir(dir);
            assertFalse(dir.exists());
        }
    }

    public void testRmdirDirectoryContainsSymlinkToParent() throws IOException
    {
        for (LinkMaker linkMaker: getLinkMakers())
        {
            File dir = new File(tmpDir, "dir");
            createDirectoryLayout(dir);

            File parent = new File(dir, "parent");
            assertTrue(parent.mkdir());

            File link = new File(parent, "link");
            linkMaker.makeLink(link, parent);

            FileSystemUtils.rmdir(dir);
            assertFalse(dir.exists());
        }
    }

    public void testRmdirDirectoryContainsSymlinkToAncestor() throws IOException
    {
        for (LinkMaker linkMaker: getLinkMakers())
        {
            File dir = new File(tmpDir, "dir");
            createDirectoryLayout(dir);

            File ancestor = new File(dir, "ancestor");
            assertTrue(ancestor.mkdir());
            File parent = new File(ancestor, "parent");
            assertTrue(parent.mkdir());

            File link = new File(parent, "link");
            linkMaker.makeLink(link, ancestor);

            FileSystemUtils.rmdir(dir);
            assertFalse(dir.exists());
        }
    }

    public void testRmdirDirectoryContainsSymlinkToDir() throws IOException
    {
        for (LinkMaker linkMaker: getLinkMakers())
        {
            File dir = new File(tmpDir, "dir");
            createDirectoryLayout(dir);

            File parent = new File(dir, "parent");
            assertTrue(parent.mkdir());

            File link = new File(parent, "link");
            linkMaker.makeLink(link, dir);

            FileSystemUtils.rmdir(dir);
            assertFalse(dir.exists());
        }
    }

    public void testRename() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");
        Files.write(TEST_FILE_CONTENT, src, Charset.defaultCharset());

        FileSystemUtils.rename(src, dest, false);

        assertEquals(TEST_FILE_CONTENT, Files.toString(dest, Charset.defaultCharset()));
    }

    public void testRenameDirectory() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");
        assertTrue(src.mkdir());

        FileSystemUtils.rename(src, dest, false);

        assertTrue(dest.isDirectory());
    }

    public void testRenameSrcDoesntExist() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");

        try
        {
            FileSystemUtils.rename(src, dest, true);
            fail("Shouldn't be able to rename non-existant source");
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("source does not exist"));
        }
    }

    public void testRobustRename() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");
        Files.write(TEST_FILE_CONTENT, src, Charset.defaultCharset());

        FileSystemUtils.robustRename(src, dest);

        assertEquals(TEST_FILE_CONTENT, Files.toString(dest, Charset.defaultCharset()));
    }

    public void testRobustRenameDirectory() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");
        assertTrue(src.mkdir());

        FileSystemUtils.robustRename(src, dest);

        assertTrue(dest.isDirectory());
    }

    public void testRobustRenameSrcDoesntExist() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");

        try
        {
            FileSystemUtils.robustRename(src, dest);
            fail("Shouldn't be able to rename non-existant source");
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("source does not exist"));
        }
    }

    public void testRobustRenameDestExists() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");
        Files.write(TEST_FILE_CONTENT, src, Charset.defaultCharset());
        assertTrue(dest.mkdir());

        try
        {
            FileSystemUtils.robustRename(src, dest);
            fail("Shouldn't be able to rename onto existing dir");
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("destination already exists"));
        }
    }

    public void testRobustRenameDestExistsNonForced() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");
        Files.write(TEST_FILE_CONTENT, src, Charset.defaultCharset());
        assertTrue(dest.mkdir());

        try
        {
            FileSystemUtils.rename(src, dest, false);
            fail("Shouldn't be able to rename non-forced over existing dir");
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("destination already exists"));
        }
    }

    public void testRobustRenameDirectoryDestExistsNonForced() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");
        assertTrue(src.mkdir());
        assertTrue(dest.createNewFile());

        try
        {
            FileSystemUtils.rename(src, dest, false);
            fail("Shouldn't be able to rename non-forced over existing dir");
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage().contains("destination already exists"));
        }
    }

    public void testRobustRenameDestExistsForced() throws IOException
    {
        File src = new File(tmpDir, "src");
        File dest = new File(tmpDir, "dest");
        Files.write(TEST_FILE_CONTENT, src, Charset.defaultCharset());
        assertTrue(dest.mkdir());

        FileSystemUtils.rename(src, dest, true);
        assertEquals(TEST_FILE_CONTENT, Files.toString(dest, Charset.defaultCharset()));
    }

    public void testRenameSourceParentNotReadable() throws IOException
    {
        if (!SystemUtils.IS_WINDOWS && notRoot())
        {
            File src = new File(tmpDir, "src");
            File dest = new File(tmpDir, "dest");
            assertTrue(src.createNewFile());
            FileSystemUtils.setPermissions(tmpDir, FileSystemUtils.PERMISSION_OWNER_WRITE);

            try
            {
                FileSystemUtils.rename(src, dest, false);
                fail("Shouldn't be able to rename when source parent is not readable");
            }
            catch (IOException e)
            {
                assertTrue(e.getMessage().contains("source's parent directory is not readable"));
            }
            finally
            {
                FileSystemUtils.setPermissions(tmpDir, FileSystemUtils.PERMISSION_OWNER_FULL);
            }
        }
    }

    public void testRenameDestParentNotWritable() throws IOException
    {
        if (!SystemUtils.IS_WINDOWS && notRoot())
        {
            File src = new File(tmpDir, "src");
            File dest = new File(tmpDir, "dest");
            assertTrue(src.createNewFile());
            FileSystemUtils.setPermissions(tmpDir, FileSystemUtils.PERMISSION_ALL_READ);

            try
            {
                FileSystemUtils.rename(src, dest, false);
                fail("Shouldn't be able to rename when destination parent is not writable");
            }
            catch (IOException e)
            {
                assertTrue(e.getMessage().contains("destination's parent directory is not writable"));
            }
            finally
            {
                FileSystemUtils.setPermissions(tmpDir, FileSystemUtils.PERMISSION_OWNER_FULL);
            }
        }

    }

    public void testTopRelativeToNull() throws Exception
    {
        assertEquals(PATH_TOP, FileSystemUtils.appendAndCanonicalise(null, PATH_TOP));
    }

    public void testNestedRelativeToNull() throws Exception
    {
        assertEquals(PATH_NESTED, FileSystemUtils.appendAndCanonicalise(null, PATH_NESTED));
    }

    public void testNestedNestedRelativeToNull() throws Exception
    {
        assertEquals(PATH_NESTED_NESTED, FileSystemUtils.appendAndCanonicalise(null, PATH_NESTED_NESTED));
    }

    public void testTopRelativeToNested() throws Exception
    {
        assertEquals(PATH_TOP, FileSystemUtils.appendAndCanonicalise(DIR_NESTED, "../" + FILENAME_TOP));
    }

    public void testNestedRelativeToNested() throws Exception
    {
        assertEquals(PATH_NESTED, FileSystemUtils.appendAndCanonicalise(DIR_NESTED, FILENAME_NESTED));
    }

    public void testNestedNestedRelativeToNested() throws Exception
    {
        assertEquals(PATH_NESTED_NESTED, FileSystemUtils.appendAndCanonicalise(DIR_NESTED, DIRNAME_NESTED_NESTED + NORMAL_SEPARATOR + FILENAME_NESTED_NESTED));
    }

    public void testTopRelativeToNestedNested() throws Exception
    {
        assertEquals(PATH_TOP, FileSystemUtils.appendAndCanonicalise(DIR_NESTED_NESTED, "../../" + FILENAME_TOP));
    }

    public void testNestedRelativeToNestedNested() throws Exception
    {
        assertEquals(PATH_NESTED, FileSystemUtils.appendAndCanonicalise(DIR_NESTED_NESTED, "../" + FILENAME_NESTED));
    }

    public void testNestedNestedRelativeToNestedNested() throws Exception
    {
        assertEquals(PATH_NESTED_NESTED, FileSystemUtils.appendAndCanonicalise(DIR_NESTED_NESTED, FILENAME_NESTED_NESTED));
    }

    public void testSameDirectoryNormalised() throws Exception
    {
        assertEquals(PATH_NESTED_NESTED, FileSystemUtils.appendAndCanonicalise(DIR_NESTED_NESTED, "././" + FILENAME_NESTED_NESTED));
    }

    public void testAbsolutePathToTop() throws Exception
    {
        assertEquals("/" + PATH_TOP, FileSystemUtils.appendAndCanonicalise(DIR_NESTED_NESTED, "/" + FILENAME_TOP));
    }

    public void testAbsolutePathToNestedNested() throws Exception
    {
        assertEquals("/" + PATH_NESTED_NESTED, FileSystemUtils.appendAndCanonicalise(DIR_NESTED, "/" + PATH_NESTED_NESTED));
    }

    public void testEncodeFilenameComponent()
    {
        final String ALL_SAFE = "alphanumeric_is-123.ok";
        final String UNTRUSTED = "% !@#$%^&*()'\";:\\//<>|`~,";

        assertEquals(ALL_SAFE, FileSystemUtils.encodeFilenameComponent(ALL_SAFE));
        assertEquals("%25%20%21%40%23%24%25%5e%26%2a%28%29%27%22%3b%3a%5c%2f%2f%3c%3e%7c%60%7e%2c", FileSystemUtils.encodeFilenameComponent(UNTRUSTED));
    }

    public void testFilterFiles() throws IOException
    {
        assertCreateFiles("root.txt", "a/file1.txt", "b/file2.txt", "c/d/file3.txt");

        List<File> files = FileSystemUtils.filter(tmpDir, new IsFilePredicate());

        List<String> filenames = mapToFilenames(files);

        assertEquals(4, filenames.size());
        assertTrue(filenames.contains("file1.txt"));
        assertTrue(filenames.contains("file2.txt"));
        assertTrue(filenames.contains("file3.txt"));
        assertTrue(filenames.contains("root.txt"));

        files = FileSystemUtils.filter(tmpDir, new IsDirectoryPredicate());
        filenames = mapToFilenames(files);

        assertEquals(4, filenames.size());
        assertTrue(filenames.contains("a"));
        assertTrue(filenames.contains("b"));
        assertTrue(filenames.contains("c"));
        assertTrue(filenames.contains("d"));
    }

    private List<String> mapToFilenames(List<File> files)
    {
        return newArrayList(transform(files, new Function<File, String>()
        {
            public String apply(File file)
            {
                return file.getName();
            }
        }));
    }

    public void testFilterEmptyDirectory() throws IOException
    {
        assertTrue(new File(tmpDir, "a").mkdirs());
        assertTrue(new File(tmpDir, "b").mkdirs());

        List<File> files = FileSystemUtils.filter(tmpDir, Predicates.<File>alwaysTrue());
        List<String> filenames = mapToFilenames(files);
        assertEquals(2, filenames.size());
        assertTrue(filenames.contains("a"));
        assertTrue(filenames.contains("b"));
    }

    public void testFilterNonExistentDirectory() throws IOException
    {
        List<File> files = FileSystemUtils.filter(new File("nosuchfile"), Predicates.<File>alwaysTrue());
        assertEquals(0, files.size());
    }

    public void testFilterRegularFile() throws IOException
    {
        File f = new File(tmpDir, "f");
        assertTrue(f.createNewFile());
        List<File> files = FileSystemUtils.filter(f, Predicates.<File>alwaysTrue());
        assertEquals(0, files.size());
    }

    public void testList() throws IOException
    {
        assertEquals(0, FileSystemUtils.list(tmpDir).length);
        File file = new File(tmpDir, "file.txt");
        assertTrue(file.createNewFile());
        assertEquals(1, FileSystemUtils.list(tmpDir).length);
        assertEquals(0, FileSystemUtils.list(file).length);
        assertEquals(0, FileSystemUtils.list(new File("does not exist")).length);
    }

    public void testRelativePath()
    {
        assertEquals(".", relativePath(new File("./a"), new File("./a")));
        assertEquals("..", relativePath(new File("./a/b"), new File("./a")));
        assertEquals(".." + File.separator + "a", relativePath(new File("./b"), new File("./a")));
        assertEquals("b", relativePath(new File("./a"), new File("./a/b")));
    }

    private void assertCreateFiles(String... paths) throws IOException
    {
        for (String path : paths)
        {
            File file = new File(tmpDir, path);
            File parent = file.getParentFile();
            if (!parent.exists())
            {
                assertTrue(parent.mkdirs());
            }
            assertTrue(file.createNewFile());
        }
    }

    private boolean notRoot()
    {
        return !"root".equals(System.getProperty("user.name"));
    }

    private void createDirectoryLayout(File dir) throws IOException
    {
        File file1 = new File(dir, "f1");
        File file2 = new File(dir, "f2");
        File nestedDir = new File(dir, "nest");
        File nestedFile = new File(nestedDir, "f");

        assertTrue(dir.mkdir());
        assertTrue(file1.createNewFile());
        Files.write("some content\nin this file\n", file2, Charset.defaultCharset());
        assertTrue(nestedDir.mkdir());
        Files.write("yay", nestedFile, Charset.defaultCharset());
    }

    private String getOtherSeparator()
    {
        if (File.separatorChar == '/')
        {
            return "\\";
        }
        else
        {
            return "/";
        }
    }

    private void simpleEOLTest(byte[] eol, String out) throws IOException
    {
        File test = null;
        try
        {
            test = FileSystemUtils.createTempFile(FileSystemUtilsTest.class.getName(), ".tmp", "line 1\nline 2\nline 3\r\nline 4\nline 5\rline 6\r\nline 7\rline 8\r");
            FileSystemUtils.translateEOLs(test, eol, false);
            Assert.assertEquals(out, Files.toString(test, Charset.defaultCharset()));
        }
        finally
        {
            if (test != null)
            {
                test.delete();
            }
        }
    }

    /**
     * Asserts that the contents of the two given files is identical.
     *
     * @param file1 the first file to compare
     * @param file2 the second file to compare
     * @throws junit.framework.AssertionFailedError
     *          if the contents of the files differ
     */
    protected void assertFilesEqual(File file1, File file2) throws IOException
    {
        if (!file1.isFile())
        {
            throw new AssertionFailedError("File '" + file1.getAbsolutePath() + "' does not exist or is not a regular file");
        }

        if (!file2.isFile())
        {
            throw new AssertionFailedError("File '" + file2.getAbsolutePath() + "' does not exist or is not a regular file");
        }

        BufferedReader rs1 = null;
        BufferedReader rs2 = null;
        try
        {
            rs1 = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
            rs2 = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));
            while (true)
            {
                String line1 = rs1.readLine();
                String line2 = rs2.readLine();

                if (line1 == null)
                {
                    if (line2 == null)
                    {
                        return;
                    }
                    throw new AssertionFailedError("Contents of '" + file1.getAbsolutePath() + " differs from contents of '" + file2.getAbsolutePath() + "'");
                }
                else
                {
                    if (line2 == null)
                    {
                        throw new AssertionFailedError("Contents of '" + file1.getAbsolutePath() + " differs from contents of '" + file2.getAbsolutePath() + "'");
                    }
                    assertEquals(line1, line2);
                }
            }
        }
        finally
        {
            IOUtils.close(rs1);
            IOUtils.close(rs2);
        }
    }

    /**
     * Compares the content of the two given directories recursively,
     * asserting that they have identical contents.  That is, all the
     * contained files and directories are the same, as is the
     * contents of the files.
     *
     * @param dir1 the first directory in the comparison
     * @param dir2 the second directory in the comparison
     * @throws junit.framework.AssertionFailedError
     *          if the given directories
     *          differ
     */
    protected void assertDirectoriesEqual(File dir1, File dir2) throws IOException
    {
        if (!dir1.isDirectory())
        {
            throw new AssertionFailedError("Directory '" + dir1.getAbsolutePath() + "' does not exist or is not a directory");
        }

        if (!dir2.isDirectory())
        {
            throw new AssertionFailedError("Directory '" + dir2.getAbsolutePath() + "' does not exist or is not a directory");
        }

        String[] files1 = dir1.list();
        String[] files2 = dir2.list();

        // File.list does not guarantee ordering, so we do
        Arrays.sort(files1);
        Arrays.sort(files2);

        List<String> fileList1 = new LinkedList<String>(asList(files1));
        List<String> fileList2 = new LinkedList<String>(asList(files2));

        // Ignore .svn directories
        fileList1.remove(".svn");
        fileList2.remove(".svn");

        if (!fileList1.equals(fileList2))
        {
            throw new AssertionFailedError("Directory contents differ: " +
                    dir1.getAbsolutePath() + " = " + fileList1 + ", " +
                    dir2.getAbsolutePath() + " = " + fileList2);
        }

        for (String file : fileList1)
        {
            File file1 = new File(dir1, file);
            File file2 = new File(dir2, file);

            if (file1.isDirectory())
            {
                assertDirectoriesEqual(file1, file2);
            }
            else
            {
                assertFilesEqual(file1, file2);
            }
        }
    }


    private interface LinkMaker
    {
        void makeLink(File link, File linkTarget) throws IOException;
    }

    private static class SymbolicLinkMaker implements LinkMaker
    {
        public void makeLink(File link, File linkTarget) throws IOException
        {
            FileSystemUtils.createSymlink(link, linkTarget);
        }
    }

    private static class WindowsJunctionLinkMaker implements LinkMaker
    {
        public void makeLink(File link, File linkTarget) throws IOException
        {
            SystemUtils.runCommand("cmd", "/c", "mklink", "/j", link.getAbsolutePath(), linkTarget.getAbsolutePath());
        }
    }

    private List<LinkMaker> getLinkMakers()
    {
        if (SystemUtils.IS_WINDOWS)
        {
            System.setProperty(FileSystemUtils.PROPERTY_RMDIR_COMMAND, "cmd /c rmdir /s /q $(dir)");
            return Arrays.<LinkMaker>asList(new WindowsJunctionLinkMaker());
        }
        else
        {
            return Arrays.<LinkMaker>asList(new SymbolicLinkMaker());
        }
    }


    interface Copier
    {
        String getName();

        void copy(File dest, File... src) throws IOException;
    }

    class DefaultCopier implements Copier
    {
        public String getName()
        {
            return "default";
        }

        public void copy(File dest, File... src) throws IOException
        {
            FileSystemUtils.copy(dest, src);
        }
    }

    class JavaCopier implements Copier
    {
        public String getName()
        {
            return "java";
        }

        public void copy(File dest, File... src) throws IOException
        {
            FileSystemUtils.javaCopy(dest, src);
        }
    }

    class UnixCopier implements Copier
    {
        public String getName()
        {
            return "unix";
        }

        public void copy(File dest, File... src) throws IOException
        {
            FileSystemUtils.unixCopy(dest, src);
        }
    }

    interface CopyTest
    {
        void execute(Copier copier) throws IOException;
    }
}
