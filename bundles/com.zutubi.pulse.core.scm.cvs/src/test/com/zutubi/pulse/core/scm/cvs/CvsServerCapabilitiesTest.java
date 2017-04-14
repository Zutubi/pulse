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

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class CvsServerCapabilitiesTest extends PulseTestCase
{
    protected void tearDown() throws Exception
    {
        System.clearProperty(CvsServerCapabilities.SUPPORT_REMOTE_LISTING);
        System.clearProperty(CvsServerCapabilities.SUPPORT_DATE_REVISION_ON_BRANCH);
        System.clearProperty(CvsServerCapabilities.SUPPORT_RLOG_SUPPRESS_HEADER);

        super.tearDown();
    }

    public void testRemoteListing()
    {
        assertTrue(CvsServerCapabilities.supportsRemoteListing("1.12.1"));
        assertFalse(CvsServerCapabilities.supportsRemoteListing("1.11.1"));
    }

    public void testDateRevisionOnBranch()
    {
        assertTrue(CvsServerCapabilities.supportsDateRevisionOnBranch("1.12.12"));
        assertFalse(CvsServerCapabilities.supportsDateRevisionOnBranch("1.11.1"));
        assertFalse(CvsServerCapabilities.supportsDateRevisionOnBranch("1.12.9"));
        assertFalse(CvsServerCapabilities.supportsDateRevisionOnBranch("1.12"));
    }

    public void testUnexpectedFormat()
    {
        assertFalse(CvsServerCapabilities.supportsDateRevisionOnBranch("unexpected cvs version format"));
        assertFalse(CvsServerCapabilities.supportsRemoteListing("unexpected cvs version format"));
    }

    public void testUserOverride()
    {
        assertTrue(CvsServerCapabilities.supportsRemoteListing("1.12.1"));
        System.setProperty(CvsServerCapabilities.SUPPORT_REMOTE_LISTING, "false");
        assertFalse(CvsServerCapabilities.supportsRemoteListing("1.12.1"));
    }
}
