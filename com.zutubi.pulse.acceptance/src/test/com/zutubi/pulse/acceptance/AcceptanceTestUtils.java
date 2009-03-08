package com.zutubi.pulse.acceptance;

import com.zutubi.util.Condition;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class AcceptanceTestUtils
{
    public static File getWorkingDirectory()
    {
        File workingDir = new File("./working"); // from IDEA, the working directory is located in the same directory as where the projects are run.
        if (System.getProperties().contains("work.dir"))
        {
            // from the acceptance test suite, the work.dir system property is specified
            workingDir = new File(System.getProperty("work.dir"));
        }
        return workingDir;
    }

    public static File getDataDirectory() throws IOException
    {
        File userHome = new File(System.getProperty("user.home"));
        File configFile = new File(userHome, ".pulse2/config.properties");
        if (configFile.isFile())
        {
            Properties configProperties = IOUtils.read(configFile);
            return new File(configProperties.getProperty("pulse.data"));
        }

        configFile = new File(getWorkingDirectory(), "user.home/.pulse2/config.properties");
        if (configFile.isFile())
        {
            Properties configProperties = IOUtils.read(configFile);
            return new File(configProperties.getProperty("pulse.data"));
        }

        return new File("./data");
    }

    public static void waitForCondition(Condition condition, long timeout, String description)
    {
        long endTime = System.currentTimeMillis() + timeout;
        while (!condition.satisfied())
        {
            if (System.currentTimeMillis() > endTime)
            {
                throw new RuntimeException("Timed out waiting for " + description);
            }

            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException("Interrupted waiting for " + description);
            }
        }
    }

    /**
     * Returns the location of a Pulse package, based on the pulse.package
     * system property.
     *
     * @return file reference to the pulse package
     * @throws IllegalStateException if pulse.package os not set or does not
     *                               refer to a valid file
     */
    public static File getPulsePackage()
    {
        return getPackage("pulse.package");
    }

    /**
     * Returns the location of the Pulse agent pacakge, based on the agent.package
     * system property.
     *
     * @return file reference to the pulse agent package.
     */
    public static File getAgentPackage()
    {
        return getPackage("agent.package");
    }

    public static File getPackage(String packageProperty)
    {
        String pkgProperty = System.getProperty(packageProperty);
        if (!TextUtils.stringSet(pkgProperty))
        {
            throw new IllegalStateException("No package specified (use the system property " + packageProperty + ")");
        }
        File pkg = new File(pkgProperty);
        if (!pkg.isFile())
        {
            throw new IllegalStateException("Unexpected invalid " + packageProperty + ": " + pkg + " does not reference a file.");
        }
        return pkg;
    }
}
