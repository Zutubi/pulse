package com.zutubi.pulse.webwork.components;

import com.opensymphony.webwork.components.Form;
import com.opensymphony.xwork.util.OgnlValueStack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CustomForm extends Form
{
    private String heading;

    public CustomForm(OgnlValueStack stack, HttpServletRequest request, HttpServletResponse response)
    {
        super(stack, request, response);
    }

    public String getDefaultOpenTemplate()
    {
        return "cform";
    }

    public String getHeading()
    {
        return heading;
    }

    public void setHeading(String heading)
    {
        this.heading = heading;
    }

    protected void evaluateExtraParams()
    {
        super.evaluateExtraParams();
        addParameter("heading", findString(heading));
    }
}
