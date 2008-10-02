package com.zutubi.pulse.master.vfs.pulse;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import org.apache.commons.vfs.FileSystemException;

/**
 * Provider for accessing Scm instances.
 */
public interface ScmProvider
{
    ScmConfiguration getScm() throws FileSystemException;
}
