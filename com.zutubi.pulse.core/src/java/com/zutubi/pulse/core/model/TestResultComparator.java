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

import com.zutubi.util.Sort;

import java.util.Comparator;

/**
 */
public class TestResultComparator implements Comparator<PersistentTestResult>
{
    private Comparator<String> packageComparator = new Sort.PackageComparator();

    public int compare(PersistentTestResult r1, PersistentTestResult r2)
    {
        if(r1.isSuite())
        {
            if(r2.isSuite())
            {
                return packageComparator.compare(r1.getName(), r2.getName());
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if(r2.isSuite())
            {
                return 1;
            }
            else
            {
                return packageComparator.compare(r1.getName(), r2.getName());
            }
        }
    }
}
