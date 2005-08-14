package com.cinnamonbob.model;

import com.cinnamonbob.scm.Changelist;
import com.cinnamonbob.scm.Revision;
import com.cinnamonbob.scm.Change;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A trivial implementation of the Changelist interface.
 * 
 * @author jsankey
 */
public class SimpleChangelist implements Changelist
{
    private Revision revision;
    private Date date;
    private String user;
    private String comment;
    private List<Change> changes;
    
    
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

}
