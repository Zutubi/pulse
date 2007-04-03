package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.TypeConversionException;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.validation.*;

/**
 *
 *
 */
public class SaveAction extends ActionSupport
{
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private TypeRegistry typeRegistry;
    private ValidationManager validationManager;

    private String symbolicName;
    private String path;

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws Exception
    {
        if (!TextUtils.stringSet(symbolicName))
        {
            return INPUT;
        }

        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            return ERROR;
        }

        Record record = PrototypeUtils.toRecord(type, ActionContext.getContext().getParameters());
        if (!validate(record))
        {
            return INPUT;
        }

        configurationPersistenceManager.updateRecord(path, record);

        return SUCCESS;
    }

    private ValidationContext createValidationContext(Object subject)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        return new DelegatingValidationContext(new XWorkValidationAdapter(this), textProvider);
    }

    public boolean validate(Record record)
    {
        try
        {
            Type type = typeRegistry.getType(record.getSymbolicName());
            ValidationContext context = createValidationContext(type.getClazz());
            
            Object instance;
            try
            {
                instance = type.instantiate(record);
            }
            catch (TypeConversionException e)
            {
                for (String field : e.getFieldErrors())
                {
                    context.addFieldError(field, e.getFieldError(field));
                }
                return false;
            }

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
        catch (TypeException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
