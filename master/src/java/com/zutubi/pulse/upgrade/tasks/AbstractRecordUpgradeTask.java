package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.prototype.type.record.DefaultRecordSerialiser;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.upgrade.UpgradeException;

import java.io.File;

/**
 *
 *
 */
public abstract class AbstractRecordUpgradeTask extends AbstractUpgradeTask  implements ConfigurationAware
{
    private File recordRoot;

    public String getName()
    {
        return null;
    }

    public String getDescription()
    {
        return null;
    }

    public void execute() throws UpgradeException
    {
        // the question is what resources can we rely on to be available at this stage of the setup.
        DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(recordRoot);

        MutableRecord record = serialiser.deserialise("");
        traverse(record);
        serialiser.serialise("", record,  true);
    }

    private void traverse(MutableRecord record)
    {
        doUpgrade(record);

        // traverse nested children in a depth first traversal.
        for (String key : record.keySet())
        {
            Object value = record.get(key);
            if (value instanceof MutableRecord)
            {
                MutableRecord nestedRecord = (MutableRecord) value;
                traverse(nestedRecord);
            }
        }
    }

    public abstract void doUpgrade(MutableRecord record);

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        setRecordRoot(configurationManager.getData().getRecordRoot());
    }

    public void setRecordRoot(File file)
    {
        this.recordRoot = file;
    }
}
