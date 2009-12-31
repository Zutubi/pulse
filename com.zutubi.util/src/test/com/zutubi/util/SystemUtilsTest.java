package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;

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
