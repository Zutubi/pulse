package com.zutubi.util.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link java.util.concurrent.Future} that returns a
 * fixed value.
 */
public class FixedFuture<T> implements Future<T>
{
    private T result;

    public FixedFuture(T result)
    {
        this.result = result;
    }

    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return false;
    }

    public boolean isCancelled()
    {
        return false;
    }

    public boolean isDone()
    {
        return true;
    }

    public T get()
    {
        return result;
    }

    public T get(long timeout, TimeUnit unit)
    {
        return result;
    }
}
