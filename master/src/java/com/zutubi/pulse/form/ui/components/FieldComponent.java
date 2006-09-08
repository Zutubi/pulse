package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public abstract class FieldComponent extends Component
{
    public void setRequired(boolean b)
    {
        addParameter("required", b);
    }

    public boolean isRequired()
    {
        return (Boolean)getParameter("required", false);
    }

    public void setLabel(String label)
    {
        addParameter("label", label);
    }

    public String getLabel()
    {
        return (String) getParameter("label");
    }
}
