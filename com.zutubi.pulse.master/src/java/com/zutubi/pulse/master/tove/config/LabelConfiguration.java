package com.zutubi.pulse.master.tove.config;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

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
