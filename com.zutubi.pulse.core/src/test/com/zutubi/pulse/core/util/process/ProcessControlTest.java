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

package com.zutubi.pulse.core.util.process;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class ProcessControlTest extends PulseTestCase
{
    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(getName(), "");
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testDestroyTree() throws Exception
    {
        if (ProcessControl.isNativeDestroyAvailable())
        {
            File buildFile = copyInputToDirectory("xml", tempDir);
            String ant = SystemUtils.IS_WINDOWS ? "ant.bat" : "ant";
            ProcessBuilder processBuilder = new ProcessBuilder(ant, "-f", buildFile.getAbsolutePath(), "doit");
            processBuilder.redirectErrorStream(true);
            final Process p = processBuilder.start();

            Thread killer = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e)
                    {
                        // noop
                    }
                    ProcessControl.destroyProcess(p);
                }
            });
            killer.start();

            try
            {
                InputStream is = p.getInputStream();
                byte[] buffer = new byte[1024];
                while (is.read(buffer) > 0) {}
            }
            catch (IOException e)
            {
                // Can be raised when the process is killed.
            }

            p.waitFor();
            killer.join();
        }
    }
    
    public void testGetPid() throws IOException, InterruptedException
    {
        if (ProcessControl.isPidAvailable())
        {
            ProcessBuilder builder = new ProcessBuilder("sh");
            Process p = builder.start();
            assertTrue(ProcessControl.getPid(p) != 0);
            ProcessControl.destroyProcess(p);
            p.waitFor();
        }
    }
}
