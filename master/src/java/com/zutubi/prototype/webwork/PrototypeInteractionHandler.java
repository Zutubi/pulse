package com.zutubi.prototype.webwork;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeConversionException;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.validation.*;

/**
 *
 *
 */
public class PrototypeInteractionHandler
{
    private TypeRegistry typeRegistry;

    private ValidationManager validationManager;

    private ConfigurationPersistenceManager configurationPersistenceManager;

    private RecordManager recordManager;

    public boolean validate(Record subject, ValidationAware validationCallback) throws TypeException
    {
        // The type we validating against.
        Type type = typeRegistry.getType(subject.getSymbolicName());

        // Construct the validation context, wrapping it around the validation callback to that the
        // client is notified of validation errors.
        MessagesTextProvider textProvider = new MessagesTextProvider(type.getClazz());
        ValidationContext context = new DelegatingValidationContext(validationCallback, textProvider);

        // Create an instance of the object represented by the record.  It is during the instantiation that
        // type conversion errors are detected.
        Object instance;
        try
        {
            instance = type.instantiate(subject);
        }
        catch (TypeConversionException e)
        {
            for (String field : e.getFieldErrors())
            {
                context.addFieldError(field, e.getFieldError(field));
            }
            return false;
        }

        // Process the instance via the validation manager.
        try
        {
            validationManager.validate(instance, context);
            return !context.hasErrors();
        }
        catch (ValidationException e)
        {
            context.addActionError(e.getMessage());
            return false;
        }
    }

    public void save(String path, Record record)
    {
        configurationPersistenceManager.saveRecord(path, record);
    }

    public void delete(String path)
    {
        recordManager.delete(path);
    }

    public Object getInstance(String path) throws TypeException
    {
        return configurationPersistenceManager.getInstance(path);
    }

    /**
     * Required resource
     *
     * @param validationManager instance
     */
    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    /**
     * Required resource
     *
     * @param typeRegistry instance
     */
    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Required resource
     *
     * @param configurationPersistenceManager instance
     */
    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    /**
     * Required resource
     *
     * @param recordManager instance
     */
    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
