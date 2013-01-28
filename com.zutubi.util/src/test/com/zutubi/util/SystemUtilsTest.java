package com.zutubi.util;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.any;
import com.zutubi.util.junit.ZutubiTestCase;
import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

/**
 * NOTE: cygwin is requried on the path for windows machines.
 */
public class SystemUtilsTest extends ZutubiTestCase
{
    public void testFindInPathNonExistant()
    {
        assertNull(SystemUtils.findInPath("nonexistantfile"));
    }

    public void testFindInPathList()
    {
        String list;

        if (SystemUtils.IS_WINDOWS)
        {
            list = "dir";
        }
        else
        {
            list = "ls";
        }

        File bin = SystemUtils.findInPath(list);
        assertNotNull(bin);
    }

    public void testFindInPathComFile()
    {
        if(SystemUtils.IS_WINDOWS)
        {
            assertNotNull(SystemUtils.findInPath("more"));
        }
    }

    public void testFindInPathCaseInsensitive()
    {
        if(SystemUtils.IS_WINDOWS)
        {
            assertNotNull(SystemUtils.findInPath("DiR"));
        }
    }

    public void testFindInPathFindsMaven()
    {
        File mvn = SystemUtils.findInPath("mvn");
        if(mvn != null)
        {
            if(SystemUtils.IS_WINDOWS)
            {
                assertEquals("mvn.bat", mvn.getName());
            }
            else
            {
                assertEquals("mvn", mvn.getName());
            }
        }
    }

    public void testThreadDump()
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SystemUtils.threadDump(new PrintStream(baos));

        String[] dump = new String(baos.toByteArray()).split("\n");
        assertTrue(any(asList(dump), new Predicate<String>()
        {
            public boolean apply(String s)
            {
                return s.contains("at com.zutubi.util.SystemUtils.threadDump");
            }
        }));
    }

/*
    public void testFindInPathExtraPaths() throws IOException, URISyntaxException
    {
        // TODO: getTestDataFile is part of PulseTestCase, not currently available at this level.  Should it be?
        File extraPath = getTestDataFile("core", "findInPathExtraPaths", "data");
        File bin = SystemUtils.findInPath("dir", Arrays.asList(new String [] { extraPath.getAbsolutePath() } ));
        assertNotNull(bin);
        assertEquals(extraPath.getCanonicalPath(), bin.getParentFile().getCanonicalPath());
    }
*/
}
