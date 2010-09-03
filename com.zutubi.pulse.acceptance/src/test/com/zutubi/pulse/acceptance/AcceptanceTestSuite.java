package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.plugins.PluginUpgradeManagerAcceptanceTest;
import com.zutubi.pulse.acceptance.plugins.PostProcessorPluginAcceptanceTest;
import com.zutubi.pulse.acceptance.support.jython.JythonPulseTestFactoryTest;
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
        main.addTestSuite(JythonPulseTestFactoryTest.class); // check the support code works before running the acceptance test suite.
        main.addTestSuite(StartupShutdownAcceptanceTest.class);
        main.addTestSuite(PluginUpgradeManagerAcceptanceTest.class);
        main.addTestSuite(PostProcessorPluginAcceptanceTest.class);
        main.addTest(new StartPulseTestSetup(DevAcceptanceTestSuite.suite()));
        main.addTestSuite(AgentUpgradeAcceptanceTest.class);

        return main;
    }

}
