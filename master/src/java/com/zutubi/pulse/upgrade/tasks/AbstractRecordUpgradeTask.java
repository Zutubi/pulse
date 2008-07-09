package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.prototype.type.record.DefaultRecordSerialiser;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.upgrade.UpgradeException;

import java.io.File;

/**
 *
 *
 */
public abstract class AbstractRecordUpgradeTask extends AbstractTraverseRecordUpgradeTask  implements ConfigurationAware
{
    private File recordRoot;

    public void execute() throws UpgradeException
    {
        // the question is what resources can we rely on to be available at this stage of the setup.
        DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(recordRoot);

        MutableRecord baseRecord = serialiser.deserialise("");

        traverse(baseRecord);

        serialiser.serialise("", baseRecord,  true);
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
