package com.zutubi.pulse.master.tove.config;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * Transient configuration used as an argument when renaming labels.
 */
@SymbolicName("zutubi.newLabelConfig")
public class NewLabelConfiguration extends AbstractConfiguration
{
    private String label;

    public NewLabelConfiguration()
    {
    }

    public NewLabelConfiguration(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }
}
