package com.zutubi.pulse.tove.config.project.commit;

import com.zutubi.pulse.core.test.PulseTestCase;
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
