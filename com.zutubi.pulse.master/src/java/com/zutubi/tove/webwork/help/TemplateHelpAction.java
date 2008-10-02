package com.zutubi.tove.webwork.help;

import com.zutubi.pulse.master.web.ActionSupport;

/**
 * Displays documentation for template operations.
 */
public class TemplateHelpAction extends ActionSupport
{
    public String execute() throws Exception
    {
        // Currently we show generic help.
        return SUCCESS;
    }
}
