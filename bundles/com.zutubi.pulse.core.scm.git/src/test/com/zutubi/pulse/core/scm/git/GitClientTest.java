package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.git.GitConstants.*;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.ZipUtils;
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
import java.net.URL;
import java.text.ParseException;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GitClientTest extends PulseTestCase
{
    private static final String REVISION_HEAD = "HEAD";
    private static final String REVISION_INITIAL = "96e8d45dd7627d9e3cab980e90948e3ae1c99c62";
    private static final String REVISION_MASTER_LATEST = "a495e21cd263d9dca25379dfbff733461f0d9873";
    private static final String REVISION_MASTER_PREVIOUS = "2d1ce48a43c5c675c618f915af43e76ed7dac253";
    private static final String REVISION_MASTER_TWO_PREVIOUS = "e34da05e88de03a4aa5b10b338382f09bbe65d4b";
    private static final String REVISION_DEV_MERGE_NO_CONFLICTS = "9751b1dbd8cdbbeedce404bad38d1df1053078f6";
    private static final String REVISION_DEV_MERGE_CONFLICTS = "2b54f24e1facb7d97643d38e0f89cd5db88b186a";
    private static final String REVISION_MULTILINE_COMMENT = "01aaf6555b7524871204c8df64273597c0bc1f1b";
    private static final String REVISION_MASTER_INTERMEDIATE = "b69a48a6b0f567d0be110c1fbca2c48fc3e1b112";
    private static final String REVISION_SIMPLE_INTERMEDIATE = "83d35b25a6b4711c4d9424c337bf82e5398756f3";
    private static final String REVISION_SIMPLE_LATEST = "c34b545b6954b8946967c250dde7617c24a9bb4b";

    private static final String BRANCH_SIMPLE = "branch";
    private static final String BRANCH_MERGES = "devbranch";

    private static final String TEST_AUTHOR = "Jason Sankey";
    private static final String CONTENT_A_TXT = "another a edit";

    private File tmp;
    private String repository;
    private GitClient client;
    private File workingDir;
    private PulseExecutionContext context;
    private RecordingScmFeedbackHandler handler;
    private ScmContextImpl scmContext;
    private File repositoryBase;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        URL url = getClass().getResource("GitClientTest.zip");
        ZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        repositoryBase = new File(tmp, "repo");
        repository = "file://" + repositoryBase.getCanonicalPath();

        client = new GitClient(repository, "master", 0, false);

        workingDir = new File(tmp, "wd");
        context = new PulseExecutionContext();
        context.setWorkingDir(workingDir);

        File persistentWorkingDir = new File(tmp, "scm");
        assertTrue(persistentWorkingDir.mkdir());
        scmContext = new ScmContextImpl();
        scmContext.setPersistentWorkingDir(persistentWorkingDir);

        handler = new RecordingScmFeedbackHandler();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

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

        assertEquals(1,client.getRevisions(scmContext, new Revision(REVISION_MASTER_LATEST), null).size());
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
        assertThat(handler.getStatusMessages(), hasItem(startsWith("Branch local set up")));
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
        assertThat(handler.getStatusMessages(), hasItem(startsWith("Branch local set up")));

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

        NativeGit nativeGit = new NativeGit(0);
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

        NativeGit nativeGit = new NativeGit(0);
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
//  $ git log --name-status
//    commit 6e16107f3d99519a0cf341d3f455283d8244536e
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 14:04:04 2009 +0000
//
//        A regular commit to finish up.
//
//    M	b.txt
//
//    commit 01aaf6555b7524871204c8df64273597c0bc1f1b
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:37:33 2009 +0000
//
//        This is a
//        multi-line
//        commit comment
//
//    M	a.txt
//
//    commit 2b54f24e1facb7d97643d38e0f89cd5db88b186a
//    Merge: 9751b1d... a495e21...
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:13:50 2009 +0000
//
//        Fixed merge conflict.
//
//    commit a495e21cd263d9dca25379dfbff733461f0d9873
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:12:32 2009 +0000
//
//        Edit, add and remove on master.
//
//    M	a.txt
//    D	c.txt
//    A	d.txt
//
//    commit 9751b1dbd8cdbbeedce404bad38d1df1053078f6
//    Merge: 21aa65b... 2d1ce48...
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:10:25 2009 +0000
//
//        Merge branch 'master' into devbranch
//
//    commit 2d1ce48a43c5c675c618f915af43e76ed7dac253
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:10:04 2009 +0000
//
//        Simple edit to b.txt on master.
//
//    M	b.txt
//
//    commit 21aa65b9a35bffaefead12680334288d6d69e596
//    Author: Jason Sankey <jason@zutubi.com>
//    Date:   Fri Mar 27 13:09:23 2009 +0000
//
//        Simple edit to a.txt on devbranch.
//
//    M	a.txt
//
//    commit e34da05e88de03a4aa5b10b338382f09bbe65d4b
//    Author: Daniel Ostermeier <daniel@zutubi.com>
//    Date:   Sun Sep 28 15:06:49 2008 +1000
//
//        removed content from a.txt
//
//    M	a.txt
//
//    commit b69a48a6b0f567d0be110c1fbca2c48fc3e1b112
//    Author: Daniel Ostermeier <daniel@zutubi.com>
//    Date:   Sun Sep 28 15:06:32 2008 +1000
//
//        added content to a.txt
//
//    M	a.txt
//
//    commit 96e8d45dd7627d9e3cab980e90948e3ae1c99c62
//    Author: Daniel Ostermeier <daniel@zutubi.com>
//    Date:   Sun Sep 28 13:26:10 2008 +1000
//
//        initial commit
//
//    A	a.txt
//    A	b.txt
//    A	c.txt
