package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class CheckboxComponent extends FieldComponent
{
    public CheckboxComponent()
    {
        setFieldValue(Boolean.TRUE.toString());
    }

    public String getTemplateName()
    {
        return "checkbox";
    }

    public void setFieldValue(String value)
    {
        addParameter("fieldValue", value);
    }

}
