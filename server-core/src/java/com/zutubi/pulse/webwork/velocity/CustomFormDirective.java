package com.cinnamonbob.webwork.velocity;

import com.cinnamonbob.webwork.components.CustomForm;
import com.opensymphony.webwork.components.Component;
import com.opensymphony.webwork.views.velocity.components.AbstractDirective;
import com.opensymphony.xwork.util.OgnlValueStack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CustomFormDirective extends AbstractDirective
{
    protected Component getBean(OgnlValueStack stack, HttpServletRequest req, HttpServletResponse res)
    {
        return new CustomForm(stack, req, res);
    }

    public String getBeanName()
    {
        return "cform";
    }

    public int getType()
    {
        return BLOCK;
    }
}
