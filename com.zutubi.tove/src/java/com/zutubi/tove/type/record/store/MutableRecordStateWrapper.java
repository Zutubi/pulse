package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.inmemory.InMemoryStateWrapper;
import com.zutubi.tove.type.record.MutableRecord;

/**
 * An extension of the InMemoryStateWrapper class that wraps a MutableRecord instance. 
 */
public class MutableRecordStateWrapper extends InMemoryStateWrapper<MutableRecord>
{
    public MutableRecordStateWrapper(MutableRecord state)
    {
        super(state);
    }

    protected MutableRecordStateWrapper copy()
    {
        return new MutableRecordStateWrapper(get().copy(true, true));
    }
}