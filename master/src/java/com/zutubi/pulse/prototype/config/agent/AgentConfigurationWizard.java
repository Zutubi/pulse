package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;

/**
 *
 *
 */
public class AgentConfigurationWizard extends AbstractTypeWizard
{
    private CompositeType agentType;

    public void initialise()
    {
        agentType = typeRegistry.getType(AgentConfiguration.class);

        addWizardStates(agentType, templateParentRecord);

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        MutableRecord record = agentType.createNewRecord(false);
        record.update(getStateForType(agentType).getDataRecord());

        configurationTemplateManager.setParentTemplate(record, templateParentRecord.getHandle());
        if(template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }

        successPath = configurationTemplateManager.insertRecord("agent", record);
    }

    public Type getType()
    {
        return agentType;
    }
}

