package com.zutubi.pulse.core.util;

import com.zutubi.pulse.test.BobTestCase;

import java.io.File;

/**
 */
public class SystemUtilsTest extends BobTestCase
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
