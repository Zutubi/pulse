package com.zutubi.pulse.core.marshal;

import com.google.common.base.Function;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.tove.type.*;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import nu.xom.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.zutubi.pulse.core.marshal.ToveFileUtils.*;

/**
 * Stores configuration objects to XML files in a human-readable way.  Uses
 * type properties and their annotations to decide the XML format.
 *
 * @see ToveFileLoader
 */
public class ToveFileStorer
{
    private static final Sort.StringComparator STRING_COMPARATOR = new Sort.StringComparator();
    private static final String PROPERTY_NAME = "name";

    private TypeDefinitions typeDefinitions;
    private TypeRegistry typeRegistry;

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

        // Try to give a human-friendly sorting by processing properties
        // alphabetically with the exception of "name" which will always be
        // moved to the front if present.
        List<String> propertyNames = type.getPropertyNames();
        Collections.sort(propertyNames, STRING_COMPARATOR);
        if (propertyNames.remove(PROPERTY_NAME))
        {
            propertyNames.add(0, PROPERTY_NAME);
        }

        for (String propertyName: propertyNames)
        {
            storeProperty(configuration, type, type.getProperty(propertyName), element);
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

    private void storeProperty(Configuration configuration, CompositeType type, TypeProperty property, Element element)
    {
        Type propertyType = property.getType();
        if (convertsToAttribute(property))
        {
            String attributeValue = convertAttribute(configuration, type, property);
            if (attributeValue != null && !attributeValue.equals(convertAttribute(type.getDefaultInstance(), type, property)))
            {
                element.addAttribute(new Attribute(convertPropertyNameToLocalName(property.getName()), attributeValue));
            }
        }
        else if (propertyType instanceof CompositeType)
        {
            Configuration childConfiguration = (Configuration) getPropertyValue(configuration, property);
            if (childConfiguration != null)
            {
                CompositeType compositeType = (CompositeType) propertyType;
                String childName = typeDefinitions.getName(compositeType);
                if (childName == null)
                {
                    childName = convertPropertyNameToLocalName(property.getName());
                }

                element.appendChild(buildElement(childName, childConfiguration));
            }
        }
        else if (propertyType instanceof CollectionType)
        {
            storeCollection(element, configuration, type, property);
        }
        else
        {
            throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': unsupported property type '" + propertyType.getClazz().getName() + "'");
        }
    }


    private void storeCollection(Element element, Configuration configuration, CompositeType type, TypeProperty property)
    {
        Type itemType = property.getType().getTargetType();
        if (itemType instanceof SimpleType)
        {
            Addable addable = property.getAnnotation(Addable.class);
            if (addable == null)
            {
                throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': simple collections must be annotated with @Addable");
            }

            List<String> converted;
            if (itemType instanceof ReferenceType)
            {
                converted = convertReferenceCollection(configuration, type, property);
            }
            else
            {
                converted = squeezeCollection(configuration, property);
            }

            for (String s: converted)
            {
                Element itemElement = new Element(addable.value());
                applyAddable(itemElement, addable, s);
                element.appendChild(itemElement);
            }
        }
        else if (itemType instanceof CompositeType)
        {
            Addable addable = property.getAnnotation(Addable.class);
            for (Configuration item: getCollectionItems(configuration, property))
            {
                CompositeType actualItemType = typeRegistry.getType(item.getClass());
                String childName = typeDefinitions.getName(actualItemType);
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

    private List<String> squeezeCollection(final Configuration configuration, final TypeProperty property)
    {
        Type itemType = property.getType().getTargetType();
        final TypeSqueezer squeezer = Squeezers.findSqueezer(itemType.getClazz());
        if (squeezer == null)
        {
            throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': no way to convert type '" + itemType.getClazz().getName() + "' to a string");
        }

        @SuppressWarnings({"unchecked"})
        List<Object> items = (List<Object>) getPropertyValue(configuration, property);
        return newArrayList(transform(items, new Function<Object, String>()
        {
            public String apply(Object o)
            {
                try
                {
                    return squeezer.squeeze(o);
                }
                catch (SqueezeException e)
                {
                    throw new PulseRuntimeException("Unable to store property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': " + e.getMessage(), e);
                }
            }
        }));
    }

    private List<String> convertReferenceCollection(Configuration configuration, CompositeType type, TypeProperty property)
    {
        ReferenceType referenceType = (ReferenceType) property.getType().getTargetType();
        List<String> converted = new LinkedList<String>();
        CompositeType referencedType = referenceType.getReferencedType();
        TypeProperty nameProperty = getReferenceNameProperty(type, property, referencedType);

        for (Configuration item: getCollectionItems(configuration, property))
        {
            String referenceName = (String) getPropertyValue(item, nameProperty);
            converted.add(toReference(referenceName));
        }

        return converted;
    }

    private void applyAddable(Element element, Addable addable, String value)
    {
        String attribute = addable.attribute();
        if (StringUtils.stringSet(attribute))
        {
            element.addAttribute(new Attribute(attribute, value));
        }
        else
        {
            element.appendChild(new Text(value));
        }
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

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setTypeDefinitions(TypeDefinitions typeDefinitions)
    {
        this.typeDefinitions = typeDefinitions;
    }
}
