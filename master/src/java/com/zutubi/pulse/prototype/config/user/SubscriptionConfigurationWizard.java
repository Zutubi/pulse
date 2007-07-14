package com.zutubi.pulse.prototype.config.user;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.wizard.TypeWizardState;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.List;

/**
 *
 */
public class SubscriptionConfigurationWizard extends AbstractTypeWizard
{
    private CompositeType subscriptionType;
    private CompositeType projectSubscriptionType;
    private CompositeType conditionType;

    public void initialise()
    {
        subscriptionType = typeRegistry.getType(SubscriptionConfiguration.class);
        projectSubscriptionType = typeRegistry.getType(ProjectSubscriptionConfiguration.class);
        conditionType = typeRegistry.getType(SubscriptionConditionConfiguration.class);

        List<AbstractChainableState> states = addWizardStates(null, subscriptionType, null);
        addWizardStates(CollectionUtils.filter(states, new Predicate<AbstractChainableState>()
        {
            public boolean satisfied(AbstractChainableState state)
            {
                return state.getType().equals(projectSubscriptionType);
            }
        }), conditionType, null);
    }

    public void doFinish()
    {
        super.doFinish();

        MutableRecord record = getCompletedStateForType(subscriptionType).getDataRecord();
        TypeWizardState conditionState = getCompletedStateForType(conditionType);
        if (conditionState != null)
        {
            record.put("condition", conditionState.getDataRecord());
        }

        successPath = configurationTemplateManager.insertRecord(configPath, record);
    }

    public Type getType()
    {
        return subscriptionType;
    }
}
