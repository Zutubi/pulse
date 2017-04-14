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

import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;

import java.io.File;

/**
 * Set of tests to verify the ability of the Perforce Client to communicate
 * with a unicode server.
 */
public class PerforceUnicodeTest extends PerforceTestBase
{
    private static final int P4D_PORT = 6666;

    private File workDir;

    private PerforceConfiguration configuration;
    private PerforceClient client;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        workDir = new File(tmpDir, "work");

        configuration = new PerforceConfiguration(Integer.toString(P4D_PORT), "test-user", "", "daniel-PC");
    }

    public void testNonUnicodeServerCheckout() throws Exception
    {
        deployPerforceServer("snapshot", P4D_PORT, 1, false);

        client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        client.checkout(createExecutionContext(workDir, false), null, null);

        assertTrue(new File(workDir, "readme.txt").isFile());
        assertTrue(new File(workDir, "doc/readme.txt").isFile());
    }

    public void testUnicodeServerCheckout() throws Exception
    {
        deployPerforceServer("snapshot", P4D_PORT, 1, true);
        configuration.setUnicodeServer(true);
        configuration.setCharset("utf8");

        client = new PerforceClient(configuration, new PerforceWorkspaceManager());
        client.checkout(createExecutionContext(workDir, false), null, null);

        assertTrue(new File(workDir, "readme.txt").isFile());
        assertTrue(new File(workDir, "doc/readme.txt").isFile());
    }
}
