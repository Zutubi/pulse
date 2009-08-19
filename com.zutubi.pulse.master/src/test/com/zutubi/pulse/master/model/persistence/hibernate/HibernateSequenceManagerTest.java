package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.Sequence;
import com.zutubi.pulse.master.model.persistence.SequenceEntryDao;

public class HibernateSequenceManagerTest extends MasterPersistenceTestCase
{
    private HibernateSequenceManager sequenceManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        SequenceEntryDao sequenceEntryDao = (SequenceEntryDao) context.getBean("sequenceEntryDao");
        sequenceManager = new HibernateSequenceManager();
        sequenceManager.setSequenceEntryDao(sequenceEntryDao);
    }

    public void testGetNext()
    {
        Sequence sampleSequence = sequenceManager.getSequence("sample");
        assertEquals(1, sampleSequence.getNext());
        assertEquals(2, sampleSequence.getNext());
    }

    public void testGetMultipleSequences()
    {
        Sequence a = sequenceManager.getSequence("sequenceA");
        Sequence b = sequenceManager.getSequence("sequenceB");

        assertEquals(1, a.getNext());
        assertEquals(1, b.getNext());
    }

    public void testSequencePersistence()
    {
        Sequence sampleSequence = sequenceManager.getSequence("sample");
        assertEquals(1, sampleSequence.getNext());

        sampleSequence = sequenceManager.getSequence("sample");
        assertEquals(2, sampleSequence.getNext());
    }
}
