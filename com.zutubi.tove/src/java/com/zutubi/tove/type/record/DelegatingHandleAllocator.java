/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.type.record;

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
