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

package com.zutubi.pulse.master.tove.model;

import com.zutubi.util.Sort;

import java.util.Comparator;

/**
 * Compares action links by label.
 */
public class ActionLinkComparator implements Comparator<ActionLink>
{
    private Comparator<String> labelComparator = new Sort.StringComparator();

    public int compare(ActionLink link1, ActionLink link2)
    {
        return labelComparator.compare(link1.getLabel(), link2.getLabel());
    }
}
