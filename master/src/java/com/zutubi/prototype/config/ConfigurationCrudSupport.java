package com.zutubi.prototype.config;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.prototype.type.TypeConversionSupport;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.XWorkValidationAdapter;
import com.zutubi.validation.ValidationException;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ValidationAware;

import java.util.Map;

/**
 *
 *
 */
public class ConfigurationCrudSupport
{
    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ValidationManager validationManager;

    public ConfigurationCrudSupport()
    {
        ComponentContext.autowire(this);
    }

    public boolean save(String symbolicName, String path, Map parameters, ValidationAware action) throws TypeException
    {
        try
        {
            Object instance = configurationPersistenceManager.getInstance(path);
            if (instance == null)
            {
                Type type = typeRegistry.getType(symbolicName);
                instance = type.getClazz().newInstance();
            }

            if (!applyAndValidate(ActionContext.getContext().getParameters(), instance, action))
            {
                return false;
            }

//            configurationPersistenceManager.setInstance(path, instance);
            throw new RuntimeException("need to implement store.");
        }
        catch (Exception e)
        {
            throw new TypeException(e);
        }
    }

    private ValidationContext createValidationContext(Object subject, ValidationAware action)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        return new DelegatingValidationContext(new XWorkValidationAdapter(action), textProvider);
    }

    public boolean applyAndValidate(Map parameters, Object instance, ValidationAware action) throws TypeException
    {
        apply(parameters, instance);

        return validate(instance, action);
    }

    public void apply(Map parameters, Object instance) throws TypeException
    {
        TypeConversionSupport conversionSupport = new TypeConversionSupport();
        conversionSupport.setTypeRegistry(typeRegistry);
        conversionSupport.applyMapTo(parameters, instance);
    }

    public boolean validate(Object instance, ValidationAware action)
    {
        ValidationContext context = createValidationContext(instance, action);

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

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }
}
