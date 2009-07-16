package com.zutubi.pulse.acceptance.dependencies;

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
    private Repository repository;
    private String contents;
    private String path;

    protected IvyFile(Repository repository, String path)
    {
        this.repository = repository;
        this.path = path;
    }

    /**
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
        return getAttribute("status");
    }

    /**
     * Get the ivy files revision attribute.
     *
     * @return  the revision attribute, or null if it could not be located.
     */
    public String getRevision()
    {
        return getAttribute("revision");
    }

    /**
     * Get the embedded build number field from the ivy file.
     *
     * @return the build number, or null if it could not be located.
     */
    public String getBuildNumber()
    {
        return getTagContent("buildNumber");
    }

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
        return new File(repository.getBase(), path);
    }

    public String getPath()
    {
        return path;
    }
}
