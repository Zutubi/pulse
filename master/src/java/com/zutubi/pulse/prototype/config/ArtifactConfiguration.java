package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.Format;
import com.zutubi.prototype.annotation.TypeSelect;

/**
 *
 *
 */
@Format(ArtifactConfigurationFormatter.class)
public class ArtifactConfiguration implements ConfigurationExtension
{
    private String option;

    private String name;

    @TypeSelect(configurationType = ArtifactConfiguration.class, size = 5)
    public String getOption()
    {
        return option;
    }

    public void setOption(String option)
    {
        this.option = option;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
