package com.zutubi.pulse.core.model;

import com.google.common.base.Function;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.time.TimeStamps;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

/**
 * A persistent wrapper around a Changelist, which associates the changelist
 * details with a specific build.
 */
public class PersistentChangelist extends Entity
{
    private Revision revision;
    private long time;
    private String author;
    private String comment;
    /**
     * An on-demand calculated hash of the basic details of this list (all bar
     * the individual file changes).  Used for fast lookup of equivalent
     * changelists.
     */
    private String hash = null;
    private List<PersistentFileChange> changes;

    // We can possibly reference entities here but any change would need to be
    // wary of performance: we need fast lookups by both build and project.
    private long projectId;
    private long resultId;

    // For hibernate
    PersistentChangelist()
    {
    }

    public PersistentChangelist(Revision revision, long time, String author, String comment, Collection<PersistentFileChange> changes)
    {
        if (revision == null)
        {
            throw new NullPointerException("Revision may not be null");
        }

        this.revision = revision;
        this.time = time;
        this.author = author;
        this.comment = comment;
        this.changes = new LinkedList<PersistentFileChange>(changes);
    }

    public PersistentChangelist(Changelist data)
    {
        revision = data.getRevision();
        time = data.getTime();
        author = data.getAuthor();
        comment = data.getComment();
        changes = CollectionUtils.map(data.getChanges(), new Function<FileChange, PersistentFileChange>()
        {
            public PersistentFileChange apply(FileChange change)
            {
                return new PersistentFileChange(change);
            }
        });
    }

    public Date getDate()
    {
        return new Date(time);
    }

    public Revision getRevision()
    {
        return revision;
    }
    
    String getRevisionString()
    {
        return revision == null ? null : revision.getRevisionString();
    }

    void setRevisionString(String revisionString)
    {
        if (revisionString == null)
        {
            revision = null;
        }
        else
        {
            revision = new Revision(revisionString);
        }
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public String getPrettyTime()
    {
        return TimeStamps.getPrettyTime(time);
    }

    public String getPrettyDate(Locale locale)
    {
        return TimeStamps.getPrettyDate(time, locale);
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getHash()
    {
        if (hash == null)
        {
            String toHash = StringUtils.join("/", revision.getRevisionString(), Long.toString(time), safeString(author), safeString(comment));
            hash = DigestUtils.md5Hex(toHash);
        }
        return hash;
    }

    private String safeString(String s)
    {
        if (s == null)
        {
            s = "";
        }

        return s;
    }

    public void setHash(String hash)
    {
        this.hash = hash;
    }

    public List<PersistentFileChange> getChanges()
    {
        return changes;
    }

    public void setChanges(List<PersistentFileChange> changes)
    {
        this.changes = changes;
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

    /**
     * Compares changelists by equivalence.  The comparison uses all properties
     * of the change except for the file changes.  Thus if two changelists
     * occur at the same time, by the same author, with the same comment and
     * creating the same revision they are deemed equivalent.  This is
     * important for identifying the same changelist when found by different
     * {@link com.zutubi.pulse.core.scm.api.ScmClient}s.
     *
     * @param other changelist to compare to
     * @return true iff the other changelist is deemed to represent the same
     *         changelist as this one
     */
    public boolean isEquivalent(PersistentChangelist other)
    {
        if (this == other)
        {
            return true;
        }

        if (time != other.time)
        {
            return false;
        }
        if (author != null ? !author.equals(other.author) : other.author != null)
        {
            return false;
        }
        if (comment != null ? !comment.equals(other.comment) : other.comment != null)
        {
            return false;
        }
        if (revision != null ? !revision.equals(other.revision) : other.revision != null)
        {
            return false;
        }

        return true;
    }

    public String toString()
    {
        return "{ rev: " + ((revision != null) ? revision.toString() : "null") + ", changes: " + changes.toString() + " }";
    }
}
