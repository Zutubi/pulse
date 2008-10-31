package com.zutubi.pulse.master.webwork.views.velocity.components;

import com.opensymphony.webwork.components.Component;
import com.opensymphony.webwork.views.velocity.components.AbstractDirective;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.master.webwork.components.CustomCheckbox;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <class-comment/>
 */
public class CustomCheckboxDirective extends AbstractDirective
{
    public String getBeanName()
    {
        return "ccheckbox";
    }

    protected Component getBean(OgnlValueStack stack, HttpServletRequest req, HttpServletResponse res)
    {
        return new CustomCheckbox(stack, req, res);
    }

    public int getType()
    {
        return LINE;
    }
}
