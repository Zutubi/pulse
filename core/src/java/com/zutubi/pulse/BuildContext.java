package com.zutubi.pulse;

import com.zutubi.pulse.repository.FileRepository;

import java.text.SimpleDateFormat;

/**
 * The build context contains contextual information relating to the build
 * that is currently being processed.
 *
 */
public class BuildContext
{
    public static final SimpleDateFormat PULSE_BUILD_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    private long buildNumber = -1;
    private long buildTimestamp = -1;
    private String buildRevision = null;
    private FileRepository fileRepository;

    public long getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(long buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public long getBuildTimestamp()
    {
        return buildTimestamp;
    }

    public void setBuildTimestamp(long buildTimestamp)
    {
        this.buildTimestamp = buildTimestamp;
    }

    public String getBuildRevision()
    {
        return buildRevision;
    }

    public void setBuildRevision(String buildRevision)
    {
        this.buildRevision = buildRevision;
    }

    public FileRepository getFileRepository()
    {
        return fileRepository;
    }

    public void setFileRepository(FileRepository fileRepository)
    {
        this.fileRepository = fileRepository;
    }
}
