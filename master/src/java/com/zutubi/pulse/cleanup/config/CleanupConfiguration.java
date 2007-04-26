package com.zutubi.pulse.cleanup.config;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Select;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wizard;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.model.ResultState;

import java.util.List;

/**
 *
 *
 */
@Wizard("CleanupConfigurationWizard")
@SymbolicName("cleanupRuleConfig")
@Form(fieldOrder = {"name", "what", "retain", "unit"})
public class CleanupConfiguration extends AbstractNamedConfiguration
{
    private CleanupWhat what;

    @Select(optionProvider = "com.zutubi.pulse.prototype.CompletedResultStateOptionProvider")
    private List<ResultState> states;

    private int retain;

    private CleanupUnit unit;

    public CleanupWhat getWhat()
    {
        return what;
    }

    public void setWhat(CleanupWhat what)
    {
        this.what = what;
    }

    public List<ResultState> getStates()
    {
        return states;
    }

    public void setStates(List<ResultState> states)
    {
        this.states = states;
    }

    public int getRetain()
    {
        return retain;
    }

    public void setRetain(int retain)
    {
        this.retain = retain;
    }

    public CleanupUnit getUnit()
    {
        return unit;
    }

    public void setUnit(CleanupUnit unit)
    {
        this.unit = unit;
    }
}
