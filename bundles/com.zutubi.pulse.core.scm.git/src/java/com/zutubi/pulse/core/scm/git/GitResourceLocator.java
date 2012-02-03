package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.resources.api.SimpleBinaryResourceLocator;

/**
 * Locates the git binary.
 */
public class GitResourceLocator extends SimpleBinaryResourceLocator
{
    public GitResourceLocator()
    {
        super(GitConstants.RESOURCE_NAME, GitConstants.DEFAULT_GIT);
    }
}
