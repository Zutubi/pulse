package com.zutubi.pulse.master.tove.nimda;

import com.zutubi.pulse.master.tove.model.Form;

/**
 * Data model for rendering a composite type in the admin UI.
 */
public class CompositeModel
{
    private String path;
    private String displayName;
    private String introduction;

    private Form form;

    public String getPath()
    {
        return path;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getIntroduction()
    {
        return introduction;
    }

    public Form getForm()
    {
        return form;
    }
}
