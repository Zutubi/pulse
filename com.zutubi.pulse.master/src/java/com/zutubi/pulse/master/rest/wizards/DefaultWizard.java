package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;

import java.util.Map;

/**
 * The default wizard implementation presents a single step for the type.
 */
public class DefaultWizard implements ConfigurationWizard
{
    private WizardModelBuilder wizardModelBuilder;
    private ConfigurationTemplateManager configurationTemplateManager;

    @Override
    public WizardModel buildModel(CompositeType type, String parentPath, String baseName, boolean concrete)
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForType("", type, parentPath, baseName, concrete));
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, String parentPath, String baseName, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models) throws TypeException
    {
        MutableRecord record = wizardModelBuilder.buildRecord(templateOwnerPath, type, "", models.get(""));
        Configuration instance = configurationTemplateManager.validate(parentPath, null, record, concrete, false);
        if (!instance.isValid())
        {
            throw new ValidationException(instance, "");
        }

        return record;
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
