package com.cinnamonbob.model;

import java.util.List;

public class CompoundNotifyCondition implements NotifyCondition
{
    private List<NotifyCondition> children;
    private boolean disjunctive;

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
