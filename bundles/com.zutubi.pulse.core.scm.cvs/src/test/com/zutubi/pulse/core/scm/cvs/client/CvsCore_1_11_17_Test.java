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

package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.pulse.core.scm.cvs.CvsTestUtils;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import org.netbeans.lib.cvsclient.CVSRoot;

public class CvsCore_1_11_17_Test extends PulseTestCase
{
    private CvsCore cvs;

    protected void setUp() throws Exception
    {
        super.setUp();

        String password = CvsTestUtils.getPassword("cvs-1.11.17");
        assertNotNull(password);

        cvs = new CvsCore();
        cvs.setRoot(CVSRoot.parse(":ext:cvs-1.11.17:" + password + "@zutubi.com:/cvsroots/cvs-1.11.17"));
    }

    public void testVersion() throws ScmException
    {
        assertEquals("1.11.17", cvs.version());
    }
}
