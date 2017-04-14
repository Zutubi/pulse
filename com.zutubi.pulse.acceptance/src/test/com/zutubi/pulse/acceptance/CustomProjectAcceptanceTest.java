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

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.concurrent.ConcurrentUtils;
import org.apache.xmlrpc.XmlRpcException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Acceptance tests for the custom project.
 */
public class CustomProjectAcceptanceTest extends AcceptanceTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testValidationPerformanceOfLargeCustomPulseFile() throws Exception
    {
        final String pulseFileString = readInputFully("pulseFile", "xml");

        String insertionPath = ConcurrentUtils.runWithTimeout(new Callable<String>()
        {
            public String call() throws Exception
            {
                return rpcClient.RemoteApi.insertProject(randomName(), ProjectManager.GLOBAL_PROJECT_NAME, false,
                                                         rpcClient.RemoteApi.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY),
                                                         rpcClient.RemoteApi.getCustomTypeConfig(pulseFileString));
            }
        }, 4, TimeUnit.SECONDS, null);
        
        assertNotNull(insertionPath);
    }

    public void testValidationPerformedOnCustomPulseFile() throws Exception
    {
        String pulseFileString = readInputFully("pulseFile", "xml");

        String invalidFragment = pulseFileString.substring(30, 300);

        try
        {
            rpcClient.RemoteApi.insertProject(randomName(), ProjectManager.GLOBAL_PROJECT_NAME, false,
                    rpcClient.RemoteApi.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY),
                    rpcClient.RemoteApi.getCustomTypeConfig(invalidFragment));
            fail("Invalid Pulse file did not generate an exception on project insert.");
        }
        catch (XmlRpcException e)
        {
            assertTrue(e.getMessage().contains("ValidationException"));
        }
    }
}
