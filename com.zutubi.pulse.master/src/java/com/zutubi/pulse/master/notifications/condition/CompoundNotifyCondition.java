package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

import java.util.LinkedList;
import java.util.List;

public class CompoundNotifyCondition implements NotifyCondition
{
    private List<NotifyCondition> children;
    private boolean disjunctive;

    public CompoundNotifyCondition(NotifyCondition a, NotifyCondition b, boolean disjunctive)
    {
        children = new LinkedList<NotifyCondition>();
        children.add(a);
        children.add(b);
        this.disjunctive = disjunctive;
    }

    public CompoundNotifyCondition(List<NotifyCondition> children, boolean disjunctive)
    {
        this.children = children;
        this.disjunctive = disjunctive;
    }

    public boolean satisfied(BuildResult result, UserConfiguration user)
    {
        boolean ret = !disjunctive;

        for (NotifyCondition child : children)
        {
            if (child.satisfied(result, user) == disjunctive)
            {
                ret = disjunctive;
                break;
            }
        }
        return ret;
    }

    public List<NotifyCondition> getChildren()
    {
        return children;
    }

    public boolean isDisjunctive()
    {
        return disjunctive;
    }
}
