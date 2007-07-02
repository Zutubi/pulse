package com.zutubi.pulse.web.admin;

import com.zutubi.prototype.webwork.DisplayTemplatedConfigAction;

/**
 * Empty extension of the DisplayAction used to help with the consistency of configurations.
 */
public class AgentsAction extends DisplayTemplatedConfigAction
{
    public String getScope()
    {
        return "agent";
    }

    public String getPath()
    {
        return "agent";
    }
}
