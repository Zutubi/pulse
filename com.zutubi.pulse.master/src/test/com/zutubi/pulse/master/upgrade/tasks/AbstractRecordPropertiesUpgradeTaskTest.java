package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.upgrade.UpgradeException;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class AbstractRecordPropertiesUpgradeTaskTest extends PulseTestCase
{
    private static final String PATH = "test";
    private static final String SYMBOLIC_NAME = "symname";

    private RecordManager recordManager;
    private TransactionManager transactionManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        recordManager = mock(RecordManager.class);
        transactionManager = new TransactionManager();
    }

    public void testExecute() throws UpgradeException
    {
        RecordLocator locator = mock(RecordLocator.class);
        Map<String, Record> records = new HashMap<String, Record>();
        MutableRecordImpl record = new MutableRecordImpl();
        // Give it a symbolic name for the equals test in the verification.
        record.setSymbolicName(SYMBOLIC_NAME);
        records.put(PATH, record);
        doReturn(records).when(locator).locate(recordManager);

        RecordUpgrader upgrader1 = mock(RecordUpgrader.class);
        RecordUpgrader upgrader2 = mock(RecordUpgrader.class);

        TrivialRecordPropertiesUpgradeTask task = new TrivialRecordPropertiesUpgradeTask(locator, upgrader1, upgrader2);
        task.setRecordManager(recordManager);
        task.setPulseTransactionManager(transactionManager);
        task.execute();

        // Should only locate once, and should pass the records to the
        // upgraders, finally should update the record.
        verify(locator, times(1)).locate(recordManager);
        verify(upgrader1, times(1)).upgrade(PATH, record);
        verify(upgrader2, times(1)).upgrade(PATH, record);
        MutableRecordImpl verifyRecord = new MutableRecordImpl();
        verifyRecord.setSymbolicName(SYMBOLIC_NAME);
        verify(recordManager, times(1)).update(PATH, verifyRecord);
    }

    private static class TrivialRecordPropertiesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
    {
        private RecordLocator recordLocator;
        private List<RecordUpgrader> recordUpgraders;

        public TrivialRecordPropertiesUpgradeTask(RecordLocator recordLocator, RecordUpgrader... recordUpgraders)
        {
            this.recordLocator = recordLocator;
            this.recordUpgraders = Arrays.asList(recordUpgraders);
        }

        protected RecordLocator getRecordLocator()
        {
            return recordLocator;
        }

        protected List<? extends RecordUpgrader> getRecordUpgraders()
        {
            return recordUpgraders;
        }

        public String getName()
        {
            return PATH;
        }

        public String getDescription()
        {
            return PATH;
        }

        public boolean haltOnFailure()
        {
            return true;
        }
    }
}
