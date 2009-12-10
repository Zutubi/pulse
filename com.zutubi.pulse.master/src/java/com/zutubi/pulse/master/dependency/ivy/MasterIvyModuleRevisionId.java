package com.zutubi.pulse.master.dependency.ivy;

import com.zutubi.pulse.core.dependency.ivy.IvyModuleRevisionId;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import static java.lang.String.valueOf;

/**
 * A factory for ModuleRevisionIds that provide convenience methods that map
 * Pulse data types to the ModuleRevisionId.
 *
 * @see ModuleRevisionId
 */
public class MasterIvyModuleRevisionId
{
    private MasterIvyModuleRevisionId()
    {
        // ensure that this class can not be instantiated.
    }

    public static ModuleRevisionId newInstance(BuildResult build)
    {
        ProjectConfiguration project = build.getProject().getConfig();
        return newInstance(project, valueOf(build.getNumber()));
    }

    public static ModuleRevisionId newInstance(ProjectConfiguration project, String revision)
    {
        return IvyModuleRevisionId.newInstance(getOrganisation(project), project.getName(), revision);
    }

    public static ModuleRevisionId newInstance(DependencyConfiguration dependency)
    {
        ProjectConfiguration dependentProject = dependency.getProject();
        return IvyModuleRevisionId.newInstance(getOrganisation(dependentProject), dependentProject.getName(), dependency.getDependencyRevision());
    }

    private static String getOrganisation(ProjectConfiguration project)
    {
        String organisation = project.getOrganisation();
        return organisation != null ? organisation.trim() : "";
    }
}
