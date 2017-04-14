/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;


/**
 * Represents an atomic change committed to an SCM server.  For SCMs which
 * support atomic changelists (or "changesets"), they are mapped directly to
 * these changelists.  Otherwise, changelists may be emulated by guessing from
 * the available data.
 * <p/>
 * Note that changelists have been designed to be immutable.
 */
public class Changelist implements Comparable<Changelist>
{
    private static final int MAX_COMMENT_LENGTH = 4095;
    private static final String COMMENT_TRIM_MESSAGE = "... [trimmed]";

    private Revision revision;
    private long time;
    private String author;
    private String comment;

    private List<FileChange> changes;

    /**
     * Creates a new changelist with the given details.
     *
     * @param revision the new revision created by the commit of this change
     * @param time     time of the commit, in milliseconds since January 1,
     *                 1970, 00:00:00 GMT.
     * @param author   SCM account name of the user that created the change, or
     *                 null if no specific user may be identified (e.g.
     *                 anonymous commit)
     * @param comment  change comment, also known as the commit message, or
     *                 null if no comment was given
     * @param changes  a list of file changes that make up this changelist
     *
     * @throws NullPointerException if revision is null
     */
    public Changelist(Revision revision, long time, String author, String comment, Collection<FileChange> changes)
    {
        if (revision == null)
        {
            throw new NullPointerException("Revision may not be null");
        }

        this.revision = revision;
        this.time = time;
        this.author = author;

        if (comment != null)
        {
            this.comment = StringUtils.trimmedString(comment, MAX_COMMENT_LENGTH, COMMENT_TRIM_MESSAGE);
        }

        this.changes = new LinkedList<FileChange>(changes);
    }

    /**
     * @return the new revision created by the commit of this change, e.g. the
     *         changelist number for Perforce
     */
    public Revision getRevision()
    {
        return revision;
    }

   /**
    * @return the time the change was committed, in milliseconds since January
    *         1, 1970, 00:00:00 GMT.
    */
    public long getTime()
    {
        return time;
    }

    /**
     * @return the SCM account name of the user that created the change, or
     *         null if no specific author was identified
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * @return change comment or message, may be null if no comment was given
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * @return the list of file changes that make up this changelist (immutable).
     */
    public List<FileChange> getChanges()
    {
        return Collections.unmodifiableList(changes);
    }

    /**
     * Compares changelists by the time that they occurred, those occuring
     * earlier are deemed to have "less" magnitude.
     *
     * @param o changelist to compare to
     * @return {@inheritDoc}
     */
    public int compareTo(Changelist o)
    {
        if (time > o.time)
        {
            return 1;
        }
        else if (time < o.time)
        {
            return -1;
        }

        return 0;
    }

    /**
     * Compares changelists by equality.  The comparison uses all fields except
     * for the file changes.  Thus if two changelists occur at the same time,
     * by the same author, with the same comment and creating the same revision
     * they are deemed equal.  This is important for identifying the same
     * changelist when found by different {@link ScmClient}s.
     *
     * @param o object to compare to
     * @return true iff the other object is deemed to represent the same
     *         changelist as this one
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Changelist that = (Changelist) o;

        if (time != that.time)
        {
            return false;
        }
        if (author != null ? !author.equals(that.author) : that.author != null)
        {
            return false;
        }
        if (comment != null ? !comment.equals(that.comment) : that.comment != null)
        {
            return false;
        }

        return revision.equals(that.revision);
    }

    public int hashCode()
    {
        int result;
        result = revision.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "{ rev: " + revision.toString() + ", changes: " + changes.toString() + " }";
    }
}
