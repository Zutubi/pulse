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

package com.zutubi.pulse.master.notifications.renderer;

import com.zutubi.util.StringUtils;

import java.io.InputStream;
import java.util.*;

/**
 * Represents a rendered notification message.
 */
public class RenderedResult
{
    private String subject;
    private String content;
    private Map<Integer, String> trimmedContent = new HashMap<Integer, String>();
    private String mimeType;
    private List<InputStream> attachments = new LinkedList<InputStream>();

    public RenderedResult(String subject, String content, String mimeType)
    {
        this.subject = subject;
        this.content = content;
        this.mimeType = mimeType;
    }

    /**
     * A brief title for the message, suitable as e.g. an email subject.
     *
     * @return a brief title for the message
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * The main body of the message, suitable as e.g. the primary part of an
     * email.
     *
     * @return the message body
     *
     * @see #getContentTrimmed(int) 
     * @see #getMimeType()
     */
    public String getContent()
    {
        return content;
    }

    /**
     * The main body of the message, but limited to a maximum number of bytes.
     * Obtaining the trimmed version this way allows it to be shared by calls
     * using the same limit.
     *
     * @param limit maximum length of the content to return, if the content is
     *              longer it will be trimmed (with ellipsis appended)
     * @return the message body, possibly truncated
     *
     * @see #getContent() 
     */
    public String getContentTrimmed(int limit)
    {
        String trimmed = trimmedContent.get(limit);
        if (trimmed == null)
        {
            trimmed = StringUtils.trimmedString(content, limit);
            trimmedContent.put(limit, content);
        }
        
        return trimmed;
    }

    /**
     * The MIME type of the message body, e.g. text/plain.
     *
     * @return the message content's type
     *
     * @see #getContent()
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * Adds the given attachment to the message.  All attachments are assumed
     * to be plain text (i.e. MIME type text/plain).  The given stream will be
     * read exactly once.
     *
     * @param attachment the attachment to add
     */
    public void addAttachment(InputStream attachment)
    {
        attachments.add(attachment);
    }

    /**
     * Returns all attachments for the message.  Attachments are assumed to be
     * plain text (i.e. MIME type text/plain).  They are represented as streams
     * ready to be read.
     *
     * @return all attachments for the message
     */
    public List<InputStream> getAttachments()
    {
        return Collections.unmodifiableList(attachments);
    }
}
