package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.model.persistence.ChangelistDao;
import com.cinnamonbob.web.ActionSupport;

/**
 */
public class ViewChangelistAction extends ActionSupport
{
    private long id;
    private Changelist changelist;
    private ChangelistDao changelistDao;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
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
        // TODO dodgy walking of tree: read up on hibernate eager/lazy loading!
        changelist.getRevision();
        changelist.getChanges();
        return SUCCESS;
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }
}
