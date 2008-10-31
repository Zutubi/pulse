package com.zutubi.pulse.master.tove.config.project.hooks;

/**
 * Indicates if a hook is enabled.
 */
public class AutoBuildHookConfigurationStateDisplay
{
    public String formatState(AutoBuildHookConfiguration hook)
    {
        return hook.isEnabled() ? "enabled" : "disabled";
    }
}
