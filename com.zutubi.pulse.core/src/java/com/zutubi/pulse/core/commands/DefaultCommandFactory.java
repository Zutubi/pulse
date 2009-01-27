package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandFactory;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.ReflectionUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.lang.reflect.Constructor;

/**
 * Default implementation of {@link com.zutubi.pulse.core.commands.api.CommandFactory},
 * which uses the object factory to build commands.
 */
public class DefaultCommandFactory implements CommandFactory
{
    private static final Logger LOG = Logger.getLogger(DefaultCommandFactory.class);

    private ObjectFactory objectFactory;

    public Command createCommand(CommandConfiguration configuration)
    {
        // We need to pass the formal argument type to the object factory, so
        // figure it out from the actual type of the configuration.
        Class<?> formalParameterType = null;
        Class<? extends Command> type = configuration.commandType();
        for (Constructor constructor: type.getConstructors())
        {
            if (ReflectionUtils.acceptsParameters(constructor, configuration.getClass()))
            {
                formalParameterType = constructor.getParameterTypes()[0];
            }
        }

        if (formalParameterType == null)
        {
            throw new BuildException("Unable to create command '" + configuration.getName() + "': No constructor of command type '" + type.getName() + "' accepts configuration of type '" + configuration.getClass().getName() + "'");
        }

        try
        {
            return objectFactory.buildBean(type, new Class[]{formalParameterType}, new Object[]{configuration});
        }
        catch (Exception e)
        {
            LOG.severe(e);
            throw new BuildException("Unable to instantiate command '" + configuration.getName() + "': " + e.getMessage(), e);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}