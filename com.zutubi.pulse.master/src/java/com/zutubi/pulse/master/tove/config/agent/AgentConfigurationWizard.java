package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.MutableRecord;

/**
 * A wizard tailored to creating agents (or agent templates).
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
        record.update(getCompletedStateForType(agentType).getDataRecord(), false, true);

        configurationTemplateManager.setParentTemplate(record, templateParentRecord.getHandle());
        if(template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }

        successPath = configurationTemplateManager.insertRecord(MasterConfigurationRegistry.AGENTS_SCOPE, record);
    }

    public Type getType()
    {
        return agentType;
    }
}

