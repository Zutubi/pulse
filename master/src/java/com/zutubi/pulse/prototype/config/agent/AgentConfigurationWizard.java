package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.prototype.config.ConfigurationRegistry;
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
        addWizardStates(null, parentPath, agentType, templateParentRecord);
    }

    public void doFinish()
    {
        super.doFinish();

        MutableRecord record = agentType.createNewRecord(false);
        record.update(getCompletedStateForType(agentType).getDataRecord());

        configurationTemplateManager.setParentTemplate(record, templateParentRecord.getHandle());
        if(template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }

        successPath = configurationTemplateManager.insertRecord(ConfigurationRegistry.AGENTS_SCOPE, record);
    }

    public Type getType()
    {
        return agentType;
    }
}

