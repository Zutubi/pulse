package com.zutubi.pulse;

import com.zutubi.pulse.repository.FileRepository;

/**
 * The build context contains contextual information relating to the build
 * that is currently being processed.
 *
 */
public class BuildContext
{
    private long buildNumber = -1;
    private FileRepository fileRepository;

    public long getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(long buildNumber)
    {
        this.buildNumber = buildNumber;
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
