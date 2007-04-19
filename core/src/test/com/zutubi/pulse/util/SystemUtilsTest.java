package com.zutubi.pulse.util;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 */
public class SystemUtilsTest extends PulseTestCase
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

    public void testFindInPathExtraPaths() throws IOException
    {
        File extraPath = getTestDataFile("core", "findInPathExtraPaths", "data");
        File bin = SystemUtils.findInPath("dir", Arrays.asList(new String [] { extraPath.getAbsolutePath() } ));
        assertNotNull(bin);
        assertEquals(extraPath.getCanonicalPath(), bin.getParentFile().getCanonicalPath());
    }
}
