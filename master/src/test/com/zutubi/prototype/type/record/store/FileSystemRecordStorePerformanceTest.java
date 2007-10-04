package com.zutubi.prototype.type.record.store;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.UserTransaction;

import java.util.Random;
import java.io.File;

/**
 *
 *
 */
public class FileSystemRecordStorePerformanceTest extends PulseTestCase
{

    private FileSystemRecordStore recordStore;

    private TransactionManager transactionManager;

    private UserTransaction transaction;

    private File persistentDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        persistentDir = FileSystemUtils.createTempDir();

        transactionManager = new TransactionManager();
        transaction = new UserTransaction(transactionManager);

        recordStore = new FileSystemRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.setPersistenceDir(persistentDir);
        recordStore.init();
    }

    protected void tearDown() throws Exception
    {
        transactionManager = null;

        removeDirectory(persistentDir);

        super.tearDown();
    }

    public void testPerformanceOfLargeRecordSetsOutsideTransaction()
    {
        failAfterXTime(1000, new Runnable()
        {
            public void run()
            {
                for (int i = 0; i < 50; i++)
                {
                    recordStore.insert("path_" + i, createRandomRecord());
                }
            }
        });
    }

    public void testPerformanceOfLargeRecordSetsInsideTransaction()
    {
        failAfterXTime(1000, new Runnable()
        {
            public void run()
            {
                transaction.begin();
                for (int i = 0; i < 50; i++)
                {
                    recordStore.insert("path_" + i, createRandomRecord());
                }
                transaction.commit();
            }
        });
    }

    private void failAfterXTime(long timeout, Runnable r)
    {
        long startTime = System.currentTimeMillis();
        r.run();
        long endTime = System.currentTimeMillis();

        long runtime = endTime - startTime;
        if (timeout < runtime)
        {
            fail("Execution time of process has exceeded the allowed " + timeout + " milliseconds. Actual runtime: " + runtime);    
        }
    }

    private Record createRandomRecord()
    {
        Random rand = new Random(System.currentTimeMillis());

        MutableRecordImpl random = new MutableRecordImpl();
        for (int i = 0; i < rand.nextInt(10); i++)
        {
            random.put("key" + i, "value");
        }
        for (int i = 0; i < rand.nextInt(6); i++)
        {
            random.put("nested" + i, random.copy(true));
        }
        return random;
    }
}
