/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.build.queue.BuildQueueSnapshot;
import com.zutubi.pulse.master.build.queue.BuildRequestRegistry;
import com.zutubi.pulse.master.build.queue.QueuedRequest;
import com.zutubi.pulse.master.build.queue.SchedulingController;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.project.BootstrapConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildType;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.ui.actions.ActionManager;
import com.zutubi.tove.ui.links.ConfigurationLinks;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.*;
import static java.util.Arrays.asList;

/**
 * An action that provides the JSON data for rendering a project home page.
 */
public class ProjectHomeDataAction extends ProjectActionBase
{
    private static final Messages PROJECT_I18N = Messages.getInstance(ProjectConfiguration.class);

    private static final String LINK_HOMEPAGE = "homepage";
    private static final String LINK_RSS = "rss";

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
        List<BuildResult> latestCompleted = buildManager.getLatestCompletedBuildResults(project, 10);
        
        // Race detection: anything in our queue snapshot that corresponds to a
        // build number (in progress or even completed) should be filtered out,
        // that request must have been activate between snapshotting and
        // querying the builds.
        final Set<Long> activatedBuilds = new HashSet<Long>();
        activatedBuilds.addAll(transform(inProgress, BuildResults.toNumber()));
        activatedBuilds.addAll(transform(latestCompleted, BuildResults.toNumber()));
        Iterables.removeIf(queued, new Predicate<QueuedRequest>()
        {
            public boolean apply(QueuedRequest queuedRequest)
            {
                return activatedBuilds.contains(buildRequestRegistry.getBuildNumber(queuedRequest.getRequest().getId()));
            }
        });

        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        ProjectConfiguration projectConfig = project.getConfig();
        BuildResult latestCompletedResult = latestCompleted.isEmpty() ? null : latestCompleted.get(0);
        BuildResultToModelFunction buildMapping = new BuildResultToModelFunction(urls, projectConfig);
        if (latestCompletedResult != null)
        {
            buildMapping.collectArtifactsForBuildId(latestCompletedResult.getId());
        }

        model = new ProjectHomeModel(createStatusModel(latestCompletedResult, urls));
        addActivity(queued, inProgress, buildMapping);
        addRecent(latestCompleted, buildMapping);
        addChanges();
        addDescription(projectConfig);
        addLinks(urls);
        addActions();

