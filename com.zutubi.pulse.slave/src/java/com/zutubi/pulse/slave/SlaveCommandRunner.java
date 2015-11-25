package com.zutubi.pulse.slave;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.EventOutputStream;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.events.SlaveCommandCompletedEvent;
import com.zutubi.pulse.core.util.process.ProcessWrapper;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.io.IOUtils;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service to run simple exe commands on slaves with results forwarded to the master via events.
 */
public class SlaveCommandRunner extends BackgroundServiceSupport
{
    private final Set<Long> runningCommands = new HashSet<Long>();
    private EventManager eventManager;

    public SlaveCommandRunner()
    {
        super("Slave Command Runner", 1);
    }

    public synchronized void runCommand(final PulseExecutionContext context, final List<String> commandLine, final String workingDir, final long commandId, final int timeout)
    {
        runningCommands.add(commandId);

        getExecutorService().execute(new Runnable()
        {
            public void run()
            {
                SlaveCommandCompletedEvent completedEvent = null;
                OutputStream outputStream = null;
                try
                {
                    outputStream = new EventOutputStream(eventManager, false, 0, commandId);
                    context.setOutputStream(outputStream);
                    ProcessWrapper.runCommand(commandLine, workingDir, context, timeout, TimeUnit.SECONDS);
                    completedEvent = new SlaveCommandCompletedEvent(this, commandId, true, null);
                }
                catch (Exception e)
                {
                    completedEvent = new SlaveCommandCompletedEvent(this, commandId, false, e.getMessage());
                }
                finally
                {
                    IOUtils.close(outputStream);

                    if (completedEvent != null)
                    {
                        eventManager.publish(completedEvent);
                    }

                    synchronized (SlaveCommandRunner.this)
                    {
                        runningCommands.remove(commandId);
                    }
                }
            }
        });
    }

    public synchronized boolean isCommandRunning(long commandId)
    {
        return runningCommands.contains(commandId);
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
