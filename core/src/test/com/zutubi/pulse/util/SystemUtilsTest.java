package com.zutubi.pulse.util;

import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;

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

        if (SystemUtils.isWindows())
        {
            list = "dir.exe";
        }
        else
        {
            list = "ls";
        }

        File bin = SystemUtils.findInPath(list);
        assertNotNull(bin);
    }
}
