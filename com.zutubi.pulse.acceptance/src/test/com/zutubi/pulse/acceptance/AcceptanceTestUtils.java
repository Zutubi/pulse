package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.util.Condition;
import com.zutubi.util.TextUtils;
import com.zutubi.util.config.Config;
import com.zutubi.util.config.FileConfig;
import com.zutubi.util.config.ReadOnlyConfig;

import java.io.File;
import java.io.IOException;

public class AcceptanceTestUtils
{
    /**
     * The acceptance test system property for the built pulse package.
     */
    protected static final String PROPERTY_PULSE_PACKAGE = "pulse.package";

    /**
     * The acceptance test system property for the built agent package.
     */
    protected static final String PROPERTY_AGENT_PACKAGE = "agent.package";

    /**
     * The acceptance test system property for the pulse startup port.
     */
    public static final String PROPERTY_PULSE_PORT = "pulse.port";

    /**
     * The acceptance test system property for the agent startup port.
     */
    public static final String PROPERTY_AGENT_PORT = "agent.port";

    public static int getPulsePort()
    {
        return Integer.getInteger(PROPERTY_PULSE_PORT, 8080);
    }

    public static void setPulsePort(int port)
    {
        System.setProperty(PROPERTY_PULSE_PORT, Integer.toString(port));
    }

    public static int getAgentPort()
    {
        return Integer.getInteger(PROPERTY_AGENT_PORT, 8890);
    }

    public static File getWorkingDirectory()
    {
        // from IDEA, the working directory is located in the same directory as where the projects are run.
        File workingDir = new File("./working");
        if (System.getProperties().contains("work.dir"))
        {
            // from the acceptance test suite, the work.dir system property is specified
            workingDir = new File(System.getProperty("work.dir"));
        }
        return workingDir;
    }

    public static File getDataDirectory() throws IOException
    {
        // Acceptance tests should all be using the user.home directory in the /working directory.
        File userHome = new File(getWorkingDirectory(), "user.home");
        Config config = loadConfigFromHome(userHome);
        if (config != null)
        {
            return new File(config.getProperty(SystemConfiguration.PULSE_DATA));
        }

        // Guess at the ./data directory in the current working directory.
        File data = new File("./data");
        if (data.exists())
        {
            return data;
        }

        // chances are that if we pick up the systems user.home we may or may not
        // be picking up the right config so we try it last.
        userHome = new File(System.getProperty("user.home"));
        config = loadConfigFromHome(userHome);
        if (config != null)
        {
            return new File(config.getProperty(SystemConfiguration.PULSE_DATA));
        }

        return null;
    }

    /**
     * Load the config.properties instance from the specified user home directory.
     *
     * @param userHome  the user home directory being used by Pulse.
     * @return  the config instance of null if the config.properties file was not located.
     */
    private static Config loadConfigFromHome(File userHome)
    {
        File configFile = new File(userHome, MasterConfigurationManager.CONFIG_DIR + "/config.properties");
        if (configFile.exists())
        {
            return new ReadOnlyConfig(new FileConfig(configFile));
        }
        return null;
    }

    /**
     * Wait for the condition to be true before returning.  If the condition does not return true with
     * the given timeout, a runtime exception is generated with a message based on the description.  Note
     * that the wait will last at least as long as the timeout period, and maybe a little longer.
     *
     * @param condition     the condition which needs to be satisfied before returning
     * @param timeout       the amount of time given for the condition to return true before
     * generating a runtime exception
     * @param description   a human readable description of what the condition is waiting for which will be
     * used in the message of the generated timeout exception
     *
     * @throws RuntimeException if the timeout is reached or if this thread is interrupted.
     */
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
        return getPackage(PROPERTY_PULSE_PACKAGE);
    }

    /**
     * Returns the location of the Pulse agent pacakge, based on the agent.package
     * system property.
     *
     * @return file reference to the pulse agent package.
     */
    public static File getAgentPackage()
    {
        return getPackage(PROPERTY_AGENT_PACKAGE);
    }

    private static File getPackage(String packageProperty)
    {
        String pkgProperty = System.getProperty(packageProperty);
        if (!TextUtils.stringSet(pkgProperty))
        {
            throw new IllegalStateException("No package specified (use the system property " + packageProperty + ")");
        }
        File pkg = new File(pkgProperty);
        if (!pkg.exists())
        {
            throw new IllegalStateException("Unexpected invalid " + packageProperty + ": " + pkg + " does not exist.");
        }
        return pkg;
    }
}
