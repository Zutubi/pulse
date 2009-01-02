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
