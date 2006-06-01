package com.zutubi.pulse.webwork.components;

import com.opensymphony.webwork.components.Checkbox;
import com.opensymphony.xwork.util.OgnlValueStack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <class-comment/>
 */
public class CustomCheckbox extends Checkbox
{
    public CustomCheckbox(OgnlValueStack stack, HttpServletRequest request, HttpServletResponse response)
    {
        super(stack, request, response);
    }

    protected String getDefaultTemplate()
    {
        return "ccheckbox";
    }
}
