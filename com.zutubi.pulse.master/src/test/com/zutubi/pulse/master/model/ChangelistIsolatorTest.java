package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.BuildOptionsConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.Transient;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.LinkedList;
import java.util.List;

public class ChangelistIsolatorTest extends PulseTestCase
{
    private ChangelistIsolator isolator;
    private BuildManager buildManager;
    private ScmClient scmClient;
    private Project project;
    private ProjectConfiguration projectConfig;

    protected void setUp() throws Exception
    {
        super.setUp();

        buildManager = mock(BuildManager.class);
        scmClient = mock(ScmClient.class);

        project = new Project();
        project.setId(1);
        projectConfig = new ProjectConfiguration();
        projectConfig.setScm(new ScmConfiguration()
        {
            @Transient
            public String getType()
            {
                return "mock";
            }
        });
        BuildOptionsConfiguration options = new BuildOptionsConfiguration();
        options.setIsolateChangelists(true);
        projectConfig.setOptions(options);

        isolator = new ChangelistIsolator(buildManager);
        isolator.setScmManager(new ScmManager()
        {
            public void pollActiveScms() { }

            public void clearCache(long projectId)
            {
            }

            public ScmContext createContext(ProjectConfiguration projectConfiguration) throws ScmException
            {
                return new ScmContextImpl();
            }

            public ScmClient createClient(ScmConfiguration config) throws ScmException
            {
                return scmClient;
            }
        });
    }

    public void testNeverBuilt() throws ScmException
    {
        returnLatestRevision(10);
        expectRevisions(true, 10);
    }

    public void testPreviouslyBuilt() throws ScmException
    {
        returnPreviousRevision(new Revision(55));
        returnRevisions(55, 56, 57);
        expectRevisions(true, 56, 57);
    }

    public void testNoNewRevisions() throws ScmException
    {
        returnPreviousRevision(new Revision(55));
        returnRevisions(55);
        expectRevisions(false);
    }

    public void testNoNewRevisionsForced() throws ScmException
    {
        returnPreviousRevision(new Revision(55));
        returnRevisions(55);
        expectRevisions(true, 55);
    }

    public void testRemembersPreviousRevision() throws ScmException
    {
        returnPreviousRevision(new Revision(101));
        returnRevisions(101, 102, 103, 104);
        expectRevisions(true, 102, 103, 104);
        returnRevisions(104, 105);
        expectRevisions(true, 105);
    }

    private void returnPreviousRevision(Revision revision)
    {
        stub(buildManager.getPreviousRevision(project)).toReturn(revision);
    }

    private Revision returnLatestRevision(long revision) throws ScmException
    {
        Revision rev = new Revision(Long.toString(revision));
        stub(scmClient.getLatestRevision((ScmContext) anyObject())).toReturn(rev);
        return rev;
    }

    private void returnRevisions(long since, long... revisions) throws ScmException
    {
        List<Revision> result = new LinkedList<Revision>();
        for (long revision: revisions)
        {
            result.add(new Revision(Long.toString(revision)));
        }

        stub(scmClient.getRevisions((ScmContext) anyObject(), eq(new Revision(Long.toString(since))), (Revision) isNull())).toReturn(result);
    }

    private void expectRevisions(boolean force, long... revisions) throws ScmException
    {
        List<Revision> gotRevisions = isolator.getRevisionsToRequest(projectConfig, project, force);
        assertEquals(revisions.length, gotRevisions.size());
        for(int i = 0; i < revisions.length; i++)
        {
            long revision = Long.valueOf(gotRevisions.get(i).getRevisionString());
            assertEquals(revisions[i], revision);
        }
    }
}
