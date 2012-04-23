package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.TruePredicate;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * A notify condition that is satisfied if certain changes are found in the build (possibly since
 * an earlier build of a certain state, possibly including upstream changes).
 */
public class ChangedNotifyCondition implements NotifyCondition
{
    /**
     * Values that affect the collection of changes that are assessed or how they are assessed.
     */
    public enum Modifier
    {
        /**
         * Only changes by the user with the subscription are considered.
         */
        BY_ME,
        /**
         * Changes to upstream builds are also included.
         */
        INCLUDE_UPSTREAM,
        /**
         * All changes since the last healthy build are considered.
         */
        SINCE_HEALTHY,
        /**
         * Changes since the last successful build are considered.
         */
        SINCE_SUCCESS;
        
        public String asText()
        {
            return name().toLowerCase().replace('_', '.');
        }

        public static class ToTextMapping implements Mapping<Modifier, String>
        {
            public String map(Modifier modifier)
            {
                return modifier.asText();
            }
        }
    }

    private Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

    public ChangedNotifyCondition()
    {
    }

    public Set<Modifier> getModifiers()
    {
        return modifiers;
    }

    public void addModifier(Modifier modifier)
    {
        modifiers.add(modifier);
    }

    public boolean satisfied(final NotifyConditionContext context, UserConfiguration user)
    {
        if (context.getBuildResult() == null)
        {
            return false;
        }

        Set<PersistentChangelist> changelists = new HashSet<PersistentChangelist>();
        boolean includeUpstream = modifiers.contains(Modifier.INCLUDE_UPSTREAM);
        if (modifiers.contains(Modifier.SINCE_SUCCESS))
        {
            changelists.addAll(context.getChangesSinceLastSuccess());
            if (includeUpstream)
            {
                changelists.addAll(context.getUpstreamChangesSinceLastSuccess());
            }
        }
        else if (modifiers.contains(Modifier.SINCE_HEALTHY))
        {
            changelists.addAll(context.getChangesSinceLastHealthy());
            if (includeUpstream)
            {
                changelists.addAll(context.getUpstreamChangesSinceLastHealthy());
            }
        }
        else
        {
            changelists.addAll(context.getChanges());
            if (includeUpstream)
            {
                changelists.addAll(context.getUpstreamChanges());
            }
        }

        Predicate<PersistentChangelist> predicate = modifiers.contains(Modifier.BY_ME) ? new ByMePredicate(user) : new TruePredicate<PersistentChangelist>();
        return CollectionUtils.contains(changelists, predicate);
    }
}
