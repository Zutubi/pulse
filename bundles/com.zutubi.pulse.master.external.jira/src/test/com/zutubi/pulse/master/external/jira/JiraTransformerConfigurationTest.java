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

package com.zutubi.pulse.master.external.jira;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;

import java.util.Arrays;

public class JiraTransformerConfigurationTest extends PulseTestCase
{
    public void testAnyKey()
    {
        JiraTransformerConfiguration transformer = new JiraTransformerConfiguration("http://jira.zutubi.com");
        helper(transformer, "Fixed CIB-123: easy as can be", "Fixed <a href='http://jira.zutubi.com/browse/CIB-123'>CIB-123</a>: easy as can be");
        helper(transformer, "Fixed ZUT-2: too", "Fixed <a href='http://jira.zutubi.com/browse/ZUT-2'>ZUT-2</a>: too");
    }

    public void testSpecificKeys()
    {
        JiraTransformerConfiguration transformer = new JiraTransformerConfiguration("http://jira.zutubi.com", "CIB");
        helper(transformer, "Fixed CIB-123: easy as can be", "Fixed <a href='http://jira.zutubi.com/browse/CIB-123'>CIB-123</a>: easy as can be");
        helper(transformer, "Fixed ZUT-2: too", "Fixed ZUT-2: too");
    }

    private void helper(CommitMessageTransformerConfiguration transformer, String text, String expected)
    {
        CommitMessageSupport support = new CommitMessageSupport(text, Arrays.asList(transformer));
        assertEquals(expected, support.toString());
    }
}
