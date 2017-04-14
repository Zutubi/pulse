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

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import static java.util.Arrays.asList;
import java.util.List;

public class PersistentPlainFeatureTest extends PulseTestCase
{
    public void testGetSummaryLinesEmpty()
    {
        assertEquals(asList(""), getSummaryLines(""));
    }

    public void testGetSummaryLinesSingle()
    {
        assertEquals(asList("one line"), getSummaryLines("one line"));
    }

    public void testGetSummaryLinesMultiple()
    {
        assertEquals(asList("line one", "line two", "line three"), getSummaryLines("line one\nline two\nline three"));
    }

    public void testGetSummaryLinesJustNewline()
    {
        assertEquals(asList("", ""), getSummaryLines("\n"));
    }

    public void testGetSummaryLinesEmptyFirstLine()
    {
        assertEquals(asList("", "line two", "line three"), getSummaryLines("\nline two\nline three"));
    }

    public void testGetSummaryLinesEmptyMiddleLine()
    {
        assertEquals(asList("line one", "", "line three"), getSummaryLines("line one\n\nline three"));
    }

    public void testGetSummaryLinesEmptyLastLine()
    {
        assertEquals(asList("line one", "line two", ""), getSummaryLines("line one\nline two\n"));
    }

    private List<String> getSummaryLines(String summary)
    {
        PersistentPlainFeature feature = new PersistentPlainFeature(Feature.Level.ERROR, summary, 1);
        return feature.getSummaryLines();
    }
}
