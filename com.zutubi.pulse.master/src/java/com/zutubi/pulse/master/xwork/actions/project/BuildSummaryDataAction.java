package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.committransformers.LinkSubstitution;
import com.zutubi.pulse.master.committransformers.Substitution;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.security.AccessManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zutubi.pulse.master.committransformers.SubstitutionUtils.processSubstitution;

/**
 * Action to provide data for the build summary tab.
 */
public class BuildSummaryDataAction extends BuildStatusActionBase
{
    private BuildSummaryModel model;
    
    private SystemPaths systemPaths;

    public BuildSummaryModel getModel()
    {
        return model;
    }

    public String execute()
    {
        super.execute();

        final BuildResult buildResult = getRequiredBuildResult();
        Project project = buildResult.getProject();
        boolean canWrite = accessManager.hasPermission(AccessManager.ACTION_WRITE, project);
        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());

        model = new BuildSummaryModel(buildResult, urls);
        addComments(buildResult);
        addActions(buildResult, canWrite);
        addRelatedLinks(buildResult);
        addHooks(buildResult, canWrite);

        return SUCCESS;
    }

    private void addComments(BuildResult buildResult)
    {
        for (Comment comment: buildResult.getComments())
        {
            model.addComment(comment, accessManager.hasPermission(AccessManager.ACTION_DELETE, comment));
        }
    }

    private void addActions(BuildResult buildResult, boolean canWrite)
    {
        Project project = buildResult.getProject();
        Messages messages = Messages.getInstance(BuildResult.class);
        File contentRoot = systemPaths.getContentRoot();
        if (buildResult.completed())
        {
            if (canWrite)
            {
                if (buildResult.isPinned())
                {
                    model.addAction(ToveUtils.getActionLink(BuildResult.ACTION_UNPIN, messages, contentRoot));
                }
                else
                {
                    model.addAction(ToveUtils.getActionLink(BuildResult.ACTION_PIN, messages, contentRoot));
                    model.addAction(ToveUtils.getActionLink(AccessManager.ACTION_DELETE, messages, contentRoot));
                }
            }
        }
        else
        {
            if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, buildResult))
            {
                model.addAction(ToveUtils.getActionLink(BuildResult.ACTION_CANCEL, messages, contentRoot));
            }

            if (accessManager.hasPermission(AccessManager.ACTION_ADMINISTER, null))
            {
                model.addAction(ToveUtils.getActionLink(BuildResult.ACTION_KILL, messages, contentRoot));
            }
        }

        ProjectResponsibility projectResponsibility = project.getResponsibility();
        if (projectResponsibility == null && accessManager.hasPermission(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY, project))
        {
            model.addAction(ToveUtils.getActionLink(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY, messages, contentRoot));
        }

        if (projectResponsibility != null)
        {
            ProjectResponsibilityModel responsbilityModel = new ProjectResponsibilityModel(projectResponsibility.getMessage(getLoggedInUser()), projectResponsibility.getComment());
            model.setResponsibility(responsbilityModel);

            if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY, project))
            {
                model.addAction(ToveUtils.getActionLink(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY, messages, contentRoot));
                responsbilityModel.setCanClear(true);
            }
        }

        if (getLoggedInUser() != null)
        {
            model.addAction(ToveUtils.getActionLink(CommentContainer.ACTION_ADD_COMMENT, messages, contentRoot));
        }
    }

    private void addRelatedLinks(BuildResult buildResult)
    {
        List<PersistentChangelist> changelists = buildManager.getChangesForBuild(buildResult, true);

        List<LinkSubstitution> substitutions = gatherLinkSubstitutions(buildResult.getProject().getConfig());
        for (LinkSubstitution substitution: substitutions)
        {
            try
            {
                Pattern pattern = Pattern.compile(substitution.getExpression());
                for (PersistentChangelist changelist: changelists)
                {
                    Matcher matcher = pattern.matcher(changelist.getComment());
                    while (matcher.find())
                    {
                        model.addLink(new ActionLink(processSubstitution(substitution.getLinkUrl(), matcher), processSubstitution(substitution.getLinkText(), matcher), null));
                    }
                }
            }
            catch (Exception e)
            {
                // Soldier on.
            }
        }
    }

    private List<LinkSubstitution> gatherLinkSubstitutions(ProjectConfiguration projectConfig)
    {
        List<LinkSubstitution> result = new LinkedList<LinkSubstitution>();
        for (CommitMessageTransformerConfiguration transformerConfig: projectConfig.getCommitMessageTransformers().values())
        {
            List<Substitution> substitutions = transformerConfig.substitutions();
            for (Substitution substitution: substitutions)
            {
                if (substitution instanceof LinkSubstitution)
                {
                    LinkSubstitution linkSubstitution = (LinkSubstitution) substitution;
                    if( !result.contains(linkSubstitution))
                    {
                        result.add(linkSubstitution);
                    }
                }
            }
        }

        return result;
    }

    private void addHooks(BuildResult buildResult, boolean canWrite)
    {
        if (canWrite)
        {
            ProjectConfiguration projectConfig = getRequiredProject().getConfig();
            for (BuildHookConfiguration hookConfiguration: projectConfig.getBuildHooks().values())
            {
                if (hookConfiguration.canManuallyTriggerFor(buildResult))
                {
                    model.addHook(new ActionLink("triggerHook", hookConfiguration.getName(), null, Long.toString(hookConfiguration.getHandle())));
                }
            }
        }
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }
}
