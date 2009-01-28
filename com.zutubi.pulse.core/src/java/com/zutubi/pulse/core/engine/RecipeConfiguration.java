package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.RecipeVersionConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
@SymbolicName("zutubi.recipeConfig")
public class RecipeConfiguration extends AbstractNamedConfiguration
{
    @Ordered
    private Map<String, CommandConfiguration> commands = new LinkedHashMap<String, CommandConfiguration>();
    private RecipeVersionConfiguration version;

    public Map<String, CommandConfiguration> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, CommandConfiguration> commands)
    {
        this.commands = commands;
    }

    public RecipeVersionConfiguration getVersion()
    {
        return version;
    }

    public void setVersion(RecipeVersionConfiguration version)
    {
        this.version = version;
    }
}