        return SUCCESS;
    }

    private ProjectHomeModel.StatusModel createStatusModel(final BuildResult latestCompleted, final Urls urls)
    {
        Project project = getProject();

        Project.State projectState = project.getState();
        Project.Transition keyTransition = getKeyTransition(project, projectState);
        
        ProjectHomeModel.StateModel state = new ProjectHomeModel.StateModel(
                EnumUtils.toPrettyString(projectState),
                keyTransition == null ? null : EnumUtils.toPrettyString(keyTransition)
        );
        
        ProjectHomeModel.StatisticsModel statistics = new ProjectHomeModel.StatisticsModel(
                buildManager.getBuildCount(project, ResultState.getCompletedStates()),
                buildManager.getBuildCount(project, ResultState.getHealthyStates()),
                buildManager.getBuildCount(project, new ResultState[]{ResultState.FAILURE})
        );
        
        List<BuildStageModel> brokenStages = null;
        if (latestCompleted != null)
        {
            Collection<RecipeResultNode> brokenNodes = Collections2.filter(latestCompleted.getStages(), new Predicate<RecipeResultNode>()
            {
                public boolean apply(RecipeResultNode recipeResultNode)
                {
                    return !recipeResultNode.getResult().healthy();
                }
            });

            if (!brokenNodes.isEmpty())
            {
                brokenStages = newArrayList(Iterables.transform(brokenNodes, new Function<RecipeResultNode, BuildStageModel>()
                {
                    public BuildStageModel apply(RecipeResultNode recipeResultNode)
                    {
                        return new BuildStageModel(latestCompleted, recipeResultNode, urls, false);
                    }
                }));
            }
        }
        
        return new ProjectHomeModel.StatusModel(project.getName(), EnumUtils.toPrettyString(ProjectHealth.getHealth(latestCompleted)), state, statistics, brokenStages);
    }

    private Project.Transition getKeyTransition(Project project, Project.State projectState)
    {
        Project.Transition transition = null;
        if (project.isTransitionValid(Project.Transition.PAUSE))
        {
            transition = Project.Transition.PAUSE;
        }
        else if (project.isTransitionValid(Project.Transition.RESUME))
        {
            transition = Project.Transition.RESUME;
        }
        else if (projectState == Project.State.INITIALISATION_FAILED)
        {
            transition = Project.Transition.INITIALISE;
        }

        if (transition != null && !projectManager.hasStateTransitionPermission(project, transition))
        {
            transition = null;
        }
        return transition;
    }

    private void addActivity(List<QueuedRequest> queued, List<BuildResult> inProgress, BuildResultToModelFunction buildMapping)
    {
        ProjectConfiguration projectConfig = getProject().getConfig();
        final List<BuildModel> activity = model.getActivity();
        activity.addAll(transform(queued, new QueuedToBuildModelFunction(projectConfig.getName(), projectConfig)));
        activity.addAll(transform(inProgress, buildMapping));

        if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, projectConfig))
        {
            for (BuildModel buildModel : model.getActivity())
            {
                buildModel.setCancelPermitted(true);
            }
        }
    }

    private void addRecent(List<BuildResult> completed, BuildResultToModelFunction buildMapping)
    {
        model.getRecent().addAll(transform(completed, buildMapping));
    }

    private void addDescription(ProjectConfiguration projectConfig)
    {
        String description = projectConfig.getDescription();
        if (StringUtils.stringSet(description))
        {
            model.setDescription(description);
        }
    }

    private void addLinks(Urls urls)
    {
        Project project = getProject();
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

        GlobalConfiguration globalConfig = configurationTemplateManager.getInstance(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
        if (globalConfig.isRssEnabled())
        {
            model.addLink(new ActionLink(urls.base() + "rss.action?projectId=" + project.getId(), PROJECT_I18N.format(LINK_RSS + ConfigurationLinks.KEY_SUFFIX_LABEL),  LINK_RSS));
        }
    }

    private void addActions()
    {
        File contentRoot = systemPaths.getContentRoot();
        List<String> availableActions = actionManager.getActions(getProject().getConfig(), false, true);
        for (String candidateAction: asList(AccessManager.ACTION_WRITE, ACTION_MARK_CLEAN))
        {
            if (availableActions.contains(candidateAction))
            {
                model.addAction(ToveUtils.getActionLink(candidateAction, PROJECT_I18N, contentRoot));
            }
        }

        if (availableActions.contains(ACTION_TRIGGER))
        {
            addTriggerActions(contentRoot);
        }

        addResponsibilityActions(PROJECT_I18N, contentRoot);
        addViewWorkingCopyAction(PROJECT_I18N, contentRoot);
    }

    private void addTriggerActions(File contentRoot)
    {
        List<ManualTriggerConfiguration> triggers = TriggerUtils.getTriggers(getProject().getConfig(), ManualTriggerConfiguration.class);
        for (ManualTriggerConfiguration trigger: triggers)
        {
            String action = trigger.isPrompt() ? "triggerWithPrompt" : "trigger";
            ActionLink actionLink = new ActionLink(action, trigger.getName(), ToveUtils.getActionIconName(ACTION_TRIGGER, contentRoot), trigger.getName());
            model.addAction(actionLink);
        }
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
        BootstrapConfiguration config = getProject().getConfig().getBootstrap();
        if (config != null && config.getBuildType() == BuildType.INCREMENTAL_BUILD)
        {
            if (accessManager.hasPermission(ACTION_VIEW_SOURCE, getProject()))
            {
                model.addAction(ToveUtils.getActionLink(ACTION_VIEW_SOURCE, messages, contentRoot));
            }
        }
    }

    private void addChanges()
    {
        List<PersistentChangelist> latestChanges = changelistManager.getLatestChangesForProject(getProject(), 10);
        final ProjectConfiguration projectConfiguration = getProject().getConfig();
        final Collection<CommitMessageTransformerConfiguration> transformers = projectConfiguration.getCommitMessageTransformers().values();
        model.getChanges().addAll(transform(latestChanges, new Function<PersistentChangelist, ChangelistModel>()
        {
            public ChangelistModel apply(PersistentChangelist persistentChangelist)
            {
                return new ChangelistModel(persistentChangelist, projectConfiguration, transformers);
            }
        }));
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    private static class QueuedToBuildModelFunction implements Function<QueuedRequest, BuildModel>
    {
        private String projectName;
        private ProjectConfiguration projectConfiguration;

        private QueuedToBuildModelFunction(String projectName, ProjectConfiguration projectConfig)
        {
            this.projectName = projectName;
            this.projectConfiguration = projectConfig;
        }

        public BuildModel apply(QueuedRequest queuedRequest)
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
                revisionModel = new RevisionModel(revision, projectConfiguration);
            }
            
            return new BuildModel(requestEvent.getId(), -1, false, projectName, projectName, "queued", requestEvent.getPrettyQueueTime(), requestEvent.getReason().getSummary(), revisionModel, requestEvent.getStatus(), requestEvent.getVersion());
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
