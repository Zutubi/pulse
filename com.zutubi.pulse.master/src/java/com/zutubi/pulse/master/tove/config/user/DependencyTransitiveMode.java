package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder;
import static com.zutubi.pulse.master.dependency.ProjectDependencyGraphBuilder.TransitiveMode.*;

/**
 * Configuration version of transitive modes for dependency graphs.
 */
public enum DependencyTransitiveMode
{
    SHOW_FULL_CASCADE(FULL),
    TRIM_AND_SHADE_DUPLICATE_SUBTREES(TRIM_DUPLICATES),
    REMOVE_DUPLICATE_SUBTREES(REMOVE_DUPLICATES),
    SHOW_DIRECT_DEPENDENCIES_ONLY(NONE);

    private ProjectDependencyGraphBuilder.TransitiveMode correspondingMode;

    DependencyTransitiveMode(ProjectDependencyGraphBuilder.TransitiveMode correspondingMode)
    {
        this.correspondingMode = correspondingMode;
    }

    public ProjectDependencyGraphBuilder.TransitiveMode getCorrespondingMode()
    {
        return correspondingMode;
    }
}
