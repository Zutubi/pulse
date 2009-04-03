package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configures a sequence of commands to run as a single unit or recipe.
 */
@SymbolicName("zutubi.recipeConfig")
@Form(fieldOrder = {"name", "version"})
public class RecipeConfiguration extends AbstractNamedConfiguration
{
    private String version;
    @Ordered
    private Map<String, CommandConfiguration> commands = new LinkedHashMap<String, CommandConfiguration>();

    public RecipeConfiguration()
    {
    }

    public RecipeConfiguration(String name)
    {
        super(name);
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Map<String, CommandConfiguration> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, CommandConfiguration> commands)
    {
        this.commands = commands;
    }

    public void addCommand(CommandConfiguration command)
    {
        commands.put(command.getName(), command);
    }
}
