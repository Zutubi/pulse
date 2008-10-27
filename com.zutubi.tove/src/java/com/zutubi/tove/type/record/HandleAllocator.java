package com.zutubi.tove.type.record;

/**
 * Simple interface to abstract the ability to allocate unique handles.
 */
public interface HandleAllocator
{
    long allocateHandle();
}
