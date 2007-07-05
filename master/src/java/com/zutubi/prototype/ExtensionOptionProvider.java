package com.zutubi.prototype;

import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.i18n.Messages;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class ExtensionOptionProvider implements OptionProvider
{
    private CompositeType type;

    private TypeRegistry typeRegistry;

    public ExtensionOptionProvider(CompositeType type)
    {
        this.type = type;
    }

    public ExtensionOptionProvider()
    {
    }

    public Collection getOptions(Object instance, String path, TypeProperty property)
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

        List<OptionHolder> options = new LinkedList<OptionHolder>();
        for (String extension : extensions)
        {
            Messages messages = Messages.getInstance(typeRegistry.getType(extension).getClazz());
            options.add(new OptionHolder(extension, messages.format("label")));
        }
        
        return options;
    }

    public String getOptionKey()
    {
        return "key";
    }

    public String getOptionValue()
    {
        return "value";
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public class OptionHolder
    {
        private String key;
        private String value;

        public OptionHolder(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }
    }
}
