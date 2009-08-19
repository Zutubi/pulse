package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.Sequence;
import com.zutubi.pulse.master.model.SequenceManager;
import com.zutubi.pulse.master.model.SequenceEntry;
import com.zutubi.pulse.master.model.persistence.SequenceEntryDao;

/**
 * An implementation of the sequence manager that is backed by the
 * hibernate persistence system.
 */
public class HibernateSequenceManager implements SequenceManager
{
    private SequenceEntryDao dao;

    public Sequence getSequence(final String name)
    {
        final HibernateSequenceManager manager = this;
        return new Sequence()
        {
            public long getNext()
            {
                return manager.getNext(name);
            }
        };
    }

    // synchronise to ensure that each get next call produces a unique ascending value.
    private synchronized long getNext(String name)
    {
        SequenceEntry entry = dao.findByName(name);
        if (entry == null)
        {
            entry = new SequenceEntry();
            entry.setName(name);
            entry.setNext(1);
            dao.save(entry);
        }

        long next = entry.getNext();
        entry.setNext(next + 1);
        dao.save(entry);
        
        return next;
    }

    public void setSequenceEntryDao(SequenceEntryDao dao)
    {
        this.dao = dao;
    }
}
