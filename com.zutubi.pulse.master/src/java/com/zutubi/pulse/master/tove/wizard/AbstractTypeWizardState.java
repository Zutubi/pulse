package com.zutubi.pulse.master.tove.wizard;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;
import com.zutubi.pulse.master.tove.model.FormDescriptor;
import com.zutubi.pulse.master.tove.model.FormDescriptorFactory;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeProperty;

import java.util.*;

/**
 */
public abstract class AbstractTypeWizardState extends AbstractChainableState
{
    protected AbstractTypeWizard wizard;
    private String id;
    private String parentPath;
    private CompositeType baseType;
    private Set<String> ignoredFields = new HashSet<String>();

    protected ConfigurationTemplateManager configurationTemplateManager;
    protected MasterConfigurationRegistry configurationRegistry;

    public AbstractTypeWizardState(AbstractTypeWizard wizard, String id, String parentPath, CompositeType baseType)
    {
        this.wizard = wizard;
        this.id = id;
        this.parentPath = parentPath;
        this.baseType = baseType;
    }

    public String getId()
    {
        return id;
    }

    protected CompositeType getBaseType()
    {
        return baseType;
    }

    public String getName()
    {
        return "configure";
    }

    @SuppressWarnings({"unchecked"})
    public void updateRecord(Map parameters)
    {
        getDataRecord().update(ToveUtils.toRecord(getType(), parameters), false, true);
    }

    public Messages getMessages()
    {
        return Messages.getInstance(getType().getClazz());
    }

    public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
    {
        CompositeType type = getType();
        FormDescriptor descriptor = formDescriptorFactory.createDescriptor(path, null, type, !wizard.isTemplate(), name);
        Iterator<FieldDescriptor> fieldIt = descriptor.getFieldDescriptors().iterator();
        while(fieldIt.hasNext())
        {
            String fieldName = fieldIt.next().getName();
            if(!includesField(type, fieldName))
            {
                ignoredFields.add(fieldName);
                fieldIt.remove();
            }
        }

        return descriptor;
    }

    public boolean includesField(CompositeType type, String name)
    {
        TypeProperty property = type.getProperty(name);
        return !(property != null && property.getAnnotation(com.zutubi.tove.annotations.Wizard.Ignore.class) != null);
    }

    public boolean validate(ValidationAware validationCallback)
    {
        try
        {
            Configuration configuration = configurationTemplateManager.validate(parentPath, null, getRenderRecord(), !wizard.isTemplate(), false, ignoredFields);
            ToveUtils.mapErrors(configuration, validationCallback, null);
            return configuration.isValid();
        }
        catch (TypeException e)
        {
            validationCallback.addActionError(e.getMessage());
            return false;
        }
    }

    public boolean hasFields()
    {
        CompositeType type = getType();
        List<String> simpleProperties = type.getSimplePropertyNames();
        for (String property: simpleProperties)
        {
            if (includesField(type, property))
            {
                return true;
            }
        }
        
        return false;
    }

    public boolean hasConfigurationCheck()
    {
        return configurationRegistry.getConfigurationCheckType(getType()) != null;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationRegistry(MasterConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
