package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.Scm;
import org.apache.commons.vfs.FileSystemException;

/**
 * Provider for accessing Scm instances.
 */
public interface SCMProvider
{
    Scm getScm() throws FileSystemException;
}
