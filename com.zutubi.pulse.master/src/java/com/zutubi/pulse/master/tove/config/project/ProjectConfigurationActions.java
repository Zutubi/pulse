package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.Permission;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Action links for the project config page.
 */
public class ProjectConfigurationActions
{
    public static final String ACTION_CANCEL_BUILD         = "cancel build";
    public static final String ACTION_CONVERT_TO_CUSTOM    = "convertToCustom";
    public static final String ACTION_CONVERT_TO_VERSIONED = "convertToVersioned";
    public static final String ACTION_PAUSE                = "pause";
    public static final String ACTION_RESUME               = "resume";
    public static final String ACTION_VIEW_SOURCE          = "view source";
    public static final String ACTION_TRIGGER              = "trigger";
    public static final String ACTION_MARK_CLEAN           = "clean";

    private static final Logger LOG = Logger.getLogger(ProjectConfigurationActions.class);

    private ProjectManager projectManager;
    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;

    public boolean actionsEnabled(ProjectConfiguration instance, boolean deeplyValid)
    {
        return deeplyValid;
    }

    public List<String> getActions(ProjectConfiguration instance)
    {
        List<String> result = new LinkedList<String>();
        if (instance.isConcrete())
        {
            result.add(ACTION_TRIGGER);
            result.add(ACTION_MARK_CLEAN);
            Project project = projectManager.getProject(instance.getProjectId(), true);
            if (project != null)
            {
                if (project.isPaused())
                {
                    result.add(ACTION_RESUME);
                }
                else
                {
                    result.add(ACTION_PAUSE);
                }
            }
        }

        if(canConvertType(instance))
        {
            if(!(instance.getType() instanceof CustomTypeConfiguration))
            {
                result.add(ACTION_CONVERT_TO_CUSTOM);
            }

            if(!(instance.getType() instanceof VersionedTypeConfiguration))
            {
                result.add(ACTION_CONVERT_TO_VERSIONED);
            }
        }

        return result;
    }

    private boolean canConvertType(ProjectConfiguration instance)
    {
        // We can only convert types if this project owns the type (i.e. it
        // is not inherited) and it is not overridden.
        String typePath = PathUtils.getPath(instance.getConfigurationPath(), "type");
        return !configurationTemplateManager.existsInTemplateParent(typePath) && !configurationTemplateManager.isOverridden(typePath);
    }

    public String customiseTrigger(ProjectConfiguration projectConfig)
    {
        if (projectConfig.getOptions().getPrompt())
        {
            return "editBuildProperties";
        }

        return null;
    }

    @Permission(ACTION_TRIGGER)
    public void doTrigger(ProjectConfiguration projectConfig)
    {
        String user = AcegiUtils.getLoggedInUsername();
        if (user != null)
        {
            projectManager.triggerBuild(projectConfig, new ManualTriggerBuildReason(user), null, ProjectManager.TRIGGER_CATEGORY_MANUAL, false, true);
        }
    }

    @Permission(ACTION_PAUSE)
    public void doPause(ProjectConfiguration projectConfig)
    {
        Project project = projectManager.getProject(projectConfig.getProjectId(), true);
        if (project != null)
        {
            projectManager.pauseProject(project);
        }
    }

    @Permission(ACTION_PAUSE)
    public void doResume(ProjectConfiguration projectConfig)
    {
        Project project = projectManager.getProject(projectConfig.getProjectId(), true);
        if (project != null)
        {
            projectManager.resumeProject(project);
        }
    }

    public void doClean(ProjectConfiguration projectConfig)
    {
        Project project = projectManager.getProject(projectConfig.getProjectId(), true);
        if (project != null)
        {
            projectManager.markForCleanBuild(project);
        }
    }

    public CustomTypeConfiguration prepareConvertToCustom(final ProjectConfiguration projectConfiguration)
    {
        CustomTypeConfiguration result = new CustomTypeConfiguration();
        try
        {
            result.setPulseFileString(projectConfiguration.getType().getPulseFile(projectConfiguration, null, null));
        }
        catch (Exception e)
        {
            // We tried, and no damage is done.
            LOG.warning(e);
        }

        return result;
    }

    @Permission(AccessManager.ACTION_WRITE)
    public List<String> doConvertToCustom(final ProjectConfiguration projectConfig, final CustomTypeConfiguration custom)
    {
        return configurationProvider.executeInsideTransaction(new NullaryFunction<List<String>>()
        {
            public List<String> process()
            {
                if(!canConvertType(projectConfig))
                {
                    throw new IllegalArgumentException("Cannot convert type as it is either not defined at this level or overridden");
                }

                String typePath = projectConfig.getType().getConfigurationPath();
                configurationProvider.delete(typePath);
                configurationProvider.insert(typePath, custom);
                return Arrays.asList(typePath);
            }
        });
    }

    @Permission(AccessManager.ACTION_WRITE)
    public List<String> doConvertToVersioned(final ProjectConfiguration projectConfig, final VersionedTypeConfiguration versioned)
    {
        return configurationProvider.executeInsideTransaction(new NullaryFunction<List<String>>()
        {
            public List<String> process()
            {
                if(!canConvertType(projectConfig))
                {
                    throw new IllegalArgumentException("Cannot convert type as it is either not defined at this level or overridden");
                }

                String typePath = projectConfig.getType().getConfigurationPath();
                configurationProvider.delete(typePath);
                configurationProvider.insert(typePath, versioned);
                return Arrays.asList(typePath);
            }
        });
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
