package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.git.GitConstants.*;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GitClientTest extends GitClientTestBase
{
    public void testCheckout() throws ScmException, ParseException
    {
        Revision rev = client.checkout(context, null, handler);

        assertEquals(REVISION_MASTER_LATEST, rev.getRevisionString());

        assertHeadCheckedOut();

        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutOnBranch() throws ScmException, ParseException
    {
        checkoutBranchHelper();
    }

    public void testCheckoutSelectedBranch() throws ScmException, ParseException
    {
        client.setTrackSelectedBranch(true);
        checkoutBranchHelper();
    }

    private void checkoutBranchHelper() throws ScmException
    {
        client.setBranch(BRANCH_SIMPLE);
        Revision rev = client.checkout(context, null, handler);

        assertEquals(REVISION_SIMPLE_LATEST, rev.getRevisionString());

        assertFiles(workingDir, "1.txt", "2.txt", "3.txt");
        assertGitDir(workingDir);
    }

    public void testCheckoutToRevision() throws ScmException, ParseException
    {
        Revision rev = client.checkout(context, new Revision(REVISION_INITIAL), handler);

        assertEquals(REVISION_INITIAL, rev.getRevisionString());

        assertFiles(workingDir, "a.txt", "b.txt", "c.txt");
        assertGitDir(workingDir);

        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutToRevisionOnBranch() throws ScmException, ParseException
    {
        checkoutBranchToRevisionHelper();
    }

    public void testCheckoutToRevisionSelectedBranch() throws ScmException, ParseException
    {
        client.setTrackSelectedBranch(true);
        checkoutBranchToRevisionHelper();
    }

    private void checkoutBranchToRevisionHelper() throws ScmException
    {
        client.setBranch(BRANCH_SIMPLE);
        Revision rev = client.checkout(context, new Revision(REVISION_SIMPLE_INTERMEDIATE), handler);

        assertEquals(REVISION_SIMPLE_INTERMEDIATE, rev.getRevisionString());

        assertGitDir(workingDir);
        assertEquals(2, workingDir.list().length);
    }

    public void testGetLatestRevision() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals(REVISION_MASTER_LATEST, rev.getRevisionString());
    }

    public void testGetLatestRevisionOnBranch() throws ScmException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals(REVISION_SIMPLE_LATEST, rev.getRevisionString());
    }

    public void testGetRevisions() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(asList(new Revision(REVISION_MASTER_PREVIOUS), new Revision(REVISION_MASTER_LATEST)), client.getRevisions(scmContext, new Revision(REVISION_MASTER_TWO_PREVIOUS), null));
    }

    public void testGetRevisionsAfterUpstreamCommit() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(REVISION_MASTER_LATEST, client.getLatestRevision(scmContext).getRevisionString());

        // Making a commit after we init ensures a working fetch/merge for this
        // expected fast-forward case.
        makeUpstreamCommit();

        assertEquals(1, client.getRevisions(scmContext, new Revision(REVISION_MASTER_LATEST), null).size());
    }

    public void testGetRevisionsAfterRewritingHistory() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(REVISION_MASTER_LATEST, client.getLatestRevision(scmContext).getRevisionString());

        rewriteUpstreamHistory();

        try
        {
            client.getRevisions(scmContext, new Revision(REVISION_MASTER_LATEST), null);
            fail("Shouldn't be able to get revisions when pull would require merge.");
        }
        catch (ScmException e)
        {
            assertEquals("Merge after fetch is not fast-forward, likely due to history changes upstream.  Project reinitialisation required.", e.getMessage());
        }
    }

    private void makeUpstreamCommit() throws IOException, GitException
    {
        NativeGit git = getNativeGitOnMaster();

        File b = new File(repositoryBase, "b.txt");
        FileSystemUtils.createFile(b, getName());
        git.run(git.getGitCommand(), COMMAND_COMMIT, FLAG_ADD, FLAG_MESSAGE, "Will need merging");
    }

    private void rewriteUpstreamHistory() throws GitException, IOException
    {
        // Reset to delete the latest master commit
        NativeGit git = getNativeGitOnMaster();
        git.run(git.getGitCommand(), COMMAND_RESET, FLAG_HARD, "HEAD~1");
        makeUpstreamCommit();
    }

    private NativeGit getNativeGitOnMaster() throws GitException
    {
        // Add a new commit which requires merging
        NativeGit git = new NativeGit(0);
        git.setWorkingDirectory(repositoryBase);
        git.checkout(null, "master");
        return git;
    }

    public void testRetrieve() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, "a.txt", null);
        assertEquals(CONTENT_A_TXT, IOUtils.inputStreamToString(content));
    }

    public void testRetrieveFromRevision() throws IOException, ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, "a.txt", new Revision(REVISION_MASTER_INTERMEDIATE));
        assertEquals("content", IOUtils.inputStreamToString(content));
    }

    public void testRetrieveOnBranch() throws ScmException, IOException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, "1.txt", null);
        assertEquals("", IOUtils.inputStreamToString(content));
    }

    public void testRetrieveFromRevisionOnBranch() throws ScmException, IOException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, "1.txt", new Revision("7d61890eb55586ec99416c53c581bf561591a608"));
        assertEquals("content", IOUtils.inputStreamToString(content));
    }

    public void testUpdate() throws ScmException
    {
        client.checkout(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertTrue(handler.getStatusMessages().contains("Branch local set up to track remote branch refs/remotes/origin/master."));
        assertThat(handler.getStatusMessages(), not(hasItem(GitClient.I18N.format(GitClient.KEY_INCOMPLETE_CHECKOUT))));

        handler.reset();

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals("Already up-to-date.", handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateOnBranch() throws ScmException
    {
        updateOnBranchHelper();
    }

    public void testUpdateSelectedBranch() throws ScmException
    {
        client.setTrackSelectedBranch(true);
        updateOnBranchHelper();
    }

    private void updateOnBranchHelper() throws ScmException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.checkout(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertTrue(handler.getStatusMessages().contains("Branch local set up to track remote branch refs/remotes/origin/branch."));

        handler.reset();

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals("Already up-to-date.", handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateToLatestOnBranch() throws ScmException, IOException, ParseException
    {
        updateToLatestOnBranchHelper();
    }

    public void testUpdateToLatestSelectedBranch() throws ScmException, IOException, ParseException
    {
        client.setTrackSelectedBranch(true);
        updateToLatestOnBranchHelper();
    }

    private void updateToLatestOnBranchHelper() throws ScmException, IOException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.checkout(context, new Revision(REVISION_SIMPLE_INTERMEDIATE), handler);
        assertEquals(2, workingDir.list().length);

        Revision rev = client.update(context, null, handler);
        assertEquals(REVISION_SIMPLE_LATEST, rev.getRevisionString());

        assertFiles(workingDir, "1.txt", "2.txt", "3.txt");
        assertGitDir(workingDir);
    }

    public void testUpdateToRevision() throws ScmException, IOException, ParseException
    {
        updateToRevisionHelper();
    }

    public void testUpdateToRevisionSelectedBranch() throws ScmException, IOException, ParseException
    {
        client.setTrackSelectedBranch(true);
        updateToRevisionHelper();
    }

    private void updateToRevisionHelper() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        assertEquals(CONTENT_A_TXT + "\n", IOUtils.fileToString(new File(workingDir, "a.txt")));

        Revision rev = client.update(context, new Revision(REVISION_MASTER_INTERMEDIATE), handler);
        assertEquals(REVISION_MASTER_INTERMEDIATE, rev.getRevisionString());

        assertEquals("content", IOUtils.fileToString(new File(workingDir, "a.txt")));
    }

    public void testUpdateNoCheckout() throws ScmException
    {
        client.update(context, null, handler);
        assertThat(handler.getStatusMessages(), hasItem(GitClient.I18N.format(GitClient.KEY_INCOMPLETE_CHECKOUT)));
        assertHeadCheckedOut();
    }

    public void testUpdateIncompleteCheckout() throws ScmException
    {
        client.checkout(context, null, handler);
        handler.reset();

        File completeFile = new File(workingDir, GitClient.CHECKOUT_COMPLETE_FILENAME);
        assertTrue(completeFile.delete());

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages(), hasItem(GitClient.I18N.format(GitClient.KEY_INCOMPLETE_CHECKOUT)));
        assertHeadCheckedOut();
    }

    public void testUpdateAfterRewritingHistory() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        handler.reset();

        rewriteUpstreamHistory();

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages(), hasItem(GitClient.I18N.format(GitClient.KEY_MERGE_ON_UPDATE)));
        assertFiles(workingDir, "a.txt", "b.txt", "c.txt");
    }

    public void testChanges() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Changelist> changes = client.getChanges(scmContext, new Revision("HEAD~2"), null);
        assertEquals(2, changes.size());
    }

    public void testHeadChanges() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision("HEAD~1"), new Revision(REVISION_HEAD));
        assertEquals(1, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("Edit, add and remove on master.", changelist.getComment());
        assertEquals(REVISION_MASTER_LATEST, changelist.getRevision().getRevisionString());
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        List<FileChange> fileChanges = changelist.getChanges();
        assertEquals(3, fileChanges.size());

        FileChange change = fileChanges.get(0);
        assertEquals(FileChange.Action.EDIT, change.getAction());
        assertEquals("a.txt", change.getPath());

        change = fileChanges.get(1);
        assertEquals(FileChange.Action.DELETE, change.getAction());
        assertEquals("c.txt", change.getPath());

        change = fileChanges.get(2);
        assertEquals(FileChange.Action.ADD, change.getAction());
        assertEquals("d.txt", change.getPath());
    }

    public void testMergedChange() throws ScmException
    {
        client.setBranch(BRANCH_MERGES);
        client.init(scmContext, new ScmFeedbackAdapter());

        // Log over the whole branch history as the interesting cases happen
        // when there are multiple log entries in a single output stream that
        // we need to pull apart.
        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_INITIAL), new Revision(REVISION_HEAD));

        Changelist changelist = assertChangelist(changes, REVISION_DEV_MERGE_NO_CONFLICTS);
        assertEquals("Merge branch 'master' into devbranch", changelist.getComment());
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        assertEquals(0, changelist.getChanges().size());

        changelist = assertChangelist(changes, REVISION_DEV_MERGE_CONFLICTS);
        assertEquals("Fixed merge conflict.", changelist.getComment());
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        assertEquals(1, changelist.getChanges().size());
        
        FileChange change = changelist.getChanges().get(0);
        assertEquals("a.txt", change.getPath());
        assertEquals(FileChange.Action.MERGE, change.getAction());
    }

    public void testChangeWithMultilineComment() throws ScmException
    {
        client.setBranch(BRANCH_MERGES);
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_INITIAL), new Revision(REVISION_HEAD));

        Changelist changelist = assertChangelist(changes, REVISION_MULTILINE_COMMENT);
        // Some versions of git are more aggressive at collapsing the "subject"
        // or initial part of a commit comment than others, so be a bit
        // permissive.  Just ensure that the whole comment makes it, with some
        // kind of separating whitespace.
        assertThat(changelist.getComment(), matchesRegex("This is a\\smulti-line\\scommit comment"));
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        assertEquals(1, changelist.getChanges().size());

        FileChange change = changelist.getChanges().get(0);
        assertEquals("a.txt", change.getPath());
        assertEquals(FileChange.Action.EDIT, change.getAction());
    }

    private Changelist assertChangelist(List<Changelist> changes, String revision)
    {
        Changelist changelist = findChangelist(changes, revision);
        assertNotNull(changelist);
        return changelist;
    }

    private Changelist findChangelist(List<Changelist> changes, final String revision)
    {
        return CollectionUtils.find(changes, new Predicate<Changelist>()
        {
            public boolean satisfied(Changelist changelist)
            {
                return changelist.getRevision().getRevisionString().equals(revision);
            }
        });
    }

    public void testBrowse() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<ScmFile> files = client.browse(scmContext, "", null);
        assertEquals(3, files.size());
        Collections.sort(files, new ScmFileComparator());
        assertEquals("a.txt", files.get(0).getName());
        assertEquals("b.txt", files.get(1).getName());
        assertEquals("d.txt", files.get(2).getName());
    }

    public void testBrowseNotAvailableWhenContextNotAvailable()
    {
        assertFalse(client.getCapabilities(null).contains(ScmCapability.BROWSE));
    }

    public void testBrowseAvailableWhenContextAvailable()
    {
        assertTrue(client.getCapabilities(scmContext).contains(ScmCapability.BROWSE));
    }

    public void testTestConnectionOK() throws GitException
    {
        client.testConnection();
    }

    public void testTestConnectionBadRepo() throws GitException
    {
        client = new GitClient("file:///no/such/repo", "master", 0, false);
        try
        {
            client.testConnection();
            fail("Test of bad repo should fail");
        }
        catch (GitException e)
        {
            assertThat(e.getMessage(), containsString("ls-remote file:///no/such/repo master' exited with non-zero exit code"));
        }
    }

    public void testTestConnectionBadBranch() throws GitException
    {
        client = new GitClient(repository, "nosuchbranch", 0, false);
        try
        {
            client.testConnection();
            fail("Test of bad branch should fail");
        }
        catch (GitException e)
        {
            assertThat(e.getMessage(), containsString("Branch 'nosuchbranch' does not exist"));
        }
    }

    public void testTagRevision() throws ScmException, IOException
    {
        final String TAG_NAME = "test-tag";

        client.init(scmContext, new ScmFeedbackAdapter());
        client.tag(scmContext, context, new Revision(REVISION_INITIAL), TAG_NAME, false);

        NativeGit nativeGit = new NativeGit();
        nativeGit.setWorkingDirectory(repositoryBase);
        String info = IOUtils.inputStreamToString(nativeGit.show(null, TAG_NAME));
        assertThat(info, containsString(REVISION_INITIAL));
        assertThat(info, containsString("initial commit"));
    }

    public void testMoveExistingTag() throws ScmException, IOException
    {
        final String TAG_NAME = "test-tag";

        client.init(scmContext, new ScmFeedbackAdapter());
        client.tag(scmContext, context, new Revision(REVISION_INITIAL), TAG_NAME, false);
        client.tag(scmContext, context, new Revision(REVISION_MASTER_LATEST), TAG_NAME, true);

        NativeGit nativeGit = new NativeGit();
        nativeGit.setWorkingDirectory(repositoryBase);
        String info = IOUtils.inputStreamToString(nativeGit.show(null, TAG_NAME));
        assertThat(info, containsString(REVISION_MASTER_LATEST));
        assertThat(info, containsString("Edit, add and remove on master"));
    }

    private void assertHeadCheckedOut()
    {
        assertFiles(workingDir, "a.txt", "b.txt", "d.txt");
        assertGitDir(workingDir);
    }

    private void assertFiles(File base, String... filenames)
    {
        for (String filename : filenames)
        {
            assertTrue(filename + " is not a file.", new File(base, filename).isFile());
        }
    }

    private void assertGitDir(File workingDir)
    {
        assertTrue(new File(workingDir, GitClient.GIT_REPOSITORY_DIRECTORY).isDirectory());
    }

    private static class ScmFileComparator implements Comparator<ScmFile>
    {
        private final Sort.StringComparator comp = new Sort.StringComparator();

        public int compare(ScmFile o1, ScmFile o2)
        {
            return comp.compare(o1.getName(), o2.getName());
        }
    }
}
