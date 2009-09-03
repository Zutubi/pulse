package com.zutubi.pulse.master.notifications.renderer;

/**
 */
public class TemplateInfo
{
    private String template;
    private String display;
    private String mimeType;

    public TemplateInfo(String template, String display, String mimeType)
    {
        this.template = template;
        this.display = display;
        this.mimeType = mimeType;
    }

    public String getTemplate()
    {
        return template;
    }

    public String getDisplay()
    {
        return display;
    }

    public String getMimeType()
    {
        return mimeType;
    }
}
