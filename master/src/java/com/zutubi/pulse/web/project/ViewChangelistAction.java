package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
public class ViewChangelistAction extends ActionSupport
{
    private long id;
    private Changelist changelist;
    private ChangelistDao changelistDao;
    private BuildManager buildManager;
    private CommitMessageHelper commitMessageHelper;
    private long buildId;
    /** This is the build result we have drilled down from, if any. */
    private BuildResult buildResult;
    /** All builds affected by this change. */
    private List<BuildResult> buildResults;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public Changelist getChangelist()
    {
        return changelist;
    }

    public String getChangeUrl()
    {
        if(buildResult != null)
        {
            return buildResult.getProject().getScm().getChangeUrl(changelist.getRevision());
        }

        return null;
    }

    public void setChangelist(Changelist changelist)
    {
        this.changelist = changelist;
    }

    public void validate()
    {
        changelist = changelistDao.findById(id);
        if (changelist == null)
        {
            addActionError("Unknown changelist '" + id + "'");
        }
    }

    public String execute()
    {
        if(buildId != 0)
        {
            // It is valid to have no build ID set: we may not be viewing
            // the change as part of a build.
            buildResult = buildManager.getBuildResult(buildId);
        }

        buildResults = ChangelistUtils.getBuilds(buildManager, changelist);
        Collections.sort(buildResults, new Comparator<BuildResult>()
        {
            public int compare(BuildResult b1, BuildResult b2)
            {
                NamedEntityComparator comparator = new NamedEntityComparator();
                int result = comparator.compare(b1.getProject(), b2.getProject());
                if(result == 0)
                {
                    result = (int)(b1.getNumber() - b2.getNumber());
                }

                return result;
            }
        });

        return SUCCESS;
    }

    public String transformComment(Changelist changelist)
    {
        return commitMessageHelper.applyTransforms(changelist);
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public List<BuildResult> getBuildResults()
    {
        return buildResults;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        commitMessageHelper = new CommitMessageHelper(projectManager.getCommitMessageTransformers());
    }
}
