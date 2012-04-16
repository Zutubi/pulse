package com.zutubi.pulse.acceptance;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.zutubi.pulse.acceptance.utils.Repository;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.addClassToSuite;

/**
 * This test suite includes all of the acceptance tests related to the
 * dependency feature set originally introduced in 2.1.
 *
 * These tests have been given their own suite as a way to
 * a) run them by themselves in development
 * b) help categorise the growing number of tests.
 */
public class DependenciesTestSuite
{
    public static Test suite()
    {
        TestSuite dependencySuite = new TestSuite();
        addClassToSuite(dependencySuite, DependenciesAcceptanceTest.class);
        addClassToSuite(dependencySuite, DependenciesConfigurationAcceptanceTest.class);
        addClassToSuite(dependencySuite, DependenciesWithChangesAcceptanceTest.class);
        addClassToSuite(dependencySuite, DependenciesUIAcceptanceTest.class);
        addClassToSuite(dependencySuite, RebuildDependenciesAcceptanceTest.class);
        addClassToSuite(dependencySuite, BuildVersionAcceptanceTest.class);
        addClassToSuite(dependencySuite, RepositoryPermissionsAcceptanceTest.class);

        // cleanup the artifact repository before continuing on.
        return new CleanArtifactRepository(dependencySuite);
    }

    private static class CleanArtifactRepository extends TestSetup
    {
        private Repository repository;

        private CleanArtifactRepository(Test test)
        {
            super(test);
        }

        protected void tearDown() throws Exception
        {
            repository = new Repository();
            repository.clean();

            super.tearDown();
        }
    }
}
