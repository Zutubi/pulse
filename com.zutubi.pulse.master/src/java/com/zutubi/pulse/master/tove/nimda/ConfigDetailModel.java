package com.zutubi.pulse.master.tove.nimda;

import com.zutubi.pulse.master.tove.model.Form;

/**
 */
public class ConfigDetailModel
{
    private String path;
    private Form form;

    public String getPath()
    {
        return path;
    }

    public ConfigDetailModel(String path)
    {
        this.path = path;
    }

    public Form getForm()
    {
        return form;
    }

    public void setForm(Form form)
    {
        this.form = form;
    }
}
