package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.prototype.type.record.MutableRecord;

/**
 * An adapter implementation of the collection listener that provides default implementations of each
 * of the callback methods, allowing specific implementations to focus on only those callbacks they require.
 *
 */
public class CollectionAdapter<X extends Configuration> extends CollectionListener<X>
{
    public CollectionAdapter(String path, Class<X> configurationClass, boolean synchronous)
    {
        super(path, configurationClass, synchronous);
    }

    protected void preInsert(MutableRecord record)
    {

    }

    protected void instanceInserted(X instance)
    {

    }

    protected void instanceDeleted(X instance)
    {

    }

    protected void instanceChanged(X instance)
    {

    }
}
