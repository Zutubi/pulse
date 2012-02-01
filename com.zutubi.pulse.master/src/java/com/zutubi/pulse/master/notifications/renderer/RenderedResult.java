package com.zutubi.pulse.master.notifications.renderer;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a rendered notification message.
 */
public class RenderedResult
{
    private String subject;
    private String content;
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
     * @see #getMimeType()
     */
    public String getContent()
    {
        return content;
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
