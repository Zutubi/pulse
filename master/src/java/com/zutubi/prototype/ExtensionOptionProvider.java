package com.zutubi.prototype;

import com.zutubi.prototype.i18n.Messages;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An option provider that lists the extensions for a particular type.  Used,
 * for example, when the user must select a type in a wizard.
 */
public class ExtensionOptionProvider extends MapOptionProvider
{
    private List<CompositeType> extensions;

    public ExtensionOptionProvider()
    {
    }

    public ExtensionOptionProvider(List<CompositeType> extensions)
    {
        this.extensions = extensions;
    }

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    protected Map<String, String> getMap(Object instance, String parentPath, TypeProperty property)
    {
        List<CompositeType> extensions;
        if (this.extensions != null)
        {
            extensions = this.extensions;
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
        for (CompositeType extension : extensions)
        {
            Messages messages = Messages.getInstance(extension.getClazz());
            options.put(extension.getSymbolicName(), messages.format("label"));
        }

        return options;
    }
}
