package com.zutubi.pulse.tove;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.tove.config.EnumOptionProvider;

/**
 * Option provider for ResultState's that only includes completed states.
 */
public class CompletedResultStateOptionProvider extends EnumOptionProvider
{
    protected boolean includeOption(Enum e)
    {
        return e instanceof ResultState && ((ResultState) e).isCompleted();
    }
}
