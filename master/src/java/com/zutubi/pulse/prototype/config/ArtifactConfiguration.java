package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.annotation.Format;
import com.zutubi.validation.annotations.Name;

/**
 *
 *
 */
@Format("ArtifactConfigurationFormatter")
public class ArtifactConfiguration
{
    @Name
    private String name;


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
