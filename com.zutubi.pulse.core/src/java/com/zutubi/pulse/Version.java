package com.zutubi.pulse;

import com.zutubi.util.Constants;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * This class provides a java interface to the contents of the version.properties
 * file. All access to the contents of that properties file should be handled
 * through ths object.  As such, this object provides access to the following:
 * <ul>
 * <li>version number: the human friendly product version string</li>
 * <li>build date: the date on which this version of the product was built.</li>
 * <li>build number: the unique build number identifying this build.</li>
 * <li>release date: the date on which this major.minor version of the software was released.</li>
 * </ul>
 */
public class Version implements Comparable
{
    private static final Logger LOG = Logger.getLogger(Version.class);

    /**
     * The resource name relative to the location of this class.
     */
    private static final String RESOURCE = "version.properties";

    /**
     * The version number property name.
     */
    public static final String VERSION_NUMBER = "version.number";

    /**
     * The build date property name.
     */
    public static final String BUILD_DATE = "build.date";

    /**
     * The build number property name.
     */
    public static final String BUILD_NUMBER = "build.number";

    /**
     * The release date property name.
     */
    public static final String RELEASE_DATE = "release.date";

    public static final int INVALID = -1;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d-MM-yyyy");

    /**
     * For backwards compatibility only.
     */
    private static final SimpleDateFormat OLD_DATE_FORMAT = new SimpleDateFormat("d-MMMM-yyyy");

    /**
     * The value of the version number property.
     */
    private String versionNumber;

    /**
     * The value of the build date property.
     */
    private String buildDate;

    /**
     * The value of the build number property.
     */
    private String buildNumber;

    private String releaseDate;

    public Version()
    {
        // For hessian.  Better to have this empty default, as otherwise
        // using a passed-in value can lead to NPEs (see CIB-804).
    }

    public Version(String versionNumber, String buildDate, String buildNumber, String releaseDate)
    {
        this.versionNumber = versionNumber;
        this.buildDate = buildDate;
        this.buildNumber = buildNumber;
        this.releaseDate = releaseDate;

        // CIB-717: fix the build number released with 1.2 M1
        if (buildNumber.equals("010200000"))
        {
            this.buildNumber = "0102000000";
        }
    }

    /**
     * The version string is a human readable representation of the current version
     * of the system.
     *
     * @return the version number
     */
    public String getVersionNumber()
    {
        return versionNumber;
    }

    /**
     * The build date is a string representing the date that this version of the system
     * was built.
     *
     * @return the build date
     */
    public String getBuildDate()
    {
        return buildDate;
    }

    public Date getBuildDateAsDate()
    {
        try
        {
            synchronized (DATE_FORMAT)
            {
                return DATE_FORMAT.parse(getBuildDate());
            }
        }
        catch (ParseException e)
        {
            // If the version details were taken from the file system, then they may be using the old format.
            // So lets try it just in case.
            try
            {
                synchronized (OLD_DATE_FORMAT)
                {
                    return OLD_DATE_FORMAT.parse(getBuildDate());
                }
            }
            catch (ParseException e1)
            {
                LOG.severe("Failed to parse '" + getBuildDate() + "'", e);
                return null;
            }
        }
    }

    /**
     * The release date is a string representing the date on which the latest major.minor
     * version was released. For example, all 1.1.x builds would have the same release date.
     *
     * This is used to distinguish between a 'software release date' (releaseDate) and a
     * 'patch release date' (buildDate)
     *
     * @return the release date.
     */
    public String getReleaseDate()
    {
        return releaseDate;
    }

    public Date getReleaseDateAsDate()
    {
        try
        {
            synchronized (DATE_FORMAT)
            {
                return DATE_FORMAT.parse(getReleaseDate());
            }
        }
        catch (ParseException e)
        {
            // If the version details were taken from the file system, then they may be using the old format.
            // So lets try it just in case.
            try
            {
                synchronized (OLD_DATE_FORMAT)
                {
                    return OLD_DATE_FORMAT.parse(getReleaseDate());
                }
            }
            catch (ParseException e1)
            {
                LOG.severe("Failed to parse '" + getReleaseDate() + "'.", e);
                // In this case the most conservative thing is to pretend the
                // release is very new.  We give a week leeway, which is
                // quite harmless and aids testing.
                return new Date(System.currentTimeMillis() - 7 * Constants.DAY);
            }
        }
    }

    /**
     * The build number is a machine friendly representation of the current version of the
     * system.
     *
     * @return the build number.
     */
    public String getBuildNumber()
    {
        return buildNumber;
    }

    public int getBuildNumberAsInt()
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
        return read(properties);
    }

    public static Version read(Properties properties)
    {
        return new Version(properties.getProperty(VERSION_NUMBER),
                properties.getProperty(BUILD_DATE),
                properties.getProperty(BUILD_NUMBER),
                properties.getProperty(RELEASE_DATE)
        );
    }

    /**
     * Write this verison object to the specified output stream.
     *
     * @param out the output stream to which we are writing the version details.
     *
     * @throws IOException if there is a problem writing to the specified output stream
     */
    public void write(OutputStream out) throws IOException
    {
        Properties props = new Properties();
        write(props);
        props.store(out, null);
    }

    /**
     * Write this version object to the specified properties object.
     * @param props the properties object to which we are writing the version details in key:value format.
     */
    public void write(Properties props)
    {
        props.setProperty(VERSION_NUMBER, getVersionNumber());
        props.setProperty(BUILD_DATE, getBuildDate());
        props.setProperty(BUILD_NUMBER, getBuildNumber());
        props.setProperty(RELEASE_DATE, getReleaseDate());
    }

    /**
     * Helper method for writing this version to a file.
     * @param f the file to which we are writing the version details.
     *
     * @throws IOException if there is a problem writing the version details to the file.
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
     * @param f the file from which we are loading the version details.
     *
     * @return version defined in file, or null.
     *
     * @throws IOException if we failed to load the version details from the specified file.
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
            return new Version("N/A", "N/A", "N/A", "N/A");
        }
        finally
        {
            IOUtils.close(in);
        }
    }

    public int compareTo(Object o)
    {
        Version otherVersion = (Version) o;

        Integer i = getBuildNumberAsInt();
        Integer j = otherVersion.getBuildNumberAsInt();

        return i.compareTo(j);
    }

    public static int getPatch(int buildNumber)
    {
        return buildNumber % 1000;
    }

    public static String buildNumberToVersion(int buildNumber)
    {
        // Digits are: major major minor minor build build build patch patch patch
        buildNumber = buildNumber / 1000;
        int build = buildNumber % 1000;
        buildNumber = buildNumber / 1000;
        int minor = buildNumber % 100;
        int major = buildNumber / 100;

        return String.format("%d.%d.%d", major, minor, build);
    }
}
