package com.zutubi.pulse.master.xwork.actions;

/**
 * Base action used for launching the reference doc popup window.
 */
public class ReferencePopupAction extends ActionSupport
{
    private String path;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
