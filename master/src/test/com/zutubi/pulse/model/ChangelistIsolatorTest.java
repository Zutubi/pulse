package com.zutubi.pulse.model;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMClient;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class ChangelistIsolatorTest extends PulseTestCase
{
    private ChangelistIsolator isolator;
    private Mock mockBuildManager;
    private BuildManager buildManager;
    private Mock mockScm;
    private SCMClient scmClient;
    private Project project;
    private BuildSpecification buildSpecification;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockBuildManager = new Mock(BuildManager.class);
        mockScm = new Mock(SCMClient.class);

        buildSpecification = new BuildSpecification("default");
        buildSpecification.setIsolateChangelists(true);

        project = new Project("myproject", "mydesc", new CustomPulseFileDetails());
        project.setScm(new Scm()
        {
            public SCMClient createServer() throws SCMException
            {
                return scmClient;
            }

            public String getType()
            {
                return "mock";
            }

            public Map<String, String> getRepositoryProperties()
            {
                throw new RuntimeException("Method not implemented.");
            }
        });

    }

    public void testNeverBuilt() throws SCMException
    {
        returnNoMoreBuilds();
        returnLatestBuild(10);
        setupIsolator();
        expectRevisions(true, 10);
    }

    public void testPreviouslyBuilt() throws SCMException
    {
        returnBuild(55);
        returnRevisions(55, 56, 57);
        setupIsolator();
        expectRevisions(true, 56, 57);
    }

    public void testNoNewRevisions() throws SCMException
    {
        returnBuild(55);
        returnRevisions(55);
        setupIsolator();
        expectRevisions(false);
    }

    public void testNoNewRevisionsForced() throws SCMException
    {
        returnBuild(55);
        returnRevisions(55);
        setupIsolator();
        expectRevisions(true, 55);
    }

    public void testRemembersPreviousRevision() throws SCMException
    {
        returnBuild(101);
        returnRevisions(101, 102, 103, 104);
        returnRevisions(104, 105);
        setupIsolator();
        expectRevisions(true, 102, 103, 104);
        expectRevisions(true, 105);
    }

    public void testNullRevision() throws SCMException
    {
        returnNullDetails();
        returnNoMoreBuilds();
        returnLatestBuild(22);
        setupIsolator();
        expectRevisions(true, 22);
    }

    public void testSearchesBeyondNullRevision() throws SCMException
    {
        returnNullDetails();
        returnNullDetails();
        returnBuild(9);
        returnNoMoreBuilds();
        returnRevisions(9, 10, 11, 13);
        setupIsolator();
        expectRevisions(true, 10, 11, 13);
    }

    public void testReturnsDifferentRevisionObject() throws SCMException
    {
        Revision rev = returnBuild(10);
        returnRevisions(10);
        setupIsolator();

        List<Revision> gotRevisions = isolator.getRevisionsToRequest(project, buildSpecification, true);
        assertEquals(1, gotRevisions.size());
        Revision got = gotRevisions.get(0);
        assertEquals(rev.getRevisionString(), got.getRevisionString());
        assertNotSame(rev, got);
    }

    private Revision returnLatestBuild(long revision)
    {
        NumericalRevision rev = new NumericalRevision(revision);
        mockScm.expectAndReturn("getLatestRevision", C.ANY_ARGS, rev);
        return rev;
    }

    private void returnNoMoreBuilds()
    {
        mockBuildManager.expectAndReturn("queryBuilds", C.ANY_ARGS, new LinkedList<BuildResult>());
    }

    private void returnNullDetails()
    {
        BuildResult result = new BuildResult();
        mockBuildManager.expectAndReturn("queryBuilds", C.ANY_ARGS, Arrays.asList(new BuildResult[] { result }));
    }

    private Revision returnBuild(long revision)
    {
        BuildResult result = new BuildResult();
        NumericalRevision rev = new NumericalRevision(revision);
        result.setScmDetails(new BuildScmDetails(rev));
        mockBuildManager.expectAndReturn("queryBuilds", C.ANY_ARGS, Arrays.asList(new BuildResult[]{ result }));
        return rev;
    }

    private void returnRevisions(long since, long... revisions)
    {
        List<Revision> ret = new LinkedList<Revision>();
        for(long r: revisions)
        {
            ret.add(new NumericalRevision(r));
        }

        mockScm.expectAndReturn("getRevisionsSince", new NumericalRevision(since), ret);
    }

    private void setupIsolator()
    {
        buildManager = (BuildManager) mockBuildManager.proxy();
        scmClient = (SCMClient) mockScm.proxy();
        isolator = new ChangelistIsolator(buildManager);
    }

    private void expectRevisions(boolean force, long... revisions) throws SCMException
    {
        List<Revision> gotRevisions = isolator.getRevisionsToRequest(project, buildSpecification, force);
        assertEquals(revisions.length, gotRevisions.size());
        for(int i = 0; i < revisions.length; i++)
        {
            assertEquals(revisions[i], ((NumericalRevision)gotRevisions.get(i)).getRevisionNumber());
        }
    }


}
