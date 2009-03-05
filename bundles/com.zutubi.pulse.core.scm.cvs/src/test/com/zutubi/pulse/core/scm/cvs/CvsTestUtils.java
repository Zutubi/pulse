package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;

public class CvsTestUtils
{
    public static String getPassword(String name) throws IOException
    {
        return IOUtils.read(new File(".passwords")).getProperty(name);
    }
}
