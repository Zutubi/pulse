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
        // these tests use the remote api exclusively, but does not test the xmlrpc interface.
        suite.addTestSuite(LoggingAcceptanceTest.class);
        suite.addTestSuite(NotificationAcceptanceTest.class);
        suite.addTestSuite(CustomProjectAcceptanceTest.class);
        suite.addTestSuite(PluginRepositoryAcceptanceTest.class);

        suite.addTestSuite(RssAcceptanceTest.class);
        suite.addTestSuite(HibernateStatisticsAcceptanceTest.class);

        //---( web ui acceptance tests )---
        suite.addTestSuite(AgentsSectionAcceptanceTest.class);
        suite.addTestSuite(ConfigUIAcceptanceTest.class);
        suite.addTestSuite(DeleteAcceptanceTest.class);
        suite.addTestSuite(CloneAcceptanceTest.class);
        suite.addTestSuite(PullUpAcceptanceTest.class);
        suite.addTestSuite(PushDownAcceptanceTest.class);
        suite.addTestSuite(CollapsedCollectionAcceptanceTest.class);
        suite.addTestSuite(ConfigLinksAcceptanceTest.class);
        suite.addTestSuite(ConfigActionsAcceptanceTest.class);
        suite.addTestSuite(ConfigStateAcceptanceTest.class);
        suite.addTestSuite(PluginUIAcceptanceTest.class);
        suite.addTestSuite(GroupAcceptanceTest.class);
        suite.addTestSuite(ProjectLabelAcceptanceTest.class);
        suite.addTestSuite(ProjectPermissionsAcceptanceTest.class);
        suite.addTestSuite(AnonymousAccessAcceptanceTest.class);
        suite.addTestSuite(LicenseAcceptanceTest.class);
        suite.addTestSuite(DashboardAcceptanceTest.class);
        suite.addTestSuite(ProjectLifecycleAcceptanceTest.class);
        suite.addTestSuite(ProjectsSummaryAcceptanceTest.class);
        suite.addTestSuite(ServerSectionAcceptanceTest.class);
        suite.addTestSuite(ServerActivityAcceptanceTest.class);
        suite.addTestSuite(BrowseScmAcceptanceTest.class);
        suite.addTestSuite(ProjectReportsAcceptanceTest.class);
        suite.addTestSuite(ProjectWorkingCopyAcceptanceTest.class);
        suite.addTestSuite(UsersAcceptanceTest.class);
        suite.addTestSuite(InternationalisationAcceptanceTest.class);

        //---( build tests )---
        suite.addTestSuite(BuildAcceptanceTest.class);
        suite.addTestSuite(BuildNavigationAcceptanceTest.class);
        suite.addTestSuite(BuildCommentAcceptanceTest.class);
        suite.addTestSuite(BuildResponsibilityAcceptanceTest.class);
        suite.addTestSuite(MavenAcceptanceTest.class);
        suite.addTestSuite(PerforceAcceptanceTest.class);
        suite.addTestSuite(BuildHookAcceptanceTest.class);
        suite.addTestSuite(PersonalBuildAcceptanceTest.class);

        //--( 2.1: dependency tests)--
        suite.addTest(DependenciesTestSuite.suite());

        suite.addTestSuite(CleanupAcceptanceTest.class);
        suite.addTestSuite(CleanupUIAcceptanceTest.class);

        //--( dev tools tests)--
        suite.addTestSuite(SynchroniseCommandAcceptanceTest.class);
        suite.addTestSuite(ExpandCommandAcceptanceTest.class);
        suite.addTestSuite(LocalBuildAcceptanceTest.class);
        suite.addTestSuite(PostProcessCommandAcceptanceTest.class);
        
        return suite;
    }
}
