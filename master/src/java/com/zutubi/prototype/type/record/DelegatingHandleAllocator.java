package com.zutubi.prototype.type.record;

/**
 * Allocator that delegates to another allocator, that can be changed at some
 * point.  For example, we switch from transient allocation to the record
 * manager during startup.
 */
public class DelegatingHandleAllocator implements HandleAllocator
{
    private HandleAllocator delegate = new TransientHandleAllocator();

    public long allocateHandle()
    {
        return delegate.allocateHandle();
    }

    public void setDelegate(HandleAllocator delegate)
    {
        this.delegate = delegate;
    }
}
