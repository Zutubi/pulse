package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import org.apache.commons.vfs.FileSystemException;

/**
 * A provider interface that indicates the current node represents a project instance.
 *
 * @see com.zutubi.pulse.model.Project
 */
public interface ProjectProvider
{
    ProjectConfiguration getProjectConfig() throws FileSystemException;

    Project getProject() throws FileSystemException;

    long getProjectId() throws FileSystemException;
}
