package com.zutubi.pulse.master.rest.model;

import com.zutubi.pulse.master.rest.model.forms.FormModel;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.record.MutableRecord;

import java.util.*;

/**
 * Model wrapping composite types.
 */
public class CompositeTypeModel extends TypeModel
{
    private List<PropertyModel> simpleProperties;
    private Map<String, Object> simplePropertyDefaults;
    private List<PropertyModel> nestedProperties;
    private List<CompositeTypeModel> subTypes;
    private FormModel form;
    private CompositeTypeModel checkType;
    private DocModel docs;

    public CompositeTypeModel()
    {
    }

    public CompositeTypeModel(CompositeType type)
    {
        super(type.getSymbolicName());

        simpleProperties = createProperties(type, type.getSimplePropertyNames());

        try
        {
            MutableRecord defaults = type.unstantiate(type.getDefaultInstance(), null);
            if (defaults != null)
            {
                Set<String> simpleKeySet = defaults.simpleKeySet();
                if (simpleKeySet.size() > 0)
                {
                    simplePropertyDefaults = new HashMap<>();
                    for (String key: simpleKeySet)
                    {
                        simplePropertyDefaults.put(key, defaults.get(key));
                    }
                }
            }
        }
        catch (TypeException e)
        {
            // Defaults are not essential.
        }

        nestedProperties = createProperties(type, type.getNestedPropertyNames());
    }

    private List<PropertyModel> createProperties(CompositeType type, List<String> propertyNames)
    {
        List<PropertyModel> properties = null;
        if (propertyNames.size() > 0)
        {
            properties = new ArrayList<>();
            for (String propertyName: propertyNames)
            {
                TypeProperty property = type.getProperty(propertyName);
                properties.add(new PropertyModel(property));
            }
        }

        return properties;
    }

    public List<PropertyModel> getSimpleProperties()
    {
        return simpleProperties;
    }

    public Map<String, Object> getSimplePropertyDefaults()
    {
        return simplePropertyDefaults;
    }

    public void setSimplePropertyDefaults(Map<String, Object> simplePropertyDefaults)
    {
        this.simplePropertyDefaults = simplePropertyDefaults;
    }

    public List<PropertyModel> getNestedProperties()
    {
        return nestedProperties;
    }

    public List<CompositeTypeModel> getSubTypes()
    {
        return subTypes;
    }

    public void addSubType(CompositeTypeModel subType)
    {
        if (subTypes == null)
        {
            subTypes = new ArrayList<>();
        }

        subTypes.add(subType);
    }

    public FormModel getForm()
    {
        return form;
    }

    public void setForm(FormModel form)
    {
        this.form = form;
    }

    public CompositeTypeModel getCheckType()
    {
        return checkType;
    }

    public void setCheckType(CompositeTypeModel checkType)
    {
        this.checkType = checkType;
    }

    public DocModel getDocs()
    {
        return docs;
    }

    public void setDocs(DocModel docs)
    {
        this.docs = docs;
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
