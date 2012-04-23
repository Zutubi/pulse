package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.notifications.condition.ChangedNotifyCondition;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.util.LinkedList;
import java.util.List;

/**
 * A condition that is a disjunction of some common simple conditions.
 */
@SymbolicName("zutubi.selectedBuildsConditionConfig")
@Form(labelWidth = 350, fieldOrder = {"broken", "failed", "warnings", "statusChange", "includeChanges", "changesByMe", "changesSinceHealthy", "changesSinceSuccess", "upstreamChanges"})
public class SelectedBuildsConditionConfiguration extends SubscriptionConditionConfiguration implements Validateable
{
    private boolean broken;
    private boolean failed;
    private boolean warnings;
    private boolean statusChange;
    @ControllingCheckbox(checkedFields = {"changesByMe", "changesSinceHealthy", "changesSinceSuccess", "upstreamChanges"})
    private boolean includeChanges;
    private boolean changesByMe;
    private boolean changesSinceHealthy;
    private boolean changesSinceSuccess;
    private boolean upstreamChanges;

    public boolean getBroken()
    {
        return broken;
    }

    public void setBroken(boolean broken)
    {
        this.broken = broken;
    }

    public boolean isFailed()
    {
        return failed;
    }

    public void setFailed(boolean failed)
    {
        this.failed = failed;
    }

    public boolean isWarnings()
    {
        return warnings;
    }

    public void setWarnings(boolean warnings)
    {
        this.warnings = warnings;
    }

    public boolean getStatusChange()
    {
        return statusChange;
    }

    public void setStatusChange(boolean statusChange)
    {
        this.statusChange = statusChange;
    }

    public boolean getIncludeChanges()
    {
        return includeChanges;
    }

    public void setIncludeChanges(boolean includeChanges)
    {
        this.includeChanges = includeChanges;
    }

    public boolean getChangesByMe()
    {
        return changesByMe;
    }

    public void setChangesByMe(boolean changesByMe)
    {
        this.changesByMe = changesByMe;
    }

    public boolean isChangesSinceHealthy()
    {
        return changesSinceHealthy;
    }

    public void setChangesSinceHealthy(boolean changesSinceHealthy)
    {
        this.changesSinceHealthy = changesSinceHealthy;
    }

    public boolean isChangesSinceSuccess()
    {
        return changesSinceSuccess;
    }

    public void setChangesSinceSuccess(boolean changesSinceSuccess)
    {
        this.changesSinceSuccess = changesSinceSuccess;
    }

    public boolean isUpstreamChanges()
    {
        return upstreamChanges;
    }

    public void setUpstreamChanges(boolean upstreamChanges)
    {
        this.upstreamChanges = upstreamChanges;
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
        if (broken)
        {
            expressions.add(NotifyConditionFactory.BROKEN);
        }

        if (failed)
        {
            expressions.add(NotifyConditionFactory.FAILURE);
        }

        if (warnings)
        {
            expressions.add(NotifyConditionFactory.WARNINGS);
        }

        if (includeChanges)
        {
            List<ChangedNotifyCondition.Modifier> modifiers = new LinkedList<ChangedNotifyCondition.Modifier>();
            if (changesByMe)
            {
                modifiers.add(ChangedNotifyCondition.Modifier.BY_ME);
            }

            if (changesSinceHealthy)
            {
                modifiers.add(ChangedNotifyCondition.Modifier.SINCE_HEALTHY);
            }

            if (changesSinceSuccess)
            {
                modifiers.add(ChangedNotifyCondition.Modifier.SINCE_SUCCESS);
            }

            if (upstreamChanges)
            {
                modifiers.add(ChangedNotifyCondition.Modifier.INCLUDE_UPSTREAM);
            }

            String modifierExpression = "";
            if (modifiers.size() > 0)
            {
                modifierExpression = "(" + StringUtils.join(",", CollectionUtils.map(modifiers, new ChangedNotifyCondition.Modifier.ToTextMapping())) + ")";
            }

            expressions.add("changed" + modifierExpression);
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
