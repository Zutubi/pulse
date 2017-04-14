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

package com.zutubi.pulse.master.tove.config.project.commit;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;

import java.util.Arrays;

/**
 */
public class LinkTransformerConfigurationTest extends PulseTestCase
{
    public void testFixedLink()
    {
        helper("[A-Z]+", "http://example.com", "link HERE please", "link <a href='http://example.com'>HERE</a> please");
    }

    public void testGroups()
    {
        helper("[A-Z]+-([0-9]+)", "http://example.com/$1", "link HERE-001 please", "link <a href='http://example.com/001'>HERE-001</a> please");
    }

    private void helper(String expression, String url, String text, String expected)
    {
        CommitMessageSupport support = new CommitMessageSupport(text, Arrays.<CommitMessageTransformerConfiguration>asList(new LinkTransformerConfiguration("test", expression, url)));
        assertEquals(expected, support.toString());
    }
}
