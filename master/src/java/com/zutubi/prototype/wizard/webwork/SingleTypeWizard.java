package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.wizard.TypeWizardState;

/**
 * A wizard that configures a single record. perhaps including two states if
 * there are multiple extensions to choose from.
 */
public class SingleTypeWizard extends AbstractTypeWizard
{
    private CompositeType type;

    public void initialise()
    {
        type = (CompositeType) configurationTemplateManager.getType(configPath).getTargetType();
        addWizardStates(null, type, templateParentRecord);
    }

    public void doFinish()
    {
        super.doFinish();

        TypeWizardState recordState = getCompletedStateForType(type);
        successPath = configurationTemplateManager.insertRecord(configPath, recordState.getDataRecord());
    }

    public Type getType()
    {
        return type;
    }
}
