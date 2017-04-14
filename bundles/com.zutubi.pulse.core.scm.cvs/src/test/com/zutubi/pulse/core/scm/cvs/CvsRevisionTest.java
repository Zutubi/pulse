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

package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class CvsRevisionTest extends PulseTestCase
{
    private DateFormat dateFormat = new SimpleDateFormat(CvsRevision.DATE_AND_TIME_FORMAT_STRING);

    public void testGetRevisionNoAuthor() throws ScmException, ParseException
    {
        CvsRevision revision = new CvsRevision(":BRANCH:20070201-01:02:33");
        assertNull(revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertEquals(dateFormat.parse("20070201-01:02:33"), revision.getDate());
        assertEquals(":BRANCH:20070201-01:02:33", revision.getRevisionString());
    }

    public void testGetRevisionNoBranch() throws ScmException, ParseException
    {
        CvsRevision revision = new CvsRevision("author::20070201-01:02:33");
        assertEquals("author", revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(dateFormat.parse("20070201-01:02:33"), revision.getDate());
        assertEquals("author::20070201-01:02:33", revision.getRevisionString());
    }

    public void testGetRevisionDateOnly() throws ScmException, ParseException
    {
        CvsRevision revision = new CvsRevision("20070201-01:02:33");
        assertNull(revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(dateFormat.parse("20070201-01:02:33"), revision.getDate());
        assertEquals("::20070201-01:02:33", revision.getRevisionString());
    }

    public void testGetRevisionDayOnly() throws ScmException, ParseException
    {
        CvsRevision revision = new CvsRevision("20070201");
        assertNull(revision.getAuthor());
        assertNull(revision.getBranch());
        assertEquals(dateFormat.parse("20070201-00:00:00"), revision.getDate());
        assertEquals("::20070201-00:00:00", revision.getRevisionString());
    }

    public void testGetRevisionTooManyPieces() throws ScmException, ParseException
    {
        try
        {
            new CvsRevision("1:2:3:4:5:6:7");
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Invalid CVS revision '1:2:3:4:5:6:7' cannot parse '3:4:5:6:7' as a date", e.getMessage());
        }
    }

    public void testGetRevisionJustColon() throws ScmException, ParseException
    {
        try
        {
            new CvsRevision(":");
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Invalid CVS revision ':' (must be a date, or <author>:<branch>:<date>)", e.getMessage());
        }
    }
    
    public void testGetRevisionSingleColon() throws ScmException, ParseException
    {
        try
        {
            new CvsRevision(":branch");
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Invalid CVS revision ':branch' (must be a date, or <author>:<branch>:<date>)", e.getMessage());
        }
    }

    public void testGetRevisionInvalidDate() throws ScmException, ParseException
    {
        try
        {
            new CvsRevision("baddate");
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Invalid CVS revision 'baddate' (must be a date, or <author>:<branch>:<date>)", e.getMessage());
        }
    }

    public void testGetRevisionInvalidDateAfterColons() throws ScmException, ParseException
    {
        try
        {
            new CvsRevision("::baddate");
            fail();
        }
        catch (ScmException e)
        {
            assertEquals("Invalid CVS revision '::baddate' cannot parse 'baddate' as a date", e.getMessage());
        }
    }

    public void testGetRevisionForHead() throws ScmException
    {
        CvsRevision revision = new CvsRevision("::");
        assertNull(revision.getAuthor());
        assertNull(revision.getBranch());
        assertNull(revision.getDate());
        assertEquals("::", revision.getRevisionString());
    }

    public void testGetRevisionBranchOnly() throws ScmException
    {
        CvsRevision revision = new CvsRevision(":BRANCH:");
        assertNull(revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertNull(revision.getDate());
        assertEquals(":BRANCH:", revision.getRevisionString());
    }

    public void testSanity() throws ScmException, ParseException
    {
        CvsRevision revision = new CvsRevision("author:BRANCH:20070201-01:02:33");
        assertEquals("author", revision.getAuthor());
        assertEquals("BRANCH", revision.getBranch());
        assertEquals(dateFormat.parse("20070201-01:02:33"), revision.getDate());
        assertEquals("author:BRANCH:20070201-01:02:33", revision.getRevisionString());
    }
}
