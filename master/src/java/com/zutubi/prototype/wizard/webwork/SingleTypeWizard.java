package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.RecordManager;
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
        try
        {
            Type type = configurationPersistenceManager.getType(path);
            if (type instanceof CollectionType)
            {
                type = ((CollectionType)type).getCollectionType();
            }

            Record record;
            Record existingRecord = configurationPersistenceManager.getRecord(path);
            if (existingRecord != null)
            {
                record = existingRecord.clone();
            }
            else
            {
                record = new MutableRecord();
                record.setSymbolicName(type.getSymbolicName());
            }

            wizardStates = new LinkedList<WizardState>();
            addWizardStates(wizardStates, type, record);

            currentState = wizardStates.getFirst();
        }
        catch (CloneNotSupportedException e)
        {
            // not going to happen.
        }
    }

    public void doFinish()
    {
        recordManager.store(path, wizardStates.getLast().getRecord());
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
