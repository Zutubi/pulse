package com.zutubi.pulse.master.webwork.views.velocity.components;

import com.opensymphony.webwork.views.velocity.components.TabbedPanelDirective;

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
