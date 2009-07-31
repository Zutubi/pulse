package com.zutubi.pulse.master.dependency.ivy;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import static com.zutubi.pulse.master.dependency.ivy.ModuleDescriptorFactory.*;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import static java.lang.String.valueOf;
import java.util.Map;
import java.util.HashMap;

/**
 * An extension of the core IvyModuleRevisionId that adds an extra set of factory methods
 * for data types available to the master code only.
 */
public class MasterIvyModuleRevisionId extends com.zutubi.pulse.core.dependency.ivy.IvyModuleRevisionId 
{
    public static final String EXTRA_ATTRIBUTE_STAGE = NAMESPACE_EXTRA_ATTRIBUTES + ":stage";
    public static final String EXTRA_ATTRIBUTE_SOURCE_FILE = NAMESPACE_EXTRA_ATTRIBUTES + ":sourcefile";

    public static ModuleRevisionId newInstance(BuildResult build)
    {
        ProjectConfiguration project = build.getProject().getConfig();
        return newInstance(project, valueOf(build.getNumber()));
    }

    public static ModuleRevisionId newInstance(ProjectConfiguration project, String revision)
    {
        return newInstance(getOrganisation(project), project.getName(), revision);
    }

    public static ModuleRevisionId newInstance(ProjectConfiguration project, BuildStageConfiguration stage)
    {
        Map<String, String> extraAttributes = new HashMap<String, String>();
        extraAttributes.put(EXTRA_ATTRIBUTE_STAGE, IvyUtils.ivyEncodeStageName(stage.getName()));
        return newInstance(getOrganisation(project), project.getName(), null, extraAttributes);
    }

    public static ModuleRevisionId newInstance(ProjectConfiguration project, BuildStageConfiguration stage, String revision)
    {
        Map<String, String> extraAttributes = new HashMap<String, String>();
        extraAttributes.put(EXTRA_ATTRIBUTE_STAGE, IvyUtils.ivyEncodeStageName(stage.getName()));
        return ModuleRevisionId.newInstance(getOrganisation(project), project.getName(), null, revision, extraAttributes);
    }

    public static ModuleRevisionId newInstance(DependencyConfiguration dependency)
    {
        ProjectConfiguration dependentProject = dependency.getProject();
        return newInstance(getOrganisation(dependentProject), dependentProject.getName(), dependency.getDependencyRevision());
    }

    private static String getOrganisation(ProjectConfiguration project)
    {
        return project.getOrganisation() != null ? project.getOrganisation().trim() : "";
    }
}
