package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;

import java.util.Map;

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
    public MutableRecord buildRecord(CompositeType type, String parentPath, String baseName, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models) throws TypeException
    {
        UserConfigurationCreator creator = (UserConfigurationCreator) wizardModelBuilder.buildInstance(null, UserConfigurationCreator.class, "", models.get(""));
        wizardModelBuilder.validateInstance(creator, parentPath, baseName, concrete);
        if (!creator.isValid())
        {
            throw new ValidationException(creator, "");
        }

        UserConfiguration userConfiguration = creator.create();
        return type.unstantiate(userConfiguration, templateOwnerPath);
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }
}