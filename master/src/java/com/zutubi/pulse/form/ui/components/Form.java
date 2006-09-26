package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class Form extends BodyUIComponent
{
    private static final String OPEN_TEMPLATE = "form";
    private static final String TEMPLATE = "form-end";

    public String getDefaultTemplate()
    {
        return TEMPLATE;
    }

    public String getDefaultOpenTemplate()
    {
        return OPEN_TEMPLATE;
    }

    public void setOnsubmit(String onsubmit)
    {
        addParameter("onsubmit", onsubmit);
    }

    public void setAction(String action)
    {
        addParameter("action", action);
    }

    public void setTarget(String target)
    {
        addParameter("target", target);
    }

    public void setEnctype(String enctype)
    {
        addParameter("enctype", enctype);
    }

    public void setMethod(String method)
    {
        addParameter("method", method);
    }

}
