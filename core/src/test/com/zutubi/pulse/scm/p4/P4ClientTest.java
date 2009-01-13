package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.scm.SCMException;
import static com.zutubi.pulse.scm.p4.P4Constants.PATTERN_CHANGES;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.regex.Matcher;

public class P4ClientTest extends PulseTestCase
{
    public void testParseChangeSimple() throws SCMException
    {
        NumericalRevision revision = P4Client.parseChange("Change 91661 on 2009/01/13 by jblogs@some-client '   build fix godot'");
        assertEquals("91661", revision.getRevisionString());
    }

    public void testParseChangeNoComment() throws SCMException
    {
        NumericalRevision revision = P4Client.parseChange("Change 1234 on 2005/05/05 by five@fiver-client ''");
        assertEquals("1234", revision.getRevisionString());
    }

    public void testParseChangeUnrecognised()
    {
        try
        {
            P4Client.parseChange("nonsense");
            fail("Nonsense should not parse as a change");
        }
        catch (SCMException e)
        {
            assertEquals("Unrecognised response from p4 changes 'nonsense'", e.getMessage());
        }
    }

    public void testChangeWithNoCommentGroup()
    {
        Matcher matcher = PATTERN_CHANGES.matcher("Change 1 on 2005/05/05 by a@b ''");
        assertTrue(matcher.find());
        assertEquals("", matcher.group(5));
    }
}
