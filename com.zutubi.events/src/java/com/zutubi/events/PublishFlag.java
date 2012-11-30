package com.zutubi.events;

/**
 * Flags to indicates how the {@link EventManager} should publish an event.
 */
public enum PublishFlag
{
    /**
     * Publish the event after the completion of a running event handler.  Use when publishing an
     * event from within the handler of another event (the <em>pending</em> event), to ensure that
     * all listeners handle the pending event before the new event is published.
     * <p/>
     * The publish occurs immediately after all handlers of the pending event complete, on this
     * thread.  If this thread is not inside a handler, the publish occurs immediately.  If this
     * thread is nested within multiple publishes, the defer only waits for the inner publish to
     * complete (not all nested ones).
     */
    DEFERRED,

    /**
     * Publish the event immediately using this thread.  This is the default.
     * <p/>
     * Note that the dispatcher used by the event manager or the listeners themselves may be
     * asynchronous (see {@link AsynchronousDispatcher} and {@link AsynchronousDelegatingListener}).
     */
    IMMEDIATE
}
