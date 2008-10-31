package com.zutubi.pulse.acceptance;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.zutubi.pulse.acceptance.support.JythonPackageFactoryTest;
import com.zutubi.pulse.acceptance.plugins.PluginUpgradeManagerAcceptanceTest;
import com.zutubi.pulse.acceptance.plugins.PostProcessorPluginAcceptanceTest;

/**
 * Collection of all acceptance tests, mainly required to ensure the setup
 * test runs first.
 */
public class AcceptanceTestSuite
{
    public static Test suite()
    {
        //---( other acceptance tests )---

        TestSuite main = new TestSuite();
        main.addTestSuite(JythonPackageFactoryTest.class); // check the support code works before running the acceptance test suite.
        main.addTestSuite(PluginUpgradeManagerAcceptanceTest.class);
        main.addTestSuite(PostProcessorPluginAcceptanceTest.class);
        main.addTest(new AcceptanceTestSuiteSetupTeardown(DevAcceptanceTestSuite.suite()));
        main.addTestSuite(AgentUpgradeAcceptanceTest.class);
        main.addTestSuite(StartupShutdownAcceptanceTest.class);

        return main;
    }

}
