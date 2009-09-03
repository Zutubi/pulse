package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.util.LinkedList;
import java.util.List;

/**
 * A condition that is a disjunction of some common simple conditions.
 */
@SymbolicName("zutubi.selectedBuildsConditionConfig")
@Form(labelWidth = 350, fieldOrder = {"unsuccessful", "statusChange", "includeChanges", "includeChangesByMe"})
public class SelectedBuildsConditionConfiguration extends SubscriptionConditionConfiguration implements Validateable
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
        List<String> expressions = determineExpressions();
        if(expressions.size() > 0)
        {
            return StringUtils.join(" or ", expressions);
        }
        else
        {
            return "false";
        }
    }

    private List<String> determineExpressions()
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
        return expressions;
    }

    public void validate(ValidationContext context)
    {
        if(determineExpressions().size() == 0)
        {
            context.addActionError(Messages.getInstance(SelectedBuildsConditionConfiguration.class).format("no.conditions.selected"));
        }
    }
}
