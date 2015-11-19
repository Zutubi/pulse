package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;

import java.util.Map;

/**
 * Custom wizard for creating projects.  Adds the essentials (SCM, type) and optionally defaults
 * like triggers.
 */
public class ProjectConfigurationWizard implements ConfigurationWizard
{
    private WizardModelBuilder wizardModelBuilder;
    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;

    @Override
    public WizardModel buildModel(CompositeType type, String parentPath, String baseName, boolean concrete) throws TypeException
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForType("", type, parentPath, baseName, concrete));
        // FIXME kendo these paths are dodgy, maybe paths need to be removed.
        model.appendStep(wizardModelBuilder.buildStepForType("scm", typeRegistry.getType(ScmConfiguration.class), parentPath, baseName, concrete));
        model.appendStep(wizardModelBuilder.buildStepForType("type", typeRegistry.getType(TypeConfiguration.class), parentPath, baseName, concrete));
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, String parentPath, String baseName, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models) throws TypeException
    {
        MutableRecord projectRecord = buildForKey(type, parentPath, templateOwnerPath, concrete, models, "");
        projectRecord.put("scm", buildForKey(typeRegistry.getType(ScmConfiguration.class), parentPath, templateOwnerPath, concrete, models, "scm"));
        projectRecord.put("type", buildForKey(typeRegistry.getType(TypeConfiguration.class), parentPath, templateOwnerPath, concrete, models, "type"));
        return projectRecord;
    }

    private MutableRecord buildForKey(CompositeType type, String parentPath, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models, String key) throws TypeException
    {
        MutableRecord projectRecord = wizardModelBuilder.buildRecord(templateOwnerPath, type, key, models.get(key));
        Configuration instance = configurationTemplateManager.validate(parentPath, null, projectRecord, concrete, false);
        if (!instance.isValid())
        {
            throw new ValidationException(instance, key);
        }
        return projectRecord;
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
