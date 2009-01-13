package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.PATTERN_CHANGES;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.regex.Matcher;

public class PerforceCoreTest extends PulseTestCase
{
    public void testParseChangeSimple() throws ScmException
    {
        Revision revision = PerforceCore.parseChange("Change 91661 on 2009/01/13 by jblogs@some-client '   build fix godot'");
        assertEquals("91661", revision.getRevisionString());
    }

    public void testParseChangeNoComment() throws ScmException
    {
        Revision revision = PerforceCore.parseChange("Change 1234 on 2005/05/05 by five@fiver-client ''");
        assertEquals("1234", revision.getRevisionString());
    }

    public void testParseChangeUnrecognised()
    {
        try
        {
            PerforceCore.parseChange("nonsense");
            fail("Nonsense should not parse as a change");
        }
        catch (ScmException e)
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
