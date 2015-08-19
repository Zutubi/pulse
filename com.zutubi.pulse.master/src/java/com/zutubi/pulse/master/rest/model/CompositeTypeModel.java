package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Model wrapping composite types.
 */
public class CompositeTypeModel extends TypeModel
{
    private List<PropertyModel> simpleProperties;
    private List<PropertyModel> nestedProperties;
    private List<CompositeTypeModel> subTypes;

    public CompositeTypeModel(CompositeType type)
    {
        super(type);

        List<String> simplePropertyNames = type.getSimplePropertyNames();
        if (simplePropertyNames.size() > 0)
        {
            simpleProperties = new ArrayList<>();
            for (String propertyName: simplePropertyNames)
            {
                TypeProperty property = type.getProperty(propertyName);
                simpleProperties.add(new PropertyModel(property));
            }
        }

        List<String> nestedPropertyNames = type.getNestedPropertyNames();
        if (nestedPropertyNames.size() > 0)
        {
            nestedProperties = new ArrayList<>();
            for (String propertyName: nestedPropertyNames)
            {
                TypeProperty property = type.getProperty(propertyName);
                nestedProperties.add(new PropertyModel(property));
            }
        }

        List<CompositeType> extensions = type.getExtensions();
        if (extensions.size() > 0)
        {
            subTypes = new ArrayList<>();
            for (CompositeType extension: extensions)
            {
                subTypes.add(new CompositeTypeModel(extension));
            }
        }
    }

    public List<PropertyModel> getSimpleProperties()
    {
        return simpleProperties;
    }

    public List<PropertyModel> getNestedProperties()
    {
        return nestedProperties;
    }

    public List<CompositeTypeModel> getSubTypes()
    {
        return subTypes;
    }

    public static class PropertyModel
    {
        private TypeProperty property;

        public PropertyModel(TypeProperty property)
        {
            this.property = property;
        }

        public String getName()
        {
            return property.getName();
        }

        public String getShortType()
        {
            return formatShortType(property.getType());
        }
    }
}
