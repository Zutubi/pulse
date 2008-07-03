package com.zutubi.pulse.acceptance;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collection of all acceptance tests, mainly required to ensure the setup
 * test runs first.
 */
public class AcceptanceTestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        // need to run this one first as it runs through the installation setup.
        suite.addTestSuite(SetupAcceptanceTest.class);

        // now we can run the rest of the tests.

        //---( xml rpc acceptance tests )---
        suite.addTestSuite(ConfigXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ProjectXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ReportingXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ResourceAcceptanceTest.class);

        //---( web ui acceptance tests )---
        suite.addTestSuite(ConfigUIAcceptanceTest.class);
        suite.addTestSuite(DeleteAcceptanceTest.class);
        suite.addTestSuite(CloneAcceptanceTest.class);
        suite.addTestSuite(ConfigActionsAcceptanceTest.class);
        suite.addTestSuite(PluginUIAcceptanceTest.class);
        suite.addTestSuite(GroupAcceptanceTest.class);
        suite.addTestSuite(ProjectLabelAcceptanceTest.class);
        suite.addTestSuite(AnonymousAccessAcceptanceTest.class);
        suite.addTestSuite(LicenseAcceptanceTest.class);
        suite.addTestSuite(DashboardAcceptanceTest.class);
        suite.addTestSuite(ServerSectionAcceptanceTest.class);

        //---( build tests )---
        suite.addTestSuite(BuildAcceptanceTest.class);
        suite.addTestSuite(BuildHookAcceptanceTest.class);
        suite.addTestSuite(PersonalBuildAcceptanceTest.class);

        //---( other acceptance tests )---
//        suite.addTestSuite(StartupShutdownAcceptanceTest.class); - tries to launch pulse inline, does not work.
        return suite;
    }
}
