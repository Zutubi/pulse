package com.zutubi.tove.type.record.store;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.transaction.UserTransaction;
import com.zutubi.tove.type.record.Record;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FileSystemRecordStorePerformanceTest extends RecordStoreTestCase
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
        removeDirectory(persistentDirectory);

        super.tearDown();
    }

    public void testPerformanceOfLargeRecordSetsOutsideTransaction()
    {
        final List<Record> records = new LinkedList<Record>();
        for (int i = 0; i < 50; i++)
        {
            records.add(createSampleRecord(3, 5));
        }

        failAfterXTime(2000, new Runnable()
        {
            public void run()
            {
                int i = 0;
                for (Record record : records)
                {
                    recordStore.insert("path_" + i, record);
                    i++;
                }
            }
        });
    }

    public void testPerformanceOfLargeRecordSetsInsideTransaction()
    {
        final List<Record> records = new LinkedList<Record>();
        for (int i = 0; i < 100; i++)
        {
            records.add(createSampleRecord(3, 5));
        }

        failAfterXTime(2000, new Runnable()
        {
            public void run()
            {
                transaction.begin();
                int i = 0;
                for (Record record : records)
                {
                    recordStore.insert("path_" + i, record);
                    i++;
                }
                transaction.commit();
            }
        });
    }

    public void manualPerformanceTest() throws Exception
    {
        FileSystemRecordStore recordStore = new FileSystemRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.setPersistenceDirectory(persistentDirectory);
        recordStore.setCompactionInterval(10);
        recordStore.initAndStartAutoCompaction();

        System.out.println(persistentDirectory.getAbsolutePath());

        for (int j = 0; j < 500; j++)
        {
            long time = 0;
            for (int i = 0; i < 10; i++)
            {
                String s = "path_" + (i + 10 * j);
                try
                {
                    long start = System.currentTimeMillis();
                    Record record = createRandomRecord();
                    recordStore.insert(s, record);
                    long end = System.currentTimeMillis();
                    time = time + (end - start);
                }
                catch (Exception e)
                {
                    System.out.println(s);
                    e.printStackTrace();
                }
                Thread.sleep(200);
            }
            System.out.println("Average("+(j + 1)+"): " + (time / 10) + "    dir list length: " + persistentDirectory.list().length + "   size of store: " + sizeOfStore(recordStore));
        }

        recordStore.stopAutoCompaction();
    }

    private long sizeOfStore(RecordStore store)
    {
        Record r = store.select();
        return countNested(r);        
    }

    private long countNested(Record r)
    {
        long count = 0;
        for (String key : r.nestedKeySet())
        {
            count++;
            count = count + countNested((Record) r.get(key));
        }
        return count;
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
}
