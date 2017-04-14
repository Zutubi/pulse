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

package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The cvs revision is a composite of information used to identify a
 * particular checkin (new revision). Because cvs does not support atomic
 * commits, these revisions are a best guess.
 */
public class CvsRevision
{
    public static final String DATE_AND_TIME_FORMAT_STRING = "yyyyMMdd-HH:mm:ss";
    public static final String DATE_ONLY_FORMAT_STRING = "yyyyMMdd";

    private static final DateFormat DATE_AND_TIME_FORMAT = new SimpleDateFormat(DATE_AND_TIME_FORMAT_STRING);
    private static final DateFormat DATE_ONLY_FORMAT = new SimpleDateFormat(DATE_ONLY_FORMAT_STRING);

    private static final int MAX_COMMENT_LENGTH = 4095;
    private static final String COMMENT_TRIM_MESSAGE = "... [trimmed]";

    private String author;
    private String comment;
    private String branch;
    private long time;
    private String revisionString;

    public static final CvsRevision HEAD = null;

    protected CvsRevision()
    {

    }

    public CvsRevision(String author, String tag, String comment, Date date)
    {
        this(author, comment, date);
        setBranch(tag);
        setRevisionString(generateRevisionString());
    }

    public CvsRevision(String author, String comment, Date date)
    {
        this.author = author;
        this.comment = trimComment(comment);
        setDate(date);
        setRevisionString(generateRevisionString());
    }

    private String trimComment(String comment)
    {
        if(comment != null && comment.length() > MAX_COMMENT_LENGTH)
        {
            comment = comment.substring(0, MAX_COMMENT_LENGTH - COMMENT_TRIM_MESSAGE.length()) + COMMENT_TRIM_MESSAGE;
        }

        return comment;
    }


    public CvsRevision(String revStr) throws ScmException
    {
        // special case formats:
        // a) date and time.
        try
        {
            synchronized (DATE_AND_TIME_FORMAT)
            {
                setDate(DATE_AND_TIME_FORMAT.parse(revStr));
            }
            setRevisionString(generateRevisionString());
            return;
        }
        catch (ParseException e)
        {
            // noop.
        }

        // b) just a date, no time.
        try
        {
            synchronized (DATE_ONLY_FORMAT)
            {
                setDate(DATE_ONLY_FORMAT.parse(revStr));
            }
            setRevisionString(generateRevisionString());
            return;
        }
        catch (ParseException e)
        {
            // noop.
        }

        setRevisionString(revStr);

        // <author>:<branch/tag>:<date>
        if (revStr == null || StringUtils.count(revStr, ':') < 2)
        {
            throw new ScmException("Invalid CVS revision '" + revStr + "' (must be a date, or <author>:<branch>:<date>)");
        }

        String author = revStr.substring(0, revStr.indexOf(":"));
        String remainder = revStr.substring(revStr.indexOf(":") + 1);
        String branch = remainder.substring(0, remainder.indexOf(":"));
        String date = remainder.substring(remainder.indexOf(":") + 1);

        if (author.length() > 0)
        {
            setAuthor(author);
        }

        if (branch.length() > 0)
        {
            setBranch(branch);
        }

        if (date.length() > 0)
        {
            // accept two types of date dateOnlyFormat.
            try
            {
                synchronized (DATE_AND_TIME_FORMAT)
                {
                    setDate(DATE_AND_TIME_FORMAT.parse(date));
                }
            }
            catch (ParseException e)
            {
                try
                {
                    synchronized (DATE_ONLY_FORMAT)
                    {
                        setDate(DATE_ONLY_FORMAT.parse(date));
                    }
                }
                catch (ParseException ex)
                {
                    throw new ScmException("Invalid CVS revision '" + revStr + "' cannot parse '" + date + "' as a date");
                }
            }
        }
    }

    private String generateRevisionString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getAuthor() != null ? getAuthor() : "");
        buffer.append(":");
        buffer.append(getBranch() != null ? getBranch() : "");
        buffer.append(":");
        if (getDate() != null)
        {
            synchronized (DATE_AND_TIME_FORMAT)
            {
                buffer.append(DATE_AND_TIME_FORMAT.format(getDate()));
            }
        }
        return buffer.toString();
    }

    public boolean isHead()
    {
        return getAuthor() == null && getBranch() == null && getComment() == null && getDate() == null;
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

    public String getBranch()
    {
        return branch;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
    }

    /**
     * The date of this change.
     */
    public Date getDate()
    {
        if (time > 0)
        {
            return new Date(time);
        }
        return null;
    }

    public void setDate(Date date)
    {
        if (date != null)
        {
            this.time = date.getTime();
        }
        else
        {
            this.time = -1;
        }
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CvsRevision that = (CvsRevision) o;

        if (time != that.time) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (branch != null ? !branch.equals(that.branch) : that.branch != null) return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (revisionString != null ? !revisionString.equals(that.revisionString) : that.revisionString != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (branch != null ? branch.hashCode() : 0);
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (revisionString != null ? revisionString.hashCode() : 0);
        return result;
    }
}
