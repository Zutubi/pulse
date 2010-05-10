package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.model.EntityWithIdPredicate;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Comment;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectResponsibility;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.config.NamedConfigurationComparator;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;
import org.apache.velocity.context.Context;

import java.io.File;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Action to provide data for the build summary tab.
 */
public class BuildSummaryDataAction extends BuildStatusActionBase
{
    private static final Logger LOG = Logger.getLogger(BuildSummaryDataAction.class);
    
    private List<PersistentChangelist> changelists;
    private String responsibleOwner;
    private String responsibleComment;
    private boolean canClearResponsible = false;
    private List<ActionLink> actions = new LinkedList<ActionLink>();
    private List<BuildHookConfiguration> hooks;

    private SummaryData data;
    
    private SystemPaths systemPaths;

    public SummaryData getData()
    {
        return data;
    }

    public String getResponsibleOwner()
    {
        return responsibleOwner;
    }

    public String getResponsibleComment()
    {
        return responsibleComment;
    }

    public boolean isCanClearResponsible()
    {
        return canClearResponsible;
    }

    public List<PersistentChangelist> getChangelists()
    {
        if (changelists == null)
        {
            changelists = buildManager.getChangesForBuild(getResult(), true);
        }
        return changelists;
    }

    public boolean canDeleteComment(final long id)
    {
        Comment comment = CollectionUtils.find(getBuildResult().getComments(), new EntityWithIdPredicate<Comment>(id));
        if (comment == null)
        {
            return false;
        }

        return accessManager.hasPermission(AccessManager.ACTION_DELETE, comment);
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public List<BuildHookConfiguration> getHooks()
    {
        return hooks;
    }

    public String execute()
    {
        super.execute();

        final BuildResult result = getRequiredBuildResult();
        Project project = result.getProject();
        boolean canWrite = accessManager.hasPermission(AccessManager.ACTION_WRITE, project);
        if (canWrite)
        {
            ProjectConfiguration projectConfig = getRequiredProject().getConfig();
            hooks = CollectionUtils.filter(projectConfig.getBuildHooks().values(), new Predicate<BuildHookConfiguration>()
            {
                public boolean satisfied(BuildHookConfiguration hookConfiguration)
                {
                    return hookConfiguration.canManuallyTriggerFor(result);
                }
            });
            Collections.sort(hooks, new NamedConfigurationComparator());
        }
        else
        {
            hooks = Collections.emptyList();
        }

        Messages messages = Messages.getInstance(BuildResult.class);
        File contentRoot = systemPaths.getContentRoot();
        if (result.completed())
        {
            if (canWrite)
            {
                actions.add(ToveUtils.getActionLink(AccessManager.ACTION_DELETE, messages, contentRoot));
            }
        }
        else
        {
            if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, result))
            {
                actions.add(ToveUtils.getActionLink(BuildResult.ACTION_CANCEL, messages, contentRoot));
            }
        }

        ProjectResponsibility projectResponsibility = project.getResponsibility();
        if (projectResponsibility == null && accessManager.hasPermission(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY, project))
        {
            actions.add(ToveUtils.getActionLink(ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY, messages, contentRoot));
        }

        if (projectResponsibility != null)
        {
            responsibleOwner = projectResponsibility.getMessage(getLoggedInUser());
            responsibleComment = projectResponsibility.getComment();

            if (accessManager.hasPermission(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY, project))
            {
                canClearResponsible = true;
                actions.add(ToveUtils.getActionLink(ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY, messages, contentRoot));
            }
        }

        if (getLoggedInUser() != null)
        {
            actions.add(ToveUtils.getActionLink(BuildResult.ACTION_ADD_COMMENT, messages, contentRoot));
        }

        try
        {
            ActionContext actionContext = ActionContext.getContext();
            VelocityManager velocityManager = VelocityManager.getInstance();
            Context context = velocityManager.createContext(actionContext.getValueStack(), ServletActionContext.getRequest(), ServletActionContext.getResponse());
            String mainPanel = renderTemplate("ajax/build-summary-main.vm", context, velocityManager);
            String rightPanel = renderTemplate("ajax/build-summary-right.vm", context, velocityManager);
            data = new SummaryData(mainPanel, rightPanel);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return ERROR;
        }

        return SUCCESS;
    }

    private String renderTemplate(String template, Context context, VelocityManager velocityManager) throws Exception
    {
        StringWriter writer = new StringWriter();
        velocityManager.getVelocityEngine().mergeTemplate(template, context, writer);
        return writer.toString();
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }
    
    public static class SummaryData
    {
        private String mainPanel;
        private String rightPanel;

        public SummaryData(String mainPanel, String rightPanel)
        {
            this.mainPanel = mainPanel;
            this.rightPanel = rightPanel;
        }

        public String getMainPanel()
        {
            return mainPanel;
        }

        public String getRightPanel()
        {
            return rightPanel;
        }
    }
}
