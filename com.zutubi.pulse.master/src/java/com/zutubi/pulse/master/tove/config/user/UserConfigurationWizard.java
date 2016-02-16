package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.rest.wizards.UserConfigurationCreator;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.model.WizardModel;
import com.zutubi.tove.ui.wizards.ConfigurationWizard;
import com.zutubi.tove.ui.wizards.WizardContext;
import com.zutubi.tove.ui.wizards.WizardModelBuilder;

/**
 * Customised interface for creating users that handles password confirmation and creation of a
 * default contact point.
 */
public class UserConfigurationWizard implements ConfigurationWizard
{
    private WizardModelBuilder wizardModelBuilder;

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context) throws TypeException
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForClass("", UserConfigurationCreator.class, context));
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, WizardContext wizardContext) throws TypeException
    {
        CompositeType actualType = wizardModelBuilder.typeCheck(wizardContext.getModels(), "", UserConfigurationCreator.class);
        MutableRecord record = wizardModelBuilder.buildRecord(wizardContext.getTemplateParentRecord(), wizardContext.getTemplateOwnerPath(), actualType, wizardContext.getModels().get(""));
        UserConfigurationCreator creator = (UserConfigurationCreator) wizardModelBuilder.buildAndValidateTransientInstance(actualType, wizardContext.getParentPath(), wizardContext.getBaseName(), record);
        UserConfiguration userConfiguration = creator.create();
        return type.unstantiate(userConfiguration, wizardContext.getTemplateOwnerPath());
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }
}