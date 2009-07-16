package com.zutubi.pulse.acceptance.dependencies;

import java.io.File;

/**
 * A reference to an artifact file within the repository.
 */
public class ArtifactFile
{
    private Repository repository;
    private String path;

    protected ArtifactFile(Repository repository, String path)
    {
        this.repository = repository;
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public boolean exists()
    {
        return getFile().exists();
    }

    private File getFile()
    {
        return new File(repository.getBase(), path);
    }
}
