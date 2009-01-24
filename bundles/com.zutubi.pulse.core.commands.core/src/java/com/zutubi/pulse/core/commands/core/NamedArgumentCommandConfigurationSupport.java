package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.namedArgumentCommandConfig")
public abstract class NamedArgumentCommandConfigurationSupport extends ExecutableCommandConfiguration
{
    public NamedArgumentCommandConfigurationSupport()
    {
    }

    protected NamedArgumentCommandConfigurationSupport(Class<? extends ExecutableCommand> clazz)
    {
        super(clazz);
    }

    protected NamedArgumentCommandConfigurationSupport(Class<? extends ExecutableCommand> clazz, String exeProperty, String defaultExe)
    {
        super(clazz, exeProperty, defaultExe);
    }

    @Override
    public List<String> getCombinedArguments()
    {
        List<String> combined = new LinkedList<String>();
        List<NamedArgument> namedArguments = getNamedArguments();
        for (NamedArgument namedArgument: namedArguments)
        {
            combined.addAll(namedArgument.getArgs());
        }

        combined.addAll(super.getCombinedArguments());
        return combined;
    }

    @Transient
    protected abstract List<NamedArgument> getNamedArguments();

    public static class NamedArgument
    {
        String name;
        String value;
        List<String> args;

        public NamedArgument(String name, String value)
        {
            this(name, value, Arrays.asList(value));
        }

        public NamedArgument(String name, String value, String flag)
        {
            this(name, value, Arrays.asList(flag, value));
        }

        public NamedArgument(String name, String value, List<String> args)
        {
            this.name = name;
            this.value = value;
            this.args = args;
        }

        public String getName()
        {
            return name;
        }

        public String getValue()
        {
            return value;
        }

        public List<String> getArgs()
        {
            return args;
        }
    }
}
