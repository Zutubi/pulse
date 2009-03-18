package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

@SymbolicName("fakeRecipe")
public class FakeRecipe extends AbstractNamedConfiguration
{
    @Ordered
    private Map<String, FakeCommand> commands = new LinkedHashMap<String, FakeCommand>();

    public FakeRecipe()
    {
    }

    public FakeRecipe(String name)
    {
        super(name);
    }

    public Map<String, FakeCommand> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, FakeCommand> commands)
    {
        this.commands = commands;
    }

    public void addCommand(FakeCommand command)
    {
        commands.put(command.getName(), command);
    }
}
