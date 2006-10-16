package com.zutubi.pulse.test;

import com.zutubi.pulse.Version;

import java.io.File;
import java.net.URL;

/**
 * <class comment/>
 */
public class TestUtils
{
    public static File getPulseRoot()
    {
        // First, take a guess at the working directory (which is likely to
        // work if we are running tests using Ant)
        File master = new File("master");
        if (master.isDirectory())
        {
            return new File(".");
        }
        else
        {
            // OK, maybe we can find indirectly via the classpath
            URL resource = Version.class.getResource("version.properties");
            return new File(resource.getPath().replaceFirst("core/classes/.*", ""));
        }
    }
}
