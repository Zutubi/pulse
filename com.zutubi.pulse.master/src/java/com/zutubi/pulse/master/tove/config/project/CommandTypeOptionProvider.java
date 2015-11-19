package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.master.tove.handler.ExtensionOptionProvider;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.TypeRegistry;

import java.util.List;
import java.util.Map;

/**
 * An option provider that lists all available command types, mapping to the
 * symbolic names.
 */
public class CommandTypeOptionProvider extends MapOptionProvider
{
    private TypeRegistry typeRegistry;

    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    protected Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        List<CompositeType> extensions = typeRegistry.getType(CommandConfiguration.class).getExtensions();
        ExtensionOptionProvider delegate = new ExtensionOptionProvider(extensions);
        return delegate.getMap(property, context);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
