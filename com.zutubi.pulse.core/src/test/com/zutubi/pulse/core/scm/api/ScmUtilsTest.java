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

package com.zutubi.pulse.core.scm.api;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ScmUtilsTest extends PulseTestCase
{
    public void testFiltering()
    {
        Changelist c = getChangelist("a.txt", "b.txt", "c.xml");
        List<Changelist> changelists = ScmUtils.filter(Arrays.asList(c), newPathFilter(".xml"));
        assertEquals(1, changelists.size());
        assertEquals(1, changelists.get(0).getChanges().size());

        changelists = ScmUtils.filter(Arrays.asList(c), newPathFilter(".txt"));
        assertEquals(1, changelists.size());
        assertEquals(2, changelists.get(0).getChanges().size());

        changelists = ScmUtils.filter(Arrays.asList(c), newPathFilter(".ftl"));
        assertEquals(0, changelists.size());
    }

    private Predicate<String> newPathFilter(final String endsWith)
    {
        return new Predicate<String>()
        {
            public boolean apply(String path)
            {
                return path.endsWith(endsWith);
            }
        };
    }

    private Changelist getChangelist(String... filePaths)
    {
        return new Changelist(new Revision(), 0, null, null, getFiles(filePaths));
    }

    private List<FileChange> getFiles(String... paths)
    {
        List<FileChange> files = new LinkedList<FileChange>();
        for (String path : paths)
        {
            files.add(new FileChange(path, null, null));
        }
        return files;
    }
}
