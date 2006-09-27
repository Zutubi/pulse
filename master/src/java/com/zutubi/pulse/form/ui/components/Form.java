package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class Form extends BodyUIComponent
{
    private static final String OPEN_TEMPLATE = "form";
    private static final String TEMPLATE = "form-end";

    protected String onsubmit;
    protected String action;
    protected String target;
    protected String enctype;
    protected String method;

    public String getDefaultTemplate()
    {
        return TEMPLATE;
    }

    public String getDefaultOpenTemplate()
    {
        return OPEN_TEMPLATE;
    }

    protected void evaluateExtraParameters()
    {
        super.evaluateExtraParameters();
        if (onsubmit != null)
        {
            addParameter("onsubmit", onsubmit);
        }
        if (action != null)
        {
            addParameter("action", action);
        }
        if (target != null)
        {
            addParameter("target", target);
        }
        if (enctype != null)
        {
            addParameter("enctype", enctype);
        }
        if (method != null)
        {
            addParameter("method", method);
        }
    }

    public void setOnsubmit(String onsubmit)
    {
        this.onsubmit = onsubmit;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public void setEnctype(String enctype)
    {
        this.enctype = enctype;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

}
