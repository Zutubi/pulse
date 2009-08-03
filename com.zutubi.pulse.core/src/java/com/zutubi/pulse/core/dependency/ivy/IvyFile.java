package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.io.IOUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.IOException;

/**
 * A reference to an ivy file within the repository.
 */
public class IvyFile
{
    private static final String ATTRIBUTE_STATUS = "status";
    private static final String ATTRIBUTE_REVISION = "revision";
    private static final String TAG_BUILD_NUMBER = "buildNumber";

    private File repository;
    private String contents;
    private String path;

    /**
     * Create a new instance of the IvyFile.
     *
     * @param repository    the base directory of the repository in which this ivy file is located.
     * @param path          the path relative to the repository base that defines the location of the ivy file.
     */
    public IvyFile(File repository, String path)
    {
        this.repository = repository;
        this.path = path;
    }

    /**
     * Returns true if the file exists within the repository, false otherwise.
     *
     * @return true if the ivy file exists.
     */
    public boolean exists()
    {
        return getFile().exists();
    }

    /**
     * Get the ivy files status attribute.
     *
     * @return  the status attribute, or null if it could not be located.
     */
    public String getStatus()
    {
        return getAttribute(ATTRIBUTE_STATUS);
    }

    /**
     * Get the ivy files revision attribute.
     *
     * @return  the revision attribute, or null if it could not be located.
     */
    public String getRevision()
    {
        return getAttribute(ATTRIBUTE_REVISION);
    }

    /**
     * Get the embedded build number field from the ivy file.
     *
     * @return the build number, or null if it could not be located.
     */
    public String getBuildNumber()
    {
        return getTagContent(TAG_BUILD_NUMBER);
    }

    /**
     * Get the repository path to this ivy file.
     * 
     * @return  the repository relative path to this ivy file.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Retrieve the attribute from within the xml file content.  Note that the attribute is
     * not bound to any specific tag within the ivy file, so if an attribute appears multiple
     * times within the xml file, its first occurance will be returned.
     *
     * @param attributeName the name of the attribute.
     * 
     * @return  the value of the attribute, or null if it is not found.
     */
    private String getAttribute(String attributeName)
    {
        Pattern pattern = Pattern.compile(".*" + attributeName + "=\"(.*?)\".*", Pattern.DOTALL);
        Matcher m = pattern.matcher(getContents());
        if (m.matches())
        {
            return m.group(1);
        }
        return null;
    }

    private String getTagContent(String tagName)
    {
        Pattern pattern = Pattern.compile(".*<" + tagName + ">(.*)</" + tagName + ">.*", Pattern.DOTALL);
        Matcher m = pattern.matcher(getContents());
        if (m.matches())
        {
            return m.group(1);
        }
        return null;
    }

    private String getContents()
    {
        if (contents == null)
        {
            try
            {
                contents = IOUtils.fileToString(getFile());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return contents;
    }

    private File getFile()
    {
        return new File(repository, path);
    }

}