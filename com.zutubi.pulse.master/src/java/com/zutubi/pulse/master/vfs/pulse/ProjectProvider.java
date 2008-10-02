package com.zutubi.pulse.master.vfs.pulse;

import com.zutubi.pulse.master.model.Project;
import org.apache.commons.vfs.FileSystemException;

/**
 * A provider interface that indicates the current node represents a project instance.
 *
 * @see com.zutubi.pulse.master.model.Project
 */
public interface ProjectProvider extends ProjectConfigProvider
{
    Project getProject() throws FileSystemException;
    long getProjectId() throws FileSystemException;
}
