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

import com.zutubi.pulse.master.notifications.NotifyConditionContext;
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

    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        boolean ret = !disjunctive;

        for (NotifyCondition child : children)
        {
            if (child.satisfied(context, user) == disjunctive)
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
