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

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.util.StringUtils;
import com.zutubi.util.time.TimeStamps;

import java.util.Locale;

/**
 * A message left by some user on another entity.
 */
public class Comment extends Entity
{
    /** Maximum length of the message. */
    private static final int LENGTH_LIMIT = 4095;

    /**
     * Login of the user that made this comment.  We don't link directly to the
     * user as a user's comments may live on despite the user being deleted.
     */
    private String author;
    /**
     * Time the comment was left, in milliseconds since the epoch.
     */
    private long time;
    /**
     * The actual message left be the user.
     */
    private String message;

    public Comment()
    {
    }

    public Comment(String author, long time, String message)
    {
        setAuthor(author);
        setTime(time);
        setMessage(message);
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public String getPrettyDate(Locale locale)
    {
        return TimeStamps.getPrettyDate(time, locale);
    }

    public String getPrettyTime()
    {
        return TimeStamps.getPrettyTime(time);
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message == null ? null : StringUtils.trimmedString(message, LENGTH_LIMIT);
    }
}
