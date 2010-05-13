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
     * Indicates whether or not a modifiable copy of the wrapped state has
     * been requested.
     */
    private boolean dirty;

    /**
     * The wrapped state.
     */
    private U state;

    protected InMemoryStateWrapper(U state)
    {
        this.state = state;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
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