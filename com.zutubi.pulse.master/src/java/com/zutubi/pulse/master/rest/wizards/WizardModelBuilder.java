package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.ConfigModelBuilder;
import com.zutubi.pulse.master.rest.Utils;
import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.CompositeTypeModel;
import com.zutubi.pulse.master.rest.model.TypedWizardStepModel;
import com.zutubi.pulse.master.rest.model.WizardTypeModel;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.SimpleInstantiator;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.i18n.MessagesTextProvider;

import java.util.Collections;
import java.util.List;

/**
 * A service that helps create and process wizard steps for types.
 */
public class WizardModelBuilder
{
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private ConfigModelBuilder configModelBuilder;
    private TypeRegistry typeRegistry;
    private ValidationManager validationManager;

    public TypedWizardStepModel buildStepForType(CompositeType type, String parentPath, String baseName, boolean concrete)
    {
        List<CompositeType> types;
        if (type.isExtendable())
        {
            types = type.getExtensions();
        }
        else
        {
            types = Collections.singletonList(type);
        }

        Messages messages = Messages.getInstance(type.getClazz());
        TypedWizardStepModel step = new TypedWizardStepModel("", messages.format("label"));
        for (CompositeType stepType : types)
        {
            messages = Messages.getInstance(stepType.getClazz());
            String labelKey = messages.isKeyDefined("wizard.label") ? "wizard.label" : "label";
            step.addType(new WizardTypeModel(configModelBuilder.buildCompositeTypeModel(parentPath, baseName, stepType, concrete), messages.format(labelKey)));
        }
        return step;
    }

    public MutableRecord buildRecord(String templateOwnerPath, CompositeType expectedType, String key, CompositeModel model) throws TypeException
    {
        if (model == null)
        {
            throw new IllegalArgumentException("A model with key '" + key + "' is required");
        }

        CompositeType actualType = null;
        CompositeType postedType = null;
        CompositeTypeModel typeModel = model.getType();

        if (expectedType.isExtendable())
        {
            if (expectedType.getExtensions().size() == 1)
            {
                actualType = expectedType.getExtensions().get(0);
            }
        }
        else
        {
            actualType = expectedType;
        }

        if (typeModel != null && typeModel.getSymbolicName() != null)
        {
            postedType = typeRegistry.getType(typeModel.getSymbolicName());
            if (postedType == null)
            {
                throw new IllegalArgumentException("Model for key '" + key + "' has type with unknown symbolic name '" + typeModel.getSymbolicName() + "'");
            }
        }

        if (actualType == null)
        {
            if (postedType == null)
            {
                throw new IllegalArgumentException("Model for key '" + key + "' requires a type with symbolic name.");
            }

            actualType = postedType;
        }

        if (!expectedType.isAssignableFrom(actualType))
        {
            throw new IllegalArgumentException("Model for key '" + key + "' has incompatible type '" + actualType.getSymbolicName() + "'.");
        }

        return Utils.convertProperties(actualType, templateOwnerPath, model.getProperties());
    }

    public Configuration buildInstance(String templateOwnerPath, CompositeType expectedType, String key, CompositeModel model) throws TypeException
    {
        MutableRecord record = buildRecord(templateOwnerPath, expectedType, key, model);
        SimpleInstantiator instantiator = new SimpleInstantiator(templateOwnerPath, configurationReferenceManager, configurationTemplateManager);
        return (Configuration) instantiator.instantiate(expectedType, record);
    }

    public void validateInstance(Configuration instance, String parentPath, String baseName, boolean concrete)
    {
        ConfigurationValidationContext validationContext = new ConfigurationValidationContext(instance, new MessagesTextProvider(instance), parentPath, baseName, !concrete, false, configurationTemplateManager);
        try
        {
            validationManager.validate(instance, validationContext);
        }
        catch (com.zutubi.validation.ValidationException e)
        {
            instance.addInstanceError(e.getMessage());
        }
        catch (Throwable e)
        {
            String message = "Unexpected error during validation: " + e.getClass().getName();
            if (e.getMessage() != null)
            {
                message += ": " + e.getMessage();
            }

            instance.addInstanceError(message);
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setConfigModelBuilder(ConfigModelBuilder configModelBuilder)
    {
        this.configModelBuilder = configModelBuilder;
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
