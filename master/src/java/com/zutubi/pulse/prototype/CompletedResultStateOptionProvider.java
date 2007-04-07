package com.zutubi.pulse.prototype;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.prototype.config.EnumOptionProvider;

/**
 * Option provider for ResultState's that only includes completed states.
 */
public class CompletedResultStateOptionProvider extends EnumOptionProvider
{
    protected boolean includeOption(Enum e)
    {
        if(e instanceof ResultState)
        {
            return ((ResultState)e).isCompleted();
        }

        return false;
    }
}
