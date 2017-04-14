/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.notifications.condition;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import static com.google.common.collect.Iterables.any;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

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

        public static class ToTextFunction implements Function<Modifier, String>
        {
            public String apply(Modifier modifier)
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

        Predicate<PersistentChangelist> predicate = modifiers.contains(Modifier.BY_ME) ? new ByMePredicate(user) : Predicates.<PersistentChangelist>alwaysTrue();
        return any(changelists, predicate);
    }
}
