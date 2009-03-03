package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Referenceable;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.tove.type.*;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import nu.xom.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores configuration objects to XML files in a human-readable way.  Uses
 * type properties and their annotations to decide the XML format.
 *
 * @see ToveFileLoader
 */
public class ToveFileStorer
{
    private Map<CompositeType, String> typeDefinitions = new HashMap<CompositeType, String>();
    private TypeRegistry typeRegistry;

    public void register(CompositeType type, String name)
    {
        typeDefinitions.put(type, name);
    }

    public void store(File file, Configuration configuration, Element root) throws IOException
    {
        FileOutputStream stream = new FileOutputStream(file);
        try
        {
            store(stream, configuration, root);
        }
        finally
        {
            IOUtils.close(stream);
        }
    }

    public void store(OutputStream output, Configuration configuration, Element root) throws IOException
    {
        Serializer serializer = new Serializer(output);
        serializer.setIndent(4);

        Document document = new Document(root);
        fillElement(configuration, root);
        serializer.write(document);
    }

    private Element buildElement(String localName, Configuration configuration)
    {
        return fillElement(configuration, new Element(localName));
    }

    private Element fillElement(Configuration configuration, Element element)
    {
        CompositeType type = getType(configuration);
        for (TypeProperty property: type.getProperties())
        {
            storeProperty(configuration, property, element);
        }

        return element;
    }

    private CompositeType getType(Configuration configuration)
    {
        CompositeType type = typeRegistry.getType(configuration.getClass());
        if (type == null)
        {
            throw new PulseRuntimeException("Attempt to store instance of unregistered configuration class '" + configuration.getClass().getName() + "'");
        }
        return type;
    }

    private void storeProperty(Configuration configuration, TypeProperty property, Element element)
    {
        Type propertyType = property.getType();
        if (convertsToAttribute(propertyType))
        {
            String attributeValue;
            if (propertyType instanceof PrimitiveType)
            {
                attributeValue = convertPrimitiveProperty(configuration, property);
            }
            else if (propertyType instanceof EnumType)
            {
                attributeValue = convertEnumProperty(configuration, property);
            }
            else if (propertyType instanceof ReferenceType)
            {
                attributeValue = convertReferenceProperty(configuration, property);
            }
            else
            {
                attributeValue = convertPrimitiveListProperty(configuration, property);
            }

            if (attributeValue != null)
            {
                element.addAttribute(new Attribute(property.getName(), attributeValue));
            }
        }
        else if (propertyType instanceof CompositeType)
        {
            Configuration childConfiguration = (Configuration) getPropertyValue(configuration, property);
            if (childConfiguration != null)
            {
                CompositeType compositeType = (CompositeType) propertyType;
                String childName = typeDefinitions.get(compositeType);
                if (childName == null)
                {
                    childName = property.getName();
                }

                element.appendChild(buildElement(childName, childConfiguration));
            }
        }
        else if (propertyType instanceof CollectionType)
        {
            storeCollection(element, configuration, property);
        }
        else
        {
            throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': unsupported property type '" + propertyType.getClazz().getName() + "'");
        }
    }

    private boolean convertsToAttribute(Type propertyType)
    {
        return propertyType instanceof SimpleType || propertyType instanceof ListType && propertyType.getTargetType() instanceof PrimitiveType;
    }

    private String convertPrimitiveProperty(Configuration configuration, TypeProperty simpleProperty)
    {
        TypeSqueezer squeezer = Squeezers.findSqueezer(simpleProperty.getClazz());
        if (squeezer == null)
        {
            throw new PulseRuntimeException("Unable to convert simple property '" + simpleProperty.getName() + "' of class '" + configuration.getClass().getName() + "': no squeezer for class '" + simpleProperty.getClazz().getName() + "'");
        }

        try
        {
            return squeezer.squeeze(getPropertyValue(configuration, simpleProperty));
        }
        catch (SqueezeException e)
        {
            throw new PulseRuntimeException("Unable to convert simple property '" + simpleProperty.getName() + "' of class '" + configuration.getClass().getName() + "': " + e.getMessage(), e);
        }
    }

    private String convertEnumProperty(Configuration configuration, TypeProperty enumProperty)
    {
        return getPropertyValue(configuration, enumProperty).toString();
    }

    private String convertReferenceProperty(Configuration configuration, TypeProperty property)
    {
        ReferenceType propertyType = (ReferenceType) property.getType();
        CompositeType referencedType = propertyType.getReferencedType();
        TypeProperty nameProperty = getReferenceNameProperty(configuration, property, referencedType);

        return toReference((String) getPropertyValue(configuration, nameProperty));
    }

