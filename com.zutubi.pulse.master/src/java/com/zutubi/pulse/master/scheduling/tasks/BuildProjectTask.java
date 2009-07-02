package com.zutubi.pulse.master.scheduling.tasks;

import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerBuildReason;
import com.zutubi.pulse.master.scheduling.Task;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A trigger task which triggers a project build.
 */
public class BuildProjectTask implements Task
{
    /**
     * The fixed revision to be built, if not present the revision will float
     * to the latest (Revision, optional).
     */
    public static final String PARAM_REVISION  = "revision";
    /**
     * Indicates if the raised build request should be replaceable by later
     * requests from the same source while queued (Boolean, optional, defaults
     * to true).
     */
    public static final String PARAM_REPLACEABLE = "replaceable";

    /**
     * The status to be used for the build.
     */
    public static final String PARAM_STATUS = "status";

    /**
     * The version to be used for the build.
     */
    public static final String PARAM_VERSION = "version";
    /**
     * Indicates if the associated version has been propagated from
     * a previous build.
     */
    public static final String PARAM_VERSION_PROPAGATED = "version.propagated";
    /**
     * Indicates if this build project task is triggered as a result of
     * a dependency relationship by the completion of a dependent projects build.
     */
    public static final String PARAM_DEPENDENT = "dependent";

    private static final Logger LOG = Logger.getLogger(BuildProjectTask.class);

    private ProjectManager projectManager;

    public void execute(TaskExecutionContext context)
    {
        Trigger trigger = context.getTrigger();
        long projectId = trigger.getProject();
        Revision revision = (Revision) context.get(PARAM_REVISION);

        Boolean replaceableValue = (Boolean) context.get(PARAM_REPLACEABLE);
        boolean replaceable = replaceableValue == null || replaceableValue;
        String status = (String) context.get(PARAM_STATUS);

        Boolean dependentValue = (Boolean) context.get(PARAM_DEPENDENT);
        boolean dependent = dependentValue != null && dependentValue;

        Boolean versionPropagatedValue = (Boolean) context.get(PARAM_VERSION_PROPAGATED);
        boolean versionPropagated = versionPropagatedValue != null && versionPropagatedValue;

        String version = (String) context.get(PARAM_VERSION);

        ProjectConfiguration project = projectManager.getProjectConfig(projectId, false);
        if (project != null)
        {
            @SuppressWarnings({"unchecked"})
            Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) project.getExtensions().get(MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS);
            TriggerConfiguration triggerConfig = triggers.get(trigger.getName());
            Collection<ResourcePropertyConfiguration> properties;
            if (triggerConfig == null)
            {
                properties = Collections.emptyList();
            }
            else
            {
                properties = triggerConfig.getProperties().values();
            }

            // generate build request.
            TriggerOptions options = new TriggerOptions(new TriggerBuildReason(trigger.getName()), getSource(trigger));
            options.setReplaceable(replaceable);
            options.setForce(false);
            options.setProperties(properties);
            options.setStatus(status);
            options.setResolveVersion(!versionPropagated);
            options.setVersion(version);
            options.setDependent(dependent);
            projectManager.triggerBuild(project, options, revision);
        }
        else
        {
            LOG.warning("Build project task fired for unknown project '" + projectId + "' (trigger '" + trigger.getName() + "')");
        }
    }

    private static String getSource(Trigger trigger)
    {
        return "trigger '" + trigger.getName() + "'";
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
