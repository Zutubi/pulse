package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.tove.ui.forms.EnumOptionProvider;

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
