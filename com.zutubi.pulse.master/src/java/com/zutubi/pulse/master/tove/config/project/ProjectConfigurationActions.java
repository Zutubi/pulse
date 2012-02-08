package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.build.queue.graph.BuildGraphData;
import com.zutubi.pulse.master.build.queue.graph.GraphBuilder;
import com.zutubi.pulse.master.build.queue.graph.GraphFilters;
import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.scm.ScmFileResolver;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.tove.annotations.Permission;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.TreeNode;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    public static final String ACTION_MARK_CLEAN           = "clean";
    public static final String ACTION_REBUILD              = "rebuild";

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

    public List<String> getActions(ProjectConfiguration instance)
    {
        // Templates are considered initialised
        boolean initialised = true;
        List<String> result = new LinkedList<String>();
        if (instance.isConcrete())
        {
            result.add(ACTION_MARK_CLEAN);
            Project project = projectManager.getProject(instance.getProjectId(), true);
            if (project != null)
            {
                Project.State state = project.getState();
                if (state.acceptTrigger(false))
                {
                    result.add(ACTION_TRIGGER);
                    
                    if (hasDependencyOfBuildableStatus(instance))
                    {
                        result.add(ACTION_REBUILD);
                    }
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

    private boolean hasDependencyOfBuildableStatus(ProjectConfiguration projectConfig)
    {
        if (projectConfig.hasDependencies())
        {
            if (projectConfig.getOptions().getPrompt())
            {
                // The user can set a status for the build at the prompt.
                return true;
            }

            String ourStatus = projectConfig.getDependencies().getStatus();
            GraphBuilder builder = objectFactory.buildBean(GraphBuilder.class);
            GraphFilters filters = objectFactory.buildBean(GraphFilters.class);
            TreeNode<BuildGraphData> upstream = builder.buildUpstreamGraph(projectConfig,
                    filters.status(ourStatus),
                    filters.transitive(),
                    filters.duplicate());
            return upstream.getChildren().size() > 0;
        }

        return false;
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
            Project project = projectManager.getProject(projectConfiguration.getProjectId(), false);
            if (project != null)
            {
                ScmFileResolver resolver = new ScmFileResolver(project, Revision.HEAD, scmManager);
                result.setPulseFileString(projectConfiguration.getType().getPulseFile().getFileContent(resolver));
            }
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
