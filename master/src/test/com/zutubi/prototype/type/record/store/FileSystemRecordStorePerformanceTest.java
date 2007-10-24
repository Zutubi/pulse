package com.zutubi.prototype.type.record.store;

import com.zutubi.prototype.transaction.TransactionManager;
import com.zutubi.prototype.transaction.UserTransaction;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.Random;

/**
 *
 *
 */
public class FileSystemRecordStorePerformanceTest extends PulseTestCase
{

    private RecordStore recordStore;

    private TransactionManager transactionManager;

    private UserTransaction transaction;

    private File persistentDirectory;

    protected void setUp() throws Exception
    {
        super.setUp();

        persistentDirectory = FileSystemUtils.createTempDir();

        transactionManager = new TransactionManager();
        transaction = new UserTransaction(transactionManager);

        FileSystemRecordStore fileSystemRecordStore = new FileSystemRecordStore();
        fileSystemRecordStore.setTransactionManager(transactionManager);
        fileSystemRecordStore.setPersistenceDirectory(persistentDirectory);
        fileSystemRecordStore.init();

        recordStore = fileSystemRecordStore;
    }

    protected void tearDown() throws Exception
    {
        transactionManager = null;

        removeDirectory(persistentDirectory);

        super.tearDown();
    }

    public void testPerformanceOfLargeRecordSetsOutsideTransaction()
    {
        failAfterXTime(2500, new Runnable()
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
        failAfterXTime(2500, new Runnable()
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

/*
    public void testIt() throws Exception
    {
        FSRecordStore recordStore = new FSRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.setPersistenceDirectory(persistentDirectory);
        recordStore.setCompactionInterval(10);
        recordStore.initAndStartAutoCompaction();

        for (int i = 0; i < 1000; i++)
        {
            String s = "path_" + i;
            try
            {
                recordStore.insert(s, createRandomRecord());
            }
            catch (Exception e)
            {
                System.out.println(s);
                e.printStackTrace();
            }
            Thread.sleep(200);
        }

        recordStore.stopAutoCompaction();
    }
*/

    private void time(Runnable r)
    {
        long start = System.currentTimeMillis();
        r.run();
        long end = System.currentTimeMillis();
        System.out.println("" + (end - start));
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
        return createSampleRecord(rand.nextInt(6), rand.nextInt(10));
    }

    private Record createSampleRecord(int depth, int keys)
    {
        MutableRecordImpl random = new MutableRecordImpl();
        for (int i = 0; i < keys; i++)
        {
            random.put("key" + i, "value");
        }
        for (int i = 0; i < depth; i++)
        {
            random.put("nested" + i, random.copy(true));
        }
        return random;
    }
}
