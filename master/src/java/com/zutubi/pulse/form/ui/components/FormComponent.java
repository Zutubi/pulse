package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class FormComponent extends BodyComponent
{
    public String getBaseTemplateName()
    {
        return "form";
    }

    public void setMethod(String method)
    {
        addParameter("method", method);
    }

    public void setAction(String action)
    {
        addParameter("action", action);
    }

}
