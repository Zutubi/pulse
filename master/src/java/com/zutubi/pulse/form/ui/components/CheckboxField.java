package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class CheckboxField extends UIComponent
{
    protected String fieldValue;

    public String getDefaultTemplate()
    {
        return "checkbox";
    }

    protected void evaluateExtraParams()
    {
        if (fieldValue != null)
        {
            addParameter("fieldValue", fieldValue);
        }
        else
        {
            addParameter("fieldValue", "true");
        }
    }

    public void setFieldValue(String fieldValue)
    {
        this.fieldValue = fieldValue;
    }
}
