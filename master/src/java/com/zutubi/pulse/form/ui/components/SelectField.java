package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class SelectField extends ListUIComponent
{
    public String getDefaultTemplate()
    {
        return "select";
    }

    public void evaluateExtraParameters()
    {
        super.evaluateExtraParameters();

        if ((headerKey != null) && (headerValue != null)) {
            addParameter("headerKey", headerKey);
            addParameter("headerValue", headerValue);
        }
    }

    protected String headerKey;
    protected String headerValue;

    public void setEmptyOption(String emptyOption)
    {
        addParameter("emptyOption", emptyOption);
    }

    public void setHeaderKey(String headerKey)
    {
        this.headerKey = headerKey;
    }

    public void setHeaderValue(String headerValue)
    {
        this.headerValue = headerValue;
    }

    public void setMultiple(String multiple)
    {
        addParameter("multiple", multiple);
    }

    public void setSize(String size)
    {
        addParameter("size", size);
    }
}
