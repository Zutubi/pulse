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

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.addClassToSuite;
import junit.framework.TestSuite;

public class DevAcceptanceTestSuite
{
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();
        // need to run this one first as it runs through the installation setup.
        addClassToSuite(suite, SetupAcceptanceTest.class);

        // now we can run the rest of the tests.

        //---( xml rpc acceptance tests )---
        addClassToSuite(suite, ConfigXmlRpcAcceptanceTest.class);
        addClassToSuite(suite, ProjectXmlRpcAcceptanceTest.class);
        addClassToSuite(suite, ReportingXmlRpcAcceptanceTest.class);
        addClassToSuite(suite, MonitorXmlRpcAcceptanceTest.class);
        addClassToSuite(suite, ResourceXmlRpcAcceptanceTest.class);
        // these tests use the remote api exclusively, but does not test the xmlrpc interface.
        addClassToSuite(suite, LoggingAcceptanceTest.class);
//        AcceptanceTestUtils.addClassToSuite(suite, NotificationAcceptanceTest.class);
        addClassToSuite(suite, CustomProjectAcceptanceTest.class);
        addClassToSuite(suite, PluginRepositoryAcceptanceTest.class);

        addClassToSuite(suite, RssAcceptanceTest.class);
        addClassToSuite(suite, HibernateStatisticsAcceptanceTest.class);

        //---( web ui acceptance tests )---
        addClassToSuite(suite, AgentsSectionAcceptanceTest.class);
        addClassToSuite(suite, AgentStatusAcceptanceTest.class);
        addClassToSuite(suite, AgentCommentAcceptanceTest.class);
        addClassToSuite(suite, AgentHistoryAcceptanceTest.class);
        addClassToSuite(suite, ConfigUIAcceptanceTest.class);
        addClassToSuite(suite, DeleteAcceptanceTest.class);
        addClassToSuite(suite, CloneAcceptanceTest.class);
        addClassToSuite(suite, PullUpAcceptanceTest.class);
        addClassToSuite(suite, PushDownAcceptanceTest.class);
        addClassToSuite(suite, CollapsedCollectionAcceptanceTest.class);
        addClassToSuite(suite, ConfigLinksAcceptanceTest.class);
        addClassToSuite(suite, ConfigActionsAcceptanceTest.class);
        addClassToSuite(suite, ConfigStateAcceptanceTest.class);
        addClassToSuite(suite, PluginUIAcceptanceTest.class);
        addClassToSuite(suite, GroupAcceptanceTest.class);
        addClassToSuite(suite, ProjectLabelAcceptanceTest.class);
        addClassToSuite(suite, ProjectPermissionsAcceptanceTest.class);
        addClassToSuite(suite, AnonymousAccessAcceptanceTest.class);
        addClassToSuite(suite, UserAccessAcceptanceTest.class);
        addClassToSuite(suite, DashboardAcceptanceTest.class);
        addClassToSuite(suite, ProjectHomeAcceptanceTest.class);
        addClassToSuite(suite, ProjectHistoryAcceptanceTest.class);
        addClassToSuite(suite, ProjectLifecycleAcceptanceTest.class);
        addClassToSuite(suite, ProjectsSummaryAcceptanceTest.class);
        addClassToSuite(suite, ServerActivityAcceptanceTest.class);
        addClassToSuite(suite, ServerHistoryAcceptanceTest.class);
        addClassToSuite(suite, ServerMessagesAcceptanceTest.class);
        addClassToSuite(suite, ServerInfoAcceptanceTest.class);
        addClassToSuite(suite, BrowseScmAcceptanceTest.class);
        addClassToSuite(suite, ProjectReportsAcceptanceTest.class);
        addClassToSuite(suite, ProjectWorkingCopyAcceptanceTest.class);
        addClassToSuite(suite, UsersAcceptanceTest.class);
        addClassToSuite(suite, RememberMeAcceptanceTest.class);
        addClassToSuite(suite, AccessAcceptanceTest.class);
        if (AcceptanceTestUtils.includeTestClass(LdapAcceptanceTest.class))
        {
            suite.addTest(new EnableLdapTestSetup(new TestSuite(LdapAcceptanceTest.class)));
        }
        addClassToSuite(suite, InternationalisationAcceptanceTest.class);

        //---( build tests )---
        addClassToSuite(suite, BuildAcceptanceTest.class);
        addClassToSuite(suite, BuildNavigationAcceptanceTest.class);
        addClassToSuite(suite, BuildCommentAcceptanceTest.class);
        addClassToSuite(suite, BuildResponsibilityAcceptanceTest.class);
        addClassToSuite(suite, BuildPinningAcceptanceTest.class);
        addClassToSuite(suite, MavenAcceptanceTest.class);
        addClassToSuite(suite, PerforceAcceptanceTest.class);
        addClassToSuite(suite, BuildHookAcceptanceTest.class);
        addClassToSuite(suite, PersonalBuildAcceptanceTest.class);
        addClassToSuite(suite, BuildCommandEnableDisableAcceptanceTest.class);
        addClassToSuite(suite, BuildStageEnableDisableAcceptanceTest.class);
        addClassToSuite(suite, BuildPriorityAcceptanceTest.class);

        //--( 2.1: dependency tests)--
        suite.addTest(DependenciesTestSuite.suite());

        addClassToSuite(suite, CleanupAcceptanceTest.class);
        addClassToSuite(suite, CleanupUIAcceptanceTest.class);

        //--( dev tools tests)--
        addClassToSuite(suite, SynchroniseCommandAcceptanceTest.class);
        addClassToSuite(suite, ExpandCommandAcceptanceTest.class);
        addClassToSuite(suite, LocalBuildAcceptanceTest.class);
        addClassToSuite(suite, PersonalBuildCommandAcceptanceTest.class);
        addClassToSuite(suite, PostProcessCommandAcceptanceTest.class);

        return suite;
    }
}
