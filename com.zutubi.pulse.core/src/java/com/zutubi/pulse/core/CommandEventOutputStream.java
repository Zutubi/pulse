package com.zutubi.pulse.core;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.CommandOutputEvent;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that converts the output into {@link CommandOutputEvent}s
 * and publishes them.  The stream attempts to combine small writes into larger
 * events to avoid unnecessary overhead, but can also be configured to
 * automatically flush after a delay to avoid excessive lag.
 */
public class CommandEventOutputStream extends OutputStream implements Runnable
{
    /**
     * Minimum number of bytes we will transmit, if there are fewer then the
     * overhead of transmission would be too great.  If we don't have enough
     * bytes on a write call, those bytes are stored in a buffer.  If we get
     * more, we just send a large event.
     */
    public static int MINIMUM_SIZE = 10240;
    /**
     * Flush interval to use to disable auto flushing.
     */
    public static final long DISABLE_AUTO_FLUSH = 0;
    /**
     * The default auto flush interval. Enables auto flushing.
     */
    public static final long DEFAULT_AUTO_FLUSH_INTERVAL = 10000;

    private byte[] buffer;
    private int offset;
    private long recipeId;
    private long autoflushInterval;

    private EventManager eventManager;

    /**
     * Create a new stream that will publish events for the give recipe to the
     * given manager.  Autoflush is enabled with the default flush interval.
     *
     * @param eventManager manager used to publish events
     * @param recipeId     recipe id to use for published events
     */
    public CommandEventOutputStream(EventManager eventManager, long recipeId)
    {
        this(eventManager, recipeId, DEFAULT_AUTO_FLUSH_INTERVAL);
    }

    /**
     * Create a new stream that will publish events for the give recipe to the
     * given manager.  Autoflush is configured via the given interval -- it may
     * be disabled by passing {@link #DISABLE_AUTO_FLUSH}.
     *
     * @param eventManager      manager used to publish events
     * @param recipeId          recipe id to use for published events
     * @param autoflushInterval interval, in milliseconds, at which to
     *                          automatically flush any accumulated output
     */
    public CommandEventOutputStream(EventManager eventManager, long recipeId, long autoflushInterval)
    {
        buffer = new byte[MINIMUM_SIZE];
        offset = 0;

        this.recipeId = recipeId;
        this.autoflushInterval = autoflushInterval;
        this.eventManager = eventManager;

        if (autoflushInterval != DISABLE_AUTO_FLUSH)
        {
            Thread flusher = new Thread(this);
            flusher.start();
        }
    }

    public synchronized void write(int b)
    {
        if (buffer != null)
        {
            buffer[offset++] = (byte) b;
            checkBuffer();
        }
    }

    public synchronized void write(byte b[], int off, int len) throws IOException
    {
        if (buffer != null)
        {
            if (offset + len <= MINIMUM_SIZE)
            {
                // It fits in the buffer, chuck it there
                System.arraycopy(b, off, buffer, offset, len);
                offset += len;
                checkBuffer();
            }
            else
            {
                // We have more data than we need.  Assemble into a buffer and
                // send.
                byte[] sendBuffer = new byte[offset + len];
                System.arraycopy(buffer, 0, sendBuffer, 0, offset);
                System.arraycopy(b, off, sendBuffer, offset, len);

                sendEvent(sendBuffer);
            }
        }
    }

    public synchronized void flush()
    {
        // Just send whatever data we have, even if it is below the minimum
        if (offset > 0)
        {
            sendBuffer();
        }
    }

    public synchronized void close()
    {
        flush();
        buffer = null;
        offset = 0;
    }

    private synchronized void checkBuffer()
    {
        if (offset == MINIMUM_SIZE)
        {
            sendBuffer();
        }
    }

    private synchronized void sendBuffer()
    {
        byte[] sendBuffer = new byte[offset];
        System.arraycopy(buffer, 0, sendBuffer, 0, offset);
        sendEvent(sendBuffer);
    }

    private void sendEvent(byte[] sendBuffer)
    {
        CommandOutputEvent event = new CommandOutputEvent(this, recipeId, sendBuffer);
        eventManager.publish(event);
        offset = 0;
    }

    public void run()
    {
        while(buffer != null)
        {
            try
            {
                Thread.sleep(autoflushInterval);
            }
            catch (InterruptedException e)
            {
                // Empty
            }

            flush();
        }
    }
}
