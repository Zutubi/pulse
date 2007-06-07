package com.zutubi.pulse.prototype.config.user;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.TypeWizardState;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.util.logging.Logger;

import java.util.LinkedList;

/**
 *
 */
public class SubscriptionConfigurationWizard extends AbstractTypeWizard
{
    private static final Logger LOG = Logger.getLogger(SubscriptionConfigurationWizard.class);

    private static final TemplateRecord EMPTY_RECORD = new TemplateRecord("empty", null, new MutableRecordImpl());

    private CompositeType subscriptionType;

    public void initialise()
    {
        TemplateRecord templateRecord = EMPTY_RECORD;

        subscriptionType = typeRegistry.getType(SubscriptionConfiguration.class);

        CompositeType conditionType = (CompositeType) subscriptionType.getProperty("condition").getType();

        wizardStates = new LinkedList<TypeWizardState>();
        addWizardStates(wizardStates, subscriptionType, templateRecord);
        addWizardStates(wizardStates, conditionType, (TemplateRecord) templateRecord.get("condition"));

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        MutableRecord record = subscriptionType.createNewRecord();
        record.update(wizardStates.get(0).getRecord());
        record.put("condition", wizardStates.get(2).getRecord());

        successPath = configurationTemplateManager.insertRecord(path, record);
    }

    public Type getType()
    {
        return subscriptionType;
    }
}
