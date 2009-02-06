package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.master.tove.handler.MapOption;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.TypeRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An option provider that lists all available command types, mapping to the
 * symbolic names.
 */
public class CommandTypeOptionProvider extends MapOptionProvider
{
    private TypeRegistry typeRegistry;

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    protected Map<String, String> getMap(Object instance, String parentPath, TypeProperty property)
    {
        List<CompositeType> extensions = typeRegistry.getType(CommandConfiguration.class).getExtensions();
        Map<String, String> options = new HashMap<String, String>(extensions.size());
        for (CompositeType extension : extensions)
        {
            Messages messages = Messages.getInstance(extension.getClazz());
            String key = messages.isKeyDefined("wizard.label") ? "wizard.label" : "label";
            options.put(extension.getSymbolicName(), messages.format(key));
        }

        return options;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
