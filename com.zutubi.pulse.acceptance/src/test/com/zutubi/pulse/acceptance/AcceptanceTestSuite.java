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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collection of all acceptance tests, mainly required to ensure the setup
 * test runs first.
 */
public class AcceptanceTestSuite
{
    public static Test suite() throws Exception
    {
        //---( other acceptance tests )---

        TestSuite main = new TestSuite();
//        addClassToSuite(main, JythonPulseTestFactoryTest.class); // check the support code works before running the acceptance test suite.
//        addClassToSuite(main, StartupShutdownAcceptanceTest.class);
//        addClassToSuite(main, PluginUpgradeManagerAcceptanceTest.class);
//        addClassToSuite(main, PostProcessorPluginAcceptanceTest.class);
        main.addTest(new StartPulseTestSetup(DevAcceptanceTestSuite.suite()));
//        addClassToSuite(main, AgentUpgradeAcceptanceTest.class);
        return main;
    }

}
