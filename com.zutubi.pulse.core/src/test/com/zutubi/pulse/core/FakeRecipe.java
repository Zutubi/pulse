package com.zutubi.pulse.core;

import java.util.LinkedList;
import java.util.List;

public class FakeRecipe
{
    private String name;
    private List<FakeCommand> commands = new LinkedList<FakeCommand>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void add(FakeCommand command)
    {
        commands.add(command);
    }

    public List<FakeCommand> getCommands()
    {
        return commands;
    }
}
