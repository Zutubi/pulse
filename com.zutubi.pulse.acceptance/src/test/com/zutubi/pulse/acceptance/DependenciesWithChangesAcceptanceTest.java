package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildChangesPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.acceptance.pages.browse.ViewChangelistPage;
import com.zutubi.pulse.acceptance.utils.BuildRunner;
import com.zutubi.pulse.acceptance.utils.TriviAntProject;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.util.adt.Pair;

import java.util.*;

import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * Acceptance tests for interactions between dependencies and changes between builds.
 */
public class DependenciesWithChangesAcceptanceTest extends AcceptanceTestBase
{
    private BuildRunner buildRunner;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
        buildRunner = new BuildRunner(rpcClient.RemoteApi);
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testChangeToUpstreamBuild() throws Exception
    {
        TriviAntProject upstream = projectConfigurations.createTrivialAntProject(random + "-upstream");
        CONFIGURATION_HELPER.insertProject(upstream.getConfig(), false);
        TriviAntProject downstream = projectConfigurations.createTrivialAntProject(random + "-downstream");
        downstream.addDependency(upstream);
        CONFIGURATION_HELPER.insertProject(downstream.getConfig(), false);

        // Run two upstream builds with a change between.  Wait for the second triggered downstream
        // build to complete.
        buildRunner.triggerSuccessfulBuild(upstream);
        upstream.editAndCommitBuildFile("a change", random);
        buildRunner.triggerSuccessfulBuild(upstream);
        rpcClient.RemoteApi.waitForBuildToComplete(downstream.getName(), 2);

        // Check changes via the API.
        Vector<Hashtable<String,Object>> changes = rpcClient.RemoteApi.getChangesInBuild(downstream.getName(), 2, false);
        assertEquals(1, changes.size());
        Hashtable<String, Object> change = changes.get(0);
        assertEquals("a change", change.get("comment"));
        assertNull(change.get("via"));

        changes = rpcClient.RemoteApi.getChangesInBuild(downstream.getName(), 2, true);
        assertEquals(2, changes.size());
        change = changes.get(0);
        assertEquals("a change", change.get("comment"));
        assertNull(change.get("via"));
        assertUpstreamChange(changes.get(1), "a change", upstream.getName() + ":2");

        // Now check the upstream change appears in the changes affecting the downstream build.
        getBrowser().loginAsAdmin();
        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, downstream.getName(), 2L);
        assertEquals(1, changesPage.getUpstreamChangeCount());
        Changelist changelist = changesPage.getUpstreamChangelists().get(0);
        assertEquals("a change", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());
        assertEquals("/accept/trunk/triviant/build.xml", changelist.getChanges().get(0).getPath());

        assertEquals(asList(asList(asPair(upstream.getName(), 2L))), changesPage.getUpstreamChangeVia(1));
        
