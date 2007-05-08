package com.zutubi.pulse.prototype.config.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("internal.customTypeConfig")
public class CustomTypeConfiguration extends AbstractConfiguration
{
    private String pulseFile;

    public String getPulseFile()
    {
        return pulseFile;
    }

    public void setPulseFile(String pulseFile)
    {
        this.pulseFile = pulseFile;
    }
}
