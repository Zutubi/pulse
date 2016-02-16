package com.zutubi.tove.ui.handler;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeProperty;

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

    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public Map<String, String> getMap(TypeProperty property, FormContext context)
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
                extensions = Collections.emptyList();
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
            String key = messages.isKeyDefined("wizard.label") ? "wizard.label" : "label";
            options.put(extension.getSymbolicName(), messages.format(key));
        }

        return options;
    }
}
