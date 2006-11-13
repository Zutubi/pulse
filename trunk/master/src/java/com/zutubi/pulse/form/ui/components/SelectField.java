package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class SelectField extends ListUIComponent
{
    protected String headerKey;
    protected String headerValue;
    protected String emptyOption;
    protected String multiple;
    protected String size;

    public String getDefaultTemplate()
    {
        return "select";
    }

    public void evaluateExtraParameters()
    {
        super.evaluateExtraParameters();

        if ((headerKey != null) && (headerValue != null))
        {
            addParameter("headerKey", headerKey);
            addParameter("headerValue", headerValue);
        }

        if (emptyOption != null)
        {
            addParameter("emptyOption", emptyOption);
        }
        if (multiple != null)
        {
            addParameter("multiple", multiple);
        }
        if (size != null)
        {
            addParameter("size", size);
        }
    }

    public void setEmptyOption(String emptyOption)
    {
        this.emptyOption = emptyOption;
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
        this.multiple = multiple;
    }

    public void setSize(String size)
    {
        this.size = size;
    }
}
