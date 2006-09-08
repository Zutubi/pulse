package com.zutubi.pulse.form;

import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.validator.Validator;
import com.zutubi.pulse.form.validator.validators.EmailFieldValidator;

import java.util.*;

/**
 * <class-comment/>
 */
public class DefaultFieldTypeRegistry implements FieldTypeRegistry
{
    private Map<String, FieldTypeDefinition> internalRegistry = new HashMap<String, FieldTypeDefinition>();

    private Map<String, FieldTypeDefinition> customRegistry = new HashMap<String, FieldTypeDefinition>();

    private Map<Class, String> typeMappings = new HashMap<Class, String>();

    public void init()
    {
        // setup the built in definitions.
        internalRegistry.put(FieldType.TEXT, new FieldTypeDefinition(
                new LinkedList<Validator>(),
                Squeezers.findSqueezer(String.class))
        );
        internalRegistry.put(FieldType.EMAIL, new FieldTypeDefinition(
                Arrays.asList((Validator)new EmailFieldValidator()),
                Squeezers.findSqueezer(String.class))
        );

        typeMappings.put(String.class, FieldType.TEXT);
    }

    public String getFieldType(Class type)
    {
        return typeMappings.get(type);
    }

    public boolean supportsFieldType(String fieldType)
    {
        return getRegistry(fieldType) != null;
    }

    public void register(String fieldType, TypeSqueezer squeezer, List<Validator> validators)
    {
        throw new UnsupportedOperationException();
    }

    public void unregister(String fieldType)
    {
        throw new UnsupportedOperationException();
    }

    public TypeSqueezer getSqueezer(String fieldType)
    {
        if (!supportsFieldType(fieldType))
        {
            throw new IllegalArgumentException();
        }
        return getRegistry(fieldType).squeezer;
    }

    public List<Validator> getValidators(String fieldType)
    {
        if (!supportsFieldType(fieldType))
        {
            throw new IllegalArgumentException();
        }
        return getRegistry(fieldType).validators;
    }

    protected FieldTypeDefinition getRegistry(String fieldType)
    {
        if (customRegistry.containsKey(fieldType))
        {
            return customRegistry.get(fieldType);
        }
        if (internalRegistry.containsKey(fieldType))
        {
            return internalRegistry.get(fieldType);
        }
        return null;
    }

    private class FieldTypeDefinition
    {
        List<Validator> validators;
        TypeSqueezer squeezer;

        public FieldTypeDefinition(List<Validator> validators, TypeSqueezer squeezer)
        {
            this.validators = validators;
            this.squeezer = squeezer;
        }
    }
}
