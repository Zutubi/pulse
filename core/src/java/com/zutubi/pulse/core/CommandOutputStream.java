package com.zutubi.pulse.core;

import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.CommandOutputEvent;

import java.io.OutputStream;
import java.io.IOException;

/**
 *
 */
public class CommandOutputStream extends OutputStream implements Runnable
{
    /**
     * Minimum number of bytes we will transmit, if there are fewer then the
     * overhead of transmission would be too great.  If we don't have enough
     * bytes on a write call, those bytes are stored in a buffer.  If we get
     * more, we just send a large event.
     */
    public static int MINIMUM_SIZE = 1024;

    private EventManager eventManager;
    private long recipeId;
    private byte[] buffer;
    private int offset;
    private Thread flusher;

    /**
     * The default auto flush interval. When auto flushing is enabled, the output stream
     * will be actively flushed once every interval.
     */
    private static final int AUTO_FLUSH_INTERVAL = 5000;

    public CommandOutputStream(EventManager eventManager, long recipeId, boolean autoflush)
    {
        this.eventManager = eventManager;
        this.recipeId = recipeId;
        buffer = new byte[MINIMUM_SIZE];
        offset = 0;

        if(autoflush)
        {
            flusher = new Thread(this);
            flusher.start();
        }
    }

    public synchronized void write(int b)
    {
        buffer[offset++] = (byte) b;
        checkBuffer();
    }

    public synchronized void write(byte b[], int off, int len) throws IOException
    {
        if (buffer == null)
        {
            throw new IOException("Attempting to write to closed OutputStream.");
        }
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

    public synchronized void flush()
    {
        // Just send whatever data we have, even if it is below the minimum
        if(offset > 0)
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
                Thread.sleep(AUTO_FLUSH_INTERVAL);
            }
            catch (InterruptedException e)
            {
            }

            flush();
        }
    }
}
