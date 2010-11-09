package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.master.build.queue.BuildQueueSnapshot;
import com.zutubi.pulse.master.build.queue.BuildRequestRegistry;
import com.zutubi.pulse.master.build.queue.QueuedRequest;
import com.zutubi.pulse.master.build.queue.SchedulingController;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.BuildResultToNumberMapping;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectResponsibility;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.*;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.links.ConfigurationLinks;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.*;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An action that provides the JSON data for rendering a project home page.
 */
public class ProjectHomeDataAction extends ProjectActionBase
{
    private static final Messages PROJECT_I18N = Messages.getInstance(ProjectConfiguration.class);

    private static final String LINK_HOMEPAGE = "homepage";

    private ProjectHomeModel model;
    
    private ActionManager actionManager;
    private SystemPaths systemPaths;
    private SchedulingController schedulingController;
    private BuildRequestRegistry buildRequestRegistry;
    private ConfigurationManager configurationManager;

    public ProjectHomeModel getModel()
    {
        return model;
    }

    public String execute()
    {
        Project project = getRequiredProject();
        
        // Get queued builds first, so we can detect a race with a build moving
        // from queued to active.
        BuildQueueSnapshot snapshot = schedulingController.getSnapshot();
        List<QueuedRequest> queued = snapshot.getQueuedRequestsByOwner(project);
        List<BuildResult> inProgress = buildManager.queryBuilds(project, ResultState.getIncompleteStates(), -1, -1, -1, -1, true, false);
        List<BuildResult> completed = buildManager.getLatestCompletedBuildResults(project, 11);
        BuildResult latest = completed.isEmpty() ? null : completed.remove(0);
        
        // Race detection: anything in our queue snapshot that corresponds to a
        // build number (in progress or even completed) should be filtered out,
        // that request must have been activate between snapshotting and
        // querying the builds.
        final Set<Long> activatedBuilds = new HashSet<Long>();
        CollectionUtils.map(inProgress, new BuildResultToNumberMapping(), activatedBuilds);
        CollectionUtils.map(completed, new BuildResultToNumberMapping(), activatedBuilds);
        queued = CollectionUtils.filter(queued, new Predicate<QueuedRequest>()
        {
            public boolean satisfied(QueuedRequest queuedRequest)
            {
                return !activatedBuilds.contains(buildRequestRegistry.getBuildNumber(queuedRequest.getRequest().getId()));
            }
        });

        ProjectConfiguration projectConfig = project.getConfig();
        BuildResultToModelMapping buildMapping = new BuildResultToModelMapping(projectConfig.getChangeViewer());

        model = new ProjectHomeModel(createStatusModel());
        addActivity(queued, inProgress, buildMapping);
        addLatest(latest, buildMapping);
        addRecent(completed, buildMapping);
        addChanges();
        addLinks();
        addActions();

        return SUCCESS;
    }

    private ProjectHomeModel.StatusModel createStatusModel()
    {
        Project project = getProject();
        
        ProjectHomeModel.StateModel state = new ProjectHomeModel.StateModel(
                EnumUtils.toPrettyString(project.getState()),
                project.isTransitionValid(Project.Transition.PAUSE),
                project.isTransitionValid(Project.Transition.RESUME)
        );
        
        ProjectHomeModel.StatisticsModel statistics = new ProjectHomeModel.StatisticsModel(
                buildManager.getBuildCount(project, ResultState.getCompletedStates()),
                buildManager.getBuildCount(project, new ResultState[]{ResultState.SUCCESS}),
                buildManager.getBuildCount(project, new ResultState[]{ResultState.FAILURE})
        );
        
        return new ProjectHomeModel.StatusModel(project.getName(), EnumUtils.toPrettyString(ProjectHealth.getHealth(buildManager, project)), state, statistics);
    }

    private void addActivity(List<QueuedRequest> queued, List<BuildResult> inProgress, BuildResultToModelMapping buildMapping)
    {
        ProjectConfiguration projectConfig = getProject().getConfig();
        CollectionUtils.map(queued, new QueuedToBuildModelMapping(projectConfig.getName(), projectConfig.getChangeViewer()), model.getActivity());
        CollectionUtils.map(inProgress, buildMapping, model.getActivity());
    }

    private void addLatest(BuildResult latest, BuildResultToModelMapping buildMapping)
    {
        if (latest != null)
        {
            model.setLatest(buildMapping.map(latest));
        }
    }

    private void addRecent(List<BuildResult> completed, BuildResultToModelMapping buildMapping)
    {
        CollectionUtils.map(completed, buildMapping, model.getRecent());
    }

