package com.zutubi.pulse;

import com.zutubi.pulse.repository.FileRepository;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * The build context contains contextual information relating to the build
 * that is currently being processed.
 *
 */
public class BuildContext
{
    public static final SimpleDateFormat PULSE_BUILD_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    private long buildNumber = -1;
    private Map<String, String> properties = new HashMap<String, String>();
    /**
     * The version can be extracted while executing a command, and is
     * communicated back out by setting it here.
     */
    private String buildVersion = null;
    private FileRepository fileRepository;

    /**
     * The name of the project being built.
     */
    private String projectName;

    public long getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(long buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public String getBuildVersion()
    {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion)
    {
        this.buildVersion = buildVersion;
    }

    public FileRepository getFileRepository()
    {
        return fileRepository;
    }

    public void setFileRepository(FileRepository fileRepository)
    {
        this.fileRepository = fileRepository;
    }

    public void setProjectName(String project)
    {
        this.projectName = project;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void addProperty(String name, String value)
    {
        properties.put(name, value);
    }
}
