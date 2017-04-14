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
