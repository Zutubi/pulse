package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.model.Project;

/**
 * A provider interface that indicates the current node represents a project instance.
 *
 * @see com.zutubi.pulse.model.Project
 */
public interface ProjectProvider
{
    Project getProject();

    long getProjectId();
}
