package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.web.ActionSupport;
import org.acegisecurity.AccessDeniedException;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class ProjectActionSupport extends ActionSupport
{
    protected ProjectManager projectManager;
    protected BuildManager buildManager;
    protected ScmManager scmManager;
    protected UserManager userManager;
    protected Scheduler scheduler;

    private static final long NONE_SPECIFIED = -1;

    protected long projectId = NONE_SPECIFIED;

    protected String projectName = null;

    private CommitMessageHelper commitMessageHelper;

    public void setProjectManager(ProjectManager manager)
    {
        projectManager = manager;
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public BuildManager getBuildManager()
    {
        return buildManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public ScmManager getScmManager()
    {
        return scmManager;
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public Feature.Level getErrorLevel()
    {
        return Feature.Level.ERROR;
    }

    public Feature.Level getWarningLevel()
    {
        return Feature.Level.WARNING;
    }

    public List<Feature.Level> getFeatureLevels()
    {
        List<Feature.Level> levels = new LinkedList<Feature.Level>();
        levels.add(Feature.Level.ERROR);
        levels.add(Feature.Level.WARNING);
        levels.add(Feature.Level.INFO);
        return levels;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public Project getProject()
    {
        if (projectId != NONE_SPECIFIED)
        {
            return getProject(projectId);
        }
        else if (TextUtils.stringSet(projectName))
        {
            return getProject(projectName);
        }
        return null;
    }

    protected Project getProject(long id)
    {
        return projectManager.getProject(id);
    }

    protected Project getProject(String projectName)
    {
        return getProjectManager().getProject(projectName);
    }

    public void addUnknownProjectActionError()
    {
        if (projectId != NONE_SPECIFIED)
        {
            addActionError("Unknown project [" + projectId + "]");
        }
        else if (TextUtils.stringSet(projectName))
        {
            addActionError("Unknown project [" + projectName + "]");
        }
        else
        {
            addActionError("Require either a project name or id.");
        }
    }

    public void addUnknownProjectFieldError()
    {
        if (projectId != NONE_SPECIFIED)
        {
            addFieldError("projectId", "Unknown project [" + projectId + "]");
        }
        else if (TextUtils.stringSet(projectName))
        {
            addFieldError("projectName", "Unknown project [" + projectName + "]");
        }
        else
        {
            addActionError("Require either a project name or id.");
        }
    }

    public Project lookupProject(long id)
    {
        Project p = projectManager.getProject(id);
        if(p == null)
        {
            addActionError("Unknown project [" + id + "]");
        }

        return p;
    }

    public boolean canWrite(Project project)
    {
        try
        {
            getProjectManager().checkWrite(project);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public User getLoggedInUser()
    {
        Object principle = getPrinciple();
        if(principle != null && principle instanceof String)
        {
            return userManager.getUser((String)principle);
        }

        return null;
    }

    public void checkPermissions(BuildResult result)
    {
        if(result.isPersonal())
        {
            User user = getLoggedInUser();
            if(!result.getUser().equals(user))
            {
                throw new AccessDeniedException("Only the owner can view a personal build");
            }
        }
    }

    public String getChangeUrl(Project project, Revision revision)
    {
        if(project != null)
        {
            return project.getScm().getChangeUrl(revision);
        }

        return null;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public String transformComment(Changelist changelist)
    {
        if(commitMessageHelper == null)
        {
            commitMessageHelper = new CommitMessageHelper(getProjectManager().getCommitMessageTransformers());
        }
        return commitMessageHelper.applyTransforms(changelist);
    }

    public String transformComment(Changelist changelist, int maxChars)
    {
        if(commitMessageHelper == null)
        {
            commitMessageHelper = new CommitMessageHelper(getProjectManager().getCommitMessageTransformers());
        }
        return commitMessageHelper.applyTransforms(changelist, maxChars);
    }
}
