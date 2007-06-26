package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.wizard.WizardState;

/**
 * A wizard that configures a single record. perhaps including two states if
 * there are multiple extensions to choose from.
 */
public class SingleTypeWizard extends AbstractTypeWizard
{
    private WizardState recordState;
    
    private CompositeType type;

    public void initialise()
    {
        type = (CompositeType) configurationTemplateManager.getType(configPath).getTargetType();
        recordState = addWizardStates(type, templateParentRecord);
        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        successPath = configurationTemplateManager.insertRecord(configPath, recordState.getDataRecord());
    }

    public Type getType()
    {
        return type;
    }
}
