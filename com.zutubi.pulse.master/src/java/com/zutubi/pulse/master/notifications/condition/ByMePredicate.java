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

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * A predicate to test if a changelist includes is by a given user.
 */
public class ByMePredicate implements Predicate<PersistentChangelist>
{
    private UserConfiguration user;

    public ByMePredicate(UserConfiguration user)
    {
        this.user = user;
    }

    public boolean apply(PersistentChangelist changelist)
    {
        String author = changelist.getAuthor();
        if (author.equals(user.getLogin()))
        {
            return true;
        }

        for (String alias: user.getPreferences().getAliases())
        {
            if (author.equals(alias))
            {
                return true;
            }
        }

        return false;
    }
}
