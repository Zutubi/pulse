package com.zutubi.pulse.master.rest.model;

/**
 * Model for configuration checking.
 */
public class CheckModel
{
    private CompositeModel main;
    private CompositeModel check;

    public CompositeModel getMain()
    {
        return main;
    }

    public void setMain(CompositeModel main)
    {
        this.main = main;
    }

    public CompositeModel getCheck()
    {
        return check;
    }

    public void setCheck(CompositeModel check)
    {
        this.check = check;
    }
}
