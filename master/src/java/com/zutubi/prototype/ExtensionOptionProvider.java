package com.zutubi.prototype;

import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.i18n.Messages;

import java.util.*;

/**
 * An option provider that lists the extensions for a particular type.  Used,
 * for example, when the user must select a type in a wizard.
 */
public class ExtensionOptionProvider extends MapOptionProvider
{
    private CompositeType type;
    private TypeRegistry typeRegistry;

    public ExtensionOptionProvider()
    {
    }

    public ExtensionOptionProvider(CompositeType type)
    {
        this.type = type;
    }

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    protected Map<String, String> getMap(Object instance, String parentPath, TypeProperty property)
    {
        List<String> extensions;
        if (type != null)
        {
            extensions = type.getExtensions();
        }
        else
        {
            Type propertyType = property.getType();
            if (!(propertyType instanceof CompositeType))
            {
                extensions = Collections.EMPTY_LIST;
            }
            else
            {
                extensions = ((CompositeType) propertyType).getExtensions();
            }
        }

        Map<String, String> options = new HashMap<String, String>(extensions.size());
        for (String extension : extensions)
        {
            Messages messages = Messages.getInstance(typeRegistry.getType(extension).getClazz());
            options.put(extension, messages.format("label"));
        }

        return options;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
