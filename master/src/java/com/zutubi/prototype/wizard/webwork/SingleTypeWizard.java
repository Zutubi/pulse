package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.WizardState;

import java.util.LinkedList;

/**
 *
 *
 */
public class SingleTypeWizard extends AbstractTypeWizard
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private RecordManager recordManager;

    private String path;

    public SingleTypeWizard(String path)
    {
        this.path = path;
    }

    public void initialise()
    {
        Type type = configurationPersistenceManager.getType(path);
        if (type instanceof CollectionType)
        {
            type = ((CollectionType) type).getCollectionType();
        }

        TemplateRecord templateRecord = new TemplateRecord("owner", null, new MutableRecord());

        wizardStates = new LinkedList<WizardState>();
        addWizardStates(wizardStates, type, templateRecord);

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
//        recordManager.insert(path, wizardStates.getLast().getRecord());
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
