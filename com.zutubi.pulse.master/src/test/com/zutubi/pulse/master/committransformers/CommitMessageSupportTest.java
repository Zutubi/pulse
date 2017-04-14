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

package com.zutubi.pulse.master.committransformers;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.LinkTransformerConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class CommitMessageSupportTest extends PulseTestCase
{
    private List<CommitMessageTransformerConfiguration> transformers;

    protected void setUp() throws Exception
    {
        super.setUp();

        transformers = new LinkedList<CommitMessageTransformerConfiguration>();
        transformers.add(new LinkTransformerConfiguration("foo", "foo+", "http://foo/$0"));
        transformers.add(new LinkTransformerConfiguration("bug", "bug ([0-9]+)", "http://bugs/$1"));
        transformers.add(new LinkTransformerConfiguration("bad", "bad", "http://$1"));
    }

    public void testBasicReplacement()
    {
        assertReplacement("this is foo", "this is <a href='http://foo/foo'>foo</a>");
    }

    public void testGroupReplacement()
    {
        assertReplacement("fixed bug 123 with much effort", "fixed <a href='http://bugs/123'>bug 123</a> with much effort");
    }

    public void testMultipleReplacements()
    {
        assertReplacement("foo bug 1", "<a href='http://foo/foo'>foo</a> <a href='http://bugs/1'>bug 1</a>");
    }

    public void testInvalidGroup()
    {
        assertReplacement("bad", "bad");
    }

    public void testEscapesHTML()
    {
        assertReplacement("<foo&", "&lt;<a href='http://foo/foo'>foo</a>&amp;");
    }

    public void testTrim()
    {
        assertReplacement("some text", "som...", 6);
    }

    public void testTrimmedLink()
    {
        assertReplacement("bug 123", "<a href='http://bugs/123'>bug...</a>", 6);
    }

    public void testTrimmedLinkJustFits()
    {
        assertReplacement("bug 123 is really cool", "<a href='http://bugs/123'>bug 123</a>...", 10);
    }

    public void testDuplicatesFiltered()
    {
        LinkTransformerConfiguration transformer = new LinkTransformerConfiguration("dup", "[A-Z]+", "http://example.com/$0");
        LinkTransformerConfiguration transformer2 = new LinkTransformerConfiguration("dup", "[A-Z]+", "http://example.com/$0");
        transformers.add(transformer);
        transformers.add(transformer);
        transformers.add(transformer2);
        assertReplacement("link THIS", "link <a href='http://example.com/THIS'>THIS</a>");
    }

    private void assertReplacement(String input, String replacement)
    {
        assertReplacement(input, replacement, 0);
    }

    private void assertReplacement(String input, String replacement, int limit)
    {
        CommitMessageSupport support = new CommitMessageSupport(input, transformers);

        if(limit == 0)
        {
            assertEquals(replacement, support.wrap(80));
        }
        else
        {
            assertEquals(replacement, support.trim(limit));
        }
    }
}
