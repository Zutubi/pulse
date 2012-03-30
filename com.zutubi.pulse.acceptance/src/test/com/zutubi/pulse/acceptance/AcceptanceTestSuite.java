package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.plugins.PluginUpgradeManagerAcceptanceTest;
import com.zutubi.pulse.acceptance.plugins.PostProcessorPluginAcceptanceTest;
import com.zutubi.pulse.acceptance.support.jython.JythonPulseTestFactoryTest;
import junit.framework.Test;
import junit.framework.TestSuite;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.addClassToSuite;

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
        addClassToSuite(main, JythonPulseTestFactoryTest.class); // check the support code works before running the acceptance test suite.
        addClassToSuite(main, StartupShutdownAcceptanceTest.class);
        addClassToSuite(main, PluginUpgradeManagerAcceptanceTest.class);
        addClassToSuite(main, PostProcessorPluginAcceptanceTest.class);
        main.addTest(new StartPulseTestSetup(DevAcceptanceTestSuite.suite()));
        addClassToSuite(main, AgentUpgradeAcceptanceTest.class);
        return main;
    }

}
