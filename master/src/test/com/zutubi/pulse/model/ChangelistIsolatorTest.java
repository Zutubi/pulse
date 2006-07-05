package com.zutubi.pulse.model;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class ChangelistIsolatorTest extends PulseTestCase
{
    private ChangelistIsolator isolator;
    private Mock mockBuildManager;
    private BuildManager buildManager;
    private Mock mockScm;
    private SCMServer scmServer;
    private Project project;
    private BuildSpecification buildSpecification;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockBuildManager = new Mock(BuildManager.class);
        mockScm = new Mock(SCMServer.class);

        buildSpecification = new BuildSpecification("default");
        buildSpecification.setIsolateChangelists(true);

        project = new Project("myproject", "mydesc", new CustomPulseFileDetails());
        project.setScm(new Scm()
        {
            public SCMServer createServer() throws SCMException
            {
                return scmServer;
            }
        });

    }

    public void testNeverBuilt() throws SCMException
    {
        mockBuildManager.expectAndReturn("queryBuilds", C.ANY_ARGS, new LinkedList<BuildResult>());
        NumericalRevision rev = new NumericalRevision(10);
        mockScm.expectAndReturn("getLatestRevision", C.ANY_ARGS, rev);
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

    private void returnBuild(long revision)
    {
        BuildResult result = new BuildResult();
        result.setScmDetails(new BuildScmDetails(new NumericalRevision(revision)));
        mockBuildManager.expectAndReturn("queryBuilds", C.ANY_ARGS, Arrays.asList(new BuildResult[]{ result }));
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
        scmServer = (SCMServer) mockScm.proxy();
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
