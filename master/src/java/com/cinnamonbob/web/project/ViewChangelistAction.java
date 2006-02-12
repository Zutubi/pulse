package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.model.persistence.ChangelistDao;
import com.cinnamonbob.model.persistence.BuildResultDao;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.web.ActionSupport;

/**
 */
public class ViewChangelistAction extends ActionSupport
{
    private long id;
    private long buildId;
    private Changelist changelist;
    private ChangelistDao changelistDao;
    private BuildResultDao buildResultDao;
    private BuildResult buildResult;

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
        buildResult = buildResultDao.findById(buildId);
        // TODO dodgy walking of tree: hibernate eager/lazy loading!
        changelist.getRevision();
        changelist.getChanges();
        return SUCCESS;
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }
}