    private void addLinks()
    {
        Project project = getProject();
        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        if (accessManager.hasPermission(AccessManager.ACTION_WRITE, project.getConfig()))
        {
            model.addLink(new ActionLink(urls.adminProject(project), PROJECT_I18N.format(AccessManager.ACTION_WRITE + ConfigurationLinks.KEY_SUFFIX_LABEL), AccessManager.ACTION_WRITE));
        }

        String url = project.getConfig().getUrl();
        if (StringUtils.stringSet(url))
        {
            model.setUrl(url);
            model.addLink(new ActionLink(url, PROJECT_I18N.format(LINK_HOMEPAGE + ConfigurationLinks.KEY_SUFFIX_LABEL), LINK_HOMEPAGE));
        }
    }

    private void addActions()
    {
        File contentRoot = systemPaths.getContentRoot();
        List<String> availableActions = actionManager.getActions(getProject().getConfig(), false, true);
        for (String candidateAction: asList(AccessManager.ACTION_WRITE, ACTION_MARK_CLEAN, ACTION_TRIGGER, ACTION_REBUILD))
        {
            if (availableActions.contains(candidateAction))
            {
                model.addAction(ToveUtils.getActionLink(candidateAction, PROJECT_I18N, contentRoot));
            }
        }

        addResponsibilityActions(PROJECT_I18N, contentRoot);
        addViewWorkingCopyAction(PROJECT_I18N, contentRoot);
    }

    private void addResponsibilityActions(Messages messages, File contentRoot)
    {
        Project project = getProject();
        ProjectResponsibility projectResponsibility = project.getResponsibility();
        if (projectResponsibility == null && accessManager.hasPermission(ACTION_TAKE_RESPONSIBILITY, project))
        {
            model.addAction(ToveUtils.getActionLink(ACTION_TAKE_RESPONSIBILITY, messages, contentRoot));
        }

        if (projectResponsibility != null)
        {
            String responsibleOwner = projectResponsibility.getMessage(getLoggedInUser());
            String responsibleComment = projectResponsibility.getComment();

            ProjectResponsibilityModel responsibilityModel = new ProjectResponsibilityModel(responsibleOwner, responsibleComment);
            model.setResponsibility(responsibilityModel);
            if (accessManager.hasPermission(ACTION_CLEAR_RESPONSIBILITY, project))
            {
                responsibilityModel.setCanClear(true);
                model.addAction(ToveUtils.getActionLink(ACTION_CLEAR_RESPONSIBILITY, messages, contentRoot));
            }
        }
    }

    private void addViewWorkingCopyAction(Messages messages, File contentRoot)
    {
        ProjectConfiguration config = getProject().getConfig();
        if (config.getScm().getCheckoutScheme() == CheckoutScheme.INCREMENTAL_UPDATE)
        {
            if (accessManager.hasPermission(ACTION_VIEW_SOURCE, getProject()))
            {
                model.addAction(ToveUtils.getActionLink(ACTION_VIEW_SOURCE, messages, contentRoot));
            }
        }
    }

    private void addChanges()
    {
        List<PersistentChangelist> latestChanges = buildManager.getLatestChangesForProject(getProject(), 10);
        CollectionUtils.map(latestChanges, new Mapping<PersistentChangelist, ProjectHomeModel.ChangelistModel>()
        {
            public ProjectHomeModel.ChangelistModel map(PersistentChangelist persistentChangelist)
            {
                return new ProjectHomeModel.ChangelistModel(persistentChangelist, getProject().getConfig());
            }
        }, model.getChanges());
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    private static class QueuedToBuildModelMapping implements Mapping<QueuedRequest, BuildModel>
    {
        private String projectName;
        private ChangeViewerConfiguration changeViewerConfig;

        private QueuedToBuildModelMapping(String projectName, ChangeViewerConfiguration changeViewerConfig)
        {
            this.projectName = projectName;
            this.changeViewerConfig = changeViewerConfig;
        }

        public BuildModel map(QueuedRequest queuedRequest)
        {
            BuildRequestEvent requestEvent = queuedRequest.getRequest();
            Revision revision = requestEvent.getRevision().getRevision();
            RevisionModel revisionModel;
            if (revision == null)
            {
                revisionModel = new RevisionModel("[floating]");
            }
            else
            {
                revisionModel = new RevisionModel(revision, changeViewerConfig);
            }
            
            return new BuildModel(requestEvent.getId(), -1, false, projectName, projectName, "queued", requestEvent.getPrettyQueueTime(), requestEvent.getReason().getSummary(), revisionModel);
        }
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setSchedulingController(SchedulingController schedulingController)
    {
        this.schedulingController = schedulingController;
    }

    public void setBuildRequestRegistry(BuildRequestRegistry buildRequestRegistry)
    {
        this.buildRequestRegistry = buildRequestRegistry;
    }
}
