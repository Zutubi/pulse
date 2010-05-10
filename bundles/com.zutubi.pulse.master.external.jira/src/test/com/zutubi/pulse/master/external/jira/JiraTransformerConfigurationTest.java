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
