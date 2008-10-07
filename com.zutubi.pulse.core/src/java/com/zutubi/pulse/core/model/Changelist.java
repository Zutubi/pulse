package com.zutubi.pulse.core.model;

import com.zutubi.util.TimeStamps;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * A trivial implementation of the Changelist interface.
 *
 * @author jsankey
 */
public class Changelist extends Entity
{
    private Revision revision;
    /**
     * A collection of details from the revision that can be used to identify
     * duplicate changelists hashed using MD5.
     */
    private String hash;

    private List<PersistentFileChange> changes;

    private long projectId;
    private long resultId;

    protected Changelist()
    {

    }

    public Changelist(Revision revision)
    {
        this.revision = revision;
        this.changes = new LinkedList<PersistentFileChange>();
    }

    public boolean isEquivalent(Changelist other)
    {
        return other != null &&
                safeTime(getDate()) == safeTime(other.getDate()) &&
                safeString(revision.getAuthor()).equals(safeString(other.revision.getAuthor())) &&
                safeString(revision.getBranch()).equals(safeString(other.revision.getBranch())) &&
                safeString(revision.getComment()).equals(safeString(other.revision.getComment())) &&
                safeString(revision.getRevisionString()).equals(safeString(other.revision.getRevisionString()));

    }

    public String getHash()
    {
        if(hash == null)
        {
            // Calculate on demand.
            String input;
            if(revision == null)
            {
                input = "////";
            }
            else
            {
                input = safeString(Long.toString(safeTime(revision.getDate())) + "/" + revision.getAuthor() + "/" + safeString(revision.getBranch()) + "/" + safeString(revision.getComment()) + "/" + safeString(revision.getRevisionString()));
            }

            hash = DigestUtils.md5Hex(input);
        }
        return hash;
    }

    private long safeTime(Date date)
    {
        return date == null ? 0 : date.getTime();
    }

    private String safeString(String in)
    {
        if(in == null)
        {
            return "";
        }
        else
        {
            return in;
        }
    }

    public void setHash(String hash)
    {
        this.hash = hash;
    }

    public void addChange(PersistentFileChange change)
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

    public String getPrettyTime()
    {
        return TimeStamps.getPrettyTime(getRevision().getDate().getTime());
    }

    public String getPrettyDate(Locale locale)
    {
        return TimeStamps.getPrettyDate(getRevision().getDate().getTime(), locale);
    }

    public String getUser()
    {
        return getRevision().getAuthor();
    }

    public String getComment()
    {
        return getRevision().getComment();
    }

    public List<PersistentFileChange> getChanges()
    {
        return changes;
    }

    private void setChanges(List<PersistentFileChange> changes)
    {
        this.changes = changes;
    }

    private void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getResultId()
    {
        return resultId;
    }

    public void setResultId(long resultId)
    {
        this.resultId = resultId;
    }

    public String toString()
    {
        return "{ rev: " + ((revision != null) ? revision.toString() : "null") + ", changes: " + changes.toString() + " }";
    }
}
