package com.cinnamonbob.model;

import java.util.List;
import java.util.LinkedList;

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
    
    public boolean satisfied(BuildResult result)
    {
        boolean ret = !disjunctive;
        
        for(NotifyCondition child: children)
        {
            if(child.satisfied(result) == disjunctive)
            {
                ret = disjunctive;
                break;
            }
        }        
        return ret;
    }
}
