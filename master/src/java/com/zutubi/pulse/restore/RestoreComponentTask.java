package com.zutubi.pulse.restore;

import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.restore.feedback.TaskListener;

import java.io.File;
import java.util.Random;

/**
 *
 *
 */
public class RestoreComponentTask implements RestoreTask
{
    private ArchiveableComponent component;
    private File archiveBase;

    private long startTimestamp = 0;

    private long finishTimestamp = 0;

    private TaskListener listener;

    public RestoreComponentTask(ArchiveableComponent component, File archiveBase)
    {
        this.component = component;
        this.archiveBase = archiveBase;
    }

    public void execute() throws ArchiveException
    {
        startTimestamp = System.currentTimeMillis();

        try
        {
            Thread.sleep(new Random().nextInt(5000) + 5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        component.restore(archiveBase);

        finishTimestamp = System.currentTimeMillis();
    }

    public String getName()
    {
        String name = component.getName();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String getDescription()
    {
        return component.getDescription();
    }

    public String getStatusMessage()
    {
        return "place holder";
    }

    public long getElapsedTime()
    {
        // if start timestamp is zero, we have not started.
        if (startTimestamp == 0)
        {
            return 0;
        }

        long elapsedTime;
        // if finish time is zero, then we have not finished.
        if (finishTimestamp == 0)
        {
            elapsedTime = System.currentTimeMillis() - startTimestamp;
        }
        else
        {
            elapsedTime = finishTimestamp - startTimestamp;
        }
        return elapsedTime;
    }

    public String getElapsedTimePretty()
    {
        return TimeStamps.getPrettyElapsed(getElapsedTime());
    }

    public String getEstimatedTimePretty()
    {
        return "unknown";
    }

    public boolean isSuccessful()
    {
        return true;
    }
    
    public int getPercentageComplete()
    {
        if (startTimestamp != 0)
        {
            return 50;
        }
        else
        {
            return 0;
        }
    }

    public void setListener(TaskListener listener)
    {
        this.listener = listener;
    }
}
