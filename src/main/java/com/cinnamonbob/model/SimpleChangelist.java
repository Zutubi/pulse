package com.cinnamonbob.model;

import com.cinnamonbob.scm.Changelist;
import com.cinnamonbob.scm.Change;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A trivial implementation of the Changelist interface.
 * 
 * @author jsankey
 */
public class SimpleChangelist extends Entity implements Changelist
{
    private Revision revision;
    private Date date;
    private String user;
    private String comment;
    private List<Change> changes;
    
    protected SimpleChangelist()
    {

    }

    public SimpleChangelist(Revision revision, Date date, String user, String comment)
    {
        this.revision = revision;
        this.date = date;
        this.user = user;
        this.comment = comment;
        this.changes = new LinkedList<Change>();
    }
    
    public void addChange(Change change)
    {
        changes.add(change);
    }
    
    public Revision getRevision()
    {
        return revision;
    }
    
    public Date getDate()
    {
        return date;
    }

    public String getUser()
    {
        return user;
    }

    public String getComment()
    {
        return comment;
    }

    public List<Change> getChanges()
    {
        return changes;
    }

    private void setChanges(List<Change> changes)
    {
        this.changes = changes;
    }

    private void setComment(String comment)
    {
        this.comment = comment;
    }

    private void setDate(Date date)
    {
        this.date = date;
    }

    private void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    private void setUser(String user)
    {
        this.user = user;
    }
}
