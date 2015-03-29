package com.zutubi.pulse.master.tove.nimda;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 */
public class ConfigDetailAction extends ActionSupport
{
    private String path;
    private NimdaModel model;

    public void setPath(String path)
    {
        this.path = path;
    }

    public NimdaModel getModel()
    {
        return model;
    }

    @Override
    public String execute() throws Exception
    {
        model = new NimdaModel(path);
        return SUCCESS;
    }
}
