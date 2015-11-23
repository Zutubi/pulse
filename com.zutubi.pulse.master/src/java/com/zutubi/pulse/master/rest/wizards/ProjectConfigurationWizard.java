package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.pulse.master.tove.handler.FormContext;
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

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context) throws TypeException
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForType("", type, context));
        model.appendStep(wizardModelBuilder.buildStepForType("scm", typeRegistry.getType(ScmConfiguration.class), context));
        model.appendStep(wizardModelBuilder.buildStepForType("type", typeRegistry.getType(TypeConfiguration.class), context));
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, String parentPath, String baseName, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models) throws TypeException
    {
        MutableRecord projectRecord = wizardModelBuilder.buildAndValidateRecord(type, parentPath, templateOwnerPath, concrete, models, "");
        projectRecord.put("scm", wizardModelBuilder.buildAndValidateRecord(typeRegistry.getType(ScmConfiguration.class), parentPath, templateOwnerPath, concrete, models, "scm"));
        projectRecord.put("type", wizardModelBuilder.buildAndValidateRecord(typeRegistry.getType(TypeConfiguration.class), parentPath, templateOwnerPath, concrete, models, "type"));
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
}
