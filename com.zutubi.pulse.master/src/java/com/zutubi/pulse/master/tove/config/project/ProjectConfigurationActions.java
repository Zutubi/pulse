package com.zutubi.pulse.master.tove.config.project;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.scm.ScmFileResolver;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.tove.annotations.Permission;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Action links for the project config page.
 */
public class ProjectConfigurationActions
{
    public static final String ACTION_CANCEL_BUILD         = "cancelBuild";
    public static final String ACTION_CONVERT_TO_CUSTOM    = "convertToCustom";
    public static final String ACTION_CONVERT_TO_VERSIONED = "convertToVersioned";
    public static final String ACTION_CLEAR_RESPONSIBILITY = "clearResponsibility";
    public static final String ACTION_TAKE_RESPONSIBILITY  = "takeResponsibility";
    public static final String ACTION_INITIALISE           = "initialise";
    public static final String ACTION_PAUSE                = "pause";
    public static final String ACTION_RESUME               = "resume";
    public static final String ACTION_VIEW_SOURCE          = "viewSource";
    public static final String ACTION_TRIGGER              = "trigger";
    public static final String ACTION_TRIGGER_HOOK         = "triggerHook";
    public static final String ACTION_MARK_CLEAN           = "clean";

    private static final Logger LOG = Logger.getLogger(ProjectConfigurationActions.class);
    
    private ProjectManager projectManager;
    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ScmManager scmManager;
    private ObjectFactory objectFactory;

    public boolean actionsEnabled(ProjectConfiguration instance, boolean deeplyValid)
    {
        return deeplyValid;
    }

    public List<String> getActions(final ProjectConfiguration instance)
    {
        // Templates are considered initialised
        boolean initialised = true;
        List<String> result = new LinkedList<String>();
        if (instance.isConcrete())
        {
            result.add(ACTION_MARK_CLEAN);
            Project project = null;
            try
            {
                // Security checks are done at a higher level, and should not apply here.
                project = SecurityUtils.callAsSystem(new Callable<Project>()
                {
                    public Project call()
                    {
                        return projectManager.getProject(instance.getProjectId(), true);
                    }
                });
            }
            catch (Exception e)
            {
                LOG.severe(e);
            }

            if (project != null)
            {
                Project.State state = project.getState();
                if (state.acceptTrigger(false))
                {
                    result.add(ACTION_TRIGGER);
                }

                initialised = state.isInitialised();

                Set<Project.Transition> validTransitions = state.getValidTransitions().keySet();
                if (validTransitions.contains(Project.Transition.INITIALISE))
                {
                    result.add(ACTION_INITIALISE);
                }

                if (validTransitions.contains(Project.Transition.PAUSE))
                {
                    result.add(ACTION_PAUSE);
                }

                if (validTransitions.contains(Project.Transition.RESUME))
                {
                    result.add(ACTION_RESUME);
                }
            }
        }

        if(initialised && canConvertType(instance))
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

    @Permission(AccessManager.ACTION_WRITE)
    public void doInitialise(ProjectConfiguration projectConfig)
    {
        projectManager.makeStateTransition(projectConfig.getProjectId(), Project.Transition.INITIALISE);
    }

    @Permission(ACTION_TRIGGER)
    public void doTrigger(ProjectConfiguration projectConfig)
    {
        String user = SecurityUtils.getLoggedInUsername();
        TriggerOptions options = new TriggerOptions(new ManualTriggerBuildReason(user), ProjectManager.TRIGGER_CATEGORY_MANUAL);
        projectManager.triggerBuild(projectConfig, options, null);
    }

    public List<String> variantsOfTrigger(ProjectConfiguration projectConfiguration)
    {
        List<ManualTriggerConfiguration> triggers = TriggerUtils.getTriggers(projectConfiguration, ManualTriggerConfiguration.class);
        return Lists.transform(triggers, new Function<ManualTriggerConfiguration, String>()
        {
            public String apply(ManualTriggerConfiguration input)
            {
                return input.getName();
            }
        });
    }

    @Permission(ACTION_TRIGGER)
    public void doRebuild(ProjectConfiguration projectConfig)
    {
        String user = SecurityUtils.getLoggedInUsername();
        TriggerOptions options = new TriggerOptions(new ManualTriggerBuildReason(user), ProjectManager.TRIGGER_CATEGORY_MANUAL);
        options.setRebuild(true);
        projectManager.triggerBuild(projectConfig, options, null);
    }

    @Permission(ACTION_PAUSE)
    public void doPause(ProjectConfiguration projectConfig)
    {
        projectManager.makeStateTransition(projectConfig.getProjectId(), Project.Transition.PAUSE);
    }

    @Permission(ACTION_PAUSE)
    public void doResume(ProjectConfiguration projectConfig)
    {
        projectManager.makeStateTransition(projectConfig.getProjectId(), Project.Transition.RESUME);
    }

    @Permission(ACTION_MARK_CLEAN)
    public void doClean(ProjectConfiguration projectConfig)
    {
        projectManager.cleanupWorkDirs(projectConfig);
    }

    public CustomTypeConfiguration prepareConvertToCustom(final ProjectConfiguration projectConfiguration)
    {
        CustomTypeConfiguration result = new CustomTypeConfiguration();
        try
        {
            FileResolver fileResolver;
            Project project = projectManager.getProject(projectConfiguration.getProjectId(), false);
            if (project == null)
            {
                // Project invalid, or a template (CIB-2979).  In this case just make a best effort
                // by resolving files to empty.
                fileResolver = new FileResolver()
                {
                    public InputStream resolve(String path) throws Exception
                    {
                        return new ByteArrayInputStream(new byte[0]);
                    }
                };
            }
            else
            {
                fileResolver = new ScmFileResolver(project, Revision.HEAD, scmManager);
            }

            result.setPulseFileString(projectConfiguration.getType().getPulseFile().getFileContent(fileResolver));
        }
        catch (Exception e)
        {
            // We tried, and no damage is done.
            LOG.warning(e);
        }

        return result;
    }

    @Permission(AccessManager.ACTION_WRITE)
    public ActionResult doConvertToCustom(final ProjectConfiguration projectConfig, final CustomTypeConfiguration custom)
    {
        return configurationProvider.executeInsideTransaction(new NullaryFunction<ActionResult>()
        {
            public ActionResult process()
            {
                if(!canConvertType(projectConfig))
                {
                    throw new IllegalArgumentException("Cannot convert type as it is either not defined at this level or overridden");
                }

                String typePath = projectConfig.getType().getConfigurationPath();
                configurationProvider.delete(typePath);
                configurationProvider.insert(typePath, custom);
                return new ActionResult(ActionResult.Status.SUCCESS, null, Arrays.asList(typePath));
            }
        });
    }

    @Permission(AccessManager.ACTION_WRITE)
    public ActionResult doConvertToVersioned(final ProjectConfiguration projectConfig, final VersionedTypeConfiguration versioned)
    {
        return configurationProvider.executeInsideTransaction(new NullaryFunction<ActionResult>()
        {
            public ActionResult process()
            {
                if(!canConvertType(projectConfig))
                {
                    throw new IllegalArgumentException("Cannot convert type as it is either not defined at this level or overridden");
                }

                String typePath = projectConfig.getType().getConfigurationPath();
                configurationProvider.delete(typePath);
                configurationProvider.insert(typePath, versioned);
                return new ActionResult(ActionResult.Status.SUCCESS, null, Arrays.asList(typePath));
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

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
