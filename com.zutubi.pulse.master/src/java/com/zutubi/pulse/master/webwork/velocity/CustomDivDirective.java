package com.zutubi.pulse.master.webwork.velocity;

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
