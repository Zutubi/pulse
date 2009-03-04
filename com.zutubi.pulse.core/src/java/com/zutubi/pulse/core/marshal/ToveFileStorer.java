package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.engine.api.Addable;
import static com.zutubi.pulse.core.marshal.ToveFileUtils.*;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.*;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import nu.xom.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
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
        for (TypeProperty property: type.getProperties())
        {
            storeProperty(configuration, type, property, element);
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
                String childName = typeDefinitions.getName(compositeType);
                if (childName == null)
                {
                    childName = property.getName();
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
        if (itemType instanceof ReferenceType)
        {
            ReferenceType referenceType = (ReferenceType) itemType;
            CompositeType referencedType = referenceType.getReferencedType();
            TypeProperty nameProperty = getReferenceNameProperty(type, property, referencedType);

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
