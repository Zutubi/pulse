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

/**
 * A notify condition that applies a delegate condition to the previous build result.
 */
public class PreviousNotifyCondition implements NotifyCondition
{
    private NotifyCondition delegate;

    public PreviousNotifyCondition(NotifyCondition delegate)
    {
        this.delegate = delegate;
    }

    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        return delegate.satisfied(context.getPrevious(), user);
    }

    public NotifyCondition getDelegate()
    {
        return delegate;
    }
}
