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

package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.File;

public class DefaultScmContextFactoryTest extends PulseTestCase
{
    private DefaultScmContextFactory factory;
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        // i would like to mock this directory out, but can not see a way to do so with mockito - considering
        // that the context factory attempts to create a new file based on this file.  Maybe insert a
        // file system interface?
        tmp = createTempDirectory();

        factory = new DefaultScmContextFactory();
        factory.setProjectsDir(tmp);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        tmp = null;
        factory = null;

        super.tearDown();
    }

    public void testScmContextCorrectlyConfigured() throws ScmException
    {
/*
        ScmContext context = factory.createContext(1, null);
        File expectedDir = new File(tmp, FileSystemUtils.join("1", "scm"));
        assertEquals(expectedDir, context.getPersistentWorkDir());
*/
    }
}
