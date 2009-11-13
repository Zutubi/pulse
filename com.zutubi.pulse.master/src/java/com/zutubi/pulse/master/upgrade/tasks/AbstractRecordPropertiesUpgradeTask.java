package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.upgrade.UpgradeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * A helper base class for upgrade tasks that manipulate simple properties of
 * records.  Supports:
 * <ul>
 *   <li>Adding new properties with fixed default values.</li>
 *   <li>Deleting existing properties.</li>
 *   <li>Renaming existing properties.</li>
 * </ul>
 * </p>
 * <p>
 * Support for changing existing values and adding properties with values that
 * are not constant requires knowledge of templating - if a simple value in a
 * child is the same as inherited from the parent, it should not be explicitly
 * within the child record.  For this reason such upgrades are not yet
 * supported.
 * </p>
 * <p>
 * Support for extensible types is also not yet available.  The problem in this
 * case is that there is no way to find all records for all derived types as we
 * no longer have the old type information.
 * </p>
 */
public abstract class AbstractRecordPropertiesUpgradeTask extends AbstractUpgradeTask
{
    protected RecordManager recordManager;

    public void execute() throws UpgradeException
    {
        RecordLocator recordLocator = getRecordLocator();
        List<RecordUpgrader> recordUpgraders = getRecordUpgraders();
        wireExternalDependencies(recordLocator, recordUpgraders);

        Map<String, Record> recordsToUpgrade = recordLocator.locate(recordManager);
        for (Map.Entry<String, Record> recordEntry: recordsToUpgrade.entrySet())
        {
            String path = recordEntry.getKey();
            MutableRecord mutableRecord = recordEntry.getValue().copy(false, true);
            for (RecordUpgrader upgrader: recordUpgraders)
            {
                upgrader.upgrade(path, mutableRecord);
            }

            recordManager.update(path, mutableRecord);
        }
    }

    private void wireExternalDependencies(RecordLocator recordLocator, List<RecordUpgrader> recordUpgraders) throws UpgradeException
    {
        // Create the details lazily as it takes some time and will not
        // be necessary for all tasks.
        PersistentScopes scopes = new PersistentScopes(recordManager);
        wireScopes(recordLocator, scopes);
        for (RecordUpgrader upgrader: recordUpgraders)
        {
            wireScopes(upgrader, scopes);
        }
    }

    private void wireScopes(Object o, PersistentScopes persistentScopes) throws UpgradeException
    {
        if (o instanceof PersistentScopesAware)
        {
            ((PersistentScopesAware) o).setPersistentScopes(persistentScopes);
        }
    }

    /**
     * Defines how this task identifies the records it will upgrade.  All
     * records returned by this locator will be upgraded by this task.
     * Locators should generally be obtained from the static methods on the
     * {@link RecordLocators} class.
     *
     * @return the locator used to find the records we will upgrade
     */
    protected abstract RecordLocator getRecordLocator();

    /**
     * Defines how each record is upgraded by this task.  Each record to
     * upgrade will have each of the returned upgraders applied to it in the
     * order returned.  Upgraders should generally be obtained from the static
     * methods on teh {@link RecordUpgraders} class.
     *
     * @return a list of upgraders to apply in order to all records upgraded by
     *         this task
     */
    protected abstract List<RecordUpgrader> getRecordUpgraders();

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
