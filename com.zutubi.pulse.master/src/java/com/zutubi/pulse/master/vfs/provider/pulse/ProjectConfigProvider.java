package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import org.apache.commons.vfs.FileSystemException;

/**
 */
public interface ProjectConfigProvider
{
    ProjectConfiguration getProjectConfig() throws FileSystemException;
}
