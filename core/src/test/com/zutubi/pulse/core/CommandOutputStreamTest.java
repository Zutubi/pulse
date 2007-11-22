package com.zutubi.pulse.core;

import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.CommandOutputEvent;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.model.CommandResult;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class CommandOutputStreamTest extends PulseTestCase implements EventListener
{
    private static final int CYCLE_LIMIT = 33;

    private CommandOutputStream stream;
    private EventManager eventManager;
    private List<CommandOutputEvent> receivedEvents;

    protected void setUp() throws Exception
    {
        eventManager = new DefaultEventManager();
        eventManager.register(this);
        stream = new CommandOutputStream(eventManager, 0, false);
        receivedEvents = new LinkedList<CommandOutputEvent>();
    }

    protected void tearDown() throws Exception
    {
        stream.close();
    }

    public void testWriteOneByte()
    {
        // Make sure a single byte write is not sent until flushed
        stream.write(0);
        assertEquals(0, receivedEvents.size());
        stream.flush();
        assertEvent(1, 0);
    }

    public void testWriteOneUnder() throws IOException
    {
        // Write just under the limit and ensure it is not sent until flushed
        stream.write(getBuffer(CommandOutputStream.MINIMUM_SIZE - 1));
        assertEquals(0, receivedEvents.size());
        stream.flush();
        assertEvent(CommandOutputStream.MINIMUM_SIZE - 1);
    }

    public void testWriteExact() throws IOException
    {
        stream.write(getBuffer(CommandOutputStream.MINIMUM_SIZE));
        assertEvent(CommandOutputStream.MINIMUM_SIZE);
        stream.flush();
        assertEquals(0, receivedEvents.size());
    }

    public void testWriteOver() throws IOException
    {
        stream.write(getBuffer(CommandOutputStream.MINIMUM_SIZE + 33));
        assertEvent(CommandOutputStream.MINIMUM_SIZE + 33);
        stream.flush();
        assertEquals(0, receivedEvents.size());
    }

    public void testCreepToLimit() throws IOException
    {
        stream.write(getBuffer(CommandOutputStream.MINIMUM_SIZE - 1));
        assertEquals(0, receivedEvents.size());
        stream.write((CommandOutputStream.MINIMUM_SIZE - 1) % CYCLE_LIMIT);
        assertEvent(CommandOutputStream.MINIMUM_SIZE);
        stream.flush();
        assertEquals(0, receivedEvents.size());
    }

    public void testWriteToLimit() throws IOException
    {
        stream.write(getBuffer(CommandOutputStream.MINIMUM_SIZE - CYCLE_LIMIT));
        assertEquals(0, receivedEvents.size());
        stream.write(getBuffer(CYCLE_LIMIT, CommandOutputStream.MINIMUM_SIZE - CYCLE_LIMIT));
        assertEvent(CommandOutputStream.MINIMUM_SIZE);
        stream.flush();
        assertEquals(0, receivedEvents.size());
    }

    public void testWriteToOver() throws IOException
    {
        stream.write(getBuffer(CommandOutputStream.MINIMUM_SIZE - 100));
        assertEquals(0, receivedEvents.size());
        stream.write(getBuffer(200, CommandOutputStream.MINIMUM_SIZE - 100));
        assertEvent(CommandOutputStream.MINIMUM_SIZE + 100);
        stream.flush();
        assertEquals(0, receivedEvents.size());
    }

    public void testCloseFlushes()
    {
        stream.write(11);
        assertEquals(0, receivedEvents.size());
        stream.close();
        assertEvent(1, 11);
    }

    public void testManyWrites() throws IOException
    {
        int totalWritten = 0;
        for(int i = 0; i < 100; i++)
        {
            stream.write(getBuffer(600, totalWritten));
            totalWritten += 600;
        }

        assertReceived(totalWritten);
    }

    public void testAutoFlush() throws InterruptedException, IOException
    {
        stream = new CommandOutputStream(eventManager, 1, true);
        stream.write(getBuffer(1));
        Thread.sleep(6000);
        assertReceived(1);
    }

    public void testAutoFlushAfterFullWrite() throws InterruptedException, IOException
    {
        stream = new CommandOutputStream(eventManager, 1, true);
        stream.write(getBuffer(CommandOutputStream.MINIMUM_SIZE));
        stream.write(getBuffer(1));
        Thread.sleep(6000);
        assertEvent(CommandOutputStream.MINIMUM_SIZE);
        assertEvent(1);
    }

    private byte[] getBuffer(int size)
    {
        return getBuffer(size, 0);
    }

    private byte[] getBuffer(int size, int start)
    {
        byte[] buffer = new byte[size];
        for(int i = 0; i < size; i++)
        {
            buffer[i] = (byte) ((i + start) % CYCLE_LIMIT);
        }

        return buffer;
    }

    private void assertEvent(int length)
    {
        assertEvent(length, 0);
    }

    private void assertEvent(int length, int first)
    {
        assertTrue(receivedEvents.size() > 0);
        CommandOutputEvent event = receivedEvents.remove(0);
        byte[] data = event.getData();
        assertEquals(length, data.length);
        assertData(data, first);
    }

    private void assertData(byte[] data, int first)
    {
        for(int i = 0; i < data.length; i++)
        {
            assertEquals((i + first) % CYCLE_LIMIT, data[i]);
        }
    }

    private void assertReceived(int totalWritten)
    {
        int totalReceived = 0;
        for(CommandOutputEvent event: receivedEvents)
        {
            assertData(event.getData(), totalReceived);
            totalReceived += event.getData().length;
        }

        assertEquals(totalWritten, totalReceived);
    }

    public void handleEvent(Event evt)
    {
        receivedEvents.add((CommandOutputEvent) evt);
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { CommandOutputEvent.class };
    }
}
