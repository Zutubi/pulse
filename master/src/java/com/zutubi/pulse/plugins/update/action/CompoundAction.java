package com.zutubi.pulse.plugins.update.action;

/**
 */
public class CompoundAction implements UpdateAction
{
    private UpdateAction[] children;

    public CompoundAction(UpdateAction ...children)
    {
        this.children = children;
    }

    public int getUnitsOfWork()
    {
        int total = 0;
        for(UpdateAction child: children)
        {
            total += child.getUnitsOfWork();
        }

        return total;
    }

    public UpdateResult execute(UpdateMonitor monitor)
    {
        UpdateResult result;
        monitor.started(this);

        for(UpdateAction child: children)
        {
            result = child.execute(monitor);
            if(result.getStatus() != UpdateResult.Status.SUCCESS)
            {
                return result;
            }
        }

        result = new UpdateResultImpl(UpdateResult.Status.SUCCESS);
        monitor.completed(result);
        
        return result;
    }
}
