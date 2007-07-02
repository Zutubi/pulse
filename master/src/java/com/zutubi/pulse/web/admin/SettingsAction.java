package com.zutubi.pulse.web.admin;

import com.zutubi.prototype.webwork.DisplayTemplatedConfigAction;

/**
 * Empty extension of the DisplayAction used to help with the consistency of configurations.
 */
//FIXME: this should not be extending the DisplayTemplatedConfig action since the settings are not templated.
public class SettingsAction extends DisplayTemplatedConfigAction
{
    public String getScope()
    {
        return "global";
    }

    public String getPath()
    {
        return "global";
    }
}
