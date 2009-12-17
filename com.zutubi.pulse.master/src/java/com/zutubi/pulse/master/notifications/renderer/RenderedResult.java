package com.zutubi.pulse.master.notifications.renderer;

/**
 * Represents a rendered notification message.
 */
public class RenderedResult
{
    private String subject;
    private String content;

    public RenderedResult(String subject, String content)
    {
        this.subject = subject;
        this.content = content;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getContent()
    {
        return content;
    }
}
