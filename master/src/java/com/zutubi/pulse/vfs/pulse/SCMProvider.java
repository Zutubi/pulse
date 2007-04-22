package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.servercore.config.ScmConfiguration;
import org.apache.commons.vfs.FileSystemException;

/**
 * Provider for accessing Scm instances.
 */
public interface SCMProvider
{
    ScmConfiguration getScm() throws FileSystemException;
}
