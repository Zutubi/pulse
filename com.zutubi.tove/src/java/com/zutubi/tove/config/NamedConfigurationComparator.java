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

package com.zutubi.tove.config;

import com.zutubi.util.Sort;
import com.zutubi.tove.config.api.NamedConfiguration;

import java.util.Comparator;

/**
 */
public class NamedConfigurationComparator implements Comparator<NamedConfiguration>
{
    private Sort.StringComparator c = new Sort.StringComparator();

    public int compare(NamedConfiguration s1, NamedConfiguration s2)
    {
        return c.compare(s1.getName(), s2.getName());
    }
}