        // Verify that the downstream build appears in the builds affected by the change.  It should
        // appear twice - it is directly affected and also indirectly via upstream.
        ViewChangelistPage changelistPage = getBrowser().openAndWaitFor(ViewChangelistPage.class,
                upstream.getName(), 2L, changesPage.getChangeIds().get(0), changelist.getRevision().toString());
        assertEquals(asList(
                new BuildInfo(downstream.getName(), 2, ResultState.SUCCESS, null),
                new BuildInfo(upstream.getName(), 2, ResultState.SUCCESS, null),
                new BuildInfo(downstream.getName(), 2, ResultState.SUCCESS, null)
        ), changelistPage.getBuilds());
    }

    public void testSameChangeToMultipleUpstreamBuilds() throws Exception
    {
        // util <-+
        //  ^      \
        //  |      app
        //  |      /
        // lib  <-+
        TriviAntProject util = projectConfigurations.createTrivialAntProject(random + "-util");
        CONFIGURATION_HELPER.insertProject(util.getConfig(), false);
        TriviAntProject lib = projectConfigurations.createTrivialAntProject(random + "-lib");
        lib.addDependency(util);
        CONFIGURATION_HELPER.insertProject(lib.getConfig(), false);
        TriviAntProject app = projectConfigurations.createTrivialAntProject(random + "-app");
        app.addDependency(util);
        app.addDependency(lib);
        CONFIGURATION_HELPER.insertProject(app.getConfig(), false);

        // Run two util builds with a change between.  Wait for the second triggered app build to
        // complete.  Note the projects all use the same repo, so to ensure they all see this change
        // we need to wait out the first build off app before making the change.
        buildRunner.triggerSuccessfulBuild(util);
        rpcClient.RemoteApi.waitForBuildToComplete(app.getName(), 1);
        util.editAndCommitBuildFile("multi ball", random);
        buildRunner.triggerSuccessfulBuild(util);
        rpcClient.RemoteApi.waitForBuildToComplete(app.getName(), 2);

        // Check changes via the API.
        Vector<Hashtable<String,Object>> changes = rpcClient.RemoteApi.getChangesInBuild(app.getName(), 2, false);
        assertEquals(1, changes.size());
        Hashtable<String, Object> change = changes.get(0);
        assertEquals("multi ball", change.get("comment"));
        assertNull(change.get("via"));

        changes = rpcClient.RemoteApi.getChangesInBuild(app.getName(), 2, true);
        assertEquals(2, changes.size());
        change = changes.get(0);
        assertEquals("multi ball", change.get("comment"));
        assertNull(change.get("via"));
        assertUpstreamChange(changes.get(1), "multi ball", util.getName() + ":2", lib.getName() + ":2", lib.getName() + ":2/" + util.getName() + ":2");

        // Now check the upstream change appears via multiple paths when looking at the changes to
        // the app project.
        getBrowser().loginAsAdmin();
        BuildChangesPage changesPage = getBrowser().openAndWaitFor(BuildChangesPage.class, app.getName(), 2L);
        assertEquals(1, changesPage.getUpstreamChangeCount());
        Changelist changelist = changesPage.getUpstreamChangelists().get(0);
        assertEquals("multi ball", changelist.getComment());

        List<List<Pair<String,Long>>> vias = changesPage.getUpstreamChangeVia(1);
        // Sort with longer vias last, those of equal length sorted by project name.
        Collections.sort(vias, new Comparator<List<Pair<String, Long>>>()
        {
            public int compare(List<Pair<String, Long>> o1, List<Pair<String, Long>> o2)
            {
                if (o1.size() != o2.size())
                {
                    return o1.size() - o2.size();
                }

                for (int i = 0; i < o1.size(); i++)
                {
                    int nameComp = o1.get(i).first.compareTo(o2.get(i).first);
                    if (nameComp != 0)
                    {
                        return nameComp;
                    }
                }

                return 0;
            }
        });

        assertEquals(asList(
                asList(asPair(lib.getName(), 2L)),
                asList(asPair(util.getName(), 2L)),
                asList(asPair(lib.getName(), 2L), asPair(util.getName(), 2L))
        ), vias);

        // Verify that the lib and app builds appear via all possible paths in the builds affected
        // by the change.
        ViewChangelistPage changelistPage = getBrowser().openAndWaitFor(ViewChangelistPage.class,
                util.getName(), 2L, changesPage.getChangeIds().get(0), changelist.getRevision().toString());
        assertEquals(asList(
                new BuildInfo(app.getName(), 2, ResultState.SUCCESS, null),
                new BuildInfo(lib.getName(), 2, ResultState.SUCCESS, null),
                  new BuildInfo(app.getName(), 2, ResultState.SUCCESS, null),
                new BuildInfo(util.getName(), 2, ResultState.SUCCESS, null),
                  new BuildInfo(app.getName(), 2, ResultState.SUCCESS, null),
                  new BuildInfo(lib.getName(), 2, ResultState.SUCCESS, null),
                    new BuildInfo(app.getName(), 2, ResultState.SUCCESS, null)
        ), changelistPage.getBuilds());
    }

    private void assertUpstreamChange(Hashtable<String, Object> change, String comment, String... viaPaths)
    {
        assertEquals(comment, change.get("comment"));
        Vector<Vector<Hashtable<String, Object>>> via = (Vector<Vector<Hashtable<String, Object>>>) change.get("via");
        assertNotNull(via);
        assertEquals(viaPaths.length, via.size());
        List<String> viaList = asList(viaPaths);
        for (int i = 0; i < viaPaths.length; i++)
        {
            // Render paths as strings for simplicity: <project>:<build>/<project>:<build>...
            StringBuilder pathBuilder = new StringBuilder();

            Vector<Hashtable<String, Object>> path = via.get(i);
            for (Hashtable<String, Object> pathEntry : path)
            {
                if (pathBuilder.length() > 0)
                {
                    pathBuilder.append("/");
                }

                pathBuilder.append(pathEntry.get("project"));
                pathBuilder.append(":");
                pathBuilder.append(pathEntry.get("id"));
            }

            assertThat(viaList, hasItem(pathBuilder.toString()));
        }
    }
}
