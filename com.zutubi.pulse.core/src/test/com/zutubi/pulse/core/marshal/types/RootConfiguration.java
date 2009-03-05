package com.zutubi.pulse.core.marshal.types;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Mimics the configuration type for the root of a tove file.
 */
@SymbolicName("root")
public class RootConfiguration extends AbstractConfiguration
{
    @Addable("mixed")
    private Map<String, MixedConfiguration> mixers = new HashMap<String, MixedConfiguration>();
    @Addable("required")
    private Map<String, RequiredPropertiesConfiguration> requireds = new HashMap<String, RequiredPropertiesConfiguration>();

    public Map<String, MixedConfiguration> getMixers()
    {
        return mixers;
    }

    public void setMixers(Map<String, MixedConfiguration> mixers)
    {
        this.mixers = mixers;
    }

    public Map<String, RequiredPropertiesConfiguration> getRequireds()
    {
        return requireds;
    }

    public void setRequireds(Map<String, RequiredPropertiesConfiguration> requireds)
    {
        this.requireds = requireds;
    }
}
