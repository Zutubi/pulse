package com.zutubi.pulse.web.admin;

import com.zutubi.prototype.webwork.DisplayTemplatedConfigAction;

/**
 * Empty extension of the DisplayAction used to help with the consistency of configurations.
 */
public class ProjectsAction extends DisplayTemplatedConfigAction
{
    public String getScope()
    {
        return "project";
    }

    public String getPath()
    {
        return "project";
    }
}
