package com.zutubi.pulse.master.webwork.views.velocity.components;

import com.opensymphony.webwork.views.velocity.components.DivDirective;

/**
 */
public class CustomDivDirective extends DivDirective
{
    public int getType()
    {
        return BLOCK;
    }

    public String getBeanName()
    {
        return "cdiv";
    }
}
