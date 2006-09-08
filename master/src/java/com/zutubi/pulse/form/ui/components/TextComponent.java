package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextComponent extends FieldComponent
{
    public void setSize(String size)
    {
        addParameter("size", size);
    }

    public String getSize()
    {
        return (String) getParameter("size");
    }

    public String getTemplateName()
    {
        return "text";
    }
}
