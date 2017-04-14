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

package com.zutubi.tove.transaction.inmemory;

/**
 * The base class for the state instances managed by the {@link InMemoryTransactionResource}
 *
 * This wrapper class is used to wrap the instance whose state is being managed as part of
 * the transaction, and track possible changes to the instance.
 *
 * @param <U>   the type of the state object being managed.
 *
 * @see InMemoryTransactionResource
 */
public abstract class InMemoryStateWrapper<U>
{
    /**
     * The wrapped state.
     */
    private U state;

    protected InMemoryStateWrapper(U state)
    {
        this.state = state;
    }

    public U get()
    {
        return state;
    }

    /**
     * Implementations of this method are responsible for making a
     * copy of the wrapped state.  This needs to be a complete copy
     * of everything that is considered wrapped.
     *
     * @return the new state wrapper.
     */
    protected abstract InMemoryStateWrapper<U> copy();
}