package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;

/**
 * The default wizard implementation presents a single step for the type.
 */
public class DefaultWizard implements ConfigurationWizard
{
    private WizardModelBuilder wizardModelBuilder;

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context)
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForType("", type, context));
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, WizardContext wizardContext) throws TypeException
    {
        return wizardModelBuilder.buildAndValidateRecord(type, "", wizardContext);
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }
}
