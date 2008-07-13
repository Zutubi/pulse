package com.zutubi.tove.type.record;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Allocates handles from the top of the handle space down.
 */
public class TransientHandleAllocator implements HandleAllocator
{
    private AtomicLong nextHandle = new AtomicLong(Long.MAX_VALUE);

    public long allocateHandle()
    {
        return nextHandle.decrementAndGet();
    }
}
