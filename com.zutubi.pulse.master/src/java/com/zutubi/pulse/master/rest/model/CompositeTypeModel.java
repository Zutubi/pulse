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
    private List<PropertyModel> simpleProperties = new ArrayList<>();
    private List<PropertyModel> nestedProperties = new ArrayList<>();

    public CompositeTypeModel(CompositeType type)
    {
        super(type);

        for (String propertyName: type.getSimplePropertyNames())
        {
            TypeProperty property = type.getProperty(propertyName);
            simpleProperties.add(new PropertyModel(property));
        }

        for (String propertyName: type.getNestedPropertyNames())
        {
            TypeProperty property = type.getProperty(propertyName);
            nestedProperties.add(new PropertyModel(property));
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
