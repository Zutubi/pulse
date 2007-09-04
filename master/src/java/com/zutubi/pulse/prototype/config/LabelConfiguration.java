package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 * Labels are used for freeform categorisation.
 */
@SymbolicName("zutubi.labelConfig")
public class LabelConfiguration extends AbstractConfiguration
{
    private String label;

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }
}
