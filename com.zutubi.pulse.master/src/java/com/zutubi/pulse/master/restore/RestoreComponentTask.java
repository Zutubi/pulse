package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.master.util.monitor.FeedbackAware;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class RestoreComponentTask implements RestoreTask, FeedbackAware
{
    private ArchiveableComponent component;
    private File archiveBase;

    public RestoreComponentTask(ArchiveableComponent component, File archiveBase)
    {
        this.component = component;
        this.archiveBase = archiveBase;
    }

    public void execute() throws ArchiveException
    {
        component.restore(archiveBase);
    }

    public String getName()
    {
        String name = component.getName();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public ArchiveableComponent getComponent()
    {
        return component;
    }

    public boolean hasFailed()
    {
        return false;
    }

    public List<String> getErrors()
    {
        return new LinkedList<String>();
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public String getDescription()
    {
        return component.getDescription();
    }

    public void setFeedback(TaskFeedback feedback)
    {
        if (component instanceof FeedbackAware)
        {
            ((FeedbackAware)component).setFeedback(feedback);
        }
    }
}
