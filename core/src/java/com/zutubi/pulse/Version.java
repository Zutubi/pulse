/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.zutubi.pulse.util.IOUtils;

import java.io.*;
import java.util.Properties;

/**
 * This class provides a java interface to the contents of the version.properties
 * file. All access to the contents of that properties file should be handled
 * through ths object.
 */
public class Version implements Comparable
{
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

    public static final int INVALID = -1;

    private String versionNumber;
    private String buildDate;
    private String buildNumber;

    public Version(String versionNumber, String buildDate, String buildNumber)
    {
        this.versionNumber = versionNumber;
        this.buildDate = buildDate;
        this.buildNumber = buildNumber;
    }

    /**
     * The version string is a human readable representation of the current version
     * of the system.
     */
    public String getVersionNumber()
    {
        return versionNumber;
    }

    /**
     * The build date is a string representing the date that this version of the system
     * was built.
     */
    public String getBuildDate()
    {
        return buildDate;
    }

    /**
     * The build number is a machine friendly representation of the current version of the
     * system.
     */
    public String getBuildNumber()
    {
        return buildNumber;
    }

    public int getIntBuildNumber()
    {
        try
        {
            return Integer.parseInt(buildNumber);
        }
        catch (NumberFormatException e)
        {
            return INVALID;
        }
    }

    public static Version load(InputStream in) throws IOException
    {
        Properties properties = IOUtils.read(in);
        return new Version(properties.getProperty(VERSION_NUMBER),
                properties.getProperty(BUILD_DATE),
                properties.getProperty(BUILD_NUMBER)
        );
    }

    /**
     * Write this verison object to the specified output stream.
     *
     * @param out
     *
     * @throws IOException
     */
    public void write(OutputStream out) throws IOException
    {
        Properties props = new Properties();
        props.setProperty(VERSION_NUMBER, getVersionNumber());
        props.setProperty(BUILD_DATE, getBuildDate());
        props.setProperty(BUILD_NUMBER, getBuildNumber());
        props.store(out, null);
    }

    /**
     * Helper method for writing this version to a file.
     * @param f
     */
    public void write(File f) throws IOException
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(f);
            write(out);
        }
        finally
        {
            IOUtils.close(out);
        }
    }

    /**
     * Helper method for reading version from a file.
     *
     * @param f
     *
     * @return version defined in file, or null.
     */
    public static Version load(File f) throws IOException
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream(f);
            return load(in);
        }
        finally
        {
            IOUtils.close(in);
        }
    }

    public static Version getVersion()
    {
        InputStream in = null;
        try
        {
            in = Version.class.getResourceAsStream(RESOURCE);
            return load(in);
        }
        catch (IOException e)
        {
            return new Version("N/A", "N/A", "N/A");
        }
        finally
        {
            IOUtils.close(in);
        }
    }

    public int compareTo(Object o)
    {
        Version otherVersion = (Version) o;

        Integer i = getIntBuildNumber();
        Integer j = otherVersion.getIntBuildNumber();

        return i.compareTo(j);
    }
}
