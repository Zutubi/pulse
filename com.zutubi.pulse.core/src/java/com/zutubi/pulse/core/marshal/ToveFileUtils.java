package com.zutubi.pulse.core.marshal;

import com.google.common.base.Function;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Referenceable;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.tove.type.*;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;

import java.util.List;

/**
 * Utility methods for helping with marshalling configuration instances via
 * tove files.
 */
public class ToveFileUtils
{
    /**
     * Returns the property that is used for the name of a referenceable
     * instance.  By convention referenceable instances implement
     * {@link com.zutubi.tove.config.api.NamedConfiguration} and use "name" as
     * the reference property, but this is not required.
     *
     * @param referencingType     type containing the property that references
     *                            something
     * @param referencingProperty the property that references something
     * @param referencedType      the type being referenced
     * @return the property of referencedType that yields the reference name
     *
     * @throws PulseRuntimeException if the referenced type does not have a
     *         valid name property specified
     */
    public static TypeProperty getReferenceNameProperty(CompositeType referencingType, TypeProperty referencingProperty, CompositeType referencedType)
    {
        Referenceable referenceable = referencedType.getAnnotation(Referenceable.class, true);
        if (referenceable == null)
        {
            throw new PulseRuntimeException("Unable to convert property '" + referencingProperty.getName() + "' of class '" + referencingType.getClazz().getName() + "': the property is marked @Reference, but the referenced type '" + referencedType.getClazz().getName() + "' is not @Referenceable");
        }

        TypeProperty nameProperty = referencedType.getProperty(referenceable.nameProperty());
        if (nameProperty == null)
        {
            throw new PulseRuntimeException("Unable to convert property '" + referencingProperty.getName() + "' of class '" + referencingType.getClazz().getName() + "': the referenced type '" + referencedType.getClazz().getName() + "' has an invalid nameProperty '" + referenceable.nameProperty() + "' specified in its @Referenceable annotation");
        }

        return nameProperty;
    }

    /**
     * Converts a reference name into a reference (i.e. wraps it in the $()
     * syntax, or ${} if the name includes a reserved character).
     *
     * @param referenceName the name being referenced
     * @return string form of the reference
     */
    public static String toReference(String referenceName)
    {
        for (int i = 0; i < referenceName.length(); i++)
        {
            if (VariableResolver.EXTENDED_SPECIAL_CHARS.contains(referenceName.charAt(i)))
            {
                return "${" + referenceName + "}";
            }
        }

        return "$(" + referenceName + ")";
    }

    /**
     * Retrieves the value of the given property from the given instance.
     *
     * @param configuration the instance to get the property value from
     * @param property      the property to get the value os
     * @return the property value
     *
     * @throws PulseRuntimeException on an introspection error
     */
    public static Object getPropertyValue(Configuration configuration, TypeProperty property)
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

    /**
     * Converts a type property name to the local name to use for a
     * corresponding attribute or element in the Pulse file.  Although
     * the file can contain camel case we use hyphen-separation by
     * convention.
     *
     * @param name the property name to convert (camel case)
     * @return the converted name (words separated by hypens)
     */
    public static String convertPropertyNameToLocalName(String name)
    {
        StringBuilder result = new StringBuilder(name.length() + 5);
        boolean previousLower = false;
        for (int i = 0; i < name.length(); i++)
        {
            char c = name.charAt(i);
            boolean isUpper = Character.isUpperCase(c);
            if (i != 0 && isUpper && (previousLower || isNextLower(name, i)))
            {
                result.append('-');
            }

            previousLower = !isUpper;
            result.append(Character.toLowerCase(c));
        }

        return result.toString();
    }

    private static boolean isNextLower(String name, int i)
    {
        return i < name.length() - 1 && Character.isLowerCase(name.charAt(i + 1));
    }

    /**
     * Indicates if the given property can be stored as a simple attribute in a
     * tove file.  This applies to properties that can be converted to a string
     * such as primitives and simple lists.
     *
     * @param property the property to test
     * @return true iff the property can be stored as an attribute
     */
    public static boolean convertsToAttribute(TypeProperty property)
    {
        Type propertyType = property.getType();
        if (propertyType instanceof SimpleType)
        {
            return true;
        }
        else if (propertyType instanceof ListType)
        {
            return property.getAnnotation(Addable.class) == null && propertyType.getTargetType() instanceof SimpleType;
        }

        return false;
    }

    /**
     * Converts the value of a property from the given instance to a simple
     * string form to use as an attribute.  This implementation assumes that
     * the property converts to an attribute (i.e. it does not check).
     *
     * @param configuration the instance to retrieve the property value from
     * @param type          the type of the instance
     * @param property      the property to retrieve the value from
     * @return the property value in simple string form suitable for use as
     *         an attribute value
     */
    public static String convertAttribute(Configuration configuration, CompositeType type, TypeProperty property)
    {
        Type propertyType = property.getType();
        String attributeValue;
        if (propertyType instanceof PrimitiveType || propertyType instanceof EnumType)
        {
            attributeValue = convertPrimitiveProperty(configuration, property);
        }
        else if (propertyType instanceof ReferenceType)
        {
            attributeValue = convertReferenceProperty(configuration, type, property);
        }
        else
        {
            attributeValue = convertPrimitiveListProperty(configuration, property);
        }

        return attributeValue;
    }

    private static String convertPrimitiveProperty(Configuration configuration, TypeProperty simpleProperty)
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

    private static String convertReferenceProperty(Configuration configuration, CompositeType type, TypeProperty property)
    {
        ReferenceType propertyType = (ReferenceType) property.getType();
        CompositeType referencedType = propertyType.getReferencedType();
        TypeProperty nameProperty = getReferenceNameProperty(type, property, referencedType);

        Configuration referencedInstance = (Configuration) getPropertyValue(configuration, property);
        if (referencedInstance == null)
        {
            return "";
        }
        else
        {
            return toReference((String) getPropertyValue(referencedInstance, nameProperty));
        }
    }

    private static String convertPrimitiveListProperty(final Configuration configuration, final TypeProperty property)
    {
        ListType propertyType = (ListType) property.getType();
        Type itemType = propertyType.getTargetType();

        final TypeSqueezer squeezer = Squeezers.findSqueezer(itemType.getClazz());
        if (squeezer == null)
        {
            throw new PulseRuntimeException("Unable to convert items of simple list property '" + property.getName() + "' of class '" + configuration.getClass().getName() + "': no squeezer for class '" + itemType.getClazz().getName() + "'");
        }

        @SuppressWarnings("unchecked")
        List<Object> propertyValue = (List<Object>) getPropertyValue(configuration, property);
        if (propertyValue == null)
        {
            return "";
        }
        else
        {
            List<String> list = CollectionUtils.map(propertyValue, new Function<Object, String>()
            {
                public String apply(Object o)
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
    }
}
