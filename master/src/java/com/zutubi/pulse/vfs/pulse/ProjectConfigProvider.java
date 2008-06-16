package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import org.apache.commons.vfs.FileSystemException;

/**
 */
public interface ProjectConfigProvider
{
    ProjectConfiguration getProjectConfig() throws FileSystemException;
}
