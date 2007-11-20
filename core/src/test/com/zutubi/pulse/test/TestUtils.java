package com.zutubi.pulse.test;

import com.zutubi.pulse.Version;
import com.zutubi.util.TextUtils;

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
        String pulseRoot = System.getProperty("pulse.root");
        if (TextUtils.stringSet(pulseRoot))
        {
            File rootFile = new File(pulseRoot);
            if (rootFile.isDirectory())
            {
                return rootFile;
            }
        }

        File master = new File("master");
        if (master.isDirectory())
        {
            return master.getAbsoluteFile().getParentFile();
        }

        master = new File("../master");
        if (master.isDirectory())
        {
            return master.getAbsoluteFile().getParentFile();
        }

        // OK, maybe we can find indirectly via the classpath
        URL resource = Version.class.getResource("version.properties");
        System.out.println("Version.properties path: " + resource.getPath());
        return new File(resource.getPath().replaceFirst("core/classes/.*", ""));
    }
}
