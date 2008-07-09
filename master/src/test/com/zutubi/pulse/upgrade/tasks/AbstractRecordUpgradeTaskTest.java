package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.DefaultRecordSerialiser;
import com.zutubi.prototype.type.record.RecordQueries;
import com.zutubi.prototype.type.record.store.InMemoryRecordStore;
import com.zutubi.prototype.transaction.TransactionManager;

import java.io.File;

/**
 *
 *
 */
public class AbstractRecordUpgradeTaskTest extends PulseTestCase
{
    private File tmpDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testFullTraversal() throws UpgradeException
    {
        File recordRoot = new File(tmpDir, "root");

        // set up sample data.
        InMemoryRecordStore store = new InMemoryRecordStore();
        store.setTransactionManager(new TransactionManager());
        store.insert("path", new MutableRecordImpl());
        store.insert("path/to", new MutableRecordImpl());
        store.insert("path/to/record", new MutableRecordImpl());

        DefaultRecordSerialiser serialiser = new DefaultRecordSerialiser(recordRoot);
        serialiser.serialise("", store.exportRecords(), true);

        MarkEachRecordUpgradeTask task = new MarkEachRecordUpgradeTask();
        task.setRecordRoot(recordRoot);
        task.execute();

        store.importRecords(serialiser.deserialise(""));

        RecordQueries queries = RecordQueries.getQueries(store);
        assertTrue(queries.select("path").containsKey("visited"));
        assertTrue(queries.select("path/to").containsKey("visited"));
        assertTrue(queries.select("path/to/record").containsKey("visited"));
    }

    private class MarkEachRecordUpgradeTask extends AbstractRecordUpgradeTask
    {
        public String getName()
        {
            return "test";
        }

        public String getDescription()
        {
            return "test";
        }

        public boolean haltOnFailure()
        {
            return false;
        }

        public void doUpgrade(MutableRecord record)
        {
            record.put("visited", "true");
        }
    }
}
