package com.zutubi.pulse.prototype.config.user;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;

/**
 *
 */
public class SubscriptionConfigurationWizard extends AbstractTypeWizard
{
    private CompositeType subscriptionType;
    private CompositeType conditionType;

    public void initialise()
    {
        subscriptionType = typeRegistry.getType(SubscriptionConfiguration.class);
        conditionType = (CompositeType) subscriptionType.getProperty("condition").getType();

        addWizardStates(subscriptionType, null);
        addWizardStates(conditionType, null);

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        MutableRecord record = subscriptionType.createNewRecord(true);
        record.update(getStateForType(subscriptionType).getDataRecord());
        record.put("condition", getStateForType(conditionType).getDataRecord());

        successPath = configurationTemplateManager.insertRecord(configPath, record);
    }

    public Type getType()
    {
        return subscriptionType;
    }
}
