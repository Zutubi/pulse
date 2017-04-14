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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.pulse.servercore.validation.annotation.SubscriptionCondition;

/**
 * A project build subscription condition configured with a custom boolean
 * expression.
 */
@SymbolicName("zutubi.customConditionConfig")
public class CustomConditionConfiguration extends SubscriptionConditionConfiguration
{
    @SubscriptionCondition
    private String customCondition;

    // Note this is a transient property, so we do not use the same name for
    // the actual expression property.
    public String getExpression()
    {
        return customCondition;
    }

    public String getCustomCondition()
    {
        return customCondition;
    }

    public void setCustomCondition(String customCondition)
    {
        this.customCondition = customCondition;
    }
}
