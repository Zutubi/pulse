package com.zutubi.pulse.acceptance;

import junit.framework.TestSuite;

public class DevAcceptanceTestSuite
{
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();
        // need to run this one first as it runs through the installation setup.
        suite.addTestSuite(SetupAcceptanceTest.class);

        // now we can run the rest of the tests.

        //---( xml rpc acceptance tests )---
        suite.addTestSuite(ConfigXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ProjectXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ReportingXmlRpcAcceptanceTest.class);
        suite.addTestSuite(ResourceXmlRpcAcceptanceTest.class);

        // this test uses the remote api exclusively, but does not test the xmlrpc interface.
        suite.addTestSuite(NotificationAcceptanceTest.class);
        suite.addTestSuite(CustomProjectAcceptanceTest.class);

        //---( web ui acceptance tests )---
        suite.addTestSuite(ConfigUIAcceptanceTest.class);
        suite.addTestSuite(DeleteAcceptanceTest.class);
        suite.addTestSuite(CloneAcceptanceTest.class);
        suite.addTestSuite(ConfigActionsAcceptanceTest.class);
        suite.addTestSuite(PluginUIAcceptanceTest.class);
        suite.addTestSuite(GroupAcceptanceTest.class);
        suite.addTestSuite(ProjectLabelAcceptanceTest.class);
        suite.addTestSuite(ProjectPermissionsAcceptanceTest.class);
        suite.addTestSuite(AnonymousAccessAcceptanceTest.class);
        suite.addTestSuite(LicenseAcceptanceTest.class);
        suite.addTestSuite(DashboardAcceptanceTest.class);
        suite.addTestSuite(UserPreferencesAcceptanceTest.class);
        suite.addTestSuite(ProjectLifecycleAcceptanceTest.class);
        suite.addTestSuite(ProjectsSummaryAcceptanceTest.class);
        suite.addTestSuite(ServerSectionAcceptanceTest.class);
        suite.addTestSuite(ServerActivityAcceptanceTest.class);
        suite.addTestSuite(BrowseScmAcceptanceTest.class);

        //---( build tests )---
        suite.addTestSuite(BuildAcceptanceTest.class);
        suite.addTestSuite(PerforceAcceptanceTest.class);
        suite.addTestSuite(BuildHookAcceptanceTest.class);
        suite.addTestSuite(PersonalBuildAcceptanceTest.class);
        return suite;
    }
}
