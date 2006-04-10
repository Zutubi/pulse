package com.cinnamonbob.webwork.velocity;

import com.opensymphony.webwork.views.velocity.components.TabbedPanelDirective;

/**
 */
public class CustomTabbedPanelDirective extends TabbedPanelDirective
{
    public int getType()
    {
        return BLOCK;
    }

    public String getBeanName()
    {
        return "ctabbedpanel";
    }
}
