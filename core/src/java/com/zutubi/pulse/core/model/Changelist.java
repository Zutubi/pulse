/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core.model;

import com.zutubi.pulse.util.TimeStamps;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A trivial implementation of the Changelist interface.
 *
 * @author jsankey
 */
public class Changelist extends Entity
{
    private Revision revision;
    private List<Change> changes;
    // Used to facilitate recent changes for project lookup
    private List<Long> projectIds = new LinkedList<Long>();
    /**
     * Id of the build results affected by this change: the BuildResult class
     * is up in master so we can't map them directly.  In any case, this
     * forces lazy loading of the results which is a Good Thing.
     */
    private List<Long> resultIds = new LinkedList<Long>();

    protected Changelist()
    {

    }

    public Changelist(Revision revision)
    {
        this.revision = revision;
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
        return getRevision().getDate();
    }

    public String getPrettyDate()
    {
        return TimeStamps.getPrettyTime(getRevision().getDate().getTime());
    }

    public String getUser()
    {
        return getRevision().getAuthor();
    }

    public String getComment()
    {
        return getRevision().getComment();
    }

    public List<Change> getChanges()
    {
        return changes;
    }

    private void setChanges(List<Change> changes)
    {
        this.changes = changes;
    }

    private void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    public List<Long> getProjectIds()
    {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds)
    {
        this.projectIds = projectIds;
    }

    public List<Long> getResultIds()
    {
        return resultIds;
    }

    public void setResultIds(List<Long> resultIds)
    {
        this.resultIds = resultIds;
    }

    public void addProjectId(long id)
    {
        if(!projectIds.contains(id))
        {
            projectIds.add(id);
        }
    }

    public void addResultId(long id)
    {
        resultIds.add(id);
    }
}
