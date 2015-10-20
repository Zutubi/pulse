package com.zutubi.pulse.master.tove.wizard;

import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.MutableRecord;

/**
 * A wizard that configures a single record. perhaps including two states if
 * there are multiple extensions to choose from.
 */
public class SingleTypeWizard extends AbstractTypeWizard
{
    private CompositeType type;

    public void initialise()
    {
        type = (CompositeType) configurationTemplateManager.getType(insertPath).getTargetType();
        addWizardStates(null, parentPath, type, templateParentRecord);
    }

    public void doFinish()
    {
        super.doFinish();

        TypeWizardState recordState = getCompletedStateForType(type);
        MutableRecord record = recordState.getDataRecord();
        successPath = configurationTemplateManager.insertRecord(insertPath, record);
    }

    public Type getType()
    {
        return type;
    }
}
