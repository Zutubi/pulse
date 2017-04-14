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

package com.zutubi.pulse.core.model;

import java.util.Comparator;

/**
 * Orders changelists so the most recent comes first.
 */
public class ChangelistComparator implements Comparator<PersistentChangelist>
{
    public int compare(PersistentChangelist c1, PersistentChangelist c2)
    {
        int comp = c2.getDate().compareTo(c1.getDate());
        if (comp == 0)
        {
            // If dates match, compare by id (inverse).
            long id1 = c1.getId();
            long id2 = c2.getId();
            if (id2 < id1)
            {
                comp = -1;
            }
            else if (id2 > id1)
            {
                comp = 1;
            }
        }
        return comp;
    }
}
