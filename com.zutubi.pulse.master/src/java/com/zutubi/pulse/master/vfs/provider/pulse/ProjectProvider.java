package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.Project;

/**
 * A provider interface that indicates the current node represents a project instance.
 *
 * @see com.zutubi.pulse.master.model.Project
 */
public interface ProjectProvider extends ProjectConfigProvider
{
    /**
     * @return the project instance represented by this node.
     */
    Project getProject();

    /**
     * @return the unique identifier for the project instance represented
     * by this node.
     */
    long getProjectId();
}
