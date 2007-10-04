package com.zutubi.prototype.type.record.store;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class JournalTest extends PulseTestCase
{
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testNonExistantJournalDir()
    {
        try
        {
            new Journal(new File(tmp, "does-not-exist"));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop.
        }
    }

    public void testBlankJournalDir()
    {
        Journal journal = new Journal(tmp);
        assertNotNull(journal.getEntries());
        assertEquals(0, journal.getEntries().size());
        assertEquals(0, journal.size());
    }

    public void testBlankJournalCommit()
    {
        Journal journal = new Journal(tmp);
        journal.commit();
    }

    public void testBlankJournalRollback()
    {
        Journal journal = new Journal(tmp);
        journal.rollback();        
    }

    public void testSingleAddAndRetrieve() throws IOException
    {
        Journal journal = new Journal(tmp);
        journal.add(new JournalEntry("action", "path", createSampleRecord()));
        commit(journal);

        journal = new Journal(tmp);
        assertNotNull(journal.getEntries());
        assertEquals(1, journal.size());

        JournalEntry entry = journal.get(0);
        assertEquals("action", entry.getAction());
        assertEquals("path", entry.getPath());

        Record record = entry.getRecord();
        assertEquals(1, record.size());
        assertEquals("b", record.get("a"));
    }

    public void testOrderingOfEntries() throws IOException
    {
        Journal journal = new Journal(tmp);
        journal.add(new JournalEntry("1", "path"));
        journal.add(new JournalEntry("2", "path"));
        journal.add(new JournalEntry("3", "path"));
        
        commit(journal);

        journal = new Journal(tmp);
        assertEquals(3, journal.size());

        List<JournalEntry> entries = journal.getEntries();
        assertNotNull(entries);
        assertEquals("1", entries.get(0).getAction());
        assertEquals("2", entries.get(1).getAction());
        assertEquals("3", entries.get(2).getAction());
    }

    public void testClearJournal()
    {
        Journal journal = new Journal(tmp);
        journal.add(new JournalEntry("action", "path"));
        journal.add(new JournalEntry("action", "path"));
        commit(journal);

        journal = new Journal(tmp);
        assertEquals(2, journal.size());

        journal.clear();
        commit(journal);

        journal = new Journal(tmp);
        assertEquals(0, journal.size());
    }

    public void testClearJournalRollback()
    {
        // setup a journal with sample data.
        Journal journal = new Journal(tmp);
        addSampleJournalEntries(journal);

        journal.clear();

        assertEquals(0, journal.size());

        journal.rollback();

        assertEquals(5, journal.size());
    }

    public void testClearResetsId()
    {
        Journal journal = new Journal(tmp);
        addSampleJournalEntries(journal);
        assertTrue(journal.size() > 1);

        List<JournalEntry> entries = journal.getEntries();
        JournalEntry lastEntry = entries.get(entries.size() - 1);
        assertTrue(lastEntry.getId() > 1);

        journal.clear();
        journal.commit();

        journal.add(new JournalEntry("action", "path"));
        journal.commit();

        assertEquals(1, journal.size());
        assertEquals(1, journal.get(0).getId());
    }

    public void testClearCleansUpRecordDirectories()
    {
        Journal journal = new Journal(tmp);
        addSampleJournalEntries(journal);

        // get ids for entries.
        List<String> ids = new LinkedList<String>();
        for (JournalEntry entry : journal.getEntries())
        {
            ids.add(Integer.toString(entry.getId()));
        }

        // ensure that record directories exist.
        for (String id : ids)
        {
            assertTrue(new File(tmp, id).isDirectory());
        }

        journal.clear();
        journal.commit();

        // ensure that no record directories remain.
        for (String id : ids)
        {
            assertFalse(new File(tmp, id).isDirectory());
        }
    }

    public void testClearLocksJournalUntilCommit()
    {
        Journal journal = new Journal(tmp);
        journal.clear();

        try
        {
            journal.add(new JournalEntry("a", "b"));
            fail();
        }
        catch (IllegalStateException e)
        {
        }
        
        journal.commit();

        journal.add(new JournalEntry("a", "b"));
    }

    public void testClearLocksJournalUntilRollback()
    {
        Journal journal = new Journal(tmp);
        journal.clear();

        try
        {
            journal.add(new JournalEntry("a", "b"));
            fail();
        }
        catch (IllegalStateException e)
        {
        }

        journal.rollback();

        journal.add(new JournalEntry("a", "b"));
    }

    public void testRollback() throws IOException
    {
        Journal journal = new Journal(tmp);
        journal.add(new JournalEntry("action", "path"));
        assertEquals(1, journal.size());

        journal.rollback();

        assertEquals(0, journal.size());

        journal = new Journal(tmp);
        assertEquals(0, journal.size());
    }

    public void testRecordSerialisation() throws IOException
    {
        Record r = createSampleRecord();

        Journal journal = new Journal(tmp);
        journal.add(new JournalEntry("action", "path", r));
        commit(journal);

        journal = new Journal(tmp);
        JournalEntry entry = journal.getEntries().get(0);
        assertNotNull(entry.getRecord());
        assertEquals(r.get("a"), entry.getRecord().get("a"));
    }

    // test a combination of actions to ensure that they interact sensibly.
    public void testJournalSanity()
    {
        Journal journal = new Journal(tmp);
        journal.add(new JournalEntry("action", "path", createSampleRecord()));
        commit(journal);

        assertEquals(1, journal.size());
        assertEquals("action", journal.get(0).getAction());
        assertEquals("b", journal.get(0).getRecord().get("a"));

        journal.add(new JournalEntry("action2", "path2", createSampleRecord()));
        commit(journal);

        assertEquals(2, journal.size());
        assertEquals("action2", journal.get(1).getAction());
        assertEquals("b", journal.get(1).getRecord().get("a"));

        journal.clear();
        commit(journal);

        assertEquals(0, journal.size());

        journal.add(new JournalEntry("action3", "path3", createSampleRecord("3", "3")));
        commit(journal);

        assertEquals(1, journal.size());
        assertEquals("action3", journal.get(0).getAction());
        assertEquals("3", journal.get(0).getRecord().get("3"));
    }

    private void commit(Journal journal)
    {
        journal.prepare();
        journal.commit();
    }

    private void addSampleJournalEntries(Journal journal)
    {
        journal.add(new JournalEntry("action", "path", createSampleRecord()));
        journal.add(new JournalEntry("action", "path", createSampleRecord()));
        journal.add(new JournalEntry("action", "path", createSampleRecord()));
        journal.add(new JournalEntry("action", "path", createSampleRecord()));
        journal.add(new JournalEntry("action", "path", createSampleRecord()));
        commit(journal);
    }

    private Record createSampleRecord()
    {
        return createSampleRecord("a", "b");
    }

    private Record createSampleRecord(String key, String value)
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(key, value);
        return record;
    }
}
