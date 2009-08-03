package com.zutubi.pulse.acceptance.dependencies;

/**
 * A reference to an ivy file within the repository.
 */
public class IvyFile extends com.zutubi.pulse.core.dependency.ivy.IvyFile
{
    protected IvyFile(Repository repository, String path)
    {
        super(repository.getBase(), path);
    }
}
