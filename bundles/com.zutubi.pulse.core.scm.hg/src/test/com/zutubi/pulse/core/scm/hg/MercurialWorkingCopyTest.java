package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.scm.WorkingCopyContextImpl;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.ui.TestUI;
import com.zutubi.util.config.CompositeConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

import java.io.File;

public class MercurialWorkingCopyTest extends MercurialTestBase
{
    private File workingDir;
    private MercurialCore workingCore;
    private MercurialWorkingCopy workingCopy;
    private WorkingCopyContext devContext;
    private TestUI ui;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        workingDir = new File(tmp, "working");
        
        workingCore = new MercurialCore();
        
        workingCopy = new MercurialWorkingCopy();
        ui = new TestUI();
        devContext = new WorkingCopyContextImpl(workingDir, new CompositeConfig(), ui);
    }

    private void clone(String revision) throws ScmException
    {
        workingCore.setWorkingDirectory(workingDir.getParentFile());
        workingCore.clone(null, repository, null, revision, workingDir.getName());
        workingCore.setWorkingDirectory(workingDir);
    }

    public void testGetLatestRemoteRevisionUpToDate() throws ScmException
    {
        clone(null);
        workingCore.update(null, null);
        assertEquals(REVISION_DEFAULT_LATEST, workingCopy.getLatestRemoteRevision(devContext).getRevisionString());
    }

    public void testGetLatestRemoteRevisionUpToDateOldWorkingCopy() throws ScmException
    {
        clone(null);
        workingCore.update(null, REVISION_DEFAULT_PREVIOUS);
        assertEquals(REVISION_DEFAULT_LATEST, workingCopy.getLatestRemoteRevision(devContext).getRevisionString());
    }

    public void testGetLatestRemoteRevisionOutOfDate() throws ScmException
    {
        clone(REVISION_DEFAULT_PREVIOUS);
        workingCore.update(null, null);
        assertEquals(REVISION_DEFAULT_LATEST, workingCopy.getLatestRemoteRevision(devContext).getRevisionString());
    }

    public void testGetLatestRemoteRevisionBranchUpToDate() throws ScmException
    {
        clone(null);
        workingCore.update(null, BRANCH);
        assertEquals(REVISION_BRANCH_LATEST, workingCopy.getLatestRemoteRevision(devContext).getRevisionString());
    }

    public void testGetLatestRemoteRevisionBranchUpToDateOldWorkingCopy() throws ScmException
    {
        clone(null);
        workingCore.update(null, REVISION_BRANCH_PREVIOUS);
        assertEquals(REVISION_BRANCH_LATEST, workingCopy.getLatestRemoteRevision(devContext).getRevisionString());
    }

    public void testGetLatestRemoteRevisionBranchOutOfDate() throws ScmException
    {
        clone(REVISION_BRANCH_PREVIOUS);
        workingCore.update(null, BRANCH);
        assertEquals(REVISION_BRANCH_LATEST, workingCopy.getLatestRemoteRevision(devContext).getRevisionString());
    }
    
    public void testGuessLocalRevisionUpToDate() throws ScmException
    {
        clone(null);
        workingCore.update(null, null);
        assertEquals(REVISION_DEFAULT_LATEST, workingCopy.guessLocalRevision(devContext).getRevisionString());
    }

    public void testGuessLocalRevisionUpToDateOldWorkingCopy() throws ScmException
    {
        clone(null);
        workingCore.update(null, REVISION_DEFAULT_PREVIOUS);
        assertEquals(REVISION_DEFAULT_PREVIOUS, workingCopy.guessLocalRevision(devContext).getRevisionString());
    }

    public void testGuessLocalRevisionOutOfDate() throws ScmException
    {
        clone(REVISION_DEFAULT_PREVIOUS);
        workingCore.update(null, null);
        assertEquals(REVISION_DEFAULT_PREVIOUS, workingCopy.guessLocalRevision(devContext).getRevisionString());
    }

    public void testGuessLocalRemoteRevisionBranchUpToDate() throws ScmException
    {
        clone(null);
        workingCore.update(null, BRANCH);
        assertEquals(REVISION_BRANCH_LATEST, workingCopy.guessLocalRevision(devContext).getRevisionString());
    }

    public void testGuessLocalRevisionBranchUpToDateOldWorkingCopy() throws ScmException
    {
        clone(null);
        workingCore.update(null, REVISION_BRANCH_PREVIOUS);
        assertEquals(REVISION_BRANCH_PREVIOUS, workingCopy.guessLocalRevision(devContext).getRevisionString());
    }

    public void testGuessLocalRevisionBranchOutOfDate() throws ScmException
    {
        clone(REVISION_BRANCH_PREVIOUS);
        workingCore.update(null, BRANCH);
        assertEquals(REVISION_BRANCH_PREVIOUS, workingCopy.guessLocalRevision(devContext).getRevisionString());
    }
    
    public void testUpdateUpToDate() throws ScmException
    {
        clone(null);
        workingCore.update(null, null);
        
        Revision revision = workingCopy.update(devContext, new Revision(REVISION_DEFAULT_LATEST));
        assertEquals(REVISION_DEFAULT_LATEST, revision.getRevisionString());
        assertThat(ui.getStatusMessages(), hasItem(containsString("0 files updated")));
    }

    public void testUpdateUpToDateOldWorkingCopy() throws ScmException
    {
        clone(null);
        workingCore.update(null, REVISION_DEFAULT_PREVIOUS);
        
        Revision revision = workingCopy.update(devContext, new Revision(REVISION_DEFAULT_LATEST));
        assertEquals(REVISION_DEFAULT_LATEST, revision.getRevisionString());
        assertThat(ui.getStatusMessages(), hasItem(containsString("1 files updated")));
    }

    public void testUpdateOutOfDate() throws ScmException
    {
        clone(REVISION_DEFAULT_PREVIOUS);
        workingCore.update(null, null);
        
        Revision revision = workingCopy.update(devContext, new Revision(REVISION_DEFAULT_LATEST));
        assertEquals(REVISION_DEFAULT_LATEST, revision.getRevisionString());
        assertThat(ui.getStatusMessages(), hasItem(containsString("1 files updated")));
    }

    public void testUpdateBranchUpToDate() throws ScmException
    {
        clone(null);
        workingCore.update(null, BRANCH);
        
        Revision revision = workingCopy.update(devContext, new Revision(REVISION_BRANCH_LATEST));
        assertEquals(REVISION_BRANCH_LATEST, revision.getRevisionString());
        assertThat(ui.getStatusMessages(), hasItem(containsString("0 files updated")));
    }

    public void testUpdateBranchUpToDateOldWorkingCopy() throws ScmException
    {
        clone(null);
        workingCore.update(null, REVISION_BRANCH_PREVIOUS);

        Revision revision = workingCopy.update(devContext, new Revision(REVISION_BRANCH_LATEST));
        assertEquals(REVISION_BRANCH_LATEST, revision.getRevisionString());
        assertThat(ui.getStatusMessages(), hasItem(containsString("1 files updated")));
    }

    public void testUpdateBranchOutOfDate() throws ScmException
    {
        clone(REVISION_BRANCH_PREVIOUS);
        workingCore.update(null, BRANCH);

        Revision revision = workingCopy.update(devContext, new Revision(REVISION_BRANCH_LATEST));
        assertEquals(REVISION_BRANCH_LATEST, revision.getRevisionString());
        assertThat(ui.getStatusMessages(), hasItem(containsString("1 files updated")));
    }
}
