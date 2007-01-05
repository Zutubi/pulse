package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.committransformers.LinkCommitMessageTransformer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class CommitMessageSupportTest extends PulseTestCase
{
    private List<CommitMessageTransformer> transformers;

    protected void setUp() throws Exception
    {
        super.setUp();

        transformers = new LinkedList<CommitMessageTransformer>();
        transformers.add(new LinkCommitMessageTransformer("foo", "foo+", "http://foo/$0"));
        transformers.add(new LinkCommitMessageTransformer("bug", "bug ([0-9]+)", "http://bugs/$1"));
        transformers.add(new LinkCommitMessageTransformer("bad", "bad", "http://$1"));
        CommitMessageTransformer limited = new LinkCommitMessageTransformer("limited", "limited", "http://lim/");
        limited.setProjects(Arrays.asList(new Long[] {(long) 1, (long) 3}));
        transformers.add(limited);
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

    public void testLimitedToProjects()
    {
        assertReplacement("limited", "<a href='http://lim/'>limited</a>", 1, 0);
        assertReplacement("limited", "limited", 2, 0);
    }

    public void testEscapesHTML()
    {
        assertReplacement("<foo&", "&lt;<a href='http://foo/foo'>foo</a>&amp;");
    }

    public void testTrim()
    {
        assertReplacement("some text", "som...", 0, 6);
    }

    public void testTrimmedLink()
    {
        assertReplacement("bug 123", "<a href='http://bugs/123'>bug...</a>", 0, 6);
    }

    public void testTrimmedLinkJustFits()
    {
        assertReplacement("bug 123 is really cool", "<a href='http://bugs/123'>bug 123</a>...", 0, 10);
    }

    private void assertReplacement(String input, String replacement)
    {
        assertReplacement(input, replacement, 0, 0);
    }

    private void assertReplacement(String input, String replacement, long project, int limit)
    {
        Revision rev = new Revision("author", input, 0, "rev");
        Changelist list = new Changelist("uid", rev);
        list.addProjectId(project);

        CommitMessageSupport support = new CommitMessageSupport(list, transformers);

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
