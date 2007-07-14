package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * A condition that is a disjunction of some common simple conditions.
 */
@SymbolicName("zutubi.selectedBuildsConditionConfig")
public class SelectedBuildsConditionConfiguration extends SubscriptionConditionConfiguration
{
    private boolean unsuccessful;
    private boolean includeChanges;
    private boolean includeChangesByMe;
    private boolean statusChange;

    public boolean getUnsuccessful()
    {
        return unsuccessful;
    }

    public void setUnsuccessful(boolean unsuccessful)
    {
        this.unsuccessful = unsuccessful;
    }

    public boolean getIncludeChanges()
    {
        return includeChanges;
    }

    public void setIncludeChanges(boolean includeChanges)
    {
        this.includeChanges = includeChanges;
    }

    public boolean getIncludeChangesByMe()
    {
        return includeChangesByMe;
    }

    public void setIncludeChangesByMe(boolean includeChangesByMe)
    {
        this.includeChangesByMe = includeChangesByMe;
    }

    public boolean getStatusChange()
    {
        return statusChange;
    }

    public void setStatusChange(boolean statusChange)
    {
        this.statusChange = statusChange;
    }

    public String getExpression()
    {
        List<String> expressions = new LinkedList<String>();
        if(unsuccessful)
        {
            expressions.add("not " + NotifyConditionFactory.SUCCESS);
        }

        if(includeChanges)
        {
            expressions.add(NotifyConditionFactory.CHANGED);
        }

        if(includeChangesByMe)
        {
            expressions.add(NotifyConditionFactory.CHANGED_BY_ME);
        }

        if(statusChange)
        {
            expressions.add(NotifyConditionFactory.STATE_CHANGE);
        }

        return StringUtils.join("or", expressions);
    }
}
