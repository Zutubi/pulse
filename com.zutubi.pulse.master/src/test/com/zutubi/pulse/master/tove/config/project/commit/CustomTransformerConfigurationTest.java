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
public class CustomTransformerConfigurationTest extends PulseTestCase
{
    public void testFixedReplacement()
    {
        helper("you", "i", "you rock", "i rock");
    }

    public void testGroup()
    {
        helper("you", "$0 and i", "you rock", "you and i rock");
    }

    public void testNuke()
    {
        helper("f[a-z]{3}[^a-z]", "", "nuke any fish words that seem out of place", "nuke any words that seem out of place");
    }

    private void helper(String expression, String replacement, String text, String expected)
    {
        CommitMessageSupport support = new CommitMessageSupport(text, Arrays.<CommitMessageTransformerConfiguration>asList(new CustomTransformerConfiguration("test", expression, replacement)));
        assertEquals(expected, support.toString());
    }
}