    private String convertPrimitiveListProperty(final Configuration configuration, final TypeProperty property)
    {
        ListType propertyType = (ListType) property.getType();
        PrimitiveType itemType = (PrimitiveType) propertyType.getTargetType();

        final TypeSqueezer squeezer = Squeezers.findSqueezer(itemType.getClazz());
        if (squeezer == null)
        {
            throw new PulseRuntimeException("Unable to convert items of simple list property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': no squeezer for class '" + itemType.getClazz().getName() + "'");
        }

        @SuppressWarnings("unchecked")
        List<Object> propertyValue = (List<Object>) getPropertyValue(configuration, property);
        List<String> list = CollectionUtils.map(propertyValue, new Mapping<Object, String>()
        {
            public String map(Object o)
            {
                try
                {
                    return squeezer.squeeze(o);
                }
                catch (SqueezeException e)
                {
                    throw new PulseRuntimeException("Unable to convert item of simple list property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': " + e.getMessage(), e);
                }
            }
        });

        return StringUtils.unsplit(list);
    }


    private void storeCollection(Element element, Configuration configuration, TypeProperty property)
    {
        Type itemType = property.getType().getTargetType();
        if (itemType instanceof ReferenceType)
        {
            ReferenceType referenceType = (ReferenceType) itemType;
            CompositeType referencedType = referenceType.getReferencedType();
            TypeProperty nameProperty = getReferenceNameProperty(configuration, property, referencedType);

            Addable addable = property.getAnnotation(Addable.class);
            if (addable == null)
            {
                throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': reference collections must be annotated with @Addable");
            }

            for (Configuration item: getCollectionItems(configuration, property))
            {
                Element itemElement = new Element(addable.value());
                String referenceName = (String) getPropertyValue(item, nameProperty);
                applyAddable(itemElement, addable, toReference(referenceName));
                element.appendChild(itemElement);
            }
        }
        else if (itemType instanceof CompositeType)
        {
            Addable addable = property.getAnnotation(Addable.class);
            for (Configuration item: getCollectionItems(configuration, property))
            {
                CompositeType actualItemType = typeRegistry.getType(item.getClass());
                String childName = typeDefinitions.get(actualItemType);
                if (childName == null)
                {
                    if (addable == null)
                    {
                        throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': composite collection contains item of non-registered type '" + actualItemType.getClazz().getName() + "' and is not annotated with @Addable");
                    }

                    childName = addable.value();
                }

                element.appendChild(buildElement(childName, item));
            }
        }
        else
        {
            throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': unsupported collection item type '" + itemType.getClazz().getName() + "'");
        }
    }

    private void applyAddable(Element element, Addable addable, String value)
    {
        String attribute = addable.attribute();
        if (TextUtils.stringSet(attribute))
        {
            element.addAttribute(new Attribute(attribute, value));
        }
        else
        {
            element.appendChild(new Text(value));
        }
    }

    private TypeProperty getReferenceNameProperty(Configuration configuration, TypeProperty property, CompositeType referencedType)
    {
        Referenceable referenceable = referencedType.getAnnotation(Referenceable.class, true);
        if (referenceable == null)
        {
            throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': the collection property is marked @Reference, but the collection item type '" + referencedType.getClazz().getName() + "' is not @Referenceable");
        }

        TypeProperty nameProperty = referencedType.getProperty(referenceable.nameProperty());
        if (nameProperty == null)
        {
            throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': the collection item type '" + referencedType.getClazz().getName() + "' has an invalid nameProperty '" + referenceable.nameProperty() + "' specified in its @Referenceable annotation");
        }

        return nameProperty;
    }

    private String toReference(String referenceName)
    {
        return "${" + referenceName + "}";
    }

    @SuppressWarnings("unchecked")
    private Collection<Configuration> getCollectionItems(Configuration configuration, TypeProperty property)
    {
        Collection<Configuration> items;
        if (property.getType() instanceof MapType)
        {
            Map map = (Map) getPropertyValue(configuration, property);
            items = map.values();
        }
        else
        {
            items = (List) getPropertyValue(configuration, property);
        }
        return items;
    }

    private Object getPropertyValue(Configuration configuration, TypeProperty property)
    {
        try
        {
            return property.getValue(configuration);
        }
        catch (Exception e)
        {
            throw new PulseRuntimeException("Unable to get value of property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': " + e.getMessage(), e);
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
