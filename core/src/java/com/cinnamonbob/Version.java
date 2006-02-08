package com.cinnamonbob;

import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.util.logging.Logger;

import java.util.Properties;
import java.io.IOException;

/**
 * This class provides a java interface to the contents of the version.properties
 * file. All access to the contents of that properties file should be handled
 * through ths object.
 *
 */
public class Version
{
    private static final Logger LOG = Logger.getLogger(Version.class);

    private static Properties properties;

    /**
     * The resource name relative to the location of this class.
     */
    private static final String RESOURCE = "version.properties";

    /**
     * The version number property name.
     */
    private static final String VERSION_NUMBER = "version.number";

    /**
     * The build date property name.
     */
    private static final String BUILD_DATE = "build.date";

    /**
     * The build number property name.
     */
    private static final String BUILD_NUMBER = "build.number";

    private static Properties getProperties()
    {
        if (properties == null)
        {
            try
            {
                properties = IOUtils.read(Version.class.getResourceAsStream(RESOURCE));
            }
            catch (IOException e)
            {
                properties = new Properties();
                LOG.error(e);
            }
        }
        return properties;
    }

    /**
     * The version string is a human readable representation of the current version
     * of the system.
     *
     */
    public static String getVersion()
    {
        return getProperties().getProperty(VERSION_NUMBER);
    }

    /**
     * The build date is a string representing the date that this version of the system
     * was built.
     *
     */
    public static String getBuildDate()
    {
        return getProperties().getProperty(BUILD_DATE);
    }

    /**
     * The build number is a machine friendly representation of the current version of the
     * system.
     * 
     */
    public static String getBuildNumber()
    {
        return getProperties().getProperty(BUILD_NUMBER);
    }


}
