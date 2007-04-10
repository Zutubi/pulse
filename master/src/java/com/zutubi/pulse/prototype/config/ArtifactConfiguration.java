package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.Format;
import com.zutubi.prototype.annotation.TypeSelect;
import com.zutubi.validation.annotations.Name;

/**
 *
 *
 */
@Format(ArtifactConfigurationFormatter.class)
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
