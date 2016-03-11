package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.tove.annotations.SymbolicName;

/**
 * Marker type used to identify triggers that have a manual "fire" action.  This is available on
 * triggers that don't need an upstream build to make sense.
 */
@SymbolicName("zutubi.fireableTriggerConfig")
public abstract class FireableTriggerConfiguration extends TriggerConfiguration
{
    public boolean prompt()
    {
        return false;
    }

    public boolean rebuildUpstream()
    {
        return false;
    }
}
